package tankbot;
import battlecode.common.*;
import tankbot.Game;

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
        int influence = 50;
        RobotType toBuild;
        if ((rc.getRoundNum()+1) <100){
          toBuild = Game.randomSpawnableRobotType(0,0,1);
          influence = 1;
        }
        else{
          if ((rc.getRoundNum()+1) <200){
            toBuild = Game.randomSpawnableRobotType(0.2,0.5,0.3);
            influence = 50;
          }
          else{
            if ((rc.getRoundNum()+1) <700){
            toBuild = Game.randomSpawnableRobotType(0.2,0.1,0.7);
            influence = 100;
          }
            else{
              toBuild = Game.randomSpawnableRobotType(0.3,0.1,6);
              influence = (rc.getInfluence())/7;
            }
          }
        }
        // int influence = 50;
        for (Direction dir : Game.directions) {
          if (toBuild == RobotType.POLITICIAN || toBuild == RobotType.MUCKRAKER){
            Game.tryBuild(rc, toBuild, dir, influence);
          }
          else{
            Game.tryBuild(rc, toBuild, dir, 30);
          }
        }
    }

    static void runPolitician() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius, enemy);
        if (attackable.length != 0 && rc.canEmpower(actionRadius)) {
            // System.out.println("empowering...");
            rc.empower(actionRadius);
            // System.out.println("empowered");
            return;
        }
        Game.tryMove(rc,Game.randomDirection());
    }

    static void runSlanderer() throws GameActionException {
        Game.tryMove(rc,Game.randomDirection());
    }

    static void runMuckraker() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) {
                    // System.out.println("e x p o s e d");
                    rc.expose(robot.location);
                    return;
                }
            }
        }
        Game.tryMove(rc,Game.randomDirection());
    }
}
