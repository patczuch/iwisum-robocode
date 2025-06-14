package agh.reinforced;

import agh.reinforced.actions.*;
import robocode.*;

import java.awt.*;
import java.io.IOException;
import java.util.*;

public class ReinforcedLearningRobotTuning2 extends AdvancedRobot {

    private static Map<Observation, Map<RobotAction, Double>> Q = new HashMap<>();
    private double alpha = 0.2;
    private double discountFactor = 0.2;
    private double experimentRate = 0.5;
    private static boolean qInitialized = false;
    private final Set<EventRewardWrapper> events = new HashSet<>();
    private final Set<ScannedRobotEvent> scannedRobotEvents = new HashSet<>();
    private final Random random = new Random();
    private ScannedRobotEvent last;
    boolean enemyVisible = false;
    double enemyDistance = 0;
    double gunHeadingDifference = 0;
    double timesSeenEnemy = 0;
    double damageDealt = 0;
    double damageReceived = 0;
    int totalObservations = 0;

    static int game = 0;
    static int prevComboIndex = -1;

    int gamesEach = 250;

    private final double[][] parameters = {
            {0.5, 0.1, 0.5},
            {0.5, 0.1, 0.3},
            {0.5, 0.1, 0.1},
            {0.5, 0.3, 0.5},
            {0.5, 0.5, 0.1},
            {0.5, 0.7, 0.3},
            {0.5, 0.7, 0.5},
            {0.4, 0.3, 0.3},
            {0.4, 0.1, 0.5},
            {0.4, 0.1, 0.1}
    };

    static int parameter = 0;

    private final int[] bulletHitRewards = {250, 500, 750};
    private final int[] hitByBulletPenalties = {-300, -600};
    private final int[] gunAlignmentMultipliers = {50, 100, 150};

    int bulletHitReward = 0;
    int hitByBulletPenalty = 0;
    int gunAlignmentMultiplier = 0;

    @Override
    public void run() {
        int totalCombos = bulletHitRewards.length * hitByBulletPenalties.length * gunAlignmentMultipliers.length;
        int comboIndex = game / gamesEach;

        if (prevComboIndex != comboIndex) {
            Q = new HashMap<>();
        }

        if (comboIndex >= totalCombos) {
            parameter++;
            game = 0;
            comboIndex = 0;
        }
        prevComboIndex = comboIndex;

        if (parameter >= parameters.length) {
            System.out.println("All done");
            return;
        }

        alpha = parameters[parameter][0];
        discountFactor = parameters[parameter][1];
        experimentRate = parameters[parameter][2];

        bulletHitReward = bulletHitRewards[comboIndex / (hitByBulletPenalties.length * gunAlignmentMultipliers.length)];
        hitByBulletPenalty = hitByBulletPenalties[(comboIndex / gunAlignmentMultipliers.length) % hitByBulletPenalties.length];
        gunAlignmentMultiplier = gunAlignmentMultipliers[comboIndex % gunAlignmentMultipliers.length];

        System.out.println(parameter + ", " + (comboIndex+1) + "/" + totalCombos);
        System.out.println(bulletHitReward + " " + hitByBulletPenalty + " " + gunAlignmentMultiplier + " " + ((game % gamesEach) + 1));

        game++;

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setTurnRadarRight(Double.POSITIVE_INFINITY);
        setColors(Color.red, Color.blue, Color.green);

        Observation currentObservation = observe();
        while (true) {
            RobotAction action = chooseAction(currentObservation);
            double reward = performAction(action);
            Observation newObservation = observe();
            learn(action, currentObservation, newObservation, reward);
            currentObservation = newObservation;
        }
    }

    private double performAction(RobotAction action) {
        action.invoke(this);
        double totalReward = 0;
        for (EventRewardWrapper event : events) {
            totalReward += event.reward;
            if (event.event instanceof WinEvent) {
                totalReward -= totalObservations / 100.0;
            }
        }

//        if (action.getClass().equals(Fire.class)) {
//            totalReward += 100;
//        }

        if (action.getClass().equals(TurnGunRight.class) || action.getClass().equals(TurnGunLeft.class)) {
            if (enemyVisible) {
                totalReward += 50;
            } else {
                totalReward -= 20;
            }
        }
        if (last != null) {
            if (action.getClass().equals(TurnGunRight.class) || action.getClass().equals(TurnGunLeft.class)) {
                totalReward += (1 - Math.abs(normalizeBearing(getHeading() - getGunHeading() + last.getBearing()) / 180)) * gunAlignmentMultiplier;
            }
        }
        events.clear();
        //out.println("Executed: " + action.getClass().getSimpleName() + " | Reward: " + totalReward);
        return totalReward;
    }

    private void learn(RobotAction action, Observation oldObs, Observation newObs, double reward) {
        Map<RobotAction, Double> rewards = Q.computeIfAbsent(oldObs, k -> new HashMap<>());
        rewards.put(action, (1 - alpha) * rewards.getOrDefault(action, 0.0) +
                alpha * (reward + discountFactor * maxReward(newObs)));
    }

