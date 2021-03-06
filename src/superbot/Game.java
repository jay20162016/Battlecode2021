package superbot;
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

    /*
    ██████   █████  ███    ██ ██████   ██████  ███    ███     ███████ ████████ ██    ██ ███████ ███████
    ██   ██ ██   ██ ████   ██ ██   ██ ██    ██ ████  ████     ██         ██    ██    ██ ██      ██
    ██████  ███████ ██ ██  ██ ██   ██ ██    ██ ██ ████ ██     ███████    ██    ██    ██ █████   █████
    ██   ██ ██   ██ ██  ██ ██ ██   ██ ██    ██ ██  ██  ██          ██    ██    ██    ██ ██      ██
    ██   ██ ██   ██ ██   ████ ██████   ██████  ██      ██     ███████    ██     ██████  ██      ██
    */

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

    /*
    ████████ ██████  ██    ██     ███████ ████████ ██    ██ ███████ ███████
       ██    ██   ██  ██  ██      ██         ██    ██    ██ ██      ██
       ██    ██████    ████       ███████    ██    ██    ██ █████   █████
       ██    ██   ██    ██             ██    ██    ██    ██ ██      ██
       ██    ██   ██    ██        ███████    ██     ██████  ██      ██
    */

    static boolean tryMove(RobotController rc, Direction dir) throws GameActionException {
        if (dir == null) {return false;}
        if (rc.canMove(dir) && rc.sensePassability(rc.getLocation().add(dir)) > 0.3) {
            rc.move(dir);
            return true;
        }
        Direction left = dir.rotateLeft();
        if (rc.canMove(left) && rc.sensePassability(rc.getLocation().add(left)) > 0.2) {
            rc.move(left);
            return true;
        }
        Direction right = dir.rotateRight();
        if (rc.canMove(right) && rc.sensePassability(rc.getLocation().add(right)) > 0.1) {
            rc.move(right);
            return true;
        }
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }
        if (rc.canMove(left)) {
            rc.move(left);
            return true;
        }
        if (rc.canMove(right)) {
            rc.move(right);
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

    /*
    ███████ ██       █████   ██████      ███████ ████████ ██    ██ ███████ ███████
    ██      ██      ██   ██ ██           ██         ██    ██    ██ ██      ██
    █████   ██      ███████ ██   ███     ███████    ██    ██    ██ █████   █████
    ██      ██      ██   ██ ██    ██          ██    ██    ██    ██ ██      ██
    ██      ███████ ██   ██  ██████      ███████    ██     ██████  ██      ██
    */

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
