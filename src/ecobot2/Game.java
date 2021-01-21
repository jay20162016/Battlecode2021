package ecobot2;
import battlecode.common.*;

public strictfp class Game {
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

    static Direction randomDirection(double n, double ne, double e, double se, double s, double sw, double w, double nw) {
        double random = Math.random();

        if (random < n) {
          return Direction.NORTH;
        }
        else if (random < n+ne) {
          return Direction.NORTHEAST;
        }
        else if (random < n+ne+e) {
          return Direction.EAST;
        }
        else if (random < n+ne+e+se) {
          return Direction.SOUTHEAST;
        }
        else if (random < n+ne+e+se+s) {
          return Direction.SOUTH;
        }
        else if (random < n+ne+e+se+s+sw) {
          return Direction.SOUTHWEST;
        }
        else if (random < n+ne+e+se+s+sw+w) {
          return Direction.WEST;
        }
        else if (random < n+ne+e+se+s+sw+w+nw) {
          return Direction.NORTHWEST;
        }
        else {
          return null;
        }
    }

    static RobotType randomSpawnableRobotType() {
       return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }

    static RobotType randomSpawnableRobotType(double politician, double slanderer, double muckraker) {
        double random = Math.random();

        if (random < politician) {
          return RobotType.POLITICIAN;
        }
        else if (random < politician+slanderer) {
          return RobotType.SLANDERER;
        }
        else if (random < politician+slanderer+muckraker) {
          return RobotType.MUCKRAKER;
        }
        else {
          return null;
        }
    }

    static boolean tryMove(RobotController rc, Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;

    }

    static boolean tryBuild(RobotController rc, RobotType toBuild, Direction dir, int influence) throws GameActionException {
        if (rc.canBuildRobot(toBuild, dir, influence)) {
            rc.buildRobot(toBuild, dir, influence);
            return true;
        } else return false;

    }
}
