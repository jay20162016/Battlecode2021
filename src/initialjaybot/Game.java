package initialjaybot;
import battlecode.common.*;

public strictfp class RobotPlayer {
    static final RobotType[] spawnableRobot = {
        RobotType.POLITICIAN,
        RobotType.SLANDERER,
        RobotType.MUCKRAKER,
    };

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

    static Direction randomDirection() {
      return directions[(int) (Math.random() * directions.length)];
    }

    static Direction randomDirection(int n, int ne, int e, int se, int s, int sw, int w, int nw) {
        int random = Math.random();

        if (random < n) {
          return Direction.NORTH;
        }
        else if (random < ne) {
          return Direction.NORTHEAST;
        }
        else if (random < e) {
          return Direction.EAST;
        }
        else if (random < se) {
          return Direction.SOUTHEAST;
        }
        else if (random < s) {
          return Direction.SOUTH;
        }
        else if (random < sw) {
          return Direction.SOUTHWEST;
        }
        else if (random < w) {
          return Direction.WEST;
        }
        else if (random < nw) {
          return Direction.NORTHWEST;
        }
        else {
          return null;
        }
    }

    static RobotType randomSpawnableRobotType() {
       return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }

    static RobotType randomSpawnableRobotType(int politician, int slanderer, int muckraker) {
        int random = Math.random();

        if (random < politician) {
          return RobotType.POLITICIAN;
        }
        else if (random < slanderer) {
          return RobotType.SLANDER;
        }
        else if (random < muckraker) {
          return RobotType.MUCKRAKER;
        }
        else {
          return null;
        }
    }

    static boolean tryMove(RobotController rc, Direction dir) throws GameActionException {
        System.out.println("Move " + dir +
                          "; Ready" + rc.isReady() +
                          "; Cooldown" + rc.getCooldownTurns() +
                          "; Can Move" + rc.canMove(dir));

        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;

    }
}
