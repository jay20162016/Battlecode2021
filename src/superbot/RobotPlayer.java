package superbot;
import battlecode.common.*;
import superbot.Game;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Set;
import java.util.LinkedHashSet;
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
            // if (rc.getRoundNum() > 500) {
            //   rc.resign();
            // }
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
                //// System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }
    /*
    ######   ######      ######  ## ######       ## ##
    ##   ## ##    ##     ##   ## ## ##   ##     ##   ##
    ##   ## ##    ##     ######  ## ##   ##     ##   ##
    ##   ## ##    ##     ##   ## ## ##   ##     ##   ##
    ######   ######      ######  ## ######       ## ##
    */

    static int bidinfl = 1;
    static int prevwon = 0;
    static void doBid() throws GameActionException {
      if (rc.getRoundNum() < 200) {
        rc.bid(1);
        return;
      }
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
        bidinfl = bidinfl - 2;
        bidinfl = Math.max(1, bidinfl);
        bidinfl = Math.min(bidinfl, rc.getInfluence() / 7);
      }

      if (rc.canBid(bidinfl)) {
        rc.bid(bidinfl);
      }
      prevwon = won;
    }

    /*
    ######  ##    ## ###    ##     #######  ######      ## ##
    ##   ## ##    ## ####   ##     ##      ##          ##   ##
    ######  ##    ## ## ##  ##     #####   ##          ##   ##
    ##   ## ##    ## ##  ## ##     ##      ##          ##   ##
    ##   ##  ######  ##   ####     #######  ######      ## ##
    */

    static int flag;

    static Set<Integer> ids = new LinkedHashSet<Integer>();
    static int[] idsarr = new int[1250];
    static int idsptr = 0;
    static Direction nextdir = Game.randomDirection();
    static int influence = 130;
    static RobotType toBuild = RobotType.SLANDERER;
    static void runEnlightenmentCenter() throws GameActionException {
      doBid();
      flag = 0;

      // if ((influence < 0 || influence > rc.getInfluence()) && turnCount > 2) {
      //   System.out.println("ErROR: ERRoR: eRROR: invluencez");
      //   System.out.println(influence);
      //   System.out.println(toBuild);
      //   System.out.println(rc.getInfluence());
      //   rc.resign();
      // }

      outer : {
        if (rc.getRoundNum() == 1) {
          toBuild = RobotType.SLANDERER;
          influence = 130;
        }
        else if (rc.getRoundNum() < 15 && rc.getRoundNum() > 10) {
          toBuild = RobotType.SLANDERER;
          influence = 42;
        }
        else if (rc.getRoundNum() < 22) {
          toBuild = RobotType.POLITICIAN;
          influence = 1;
          break outer;
        }
        else if (Math.abs(rc.getInfluence()-200) < 70) {
          toBuild = RobotType.SLANDERER;
          influence = (int)Math.floor((rc.getInfluence() - 10)/20)*20+1;
        }
        else if (Math.abs(rc.getInfluence()-500) < 200) {
          toBuild = RobotType.SLANDERER;
          influence = Math.min(rc.getInfluence() - 10, 949);
        }
        else if (rc.getInfluence() > 949 && Game.random() < 0.3) {
          toBuild = RobotType.SLANDERER;
          influence = 949;
        }
        else if (rc.getInfluence() < 60) {
          toBuild = RobotType.SLANDERER;
          influence = (int)Math.floor((rc.getInfluence() - 10)/20)*20+1;
        }
        else if (Game.random() < 0.5 || rc.getInfluence() < 25) {
          toBuild = RobotType.MUCKRAKER;
          influence = (int) (Game.random() * rc.getInfluence() / 3);
          if (influence < 1) {
            influence = 1;
          }
          break outer;
        }
        else {
          toBuild = RobotType.POLITICIAN;
          influence = (int) (Game.random() * Game.random() * rc.getInfluence() - 10);
          if (Game.random() < 0.2) {
            if (rc.getInfluence() < 200 || Game.random() < 0.4) {
              influence = 21;
            }
            else {
              influence = 41;
            }
          }
          break outer;
        }

        if (toBuild == RobotType.SLANDERER) {
          if (rc.getInfluence() < influence) {
            influence = rc.getInfluence() - 10;
          }
          else if (influence < 21) {
            influence = 21;
          }
          else if (rc.getInfluence() < influence) {
            toBuild = RobotType.MUCKRAKER;
            influence = 1;
          }
        }
      }

      nextdir = Game.randomDirection();
      while (!rc.onTheMap(rc.getLocation().add(nextdir))) {
        nextdir = Game.randomDirection();
      }
      while (rc.canSenseLocation(rc.getLocation().add(nextdir)) && rc.isLocationOccupied(rc.getLocation().add(nextdir))
              && Game.random() < 0.9) {
        nextdir = Game.randomDirection();
      }


      if (influence >= rc.getInfluence()) {
        influence = rc.getInfluence() - 20;
      }
      if (influence <= 0) {
        toBuild = RobotType.MUCKRAKER;
        influence = 1;
      }
      if (influence > 2000) {
        influence = (int) (Game.random() * Game.random() * 1200);
      }

      // System.out.println(toBuild);
      // System.out.println(influence);
      // System.out.println(nextdir);

      Team me = rc.getTeam();
      Team enemy = me.opponent();
      int sensorRadius = rc.getType().sensorRadiusSquared;
      int actionRadius = rc.getType().actionRadiusSquared;

      RobotInfo[] robots = rc.senseNearbyRobots(sensorRadius, me);

      for (RobotInfo robot : robots) {
        int id = robot.getID();
        if (robot.type != RobotType.ENLIGHTENMENT_CENTER) {
          if (ids.add(id)) {
            // idsarr.add(robot.getID());
            idsarr[idsptr++] = id;
            // idsptr += 1;
          }
        }
      }

      int oflag;

      for (int i = 0; i < idsptr; i++){
        int id = idsarr[i];
        if (id != 0 && rc.canGetFlag(id)) {
          oflag = rc.getFlag(id);
          if ((oflag >> 22) == 2 || (oflag >> 22) == 3) {
            if (oflag >> 22 == 2) {
              if (influence > 40) {
                toBuild = RobotType.POLITICIAN;
                influence = rc.getInfluence() - 20;
              }
            }
            flag = oflag;
            // System.out.println("BOOMBROADCASTING");
            // System.out.println("TYPE: " + ((oflag >> 22) >> 1));
            break;
          }
        }
      }

      for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
          flag = (2 << 22) + Game.toCoords(robot.location);
          if (robot.type == RobotType.MUCKRAKER) {
            toBuild = RobotType.POLITICIAN;
            influence = rc.getInfluence() - 20;
          }
          else if (robot.type == RobotType.SLANDERER) {
            toBuild = RobotType.MUCKRAKER;
            influence = 1;
            flag = (3 << 22) + Game.toCoords(robot.location);
          }
          else if (rc.getInfluence() > robot.influence) {
            toBuild = RobotType.POLITICIAN;
            influence = rc.getInfluence() - 20;
          }
          else {
            toBuild = RobotType.MUCKRAKER;
            influence = 1;
          }
          if (influence >= rc.getInfluence()) {
            influence = rc.getInfluence() - 20;
          }
          if (influence <= 0) {
            toBuild = RobotType.MUCKRAKER;
            influence = 1;
          }
          break;
      }

      Game.tryBuild(rc, toBuild, nextdir, influence);

      if (rc.canSetFlag(flag)) {
        rc.setFlag(flag);
      }
    }

    /*
    ######  ##    ## ###    ##     ######   ######  ##           ## ##
    ##   ## ##    ## ####   ##     ##   ## ##    ## ##          ##   ##
    ######  ##    ## ## ##  ##     ######  ##    ## ##          ##   ##
    ##   ## ##    ## ##  ## ##     ##      ##    ## ##          ##   ##
    ##   ##  ######  ##   ####     ##       ######  #######      ## ##
    */

    static int scout = 0;
    static Direction sdir = Game.randomDirection();

    static MapLocation target = null;
    static void runPolitician() throws GameActionException {
      // if (rc.getRoundNum() > 1493) {
      //   if (rc.canEmpower(rc.getType().actionRadiusSquared)) {rc.empower(rc.getType().actionRadiusSquared);}
      // }
      RobotInfo[] robots;
      Direction dir;
      MapLocation loc;

      flag = 0;

      if (rc.getRoundNum() < 25) {
        scout = 1;
      }

      int ecflag = -1;
      if (enlightenmentCenter != null && rc.canGetFlag(enlightenmentCenter.getID())) {
        ecflag = rc.getFlag(enlightenmentCenter.getID());
      }
      MapLocation place;
      if ((ecflag >> 22) == 2 && scout == 0) {
        place = Game.fromCoords(rc.getLocation(), ecflag);
        if (rc.getLocation().distanceSquaredTo(place) > 7) {
          target = place;
        }
        Game.tryMove(rc, rc.getLocation().directionTo(target));
        // rc.setIndicatorLine(rc.getLocation(), target, 0, 255, 0);
        // rc.setIndicatorDot(rc.getLocation(), 255, 0, 150);
      }

      Team me = rc.getTeam();
      Team enemy = me.opponent();
      int sensorRadius = rc.getType().sensorRadiusSquared;
      int actionRadius = rc.getType().actionRadiusSquared;
      outer :{
        if (rc.canEmpower(actionRadius) && scout == 0) {
            for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER || // yahoo! we blow up a EC!
                    Game.random() < 0.4 || // just blow up
                    rc.getLocation().distanceSquaredTo(robot.location) < 4 || // we're close to the enemy
                    rc.getRoundNum() > 1450 || // we're close to the end
                    (rc.getEmpowerFactor(me, 0) > 1.2 && rc.getEmpowerFactor(me, 0) * rc.getConviction() > 30) // buff
                    ) {
                    if (!(robot.type == RobotType.MUCKRAKER && (rc.getInfluence() - 10 > robot.getInfluence() * 20))) {
                      rc.empower(rc.getLocation().distanceSquaredTo(robot.location));
                      break outer;
                    }
                }
            }
            for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, Team.NEUTRAL)) {
              if (scout == 0) rc.empower(rc.getLocation().distanceSquaredTo(robot.location));
              break outer;
            }
        }
        for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemy)) {
          if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
              break outer;
          }
        }
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, Team.NEUTRAL)) {
          if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
              break outer;
          }
        }
        if (scout == 1) {
          if (!Game.tryMoveAbs(rc, sdir) && (rc.getCooldownTurns() < 1 || Game.random() < 0.3)) {
            sdir = Game.randomDirection();
          }
          flag = 1;
        }
      }

      if (rc.getCooldownTurns() < 1 && (rc.getRoundNum() / 10 + rc.getID())% 2 == 0 && scout == 0) {
        robots = rc.senseNearbyRobots(sensorRadius, me);
        Arrays.sort(robots,
            Comparator.comparingInt( o->rc.getLocation().distanceSquaredTo(o.getLocation()) )
            );

        for (RobotInfo robot : robots ) {
            if (robot.type == RobotType.ENLIGHTENMENT_CENTER && Math.random() < 0.1) {
                  RobotPlayer.enlightenmentCenter = robot;
            }
          if (Game.tryMove(rc, robot.getLocation().directionTo(rc.getLocation()))) break;
        }
      }

      robots = rc.senseNearbyRobots(sensorRadius, enemy);

      Arrays.sort(robots,
          Comparator.comparingInt( o->rc.getLocation().distanceSquaredTo(o.getLocation()) )
          );

      for (RobotInfo robot : robots) {
        if (scout == 0) Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
        // rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
        // rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 255, 0, 0);
        flag = 3;
        if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
          flag = (2 << 22) + Game.toCoords(robot.getLocation());
        }
        else if (robot.type == RobotType.SLANDERER) {
          flag = (3 << 22) + Game.toCoords(robot.getLocation());
        }
        target = robot.getLocation();
      }

      robots = rc.senseNearbyRobots(sensorRadius, Team.NEUTRAL);

      Arrays.sort(robots,
          Comparator.comparingInt( o->rc.getLocation().distanceSquaredTo(o.getLocation()) )
          );

      for (RobotInfo robot : robots) {
        if (scout == 0) Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
        // rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
        // rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 255, 0, 0);
        flag = (2 << 22) + Game.toCoords(robot.getLocation());
        target = robot.getLocation();
      }

      if (scout == 0) Game.tryMove(rc, rc.getLocation().directionTo(target));

      robots = rc.senseNearbyRobots(sensorRadius, me);
      Arrays.sort(robots,
          Comparator.comparingInt( o->rc.getLocation().distanceSquaredTo(o.getLocation()) )
          );

      int oflag;

      for (RobotInfo robot : robots) {
        if (robot.type == RobotType.ENLIGHTENMENT_CENTER && Math.random() < 0.1) {
              RobotPlayer.enlightenmentCenter = robot;
        }
        if (robot.type == RobotType.ENLIGHTENMENT_CENTER && rc.canEmpower(actionRadius) &&
          rc.getEmpowerFactor(me, 0) > 1.2 && rc.getEmpowerFactor(me, 0) * rc.getConviction() > 30 &&
          rc.getLocation().distanceSquaredTo(robot.location) <= actionRadius) {
            if (scout == 0) rc.empower(rc.getLocation().distanceSquaredTo(robot.location));
        }
        if (rc.canGetFlag(robot.ID) && rc.getID() != robot.ID && scout == 0) {
          oflag = rc.getFlag(robot.ID);
          if (oflag != 0 && (oflag >> 22) == 0) {
            oflag = oflag & 7;
            flag = Math.max(flag, oflag - 1);
            Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
            break;
            // rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 0, 255, 0);
            // rc.setIndicatorDot(rc.getLocation(), 255, 0, 150);
          }
        }
        if (Clock.getBytecodesLeft() < 1000) {
          break;
        }
      }
      if (scout == 0) Game.tryMove(rc, Game.randomDirection());


     if (rc.canSetFlag(flag)) {rc.setFlag(flag);}
    }

    /*
    ######  ##    ## ###    ##     ######   #####  ###    ## ##   ## ####### ######       ## ##
    ##   ## ##    ## ####   ##     ##   ## ##   ## ####   ## ##  ##  ##      ##   ##     ##   ##
    ######  ##    ## ## ##  ##     ######  ####### ## ##  ## #####   #####   ######      ##   ##
    ##   ## ##    ## ##  ## ##     ##   ## ##   ## ##  ## ## ##  ##  ##      ##   ##     ##   ##
    ##   ##  ######  ##   ####     ######  ##   ## ##   #### ##   ## ####### ##   ##      ## ##
    */


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

      Direction dir = Game.randomDirection();
      MapLocation loc;
      if (Game.random()<0.1 || turnCount < 10)  {
        for (int i = 0; i < 20; i++) {
          dir = Game.randomDirection();
          loc = rc.getLocation().add(dir);
          if ((loc.x % 2 == 0) ^ (loc.y % 2 == 0)) {
            if (Game.tryMoveAbs(rc, dir)) break;
          }
        }
      }

      flag = 0;

      outer: {
        for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, enemy)) {
          if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
            flag = (2 << 22) + Game.toCoords(robot.getLocation());
            break outer;
          }
          else if (robot.type == RobotType.SLANDERER) {
            flag = (3 << 22) + Game.toCoords(robot.getLocation());
            break outer;
          }
          else {
            flag = 3;
            break outer;
          }
        }
        int oflag;
        for (RobotInfo robot : rc.senseNearbyRobots(sensorRadius, ally)) {
          if (rc.canGetFlag(robot.ID) && rc.getID() != robot.ID) {
            oflag = rc.getFlag(robot.ID);
            if (oflag != 0 && (oflag >> 22) == 0) {
              oflag = oflag & 7;
              flag = Math.max(flag, oflag - 1);
              // System.out.println("CARRY" + String.valueOf(flag));
              // rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 0, 255, 0);
              // rc.setIndicatorDot(rc.getLocation(), 255, 0, 150);
            }
            break outer;
          }
          if (Clock.getBytecodesLeft() < 1000) {
            break;
          }
        }
      }


     if (rc.canSetFlag(flag)) {rc.setFlag(flag);}
    }

    /*
    ######  ##    ## ###    ##      #####  ######   ###### ##   ## ####### ######       ## ##
    ##   ## ##    ## ####   ##     ##   ## ##   ## ##      ##   ## ##      ##   ##     ##   ##
    ######  ##    ## ## ##  ##     ####### ######  ##      ####### #####   ######      ##   ##
    ##   ## ##    ## ##  ## ##     ##   ## ##   ## ##      ##   ## ##      ##   ##     ##   ##
    ##   ##  ######  ##   ####     ##   ## ##   ##  ###### ##   ## ####### ##   ##      ## ##
    */

    static void runMuckraker() throws GameActionException {
      RobotInfo[] robots;
      Direction dir;
      MapLocation loc;

      int ecflag = -1;
      if (rc.canGetFlag(enlightenmentCenter.ID)) {
          ecflag = rc.getFlag(enlightenmentCenter.ID);
      }

      flag = 0;

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

      if ((ecflag >> 22) == 3) {
        target = Game.fromCoords(rc.getLocation(), ecflag);
        Game.tryMove(rc, rc.getLocation().directionTo(target));
        // rc.setIndicatorLine(rc.getLocation(), target, 0, 255, 0);
        // rc.setIndicatorDot(rc.getLocation(), 255, 0, 150);
      }

      if (rc.getCooldownTurns() < 1 && (rc.getRoundNum() / 75 + rc.getID())% 2 == 0) {
        robots = rc.senseNearbyRobots(sensorRadius, me);
        Arrays.sort(robots,
            Comparator.comparingInt( o->rc.getLocation().distanceSquaredTo(o.getLocation()) )
            );

        for (RobotInfo robot : robots ) {
          if (robot.type == RobotType.ENLIGHTENMENT_CENTER && Math.random() < 0.1) {
                RobotPlayer.enlightenmentCenter = robot;
          }
          Game.tryMove(rc, robot.getLocation().directionTo(rc.getLocation()));
        }
      }

      robots = rc.senseNearbyRobots(sensorRadius, Team.NEUTRAL);

      Arrays.sort(robots,
          Comparator.comparingInt( o->rc.getLocation().distanceSquaredTo(o.getLocation()) )
          );

      for (RobotInfo robot : robots) {
        // Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
        // rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
        // rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 255, 0, 0);
        flag = (2 << 22) + Game.toCoords(robot.getLocation());
        // System.out.println("SSUUPPEERR BROADCAST!!!");
        target = robot.getLocation();
      }

      robots = rc.senseNearbyRobots(sensorRadius, enemy);
      Arrays.sort(robots,
          Comparator.comparingInt( o->rc.getLocation().distanceSquaredTo(o.getLocation()) )
          );

      for (RobotInfo robot : robots) {
        // rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
        // rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 255, 0, 0);
        flag = 3;
        if (robot.type == RobotType.ENLIGHTENMENT_CENTER) {
          flag = (2 << 22) + Game.toCoords(robot.getLocation());
        }
        else if (robot.type == RobotType.SLANDERER) {
          flag = (3 << 22) + Game.toCoords(robot.getLocation());
          target = robot.getLocation();
          Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
        }
        else {
          target = robot.getLocation();
          Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
        }

      }

      Game.tryMove(rc, rc.getLocation().directionTo(target));

      robots = rc.senseNearbyRobots(sensorRadius, me);
      Arrays.sort(robots,
          Comparator.comparingInt( o->rc.getLocation().distanceSquaredTo(o.getLocation()) )
          );

      int oflag;
      for (RobotInfo robot : robots) {
        if (robot.type == RobotType.ENLIGHTENMENT_CENTER && Math.random() < 0.1) {
              RobotPlayer.enlightenmentCenter = robot;
        }
        if (rc.canGetFlag(robot.ID) && rc.getID() != robot.ID) {
          oflag = rc.getFlag(robot.ID);
          if (oflag != 0 && (oflag >> 22) == 0) {
            oflag = oflag & 7;
            flag = Math.max(flag, oflag - 1);
            // System.out.println("CARRY" + String.valueOf(flag));
            Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
            break;
            // rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 0, 255, 0);
            // rc.setIndicatorDot(rc.getLocation(), 255, 0, 150);
          }
        }
        if (Clock.getBytecodesLeft() < 1000) {
          break;
        }
      }
      Game.tryMove(rc, Game.randomDirection());


     if (rc.canSetFlag(flag)) {rc.setFlag(flag);}
    }
}
