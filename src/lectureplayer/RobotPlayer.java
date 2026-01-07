package lectureplayer;

import battlecode.common.*;

public class RobotPlayer {
    public static enum State {
        INITIALIZE,
        FIND_CHEESE,
        RETURN_TO_KING,
        BUILD_TRAPS,
        EXPLORE,
        ATTACK,
    }

    public static State currentState = State.INITIALIZE;

    public static int numRatsSpawned = 0;

    public static void run(RobotController rc) {
        while (true) {
            try {
                if (rc.getType().isRatKingType()) {
                    if (numRatsSpawned < 10) {
                        MapLocation[] potentialSpawnLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 8);
                        
                        for (MapLocation loc : potentialSpawnLocations) {
                            if (rc.canBuildRat(loc)) {
                                rc.buildRat(loc);
                                numRatsSpawned++;
                                break;
                            }
                        }
                    }
                } else {
                    switch (currentState) {
                        case INITIALIZE:
                            if (rc.getRoundNum() < 50) {
                                currentState = State.FIND_CHEESE;
                            } else {
                                currentState = State.EXPLORE;
                            }

                            break;
                        case FIND_CHEESE:
                            // Logic to find cheese
                            break;
                        case RETURN_TO_KING:
                            // Logic to return to king
                            break;
                        case BUILD_TRAPS:
                            // Logic to build traps
                            break;
                        case EXPLORE:
                            // Exploration logic
                            break;
                        case ATTACK:
                            // Attack logic
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
}
