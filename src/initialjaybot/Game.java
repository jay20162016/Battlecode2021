package initialjaybot;
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

    static Direction[] directionInterval(Direction dir) {
      // Blacvk magic by youmu; i cuold do it too but i'mm too lazy...
      // even formattteing isi mezzed up
         Direction[] dirs = new Direction[3];
         if (dir == directions[0]) {
             dirs = new Direction[]{directions[7], directions[0], directions[1]};
         }
         else if (dir == directions[1]) {
             dirs = new Direction[]{directions[0], directions[1], directions[2]};
         }
         else if (dir == directions[2]) {
             dirs = new Direction[]{directions[1], directions[2], directions[3]};
         }
         else if (dir == directions[3]) {
             dirs = new Direction[]{directions[2], directions[3], directions[4]};
         }
         else if (dir == directions[4]) {
             dirs = new Direction[]{directions[3], directions[4], directions[5]};
         }
         else if (dir == directions[5]) {
             dirs = new Direction[]{directions[4], directions[5], directions[6]};
         }
         else if (dir == directions[6]) {
             dirs = new Direction[]{directions[5], directions[6], directions[7]};
         }
         else {
             dirs = new Direction[]{directions[6], directions[7], directions[0]};
         }
         return dirs;
     }


    static boolean tryMove(RobotController rc, Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }
        Direction[] dir2= directionInterval(dir);
        if (rc.canMove(dir2[0])) {
            rc.move(dir2[0]);
            return true;
        }
        if (rc.canMove(dir2[2])) {
            rc.move(dir2[2]);
            return true;
        }
        return false;

    }

    static boolean tryBuild(RobotController rc, RobotType toBuild, Direction dir, int influence) throws GameActionException {
        if (toBuild != null && rc.canBuildRobot(toBuild, dir, influence)) {
            rc.buildRobot(toBuild, dir, influence);
            return true;
        } else return false;

    }

    static int xCoord(MapLocation loc) {
      return loc.x & 127;
    }

    static int yCoord(MapLocation loc) {
      return loc.y & 127;
    }

    static MapLocation fromCoords(MapLocation loc, int x, int y) {
      int p = (loc.x - x) & 127, q = (loc.y - y) & 127;
      if (p >= 64) p -= 128; if (q >= 64) q -= 128;

      return new MapLocation(loc.x + p, loc.y + q);
    }
}
