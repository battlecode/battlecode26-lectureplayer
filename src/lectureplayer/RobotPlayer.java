package lectureplayer;

import battlecode.common.*;

import java.util.Random;

public class RobotPlayer {
    public static enum State {
        INITIALIZE,
        FIND_CHEESE,
        RETURN_TO_KING,
        BUILD_TRAPS,
        EXPLORE_AND_ATTACK,
    }

    public static Random rand = new Random(1092);

    public static State currentState = State.INITIALIZE;

    public static int numRatsSpawned = 0;

    public static Direction[] directions = Direction.values();

    public static void run(RobotController rc) {
        while (true) {
            try {
                MapLocation myLoc = rc.getLocation();

                if (rc.getType().isRatKingType()) {
                    int currentCost = rc.getCurrentRatCost();

                    if (currentCost <= 10 || rc.getAllCheese() > currentCost + 2500) {
                        MapLocation[] potentialSpawnLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 8);
                        
                        for (MapLocation loc : potentialSpawnLocations) {
                            if (rc.canBuildRat(loc)) {
                                rc.buildRat(loc);
                                numRatsSpawned++;
                                break;
                            }
                        }
                    }

                    moveRandom(rc);

                    // TODO make more efficient and expand communication in the communication lecture
                    rc.writeSharedArray(0, myLoc.x);
                    rc.writeSharedArray(1, myLoc.y);
                } else {
                    switch (currentState) {
                        case INITIALIZE:
                            if (rc.getRoundNum() < 30 || rc.getCurrentRatCost() <= 10) {
                                currentState = State.FIND_CHEESE;
                            } else {
                                currentState = State.EXPLORE_AND_ATTACK;
                            }

                            break;
                        case FIND_CHEESE:
                            // search for cheese
                            MapInfo[] nearbyInfos = rc.senseNearbyMapInfos();

                            for (MapInfo info : nearbyInfos) {
                                if (info.getCheeseAmount() > 0) {
                                    Direction toCheese = myLoc.directionTo(info.getMapLocation());

                                    if (rc.canTurn()) {
                                        rc.turn(toCheese);
                                        break;
                                    }
                                }
                            }

                            for (Direction dir : directions) {
                                MapLocation loc = myLoc.add(dir);
                                
                                if (rc.canPickUpCheese(loc)) {
                                    rc.pickUpCheese(loc);

                                    if (rc.getRawCheese() >= 5) {
                                        currentState = State.RETURN_TO_KING;
                                    }
                                }
                            }

                            moveRandom(rc);
                            break;
                        case RETURN_TO_KING:
                            MapLocation kingLoc = new MapLocation(rc.readSharedArray(0), rc.readSharedArray(1));
                            Direction toKing = myLoc.directionTo(kingLoc);
                            MapLocation nextLoc = myLoc.add(toKing);

                            if (rc.canTurn()) {
                                rc.turn(toKing);
                            }

                            if (rc.canRemoveDirt(nextLoc)) {
                                rc.removeDirt(nextLoc);
                            }

                            // TODO replace with pathfinding for the pathfinding lecture
                            if (rc.canMove(toKing)) {
                                rc.move(toKing);
                            }

                            int rawCheese = rc.getRawCheese();
                            
                            if (rc.canTransferCheese(kingLoc, rawCheese)) {
                                System.out.println("Transferred " + rawCheese + " cheese to king at " + kingLoc + ": I'm at " + myLoc);
                                rc.transferCheese(kingLoc, rawCheese);
                                currentState = State.FIND_CHEESE;
                            }

                            break;
                        case BUILD_TRAPS:
                            for (Direction dir : directions) {
                                MapLocation loc = myLoc.add(dir);
                                boolean catTraps = rand.nextBoolean();
                                
                                if (catTraps && rc.canPlaceCatTrap(loc)) {
                                    System.out.println("Built cat trap at " + loc);
                                    rc.placeCatTrap(loc);
                                } else if (rc.canPlaceRatTrap(loc)) {
                                    System.out.println("Built rat trap at " + loc);
                                    rc.placeRatTrap(loc);
                                }
                            }

                            if (rand.nextDouble() < 0.1) {
                                currentState = State.EXPLORE_AND_ATTACK;
                            }

                            moveRandom(rc);
                            break;
                        case EXPLORE_AND_ATTACK:
                            moveRandom(rc);

                            if (rc.canAttack(myLoc)) {
                                System.out.println("Attacking at " + myLoc);
                                rc.attack(myLoc);
                            }

                            if (rand.nextDouble() < 0.1) {
                                currentState = State.BUILD_TRAPS;
                            }

                            break;
                    }
                }
            } catch (GameActionException e) {
                System.out.println("GameActionException in RobotPlayer:");
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("Exception in RobotPlayer:");
                e.printStackTrace();
            } finally {
                Clock.yield();
            }
        }
    }

    public static void moveRandom(RobotController rc) throws GameActionException {
        MapLocation forwardLoc = rc.adjacentLocation(rc.getDirection());

        if (rc.canRemoveDirt(forwardLoc)) {
            rc.removeDirt(forwardLoc);
        }

        if (rc.canMoveForward()) {
            rc.moveForward();
        } else {
            Direction random = directions[rand.nextInt(directions.length)];

            if (rc.canTurn()) {
                rc.turn(random);
            }
        }
    }
}
