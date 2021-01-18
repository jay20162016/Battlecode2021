package tankbot7;
import battlecode.common.*;
import tankbot7.Game;
public strictfp class RobotPlayer {
    static RobotController rc;
    static MapLocation enlightenmentCenter;
    static int turnCount;
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
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
            try {
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER: runEnlightenmentCenter(); break;
                    case POLITICIAN:           runPolitician();          break;
                    case SLANDERER:            runSlanderer();           break;
                    case MUCKRAKER:            runMuckraker();           break;
                }
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    static void runEnlightenmentCenter() throws GameActionException {
      int influence = 21;
      RobotType toBuild = toBuild = Game.randomSpawnableRobotType(0,1,0);
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
          if (rc.getRoundNum()+1<700){
            toBuild = Game.randomSpawnableRobotType(0,0,1);
            influence = 1;
          }
          else{
            toBuild = Game.randomSpawnableRobotType(0.7,0,0.3);
            if (toBuild == RobotType.MUCKRAKER){
            influence = 3;
          }
          else{
            influence = 70;
          }

          }
          if (rc.getRoundNum()%36 < 2){
            influence = Math.min(rc.getInfluence(), 949);
            toBuild = RobotType.SLANDERER;
          }
            for (Direction dir : Game.directions) {
              for (Direction dir2 : Game.directions) {
                Game.tryBuild(rc, toBuild, Game.randomDirection(), influence);
              }
            }
          //   if (rc.getRoundNum()+1>420){
          //     if (rc.getInfluence()<300){
          //   rc.bid(rc.getInfluence()/10);
          // }
          // else{
          //   rc.bid(30);
          // }
          // }
      }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    static void runPolitician() throws GameActionException {
      Team me = rc.getTeam();
      Team enemy = rc.getTeam().opponent();
      int sensorRadius = rc.getType().sensorRadiusSquared;
      int actionRadius = rc.getType().actionRadiusSquared;
      int backing = rc.senseNearbyRobots(actionRadius, me).length;
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

      if (rc.canEmpower(actionRadius) && rc.getCooldownTurns() == 0) {
          for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
              rc.empower(actionRadius);
              return;
          }
          for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, Team.NEUTRAL)) {
              rc.empower(actionRadius);
              return;
          }
      }
      for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius,me)) {
          if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
              Game.tryMove(rc, Game.randomDirection());
              if (Game.tryMove(rc, robot.getLocation().directionTo(rc.getLocation()))) return;
          }
      }
      for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius,enemy)) {
            Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
          }
      Game.tryMove(rc, Game.randomDirection());
}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    static void runSlanderer() throws GameActionException {
      Team me = rc.getTeam();
      Team enemy = rc.getTeam().opponent();
      int sensorRadius = rc.getType().sensorRadiusSquared;
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemy)) {
              if (Game.tryMove(rc, robot.getLocation().directionTo(rc.getLocation()))) return;
      }
      for (RobotInfo robot : rc.senseNearbyRobots(3, me)) {
          if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
              Game.tryMove(rc, Game.randomDirection());
              if (Game.tryMove(rc, robot.getLocation().directionTo(rc.getLocation()))) return;
          }
      }
  }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    static void runMuckraker() throws GameActionException {
      Team me = rc.getTeam();
      Team enemy = rc.getTeam().opponent();
      int sensorRadius = rc.getType().sensorRadiusSquared;
      int actionRadius = rc.getType().actionRadiusSquared;

      int backing = rc.senseNearbyRobots(actionRadius, me).length;
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed() && rc.canExpose(robot.location) &&
            rc.getLocation().isWithinDistanceSquared(robot.location, actionRadius)) {
                rc.expose(robot.location);
                return;
            }
        }
        for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius)) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                Game.tryMove(rc, Game.randomDirection());
                if (Game.tryMove(rc, robot.getLocation().directionTo(rc.getLocation()))) return;
            }
        }
        for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius,enemy)) {
              if (backing > 10) Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
        }
        if (backing > 15) Game.tryMove(rc, Game.randomDirection());
  }
}
