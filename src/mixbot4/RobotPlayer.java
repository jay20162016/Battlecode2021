package mixbot4;
import battlecode.common.*;
import mixbot4.Game;

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
        bidinfl = bidinfl - (int) Game.random() * 3;
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
    static ArrayList<Integer> idsarr = new ArrayList<Integer>();
    static Direction nextdir = Game.randomDirection();
    static int influence = 42;
    static RobotType toBuild = RobotType.SLANDERER;
    static void runEnlightenmentCenter() throws GameActionException {
      flag = 0;
      while (!rc.onTheMap(rc.getLocation().add(nextdir))) {
        nextdir = Game.randomDirection();
      }
      while (rc.canSenseLocation(rc.getLocation().add(nextdir)) && rc.isLocationOccupied(rc.getLocation().add(nextdir)) || Game.random() < 0.05) {
        nextdir = Game.randomDirection();
      }

      if (Game.tryBuild(rc, toBuild, nextdir, influence) || (turnCount < 2 && rc.getRoundNum() > 5)) {
        if (rc.getRoundNum() < 3) {
          toBuild = RobotType.SLANDERER;
          influence = 42;
        }
        else if (rc.getRoundNum() < 22) {
          toBuild = RobotType.POLITICIAN;
          influence = 1;
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
          influence = (int)Game.random() * rc.getInfluence() / 3;
          if (influence < 1) {
            influence = 1;
          }
        }
        else {
          toBuild = RobotType.POLITICIAN;
          influence = rc.getInfluence() - 10;
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

        nextdir = Game.randomDirection();
        while (!rc.onTheMap(rc.getLocation().add(nextdir))) {
          nextdir = Game.randomDirection();
        }
        while (rc.canSenseLocation(rc.getLocation().add(nextdir)) && rc.isLocationOccupied(rc.getLocation().add(nextdir)) || Game.random() < 0.05) {
          nextdir = Game.randomDirection();
        }


        if (influence >= rc.getInfluence()) {
          influence = rc.getInfluence() - 20;
        }
        if (influence <= 0) {
          toBuild = RobotType.MUCKRAKER;
          influence = 1;
        }
        if (influence > 10000) {
          influence = 2000;
        }
      }

      // System.out.println(toBuild);
      // System.out.println(influence);
      // System.out.println(nextdir);

      Team me = rc.getTeam();
      Team enemy = me.opponent();
      int sensorRadius = rc.getType().sensorRadiusSquared;
      int actionRadius = rc.getType().actionRadiusSquared;

      for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
          if (robot.type == RobotType.MUCKRAKER) {
            toBuild = RobotType.POLITICIAN;
            influence = rc.getInfluence() - 20;
          }
          else if (robot.type == RobotType.SLANDERER) {
            toBuild = RobotType.MUCKRAKER;
            influence = 1;
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
      }

      RobotInfo[] robots = rc.senseNearbyRobots(sensorRadius, me);

      for (RobotInfo robot : robots) {
        if (robot.type != RobotType.ENLIGHTENMENT_CENTER) {
          if (ids.add(robot.getID())) {
            idsarr.add(robot.getID());
          }
        }
      }

      int id;
      int oflag;

      for (int i = idsarr.size() - 1; i>=0; i--){
        id = idsarr.get(i);
        if (rc.canGetFlag(id)) {
          oflag = rc.getFlag(id);
          if ((oflag >> 22) == 2 || (oflag >> 22) == 3) {
            if (oflag >> 22 == 2) {
              if (influence > 40) {
                toBuild = RobotType.POLITICIAN;
                influence = rc.getInfluence() - 20;
              }
            }
            flag = oflag;
            System.out.println("BOOMBROADCASTING");
            System.out.println("TYPE: " + ((oflag >> 22) >> 1));
            break;
          }
        }
      }

      doBid();

      if (rc.canSetFlag(flag)) {
        rc.setFlag(flag);
      }

      rc.setIndicatorDot(rc.getLocation(), (flag >> 16) & 7, (flag >> 8) & 7, flag & 7);
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
        if (rc.canEmpower(actionRadius)) {
            for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
                if (robot.type == RobotType.ENLIGHTENMENT_CENTER || // yahoo! we blow up a EC!
                    Game.random() < 0.4 || // just blow up
                    rc.getLocation().distanceSquaredTo(robot.location) < 4 || // we're close to the enemy
                    rc.getRoundNum() > 1450 || // we're close to the end
                    (rc.getEmpowerFactor(me, 0) > 1.5 && rc.getEmpowerFactor(me, 0) * rc.getConviction() > 30)) {

                    if (scout==0) rc.empower(rc.getLocation().distanceSquaredTo(robot.location));
                    else {
                      flag = (2 << 22) + Game.toCoords(robot.getLocation());
                    }
                    break outer;
                }
            }
            for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, Team.NEUTRAL)) {
              if (scout==0) rc.empower(rc.getLocation().distanceSquaredTo(robot.location));
              else {
                flag = (2 << 22) + Game.toCoords(robot.getLocation());
              }
              break outer;
            }
        }
        if (scout == 1) {
          if (!Game.tryMoveAbs(rc, sdir) && rc.getCooldownTurns() < 1) {
            sdir = Game.randomDirection();
          }
          flag = 1;
        }
      }
      System.out.println("BYTECODE EMPOWERSCOUT" + String.valueOf(Clock.getBytecodeNum()));

      // if ((rc.getRoundNum() / 10 + rc.getID())% 2 == 0 && scout == 0 && rc.getCooldownTurns() < 1) {
      //   robots = rc.senseNearbyRobots(sensorRadius, me);
      //   Arrays.sort(robots,
      //       Comparator.comparingInt( o->rc.getLocation().distanceSquaredTo(o.getLocation()) )
      //       );
      //
      //   for (RobotInfo robot : robots ) {
      //     if (Game.tryMove(rc, robot.getLocation().directionTo(rc.getLocation()))) break;
      //   }
      // }
      System.out.println("BYTECODE BREATHE" + String.valueOf(Clock.getBytecodeNum()));

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
      System.out.println("BYTECODE BROADCAST" + String.valueOf(Clock.getBytecodeNum()));

      if (scout == 0) Game.tryMove(rc, rc.getLocation().directionTo(target));

      // robots = rc.senseNearbyRobots(sensorRadius, me);
      // Arrays.sort(robots,
      //     Comparator.comparingInt( o->rc.getLocation().distanceSquaredTo(o.getLocation()) )
      //     );
      //
      // int oflag;
      // if (rc.getCooldownTurns() < 1) {
      //   for (RobotInfo robot : robots) {
      //     if (robot.type == RobotType.ENLIGHTENMENT_CENTER &&
      //       rc.getEmpowerFactor(me, 0) > 1.5 && rc.getEmpowerFactor(me, 0) * rc.getConviction() > 30) {
      //         if (scout == 0) rc.empower(rc.getLocation().distanceSquaredTo(robot.location));
      //     }
      //     if (rc.canGetFlag(robot.ID) && rc.getID() != robot.ID && scout == 0) {
      //       oflag = rc.getFlag(robot.ID);
      //       if (oflag != 0 && (oflag >> 22) == 0) {
      //         oflag = oflag & 7;
      //         flag = Math.max(flag, oflag - 1);
      //         Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
      //         // rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 0, 255, 0);
      //         // rc.setIndicatorDot(rc.getLocation(), 255, 0, 150);
      //       }
      //       else if ((oflag >> 22) == 2) {
      //         target = Game.fromCoords(rc.getLocation(), oflag);
      //         if (Game.tryMove(rc, rc.getLocation().directionTo(target))) break;
      //         // rc.setIndicatorLine(rc.getLocation(), target, 0, 255, 0);
      //         // rc.setIndicatorDot(rc.getLocation(), 255, 0, 150);
      //       }
      //     }
      //   }
      // }
      System.out.println("BYTECODE MESSAGEPROP" + String.valueOf(Clock.getBytecodeNum()));
      if (scout == 0) Game.tryMove(rc, Game.randomDirection());


     if (rc.canSetFlag(flag)) {rc.setFlag(flag);}

     rc.setIndicatorDot(rc.getLocation(), 0, 0, 0);
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
        }
      }

      Direction dir = Game.randomDirection();
      MapLocation loc;
      if (Game.random()<0.1 || turnCount < 10)  {
        for (int i = 0; i < 100; i++) {
          dir = Game.randomDirection();
          loc = rc.getLocation().add(dir);
          if ((loc.x % 2 == 0) ^ (loc.y % 2 == 0)) {
            if (Game.tryMoveAbs(rc, dir)) break;
          }
        }
      }


     if (rc.canSetFlag(flag)) {rc.setFlag(flag);}

     rc.setIndicatorDot(rc.getLocation(), (flag >> 16) & 7, (flag >> 8) & 7, flag & 7);
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
      if ((ecflag >> 22) == 3) {
        target = Game.fromCoords(rc.getLocation(), ecflag);
        Game.tryMove(rc, rc.getLocation().directionTo(target));
        // rc.setIndicatorLine(rc.getLocation(), target, 0, 255, 0);
        // rc.setIndicatorDot(rc.getLocation(), 255, 0, 150);
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

      if ((rc.getRoundNum() / 75 + rc.getID())% 2 == 0) {
        robots = rc.senseNearbyRobots(sensorRadius, me);
        Arrays.sort(robots,
            Comparator.comparingInt( o->rc.getLocation().distanceSquaredTo(o.getLocation()) )
            );

        for (RobotInfo robot : robots ) {
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
        if (rc.canGetFlag(robot.ID) && rc.getID() != robot.ID) {
          oflag = rc.getFlag(robot.ID);
          if (oflag != 0 && (oflag >> 22) == 0) {
            oflag = oflag & 7;
            flag = Math.max(flag, oflag - 1);
            // System.out.println("CARRY" + String.valueOf(flag));
            Game.tryMove(rc, rc.getLocation().directionTo(robot.getLocation()));
            // rc.setIndicatorLine(rc.getLocation(), robot.getLocation(), 0, 255, 0);
            // rc.setIndicatorDot(rc.getLocation(), 255, 0, 150);
          }
          else if ((oflag >> 22) == 3) {
            target = Game.fromCoords(rc.getLocation(), oflag);
            Game.tryMove(rc, rc.getLocation().directionTo(target));
            // rc.setIndicatorLine(rc.getLocation(), target, 0, 255, 0);
            // rc.setIndicatorDot(rc.getLocation(), 255, 0, 150);
          }
        }
      }
      Game.tryMove(rc, Game.randomDirection());


     if (rc.canSetFlag(flag)) {rc.setFlag(flag);}

     rc.setIndicatorDot(rc.getLocation(), (flag >> 16) & 7, (flag >> 8) & 7, flag & 7);
    }
}
