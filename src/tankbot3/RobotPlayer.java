package tankbot3;
import battlecode.common.*;
import tankbot3.Game;

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
      // System.out.println(rc.getInfluence());
      // System.out.println(rc.getConviction());
        // RobotType toBuild = Game.randomSpawnableRobotType(0.2,0.4,0.4);
        rc.bid((rc.getInfluence())/7);
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
        Team enemy = rc.getTeam().opponent();
        Team ally = rc.getTeam();
        int sensorRadius = rc.getType().sensorRadiusSquared;
        int actionRadius = rc.getType().actionRadiusSquared;
        if (rc.canEmpower(actionRadius) && rc.getCooldownTurns() == 0) {
            for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
                rc.empower(actionRadius);
                // System.out.println("initialjaybot - BIG BOOM KAMIKAZE!!!");
                return;
            }
            for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, Team.NEUTRAL)) {
                rc.empower(actionRadius);
                // System.out.println("initialjaybot - BIG BOOM KAMIKAZE!!!");
                return;
            }
        }
        if (rc.getRoundNum()<1700){
        for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemy)) {
          Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
        }
        for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius,ally)) {
          if (Game.tryMove(rc, robot.getLocation().directionTo(rc.getLocation()))) return;}
        for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, Team.NEUTRAL)) {
            Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
}
        Game.tryMove(rc, Game.randomDirection());
      }
      else{
        for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemy)) {
          Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));}
        for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius,ally)) {
          Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));}

        Game.tryMove(rc, Game.randomDirection());
      }



  }

    static void runSlanderer() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        Team ally = rc.getTeam();
        int sensorRadius = rc.getType().sensorRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemy)) {
            if (robot.type == RobotType.MUCKRAKER) {
                // AAAAARRRGGHHH! it's a muckraker!
                if (Game.tryMove(rc, robot.getLocation().directionTo(rc.getLocation()))) return;
                // System.out.println("initialjaybot - A MUCKRAKER ARCHER !!!");
            }
        }
        if (Math.random()<0.1){
        Game.tryMove(rc, Game.randomDirection());}
    }

    static void runMuckraker() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        Team ally = rc.getTeam();
        int sensorRadius = rc.getType().sensorRadiusSquared;
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed() && rc.canExpose(robot.location) &&
            rc.getLocation().isWithinDistanceSquared(robot.location, actionRadius)) {
                // It's a slanderer... go get them!
                rc.expose(robot.location);
                // System.out.println("initialjaybot - e x p o s e d and shot");
                return;
            }
            else {
                Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
            }
        }
        // for (RobotInfo robot : rc.senseNearbyRobots(actionRadius)) {
        //   Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
        //
        // }
        if (rc.getRoundNum()<1000){
                for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius,ally)) {
                if (Game.tryMove(rc, robot.getLocation().directionTo(rc.getLocation()))) return;}
}
        else{
            for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemy)) {
              Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));}
            for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius,ally)) {
              Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));}

            Game.tryMove(rc, Game.randomDirection());
          }


      }
}
