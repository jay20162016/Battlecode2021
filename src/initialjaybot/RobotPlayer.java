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
                //System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void runEnlightenmentCenter() throws GameActionException {
      // //System.out.println("initialjaybot - infl, conv");
      // //System.out.println(rc.getInfluence());
      // //System.out.println(rc.getConviction());
        // RobotType toBuild = Game.randomSpawnableRobotType(0.2, 0.2, 0.6);
        // RobotType toBuild = Game.randomSpawnableRobotType(
        //     (rc.getRoundNum() + 2000)/7000 * 0.7,
        //     0.3,
        //     0.8 - (rc.getRoundNum() + 2000)/7000 * 0.7
        // );
        double pol, sln, muk;
        pol = 0.5;
        sln = 0.2;
        muk = 0.3;
        RobotType toBuild = Game.randomSpawnableRobotType(pol, sln, muk);
        int influence;

        switch (toBuild) {
          case MUCKRAKER:
            if (rc.getInfluence() < Math.sqrt(200 * rc.getRoundNum()) && rc.getInfluence() < rc.getRoundNum()) {
              toBuild = RobotType.SLANDERER;
            }
            else {
              //       min         ( safety fudge,                                    half influence)
              influence = Math.min(rc.getInfluence() - Math.max(rc.getRoundNum(), 150), rc.getInfluence()/2);
              if (Math.random() < 400/(rc.getRoundNum()+1)) {
                influence = 1;
              }
              break;
            }
          case POLITICIAN:
            if (rc.getInfluence() < Math.sqrt(200 * rc.getRoundNum()) && rc.getInfluence() < rc.getRoundNum()) {
              toBuild = RobotType.SLANDERER;
            }
            else {
              //       min         ( safety fudge,                                    949)
              influence = Math.min(rc.getInfluence() - Math.max(rc.getRoundNum(), 150), 949);
              break;
            }
          case SLANDERER:
            //       min         ( safety fudge,                                    949)
            influence = (int) Math.floor(Math.min(rc.getInfluence() - Math.max(rc.getRoundNum(), 150), 949)/12) * 12;
            if (rc.getInfluence() <= 20 || Math.random() < 0.4) {
              toBuild = null;
              influence = -1000;
            }
            else if (rc.getInfluence() <= 24) {
              influence = 21;
            }
            else if (influence < 0) {
              influence = rc.getInfluence();
            }
            break;
          default:
            toBuild = null;
            influence = 0;
        }

        if (influence < 0) {
          influence = Math.min(rc.getInfluence(), Math.abs(rc.getInfluence() - 50));
        }

        System.out.println(toBuild);
        System.out.println(influence);

        if (rc.getRoundNum() < 20 && rc.getInfluence() == 150) {
          influence = 150;
          toBuild = RobotType.SLANDERER;
        }


        for (RobotInfo robot : rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent())) {
            //rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
            //rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 255, 0, 0);
            toBuild = RobotType.POLITICIAN;
            influence = (int) (rc.getInfluence() / 1.2) + 12;
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
                if (rc.canEmpower(actionRadius)) {
                    for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
                        if (robot.type == RobotType.ENLIGHTENMENT_CENTER || Math.random() < 0.4 ||
                            rc.getLocation().distanceSquaredTo(robot.location) < 4) {
                              rc.empower(rc.getLocation().distanceSquaredTo(robot.location));
                              // //System.out.println("initialjaybot - BIG BOOM KAMIKAZE!!!");
                              break outer;
                        }
                    }
                    for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, Team.NEUTRAL)) {
                        rc.empower(actionRadius);
                        // //System.out.println("initialjaybot - BIG BOOM KAMIKAZE!!!");
                        break outer;
                    }
                }
        }
        mouter : {
            for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, Team.NEUTRAL)) {
                Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
                //rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
                //rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 255, 0, 0);
                flag = 2;
            }
            for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemy)) {
                Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
                //rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
                //rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 255, 0, 0);
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                  flag = 6;
                }
                else {
                  flag = 2;
                }
            }

            for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, me)) {
                if (rc.canGetFlag(robot.ID) && rc.getFlag(robot.ID) != 0 && rc.getID() != robot.ID) {
                    flag = Math.max(flag, rc.getFlag(robot.ID) - 1);
                    Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
                    //rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 0, 0, 255);
                    //rc.setIndicatorDot(rc.getLocation(), 255, 0, 150);
                      // //System.out.println("initialjaybot - BIG BOOM KAMIKAZE!!!");
                }
            }
        }

        if (Math.random() < 0.4) {
          Game.tryMove(rc, Game.directions[(rc.getID() + rc.getRoundNum()/10) % 8]);
        }
        else if (Math.random() < 0.4) {
        }
        else {
          Game.tryMove(rc, Game.randomDirection());
        }

        if (rc.canSetFlag(flag) && rc.getFlag(rc.getID()) != flag) {rc.setFlag(flag);}
    }

    static void runSlanderer() throws GameActionException {
        Team enemy = rc.getTeam();
        int sensorRadius = rc.getType().sensorRadiusSquared;

        MapLocation loc = rc.getLocation(); //lattice
         for (Direction dir : Game.directions) {
           for (Direction dir2 : Game.directions) {
             Direction d3 = Game.randomDirection();
             if (!(((loc.add(d3).x % 2) == 1) ^ ((loc.add(d3).y % 2) == 1))) {
               Game.tryMove(rc, d3);
               return;
             }
           }
         }

        // if (Math.random() < 0.2) {
        //   Game.tryMove(rc, Game.directions[(rc.getID() + rc.getRoundNum()/10) % 8]);
        // }
        // else if (Math.random() < 0.4) {
        // }
        // else {
        //   Game.tryMove(rc, Game.randomDirection());
        // }
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
                // //System.out.println("initialjaybot - e x p o s e d and shot");
                break;
            }
        }
        mouter : {
            for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemy)) {
              Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
              //rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
              //rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 255, 0, 0);
              if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                flag = 6;
              }
              else {
                flag = 2;
              }
            }

            for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, me)) {
                if (rc.canGetFlag(robot.ID) && rc.getFlag(robot.ID) != 0 && rc.getID() != robot.ID) {
                    flag = Math.max(flag, rc.getFlag(robot.ID) - 1);
                    Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
                    //rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 0, 0, 255);
                    //rc.setIndicatorDot(rc.getLocation(), 255, 0, 150);
                      // //System.out.println("initialjaybot - BIG BOOM KAMIKAZE!!!");
                }
            }
        }

        if (Math.random() < 0.4) {
          Game.tryMove(rc, Game.directions[(rc.getID() + rc.getRoundNum()/10) % 8]);
        }
        else if (Math.random() < 0.4) {
        }
        else {
          Game.tryMove(rc, Game.randomDirection());
        }

        if (rc.canSetFlag(flag) && rc.getFlag(rc.getID()) != flag) {rc.setFlag(flag);}
    }
}
