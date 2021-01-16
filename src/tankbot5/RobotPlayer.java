package tankbot5;
import battlecode.common.*;
import tankbot5.Game;
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
      if (rc.getTeam()==Team.B){
          if (rc.getRoundNum()+1<700){
            toBuild = Game.randomSpawnableRobotType(0,0,1);
            influence = 1;
          }
          else{
            toBuild = Game.randomSpawnableRobotType(0.5,0,0.5);
            influence = 11;
          }
            for (Direction dir : Game.directions) {
              for (Direction dir2 : Game.directions) {
                Game.tryBuild(rc, toBuild, Game.randomDirection(), influence);
              }
            }
            if (rc.getRoundNum()+1>100){
            rc.bid(rc.getInfluence()/5);}
          }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
          else{
            if ((rc.getRoundNum()+1) <30){
              toBuild = Game.randomSpawnableRobotType(0,1,0);
              influence = 1;
            }
            else{
              if ((rc.getRoundNum()+1) <200){
                toBuild = Game.randomSpawnableRobotType(0.2,0.1,0.7);
                influence = (rc.getInfluence())-100;
              }
              else{
                if ((rc.getRoundNum()+1) <700){
                toBuild = Game.randomSpawnableRobotType(0.4,0.1,0.5);
                influence = (rc.getInfluence())/17;
              }
                else{
                  toBuild = Game.randomSpawnableRobotType(0.5,0.1,0.4);
                  influence = (rc.getInfluence())/7;
                }
              }
            }
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
            rc.bid(rc.getInfluence()/7);
        }
      }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    static void runPolitician() throws GameActionException {
      Team me = rc.getTeam();
      Team enemy = rc.getTeam().opponent();
      int sensorRadius = rc.getType().sensorRadiusSquared;
      int actionRadius = rc.getType().actionRadiusSquared;
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      if (rc.getTeam()==Team.B){
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
                  Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));}
            for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius,enemy)) {
                  Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
            }
            for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, me)) {
              Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));}
  }
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  else{
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
    if (rc.getRoundNum()%2==1){
            for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius)) {
            if (Game.tryMove(rc, robot.getLocation().directionTo(rc.getLocation()))) return;
        }
}
    else{
      for (RobotInfo robot : rc.senseNearbyRobots(actionRadius)) {
        Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
      }
    }
    for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, Team.NEUTRAL)) {
        Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
    }
    for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemy)) {
      Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
    }
    if (Math.random()<0.3){
    Game.tryMove(rc, Game.randomDirection());
  }
  }
}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    static void runSlanderer() throws GameActionException {
      Team me = rc.getTeam();
      Team enemy = rc.getTeam().opponent();
      int sensorRadius = rc.getType().sensorRadiusSquared;
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      if (rc.getTeam()==Team.B){
            for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius,me)) {
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                  Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
                }
            }
            if (Math.random()<0.3){
            for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, me)) {
              Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));}}
    }
    else{
      for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemy)) {
          if (robot.type == RobotType.MUCKRAKER) {
              // AAAAARRRGGHHH! it's a muckraker!
              if (Game.tryMove(rc, robot.getLocation().directionTo(rc.getLocation()))) return;
              // System.out.println("initialjaybot - A MUCKRAKER ARCHER !!!");
          }
      }
      if (Math.random()<0.1){
      Game.tryMove(rc, Game.randomDirection());
    }
    }
  }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    static void runMuckraker() throws GameActionException {
      Team me = rc.getTeam();
      Team enemy = rc.getTeam().opponent();
      int sensorRadius = rc.getType().sensorRadiusSquared;
      int actionRadius = rc.getType().actionRadiusSquared;
      //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      if (rc.getTeam()==Team.B){
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
                  Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));}
            for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius,me)) {
                  Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
            }

            for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, me)) {
              Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));}
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    else{
      for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
          if (robot.type.canBeExposed() && rc.canExpose(robot.location) &&
          rc.getLocation().isWithinDistanceSquared(robot.location, actionRadius)) {
              rc.expose(robot.location);
              return;
          }
          else {
              Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
          }
      }
      if (rc.getRoundNum()%2==1){
              for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius)) {
              if (Game.tryMove(rc, robot.getLocation().directionTo(rc.getLocation()))) return;
          }
}
      else{
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius)) {
          Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
        }
      }
      if (Math.random()<0.5){
      Game.tryMove(rc, Game.randomDirection());
      }
    }
  }
}
