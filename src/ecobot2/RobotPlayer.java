package ecobot2;
import battlecode.common.*;
import ecobot2.Game;
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
      Team enemy = rc.getTeam().opponent();
      int sensorRadius = rc.getType().sensorRadiusSquared;
      int enemies = rc.senseNearbyRobots(sensorRadius, enemy).length;
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
          if (rc.getRoundNum()+1<360){
            toBuild = Game.randomSpawnableRobotType(0.1,0,0.9);
            if (toBuild == RobotType.MUCKRAKER){
            influence = 1;
          }
          else{
            influence = 20;
          }
        }
          else{
            toBuild = Game.randomSpawnableRobotType(0.7,0,0.3);
            if (toBuild == RobotType.MUCKRAKER){
            influence = 7;
          }
          else{
            influence = 70;
          }

          }
          if (rc.getInfluence()<2000){
          if (rc.getRoundNum()%23 < 2 || rc.getInfluence()>1000 && enemies==0){
            influence = Math.min(rc.getInfluence(), 949);
            toBuild = RobotType.SLANDERER;
          }
        }
        else{
          if (rc.getRoundNum()%20 < 2 && enemies==0){
            influence = Math.min(rc.getInfluence(), 949);
            toBuild = RobotType.SLANDERER;
          }
        }
            for (Direction dir : Game.directions) {
              for (Direction dir2 : Game.directions) {
                Game.tryBuild(rc, toBuild, Game.randomDirection(), influence);
              }
            }
            if (rc.getRoundNum()+1>420 && rc.getTeamVotes()<751){
              if (rc.getInfluence()<300){
            rc.bid(rc.getInfluence()/10);
          }
          else{
            rc.bid(30);
          }
          }
          else{
            if (rc.getRoundNum()+1<420) rc.bid(1);
          }
      }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    static void runPolitician() throws GameActionException {
      Team me = rc.getTeam();
      Team enemy = rc.getTeam().opponent();
      int sensorRadius = rc.getType().sensorRadiusSquared;
      int actionRadius = rc.getType().actionRadiusSquared;
      int backing = rc.senseNearbyRobots(actionRadius, me).length;
      int enemies = rc.senseNearbyRobots(actionRadius, enemy).length;
      int flag = enemies-backing;
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
      for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, Team.NEUTRAL)) {
        if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
          flag = flag + 100;
          Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
      }
    }
    for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemy)) {
      if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
        flag = flag + 100;
        Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
    }
  }
      for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius,me)) {
          if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
              Game.tryMove(rc, Game.randomDirection(0,0.2,0.11,0.2,0,0.2,0.09,0.2));
              if (Game.tryMove(rc, robot.getLocation().directionTo(rc.getLocation()))) return;
          }
      }
      for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius,enemy)) {
            Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
          }
      Game.tryMove(rc, Game.randomDirection(0,0.2,0.11,0.2,0,0.2,0.09,0.2));
      if (rc.canSetFlag(flag) && rc.getFlag(rc.getID()) != flag) {
        rc.setFlag(flag);
      }
      for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, me)) {
        if (rc.canGetFlag(robot.ID) && rc.getFlag(robot.ID)>flag && rc.getID() != robot.ID && rc.getFlag(robot.ID)>0) {
          Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
        }
      }
      flag=0;
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
      Game.tryMove(rc, Game.randomDirection(0,0.2,0.11,0.2,0,0.2,0.09,0.2));
      for (RobotInfo robot : rc.senseNearbyRobots(2, me)) {
          if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
              Game.tryMove(rc, Game.randomDirection(0,0.2,0.11,0.2,0,0.2,0.09,0.2));
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
      int enemies = rc.senseNearbyRobots(actionRadius, enemy).length;
      int flag;
      flag = enemies-backing;
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed() && rc.canExpose(robot.location) &&
            rc.getLocation().isWithinDistanceSquared(robot.location, actionRadius)) {
                rc.expose(robot.location);
                return;
            }
        }
      //   for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, Team.NEUTRAL)) {
      //     if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
      //     Game.tryMove(rc, robot.getLocation().directionTo(rc.getLocation()));
      //   }
      // }
      for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, Team.NEUTRAL)) {
        if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
          flag = flag + 20;
          Game.tryMove(rc, Game.randomDirection(0,0.2,0.11,0.2,0,0.2,0.09,0.2));
          if (Game.tryMove(rc, robot.getLocation().directionTo(rc.getLocation()))) return;
          // if (Math.random()<0.3) Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
      }
    }    for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemy)) {
          if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
            flag = flag + 100;
            Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
        }
      }
        for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, me)) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                Game.tryMove(rc, Game.randomDirection(0,0.2,0.11,0.2,0,0.2,0.09,0.2));
                if (Game.tryMove(rc, robot.getLocation().directionTo(rc.getLocation()))) return;
            }
        }
        for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius,enemy)) {
              if (backing > enemies) Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
        }
        if (backing > 17) Game.tryMove(rc, Game.randomDirection(0,0.2,0.11,0.2,0,0.2,0.09,0.2));
        for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, me)) {
          if (rc.canGetFlag(robot.ID) && rc.getFlag(robot.ID)>flag && rc.getID() != robot.ID && rc.getFlag(robot.ID)>0) {
            Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
          }
        }
  // Game.tryMove(rc, Game.randomDirection(0,0.2,0.11,0.2,0,0.2,0.09,0.2));
  if (rc.canSetFlag(flag) && rc.getFlag(rc.getID()) != flag) {
    rc.setFlag(flag);
  }
  flag=0;

}
}
