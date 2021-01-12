package mixbot;
import battlecode.common.*;
import mixbot.Game;

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
      // System.out.println(rc.getInfluence());
      // System.out.println(rc.getConviction());
        // RobotType toBuild = Game.randomSpawnableRobotType(0.2,0.4,0.4);
        if (rc.canBid((rc.getInfluence())/7)) rc.bid((rc.getInfluence())/7);
        int influence = 50;
        RobotType toBuild;
        if ((rc.getRoundNum()+1) <30){
          toBuild = Game.randomSpawnableRobotType(0,1,0);
          influence = 1;
        }
        else{
          if ((rc.getRoundNum()+1) <200){
            toBuild = Game.randomSpawnableRobotType(0.2,0.1,0.7);
            influence = (rc.getInfluence())/21;
          }
          else{
            if ((rc.getRoundNum()+1) <700){
            toBuild = Game.randomSpawnableRobotType(0.4,0.2,0.4);
            influence = (rc.getInfluence())/17;
          }
            else{
              toBuild = Game.randomSpawnableRobotType(0.9,0.05,0.05);
              influence = (rc.getInfluence())/13;
            }
          }
        }
        // int influence = 50;
        for (Direction dir : Game.directions) {
          if (toBuild == RobotType.MUCKRAKER){
            Game.tryBuild(rc, toBuild, dir, 1);
          }
          if (toBuild == RobotType.POLITICIAN && influence>=10){
            Game.tryBuild(rc, toBuild, dir, influence);
          }
          else{
            Game.tryBuild(rc, toBuild, dir, 37);
          }
        }
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

        for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, me)) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                Game.tryMove(rc, robot.getLocation().directionTo(rc.getLocation()));
            }
        }

        // if (rc.senseNearbyRobots(sensorRadius, me).length < 7) {
        //   return;
        // }
        mouter : {
            for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, Team.NEUTRAL)) {
                Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
                //rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
                //rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 255, 0, 0);
                flag = 3;
            }
            for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemy)) {
                Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
                //rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
                //rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 255, 0, 0);
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                  flag = 4;
                }
                else {
                  flag = 3;
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
        Team enemy = rc.getTeam().opponent();
        int sensorRadius = rc.getType().sensorRadiusSquared;
        int flag = 0;

        outer: {
          for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemy)) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
              flag = 4;
              break outer;
            }
            else {
              flag = 3;
              break outer;
            }
          }

          for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemy.opponent())) {
              if (rc.canGetFlag(robot.ID) && rc.getFlag(robot.ID) != 0 && rc.getID() != robot.ID) {
                  flag = Math.max(flag, rc.getFlag(robot.ID) - 1);
              break outer;
              }
          }
        }

        MapLocation loc = rc.getLocation(); //lattice
         for (Direction dir : Game.directions) {
           for (Direction dir2 : Game.directions) {
             Direction d3 = Game.randomDirection();
             if (!(((loc.add(d3).x % 2) == 1) ^ ((loc.add(d3).y % 2) == 1))) {
               Game.tryMoveAbs(rc, d3);
             }
           }
         }

        if (rc.canSetFlag(flag) && rc.getFlag(rc.getID()) != flag) {rc.setFlag(flag);}

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

        for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, me)) {
            if (robot.type == RobotType.POLITICIAN && Math.random() < 0.4) {
                for (int i = 0; i < 26; i++) {
                  Game.tryMove(rc, Game.randomDirection());
                }
            }
        }

        for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, me)) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                Game.tryMove(rc, robot.getLocation().directionTo(rc.getLocation()));
            }
        }

        // if (rc.senseNearbyRobots(sensorRadius, me).length < 7) {
        //   return;
        // }
        mouter : {
            for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemy)) {
              Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
              //rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
              //rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 255, 0, 0);
              if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                flag = 4;
              }
              else {
                flag = 3;
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
