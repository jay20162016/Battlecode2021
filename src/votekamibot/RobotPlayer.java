package votekamibot;
import battlecode.common.*;

public strictfp class RobotPlayer {
        static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
        };

    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        while (true) {
            try {
              for (int i = 0; i < 30; i++) {
                  if (rc.canBuildRobot(RobotType.MUCKRAKER, directions[(int) (Math.random() * directions.length)], rc.getInfluence() / 2)) {
                      rc.buildRobot(RobotType.MUCKRAKER, directions[(int) (Math.random() * directions.length)], rc.getInfluence() / 2);
                  }
              }
            } catch (Exception e) {}

            Clock.yield();
        }
    }
}
