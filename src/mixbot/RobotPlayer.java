package mixbot;
import battlecode.common.*;
import mixbot.Game;

import java.util.Arrays;
import java.util.Comparator;

public strictfp class RobotPlayer {
    static RobotController rc;
    static RobotInfo enlightenmentCenter;


    static int turnCount;

    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        RobotPlayer.turnCount = 0;
        Game.rs = rc.getID();


        Team me = rc.getTeam();
        int sensorRadius = rc.getType().sensorRadiusSquared;
        RobotInfo[] allies = rc.senseNearbyRobots(sensorRadius, me);
        for (RobotInfo robot : allies) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
                  RobotPlayer.enlightenmentCenter = robot;
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

    static int bidinfl = 1;
    static int prevwon = 0;
    static void doBid() throws GameActionException {
      int won = rc.getTeamVotes();
      if (won >= 751) {
        return;
      }

      if (won == prevwon) {
        bidinfl = bidinfl * 3 + 1;
        bidinfl = Math.max(1, bidinfl);
        bidinfl = Math.min(bidinfl, rc.getInfluence() / 7);
      }
      else {
        bidinfl = bidinfl - (int) Game.random() * 3;
        bidinfl = Math.max(1, bidinfl);
        bidinfl = Math.min(bidinfl, rc.getInfluence() / 7);
      }

      if (rc.canBid(bidinfl)) {
        rc.bid(bidinfl);
      }
      prevwon = won;
    }

    static int[] ids = new int[6000];
    static int whatid = 0;
    static Direction nextdir = Game.randomDirection();
    static int influence = 150;
    static RobotType toBuild = RobotType.SLANDERER;
    static void runEnlightenmentCenter() throws GameActionException {
      while (!rc.onTheMap(rc.getLocation().add(nextdir))) {
        nextdir = Game.randomDirection();
      }
      System.out.println(nextdir);
      int flag = (1 << 22) + Game.dir(nextdir);

      if (Game.tryBuild(rc, toBuild, nextdir, influence)) {
        double pol, sln, muk;
        pol = (rc.getRoundNum() + 1000)/5000.0;
        sln = 0.4;
        muk = 1 - pol - sln;
        // pol = 0; sln = 0.3; muk = 0.7;
        toBuild = Game.randomSpawnableRobotType(pol, sln, muk);

        influence = Math.min(rc.getInfluence() - Math.max(rc.getRoundNum()/2, 150) / 3 * 2, 1000);

        if (toBuild == RobotType.SLANDERER) {
          influence = Math.round(influence/20)*20 + 1;
          if (influence < rc.getInfluence()) {
            influence -= 20;
          }
          if (influence < 20) {
            influence = 21;
          }
        }
        if (toBuild == RobotType.MUCKRAKER && Game.random() < 400/(rc.getRoundNum()+1)) {
          toBuild = RobotType.MUCKRAKER;
          influence = 1;
        }
        if (influence > rc.getInfluence() || influence <= 0) {
          influence = Math.max(Math.min(rc.getInfluence() / 7, 60), 30);
        }

        nextdir = Game.randomDirection();
        while (!rc.onTheMap(rc.getLocation().add(nextdir))) {
          nextdir = Game.randomDirection();
        }

        System.out.println(toBuild);
        System.out.println(influence);
        System.out.println(nextdir);
      }

      RobotInfo[] robots = rc.senseNearbyRobots(1, rc.getTeam());

      for (RobotInfo robot : robots) {
        ids[whatid] = robot.getID();
        whatid += 1;
      }

      int oflag;
      for (int id : ids) {
        if (rc.canGetFlag(id)) {
          oflag = rc.getFlag(id);
          if (oflag >> 22 == 2) {
            flag = oflag;
            break;
          }
        }
      }

      doBid();

      if (rc.canSetFlag(flag) && rc.getFlag(rc.getID()) != flag) {
        System.out.println(String.valueOf(flag) + " - " + String.valueOf(flag - (1<<22)));
        rc.setFlag(flag);
      }
    }

    static int guard = 1;
    static int guardturns = 0;
    static MapLocation target = null;
    static void runPolitician() throws GameActionException {
      //System.out.println("guard: " + String.valueOf(guard));
      RobotInfo[] robots;
      Direction dir;
      MapLocation loc;
      if (guard == 1) {
        robots = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam());
        boolean ecfound = false;

        for (RobotInfo robot : robots) {
          if (rc.getType() == RobotType.ENLIGHTENMENT_CENTER) {
            ecfound = true;
          }
        }
        if (!ecfound) {
          guard = 0;
          System.out.println("GUARD RELEASE: EC NOT FOND");
        }
      }

      int ecflag = -1;
      if (enlightenmentCenter != null && rc.canGetFlag(enlightenmentCenter.getID())) {
        ecflag = rc.getFlag(enlightenmentCenter.getID());
      }
      if (ecflag >> 22 == 1) {
        ecflag -= 1 << 22;
        dir = Game.fromDir(ecflag);
        System.out.println(dir);
        loc = rc.getLocation().subtract(dir);
        if (rc.canSenseLocation(loc)) {
          robots = rc.senseNearbyRobots(loc, 0, rc.getTeam());
          if (robots.length > 0 && robots[0].type == RobotType.ENLIGHTENMENT_CENTER) {
            guard = 0;
            System.out.println("GUARD RELEASE: EC RELEASE");
          }
        }
      }
      else if (ecflag >> 22 == 2) {
        System.out.println("SUPERBROADCAST RECEIVED");
        ecflag = ecflag & 14;
        target = Game.fromCoords(rc.getLocation(), ecflag);
        Game.tryMove(rc, rc.getLocation().directionTo(target));
        rc.setIndicatorLine(rc.getLocation(), target, 0, 255, 0);
        rc.setIndicatorDot(rc.getLocation(), 255, 0, 150);
      }

      int flag = 0;

      Team me = rc.getTeam();
      Team enemy = me.opponent();
      int sensorRadius = rc.getType().sensorRadiusSquared;
      int actionRadius = rc.getType().actionRadiusSquared;
      outer :{
        if (rc.canEmpower(actionRadius)) {
            for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER || Game.random() < 0.4 ||
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

      if (guard == 1) {
        guardturns += 1;
        return;
      }
      else if (turnCount < 40 + guardturns) {
        if (Game.random() < 0.7) {
          Game.tryMove(rc, Game.directions[(rc.getID() + rc.getRoundNum()/6) % 8]);
        }
        else if (Game.random() < 0.3) {
          Game.tryMove(rc, Game.randomDirection());
        }
      }

      robots = rc.senseNearbyRobots(sensorRadius, enemy);

      Arrays.sort(robots,
          Comparator.comparingInt( o->rc.getLocation().distanceSquaredTo(o.getLocation()) )
          );

      for (RobotInfo robot : robots) {
        Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
        rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
        rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 255, 0, 0);
        flag = 3;
        if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
          flag = 2 << 22 + Game.toCoords(robot.getLocation());
          System.out.println("SSUUPPEERR BROADCAST!!!");
        }
        target = robot.getLocation();
      }

      robots = rc.senseNearbyRobots(sensorRadius, Team.NEUTRAL);

      Arrays.sort(robots,
          Comparator.comparingInt( o->rc.getLocation().distanceSquaredTo(o.getLocation()) )
          );

      for (RobotInfo robot : robots) {
        Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
        rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
        rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 255, 0, 0);
        flag = 2 << 22 + Game.toCoords(robot.getLocation());
        System.out.println("SSUUPPEERR BROADCAST!!!");
        target = robot.getLocation();
      }

      if ((rc.getRoundNum() / 10 + rc.getID())% 2 == 0) {
        robots = rc.senseNearbyRobots(sensorRadius, me);
        Arrays.sort(robots,
            Comparator.comparingInt( o->rc.getLocation().distanceSquaredTo(o.getLocation()) )
            );

        for (RobotInfo robot : robots ) {
          Game.tryMove(rc, robot.getLocation().directionTo(rc.getLocation()));
        }
      }

      Game.tryMove(rc, rc.getLocation().directionTo(target));

      robots = rc.senseNearbyRobots(sensorRadius, me);
      Arrays.sort(robots,
          Comparator.comparingInt( o->rc.getLocation().distanceSquaredTo(o.getLocation()) )
          );

      int oflag;
      for (RobotInfo robot : robots) {
        if (rc.canGetFlag(robot.ID) && rc.getFlag(robot.ID) != 0 && rc.getID() != robot.ID) {
          oflag = rc.getFlag(robot.ID);
          if (oflag != 0 && oflag >> 22 == 0) {
            oflag = oflag & 7;
            flag = Math.max(flag, rc.getFlag(robot.ID) - 1);
            System.out.println("CARRY" + String.valueOf(flag));
            Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
            rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 0, 255, 0);
            rc.setIndicatorDot(rc.getLocation(), 255, 0, 150);
          }
          else if (oflag >> 22 == 2) {
            oflag = oflag & 14;
            target = Game.fromCoords(rc.getLocation(), oflag);
            Game.tryMove(rc, rc.getLocation().directionTo(target));
            rc.setIndicatorLine(rc.getLocation(), target, 0, 255, 0);
            rc.setIndicatorDot(rc.getLocation(), 255, 0, 150);
          }
        }
      }
      Game.tryMove(rc, Game.randomDirection());
    }

    static void runSlanderer() throws GameActionException {
      Team enemy = rc.getTeam().opponent();
      Team ally = rc.getTeam();
      int sensorRadius = rc.getType().sensorRadiusSquared;
      for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemy)) {
          if (robot.type == RobotType.MUCKRAKER) {
              // AAAAARRRGGHHH! it's a muckraker!
              if (Game.tryMove(rc, robot.getLocation().directionTo(rc.getLocation()))) return;
          }
      }


      int flag = 0;

      outer: {
        for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemy)) {
          if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
            flag = 2 << 22 + Game.toCoords(robot.getLocation());
            System.out.println("SSUUPPEERR BROADCAST!!!");
            break outer;
          }
          else {
            flag = 3;
            break outer;
          }
        }
        int oflag;
        for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, ally)) {
            if (rc.canGetFlag(robot.ID) && rc.getFlag(robot.ID) != 0 && rc.getID() != robot.ID) {
              oflag = rc.getFlag(robot.ID);
              if (oflag != 0 && oflag >> 22 == 0) {
                oflag = oflag & 7;
                flag = Math.max(flag, rc.getFlag(robot.ID) - 1);
                System.out.println("CARRY" + String.valueOf(flag));
                rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 0, 255, 0);
                rc.setIndicatorDot(rc.getLocation(), 255, 0, 150);
              }
              break outer;
            }
        }
      }

      if (Game.random()<0.1 || turnCount < 10)  {
        Game.tryMove(rc, Game.randomDirection());
      }


     if (rc.canSetFlag(flag) && rc.getFlag(rc.getID()) != flag) {rc.setFlag(flag);}
    }

    static void runMuckraker() throws GameActionException {
      //System.out.println("guard: " + String.valueOf(guard));
      RobotInfo[] robots;
      Direction dir;
      MapLocation loc;

      int ecflag = -1;
      if (rc.canGetFlag(enlightenmentCenter.ID)) {
          ecflag = rc.getFlag(enlightenmentCenter.ID);
      }
      if (ecflag >> 22 == 1) {
        ecflag -= 1 << 22;
        dir = Game.fromDir(ecflag);
        loc = rc.getLocation().subtract(dir);
        if (rc.canSenseLocation(loc)) {
          robots = rc.senseNearbyRobots(loc, 0, rc.getTeam());
          if (robots.length > 0 && robots[0].type == RobotType.ENLIGHTENMENT_CENTER) {
            guard = 0;
            System.out.println("GUARD RELEASE: EC RELEASE");
          }
        }
      }
      else if (ecflag >> 22 == 2) {
        System.out.println("SUPERBROADCAST RECEIVED");
        ecflag = ecflag & 14;
        target = Game.fromCoords(rc.getLocation(), ecflag);
        Game.tryMove(rc, rc.getLocation().directionTo(target));
        rc.setIndicatorLine(rc.getLocation(), target, 0, 255, 0);
        rc.setIndicatorDot(rc.getLocation(), 255, 0, 150);
      }

      int flag = 0;

      Team me = rc.getTeam();
      Team enemy = me.opponent();
      int sensorRadius = rc.getType().sensorRadiusSquared;
      int actionRadius = rc.getType().actionRadiusSquared;
      for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
          if (robot.type.canBeExposed() && rc.canExpose(robot.location)) {
              rc.expose(robot.location);
              break;
          }
      }

      if (guard == 1) {
        return;
      }
      else if (turnCount < 40) {
        if (Game.random() < 0.7) {
          Game.tryMove(rc, Game.directions[(rc.getID() + rc.getRoundNum()/6) % 8]);
        }
        else if (Game.random() < 0.3) {
          Game.tryMove(rc, Game.randomDirection());
        }
      }

      robots = rc.senseNearbyRobots(sensorRadius, enemy);
      Arrays.sort(robots,
          Comparator.comparingInt( o->rc.getLocation().distanceSquaredTo(o.getLocation()) )
          );

      for (RobotInfo robot : robots) {
        Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
        rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
        rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 255, 0, 0);
        flag = 3;
        if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
          flag = 2 << 22 + Game.toCoords(robot.getLocation());
          System.out.println("SSUUPPEERR BROADCAST!!!");
        }
        target = robot.getLocation();
      }

      if ((rc.getRoundNum() / 75 + rc.getID())% 2 == 0) {
        robots = rc.senseNearbyRobots(sensorRadius, me);
        Arrays.sort(robots,
            Comparator.comparingInt( o->rc.getLocation().distanceSquaredTo(o.getLocation()) )
            );

        for (RobotInfo robot : robots ) {
          Game.tryMove(rc, robot.getLocation().directionTo(rc.getLocation()));
        }
      }

      Game.tryMove(rc, rc.getLocation().directionTo(target));

      robots = rc.senseNearbyRobots(sensorRadius, me);
      Arrays.sort(robots,
          Comparator.comparingInt( o->rc.getLocation().distanceSquaredTo(o.getLocation()) )
          );

      int oflag;
      for (RobotInfo robot : robots) {
        if (rc.canGetFlag(robot.ID) && rc.getFlag(robot.ID) != 0 && rc.getID() != robot.ID) {
          oflag = rc.getFlag(robot.ID);
          if (oflag != 0 && oflag >> 22 == 0) {
            oflag = oflag & 7;
            flag = Math.max(flag, rc.getFlag(robot.ID) - 1);
            System.out.println("CARRY" + String.valueOf(flag));
            Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
            rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 0, 255, 0);
            rc.setIndicatorDot(rc.getLocation(), 255, 0, 150);
          }
          else if (oflag >> 22 == 2) {
            oflag = oflag & 14;
            target = Game.fromCoords(rc.getLocation(), oflag);
            Game.tryMove(rc, rc.getLocation().directionTo(target));
            rc.setIndicatorLine(rc.getLocation(), target, 0, 255, 0);
            rc.setIndicatorDot(rc.getLocation(), 255, 0, 150);
          }
        }
      }
      Game.tryMove(rc, Game.randomDirection());
    }
}
