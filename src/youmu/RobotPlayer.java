package youmu;
import battlecode.common.*;
import youmu.TernaryTree;
import java.util.Arrays;

public strictfp class RobotPlayer {
    static RobotController rc;
    static final RobotType[] spawnableRobot = {RobotType.POLITICIAN, RobotType.SLANDERER, RobotType.MUCKRAKER};
    static final Direction[] directions = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST};
    static int turnCount;
    @SuppressWarnings("unused")

    public static void run(RobotController rc) throws GameActionException {
        RobotPlayer.rc = rc;
        turnCount = 0;
        while (true) {
            turnCount += 1;
            try {
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER: runEnlightenmentCenter(); break;
                    case POLITICIAN:           runPolitician();          break;
                    case SLANDERER:            runSlanderer();           break;
                    case MUCKRAKER:            runMuckraker();           break;
                }
                Clock.yield();
            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void runEnlightenmentCenter() throws GameActionException {
        RobotType toBuild = randomSpawnableRobotType();
        int influence = 50;
        for (Direction dir : directions) {
            if (rc.canBuildRobot(toBuild, dir, influence)) {
                rc.buildRobot(toBuild, dir, influence);
            } else {
                break;
            }
        }
        System.out.println("Round " + rc.getRoundNum());
        if (rc.canBid(1))
            rc.bid(1);
    }

    static void runPolitician() throws GameActionException {
        int sensorRadius = rc.getType().sensorRadiusSquared;
        RobotInfo[] entities = rc.senseNearbyRobots(sensorRadius);
        for (RobotInfo entity : entities) {
            if (entity.type == RobotType.ENLIGHTENMENT_CENTER && entity.team != rc.getTeam()) {
                int actionRadius = rc.getLocation().distanceSquaredTo(entity.location);
                RobotInfo[] attackable = rc.senseNearbyRobots(actionRadius);
                if ((rc.getConviction() - 10) / attackable.length > entity.conviction && rc.canEmpower(actionRadius)) {
                    rc.empower(actionRadius);
                    return;
                } else {
                    Direction[] orders = pathFinding(entity.location);
                    for (Direction i : orders) {
                        if (tryMove(i))
                            return;
                    } 
                }
            }
        }
        tryMove(randomDirection());
    }

    static void runSlanderer() throws GameActionException {
        tryMove(randomDirection());
    }

    static void runMuckraker() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {
                if (rc.canExpose(robot.location)) {
                    rc.expose(robot.location);
                    return;
                }
            }
        }
        tryMove(randomDirection());
    }

    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    static RobotType randomSpawnableRobotType() {
        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
    }

    static boolean tryMove(Direction dir) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

    static Direction[] directionInterval(Direction dir) {
        Direction[] dirs;
        if (dir == directions[0]) {
            dirs = {directions[7], directions[0], directions[1]};
        } else if (dir == directions[1]) {
            dirs = {directions[0], directions[1], directions[2]};
        } else if (dir == directions[2]) {
            dirs = {directions[1], directions[2], directions[3]};
        } else if (dir == directions[3]) {
            dirs = {directions[2], directions[3], directions[4]};
        } else if (dir == directions[4]) {
            dirs = {directions[3], directions[4], directions[5]};
        } else if (dir == directions[5]) {
            dirs = {directions[4], directions[5], directions[6]};
        } else if (dir == directions[6]) {
            dirs = {directions[5], directions[6], directions[7]};
        } else {
            dirs = {directions[6], directions[7], directions[0]};
        }
        return dirs;
    }

    static Direction[] order(float[] values, Direction[] targets) {
        Direction[] orders = {null, null, null};
        float[] svalues = Arrays.sort(values);
        for (int i=0; i<3; i++) {
            for (int j=0; j<3; j++) {
                if (svalues[i] == values[j])
                    order[i] = targets[j];
            }
        }
        return orders;
    }

    static Direction[] pathFinding(MapLocation loc) {
        Direction[] targets = directionInterval(rc.getLocation().directionTo(loc));
        int predis = rc.getLocation().distanceSquaredTo(loc);
        TernaryTree tree = new TernaryTree(rc, rc.getLocation());
        tree.create(rc, targets);
        float[] values = tree.count(rc, predis, loc);
        return order(values, targets);
    }
}
