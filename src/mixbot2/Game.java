package mixbot2;
import battlecode.common.*;
// import java.util.Random;

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

    static int rs;
    // static Random rand = null;

    static double random() {
      // if (rand == null) {
      //   rand = new Random(rs);
      // }
      // return rand.nextDouble();
      return Math.random();
    }

    static Direction randomDirection() {
      return directions[(int) (random() * directions.length)];
    }

    static Direction randomDirection(double n, double ne, double e, double se, double s, double sw, double w, double nw) {
        double random = random();

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
       return spawnableRobot[(int) (random() * spawnableRobot.length)];
    }

    static RobotType randomSpawnableRobotType(double politician, double slanderer, double muckraker) {
        double random = random();

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
      // even formattteing isi mezzed up (not anymore)
      switch (dir) {
        case NORTH:     return new Direction[]{directions[7], directions[0], directions[1]};
        case NORTHEAST: return new Direction[]{directions[0], directions[1], directions[2]};
        case EAST:      return new Direction[]{directions[1], directions[2], directions[3]};
        case SOUTHEAST: return new Direction[]{directions[2], directions[3], directions[4]};
        case SOUTH:     return new Direction[]{directions[3], directions[4], directions[5]};
        case SOUTHWEST: return new Direction[]{directions[4], directions[5], directions[6]};
        case WEST:      return new Direction[]{directions[5], directions[6], directions[7]};
        case NORTHWEST: return new Direction[]{directions[6], directions[7], directions[0]};
        default: return null;
      }
     }


    static boolean tryMove(RobotController rc, Direction dir) throws GameActionException {
        if (dir == null) {return false;}
        if (rc.canMove(dir) && rc.sensePassability(rc.getLocation().add(dir)) > 0.15) {
            rc.move(dir);
            return true;
        }
        Direction[] dir2= directionInterval(dir);
        if (rc.canMove(dir2[0]) && rc.sensePassability(rc.getLocation().add(dir2[0])) > 0.1) {
            rc.move(dir2[0]);
            return true;
        }
        if (rc.canMove(dir2[2]) && rc.sensePassability(rc.getLocation().add(dir2[2])) > 0.05) {
            rc.move(dir2[2]);
            return true;
        }
        return false;

    }


   static boolean tryMoveAbs(RobotController rc, Direction dir) throws GameActionException {
       if (dir == null) {return false;}
       if (rc.canMove(dir)) {
           rc.move(dir);
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

    // static int toCoords(MapLocation loc) {
    //   return loc.x & 127 * 128 + loc.y & 127;
    // }
    //
    // static MapLocation fromCoords(MapLocation loc, int coord) {
    //   int x = (int) (coord / 128);
    //   int y = (int) (coord % 128);
    //   // int p = (loc.x - x) & 127, q = (loc.y - y) & 127;
    //   int p = (x - loc.x) & 127, q = (y - loc.y) & 127;
    //   if (p >= 64) p -= 128; if (q >= 64) q -= 128;
    //
    //   return new MapLocation(loc.x + p, loc.y + q);
    // }


    static final int NBITS = 7;
    static final int BITMASK = (1 << NBITS) - 1;

    static int toCoords(MapLocation location) throws GameActionException {
        int x = location.x, y = location.y;
        int encodedLocation = ((x & BITMASK) << NBITS) + (y & BITMASK);
        return encodedLocation;
    }

    static MapLocation fromCoords(MapLocation loc, int flag) {
        int y = flag & BITMASK;
        int x = (flag >> NBITS) & BITMASK;

        MapLocation currentLocation = loc;
        int offsetX128 = currentLocation.x >> NBITS;
        int offsetY128 = currentLocation.y >> NBITS;
        MapLocation actualLocation = new MapLocation((offsetX128 << NBITS) + x, (offsetY128 << NBITS) + y);

        // You can probably code this in a neater way, but it works
        MapLocation alternative = actualLocation.translate(-(1 << NBITS), 0);
        if (loc.distanceSquaredTo(alternative) < loc.distanceSquaredTo(actualLocation)) {
            actualLocation = alternative;
        }
        alternative = actualLocation.translate(1 << NBITS, 0);
        if (loc.distanceSquaredTo(alternative) < loc.distanceSquaredTo(actualLocation)) {
            actualLocation = alternative;
        }
        alternative = actualLocation.translate(0, -(1 << NBITS));
        if (loc.distanceSquaredTo(alternative) < loc.distanceSquaredTo(actualLocation)) {
            actualLocation = alternative;
        }
        alternative = actualLocation.translate(0, 1 << NBITS);
        if (loc.distanceSquaredTo(alternative) < loc.distanceSquaredTo(actualLocation)) {
            actualLocation = alternative;
        }
        return actualLocation;
    }

    static int dir(Direction dir) {
       if (dir == directions[0]) {
           return 0;
       }
       else if (dir == directions[1]) {
           return 1;
       }
       else if (dir == directions[2]) {
           return 2;
       }
       else if (dir == directions[3]) {
           return 3;
       }
       else if (dir == directions[4]) {
           return 4;
       }
       else if (dir == directions[5]) {
           return 5;
       }
       else if (dir == directions[6]) {
           return 6;
       }
       else {
           return 7;
       }
    }

    static Direction fromDir(int d) {
      // System.out.println("JEISLFJEISLFJSELIJFSLIEFJ");
      // System.out.println(d);
      if (d < 8)
        return directions[d];
      else return null;
    }
}