    private double maxReward(Observation obs) {
        Map<RobotAction, Double> rewards = Q.get(obs);
        if (rewards == null || rewards.isEmpty()) {
            return 0;
        }
        return Collections.max(rewards.values());
    }

    private RobotAction chooseAction(Observation obs) {

        Map<RobotAction, Double> rewards = Q.get(obs);

        if (rewards == null || rewards.isEmpty()) {
            Observation closest = null;
            double minDistance = Double.MAX_VALUE;

            for (Observation o : Q.keySet()) {
                if (Q.get(o) == null || Q.get(o).isEmpty()) continue;
                double distance = obs.distanceTo(o);
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = o;
                }
            }

            if (closest != null) {
                rewards = Q.get(closest);
            }
            //out.println("closest one " + minDistance);
        } else {
            //out.println("not random");
        }

        if (obs.getZeroBearing() && random.nextDouble() < 0.5) {
            return new Fire(1 + random.nextInt(4) * 0.5);
        }

        if (random.nextDouble() < experimentRate || rewards == null || rewards.isEmpty()) {
            //out.println("random");
            return switch (random.nextInt(7)) {
                case 0 -> new GoAhead(random.nextInt(10) * 5);
                case 1 -> new TurnGunLeft(random.nextInt(9) * 5);
                case 2 -> new TurnGunRight(random.nextInt(9) * 5);
                case 3 -> new TurnLeft(random.nextInt(9) * 5);
                case 4 -> new TurnRight(random.nextInt(9) * 5);
                case 5 -> new GoBack(random.nextInt(10) * 5);
                case 6 -> new Fire(1 + random.nextInt(4) * 0.5);
                default -> null;
            };
        }

        return Collections.max(rewards.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    private Observation observe() {
        ScannedRobotEvent closest = null;
        totalObservations++;
        for (ScannedRobotEvent e : scannedRobotEvents) {
            if (closest == null || e.getDistance() < closest.getDistance()) {
                closest = e;
            }
        }
        scannedRobotEvents.clear();
        enemyVisible = true;
        boolean enemyEnergyLoss = false;
        if (closest == null) {
            closest = last;
            enemyVisible = false;
        } else if (last != null) {
            enemyEnergyLoss = closest.getEnergy() - last.getEnergy() < 0;
        }
        last = closest;
        if (enemyVisible) {
            timesSeenEnemy++;
            gunHeadingDifference += Math.abs(normalizeBearing(getHeading() - getGunHeading() + closest.getBearing()));
            enemyDistance += closest.getDistance();
        }
        return new Observation(
                closest == null ? 10000 : closest.getDistance() / Math.max(getBattleFieldHeight(), getBattleFieldWidth()),
                getEnergy(),
                getHeading(),
                closest == null ? 0 : normalizeBearing(getHeading() - getGunHeading() + closest.getBearing()),
                closest == null ? 0 : normalizeBearing(closest.getBearing()),
                enemyEnergyLoss
        );
    }

    double normalizeBearing(double angle) {
        while (angle >  180) {
            angle -= 360;
        }
        while (angle < -180) {
            angle += 360;
        }
        return angle;
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        damageReceived += e.getPower();
        events.add(new EventRewardWrapper(e, hitByBulletPenalty));
    }

    @Override
    public void onBulletHit(BulletHitEvent e) {
        damageDealt += e.getBullet().getPower();
        events.add(new EventRewardWrapper(e, bulletHitReward));
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        //System.out.println("Seen robot");
        scannedRobotEvents.add(e);
    }

    private void saveStats(boolean win) {

        if (parameter >= parameters.length) {
            return;
        }

        enemyDistance /= timesSeenEnemy;
        gunHeadingDifference /= timesSeenEnemy;
        timesSeenEnemy /= totalObservations;
        try (RobocodeFileWriter writer = new RobocodeFileWriter(getDataFile(
                "res_" + alpha + "_" + discountFactor + "_" + experimentRate + "_" +
                         bulletHitReward + "_" + hitByBulletPenalty + "_" + gunAlignmentMultiplier + ".csv"
        ).getAbsolutePath(), true)) {
            writer.write(enemyDistance + ";" + gunHeadingDifference + ";" + timesSeenEnemy + ";" +
                    damageDealt + ";" + damageReceived + ";" + totalObservations + ";" + (win ? "1" : "0") + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        enemyDistance = 0;
        gunHeadingDifference = 0;
        timesSeenEnemy = 0;
        damageDealt = 0;
        damageReceived = 0;
        totalObservations = 0;
    }

    @Override
    public void onDeath(DeathEvent e) {
        events.add(new EventRewardWrapper(e));
        saveStats(false);
    }

    @Override
    public void onWin(WinEvent e) {
        events.add(new EventRewardWrapper(e));
        saveStats(true);
    }
}