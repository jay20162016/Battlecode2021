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

        RobotPlayer.turnCount = 0;


        Team me = rc.getTeam();
        int sensorRadius = rc.getType().sensorRadiusSquared;
        RobotInfo[] allies = rc.senseNearbyRobots(sensorRadius, me);
        for (RobotInfo robot : allies) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                  RobotPlayer.enlightenmentCenter = robot.getLocation();
                  break;
            }
        }

        while (true) {
            RobotPlayer.turnCount += 1;
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
      // System.out.println("initialjaybot - infl, conv");
      // System.out.println(rc.getInfluence());
      // System.out.println(rc.getConviction());
        // RobotType toBuild = Game.randomSpawnableRobotType(0.2, 0.2, 0.6);
        // RobotType toBuild = Game.randomSpawnableRobotType(
        //     (rc.getRoundNum() + 2000)/7000 * 0.7,
        //     0.3,
        //     0.8 - (rc.getRoundNum() + 2000)/7000 * 0.7
        // );
        double pol, sln, muk;
        pol = (rc.getRoundNum() + 1000)/5000.0;
        sln = 0.4;
        muk = 1 - pol - sln;
        // System.out.println("\n------\n");
        // System.out.println(pol);
        // System.out.println(sln);
        // System.out.println(muk);
        RobotType toBuild = Game.randomSpawnableRobotType(pol, sln, muk);
        int influence = Math.min(rc.getInfluence() - Math.max(rc.getRoundNum()*3, 150), 1500) / 3 * 2;

        if (rc.getInfluence() < 300 && rc.getRoundNum() > 150) {

          influence = rc.getInfluence() - 20;
          toBuild = RobotType.SLANDERER;
        }
        // int influence = rc.getInfluence() - 150;
        if (toBuild == RobotType.MUCKRAKER && Math.random() < 400/(rc.getRoundNum()+1)) {
          influence = 1;
        }


        for (RobotInfo robot : rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent())) {
            rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
            rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 255, 0, 0);
            toBuild = Game.randomSpawnableRobotType(0.7, 0, 0.3);
            if (Math.random() < 0.2) {
              influence = rc.getInfluence() / 2;
            }
            else if (robot.type == RobotType.POLITICIAN) {
              influence = influence / 3;
            }
            else {
              influence = influence / 3 * 2;
            }
        }

        for (Direction dir : Game.directions) {
          for (Direction dir2 : Game.directions) {
            Game.tryBuild(rc, toBuild, Game.randomDirection(), influence);
          }
        }

        int vote = (int) (Math.random() * rc.getInfluence()/10);
        if (rc.canBid(vote)) rc.bid(vote);
    }

    static void runPolitician() throws GameActionException {
        int flag = 0;
//Arrays.sort(arr, Comparator.comparingInt(o->rc.getLocation().distanceSquaredTo(o.getLocation()))
        Team me = rc.getTeam();
        Team enemy = me.opponent();
        int sensorRadius = rc.getType().sensorRadiusSquared;
        int actionRadius = rc.getType().actionRadiusSquared;
        outer :{
                if (rc.canEmpower(actionRadius) && rc.getCooldownTurns() == 0) {
                    for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
                        if (robot.type == RobotType.ENLIGHTENMENT_CENTER || Math.random() < 0.4 ||
                            rc.getLocation().distanceSquaredTo(robot.location) < 4) {
                              rc.empower(rc.getLocation().distanceSquaredTo(robot.location));
                              // System.out.println("initialjaybot - BIG BOOM KAMIKAZE!!!");
                              break outer;
                        }
                    }
                    for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, Team.NEUTRAL)) {
                        rc.empower(actionRadius);
                        // System.out.println("initialjaybot - BIG BOOM KAMIKAZE!!!");
                        break outer;
                    }
                }
        }
        mouter : {
            for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, Team.NEUTRAL)) {
                Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
                rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
                rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 255, 0, 0);
                flag = 2;
            }
            for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemy)) {
                Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
                rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
                rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 255, 0, 0);
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                  flag = 4;
                }
                else {
                  flag = 2;
                }
            }

            for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, me)) {
                if (rc.canGetFlag(robot.ID) && rc.getFlag(robot.ID) != 0 && rc.getID() != robot.ID) {
                    flag = Math.max(flag, rc.getFlag(robot.ID) - 1);
                    Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
                    rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 0, 255, 0);
                    rc.setIndicatorDot(rc.getLocation(), 255, 0, 150);
                      // System.out.println("initialjaybot - BIG BOOM KAMIKAZE!!!");
                }
            }
        }

        Game.tryMove(rc, Game.randomDirection());

        if (rc.canSetFlag(flag) && rc.getFlag(rc.getID()) != flag) {rc.setFlag(flag);}
    }

    static void runSlanderer() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int sensorRadius = rc.getType().sensorRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemy)) {
            if (robot.type == RobotType.MUCKRAKER) {
                // AAAAARRRGGHHH! it's a muckraker!
                if (Game.tryMove(rc, robot.getLocation().directionTo(rc.getLocation()))) return;
                // System.out.println("initialjaybot - A MUCKRAKER ARCHER !!!");
            }
        }
        Game.tryMove(rc, Game.randomDirection());
    }

    static void runMuckraker() throws GameActionException {
          int flag = 0;

        Team me = rc.getTeam();
        Team enemy = me.opponent();
        int sensorRadius = rc.getType().sensorRadiusSquared;
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed() && rc.canExpose(robot.location)) {
                // It's a slanderer... go get them!
                rc.expose(robot.location);
                // System.out.println("initialjaybot - e x p o s e d and shot");
                break;
            }
        }
        mouter : {
            for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemy)) {
              Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
              rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
              rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 255, 0, 0);
                  flag = 2;
            }

            for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, me)) {
                if (rc.canGetFlag(robot.ID) && rc.getFlag(robot.ID) != 0 && rc.getID() != robot.ID) {
                    flag = Math.max(flag, rc.getFlag(robot.ID) - 1);
                    Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
                    rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 0, 255, 0);
                    rc.setIndicatorDot(rc.getLocation(), 255, 0, 150);
                      // System.out.println("initialjaybot - BIG BOOM KAMIKAZE!!!");
                }
            }
        }

        MapLocation loc = rc.getLocation(); //lattice
        outer: for (Direction dir : Game.directions) {
          for (Direction dir2 : Game.directions) {
            Direction d3 = Game.randomDirection();
            if (!(
                  ((loc.add(d3).x % 3) == 1) ^ ((loc.add(d3).y % 3) == 1)
                  )) {
            // if (!((loc.add(d3).x+loc.add(d3).x) % 2) == 1) {
              Game.tryMove(rc, d3);
              break outer;
            }
          }
        }
        Game.tryMove(rc, Game.randomDirection());

        if (rc.canSetFlag(flag) && rc.getFlag(rc.getID()) != flag) {rc.setFlag(flag);}
    }
}
