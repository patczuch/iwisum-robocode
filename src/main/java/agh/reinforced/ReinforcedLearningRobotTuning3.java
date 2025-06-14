package agh.reinforced;

import agh.reinforced.actions.*;
import robocode.*;

import java.awt.*;
import java.io.*;
import java.util.*;

public class ReinforcedLearningRobotTuning3 extends AdvancedRobot {

    private static final String KNOWLEDGE_FILE = "q.ser";
    private static final String RESULTS_FILE = "results.csv";

    private static Map<Observation, Map<RobotAction, Double>> Q = new HashMap<>();
    private double alpha = 0.2;
    private double discountFactor = 0.2;
    private double minExperimentRate = 0.2;
    private double experimentRate = 0.5;
    private double startExperimentRate;
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
    int gamesEach = 10000;
    static int parameter = 0;
    static int stats = 0;

    private final double[][] parameters = {
            {0.5, 0.1, 0.3, 750, 600, 50},
            {0.5, 0.1, 0.3, 250, 600, 100},
            {0.5, 0.1, 0.5, 250, 300, 100},
            {0.5, 0.1, 0.5, 750, 300, 50},
            {0.4, 0.1, 0.1, 750, 300, 100}
    };

    int bulletHitReward = 0;
    int hitByBulletPenalty = 0;
    int gunAlignmentMultiplier = 0;

    @Override
    public void run() {
        if (!qInitialized) {
            Q = new HashMap<>();
            qInitialized = true;
        }
        if (game >= gamesEach) {
            game = 0;
            parameter++;
            stats = 0;
            Q = new HashMap<>();
        }
        game++;
        if (parameter >= parameters.length) {
            System.out.println("All done");
            return;
        }
        alpha = parameters[parameter][0];
        discountFactor = parameters[parameter][1];
        experimentRate = parameters[parameter][2] - stats * 0.00005;
        if (experimentRate < 0.05)
            experimentRate = 0.05;
        bulletHitReward = (int)parameters[parameter][3];
        hitByBulletPenalty = (int)parameters[parameter][4];
        gunAlignmentMultiplier = (int)parameters[parameter][5];
        startExperimentRate = experimentRate;

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
        stats++;
        System.out.println(experimentRate);

        if (parameter >= parameters.length) {
            return;
        }

        enemyDistance /= timesSeenEnemy;
        gunHeadingDifference /= timesSeenEnemy;
        timesSeenEnemy /= totalObservations;
        try (RobocodeFileWriter writer = new RobocodeFileWriter(getDataFile(
                "res_" + alpha + "_" + discountFactor + "_" + parameters[parameter][2] + "_" +
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