package thunderbird;
import battlecode.common.*;
import thunderbird.Game;
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
      int influence = 11;
      RobotType toBuild = toBuild = Game.randomSpawnableRobotType(0.5,0,0.5);
      for (Direction dir : Game.directions) {
        for (Direction dir2 : Game.directions) {
          Game.tryBuild(rc, toBuild, Game.randomDirection(), influence);
        }
      }
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    static void runPolitician() throws GameActionException {
      Team me = rc.getTeam();
      Team enemy = rc.getTeam().opponent();
      int sensorRadius = rc.getType().sensorRadiusSquared;
      int actionRadius = rc.getType().actionRadiusSquared;
      int enemies = 0;
      for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius,enemy)) {
        enemies=enemies+1;
      }
      int allies = 0;
      for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius,me)) {
        allies=allies+1;
      }
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
      if (enemies>0){
      for (RobotInfo robot : rc.senseNearbyRobots(actionRadius,me)) {
        Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
      }
    }
    if (allies*7<enemies){
      for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius,enemy)) {
        Game.tryMove(rc, Game.randomDirection(0,0.2,0.11,0.2,0,0.2,0.09,0.2));
      if (Game.tryMove(rc, robot.getLocation().directionTo(rc.getLocation()))) return;
  }
}
else{
  for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius,enemy)) {
  Game.tryMove(rc, robot.getLocation().directionTo(rc.getLocation()));
}
}
      Game.tryMove(rc, Game.randomDirection(0,0.2,0.11,0.2,0,0.2,0.09,0.2));
      enemies = 0;
      allies = 0;
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    static void runSlanderer() throws GameActionException {
      Team me = rc.getTeam();
      Team enemy = rc.getTeam().opponent();
      int sensorRadius = rc.getType().sensorRadiusSquared;
    }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    static void runMuckraker() throws GameActionException {
      Team me = rc.getTeam();
      Team enemy = rc.getTeam().opponent();
      int sensorRadius = rc.getType().sensorRadiusSquared;
      int actionRadius = rc.getType().actionRadiusSquared;
      int enemies = 0;
      for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius,enemy)) {
        enemies=enemies+1;
      }
      int allies = 0;
      for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius,me)) {
        allies=allies+1;
      }
      for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
          if (robot.type.canBeExposed() && rc.canExpose(robot.location) &&
          rc.getLocation().isWithinDistanceSquared(robot.location, actionRadius)) {
              rc.expose(robot.location);
              return;
          }
      }
      if (enemies>0){
      for (RobotInfo robot : rc.senseNearbyRobots(actionRadius,me)) {
        Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
      }
    }
    if (allies*7<enemies){
      for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius,enemy)) {
        Game.tryMove(rc, Game.randomDirection(0,0.2,0.11,0.2,0,0.2,0.09,0.2));
      if (Game.tryMove(rc, robot.getLocation().directionTo(rc.getLocation()))) return;
  }
}
else{
  for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius,enemy)) {
  Game.tryMove(rc, robot.getLocation().directionTo(rc.getLocation()));
}
}
      Game.tryMove(rc, Game.randomDirection(0,0.2,0.11,0.2,0,0.2,0.09,0.2));
      enemies = 0;
      allies = 0;
    }
}
