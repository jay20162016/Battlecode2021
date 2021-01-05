package initialjaybot;
import battlecode.common.*;
import initialjaybot.Game;

public strictfp class RobotPlayer {
    static RobotController rc;
    static MapLocation enlightenmentCenter;


    static int turnCount;

    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        turnCount = 0;


        Team me = rc.getTeam();
        int sensorRadius = rc.getType().sensorRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(sensorRadius, me);
        for (RobotInfo robot : attackable) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                  RobotPlayer.enlightenmentCenter = robot.getLocation();
                  break;
            }
        }

        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER: runEnlightenmentCenter(); break;
                    case POLITICIAN:           runPolitician();          break;
                    case SLANDERER:            runSlanderer();           break;
                    case MUCKRAKER:            runMuckraker();           break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void runEnlightenmentCenter() throws GameActionException {
      System.out.println(rc.getInfluence());
      System.out.println(rc.getConviction());
        RobotType toBuild = Game.randomSpawnableRobotType(0.2, 0.4, 0.4);
        int influence = 1;
        if (toBuild == RobotType.POLITICIAN) {
          influence = 200;
        }
        else if (toBuild == RobotType.MUCKRAKER) {
          influence = 1;
        }
        for (Direction dir : Game.directions) {
          for (Direction dir2 : Game.directions) {
            Game.tryBuild(rc, toBuild, Game.randomDirection(), influence);
          }
        }
    }

    static void runPolitician() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (rc.canEmpower(actionRadius)) {
            for (RobotInfo robot : attackable) {
                // if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                      rc.empower(actionRadius);
                      return;
                // }
            }
        }

        if (Math.random() < 0.8) {
          Game.tryMove(rc, Game.randomDirection());
        }
        else {
          Game.tryMove(rc, enlightenmentCenter.directionTo(rc.getLocation()));
        }
    }

    static void runSlanderer() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int sensorRadius = rc.getType().sensorRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemy)) {
            if (robot.type == RobotType.MUCKRAKER) {
                // AAAAARRRGGHHH! it's a muckraker!
                if (Game.tryMove(rc, robot.location.directionTo(rc.getLocation()))) return;
            }
        }
        Game.tryMove(rc, Game.randomDirection());
    }

    static void runMuckraker() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) {
                    rc.expose(robot.location);
                    return;
                }
            }
        }
        if (rc.senseNearbyRobots(actionRadius, enemy).length > 0) {
          Game.tryMove(rc, Game.randomDirection());
          return;
        }

        MapLocation loc = rc.getLocation(); //lattice
        for (Direction dir : Game.directions) {
          for (Direction dir2 : Game.directions) {
            Direction d3 = Game.randomDirection();
            if (!(((loc.add(d3).x % 3) == 1) ^ ((loc.add(d3).y % 3) == 1))) {
              Game.tryMove(rc, d3);
              return;
            }
          }
        }
        // Game.tryMove(rc, Game.randomDirection());
    }
}
