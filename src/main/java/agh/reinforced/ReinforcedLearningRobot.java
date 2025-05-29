package agh.reinforced;

import agh.reinforced.actions.*;
import robocode.*;
import java.awt.Color;
import java.io.*;
import java.util.*;

import static robocode.Rules.MAX_BULLET_POWER;
import static robocode.Rules.MIN_BULLET_POWER;

public class ReinforcedLearningRobot extends AdvancedRobot {

    private static final String KNOWLEDGE_FILE = "q.ser";

    private static Map<Observation, Map<RobotAction, Double>> Q = new HashMap<>();
    private static boolean qInitialized = false;

    private final double alpha = 0.2;
    private final double discountFactor = 0.2;
    private final double minExperimentRate = 0.1;
    private double experimentRate = 0.5;

    private final Set<EventRewardWrapper> events = new HashSet<>();
    private final Set<ScannedRobotEvent> scannedRobotEvents = new HashSet<>();
    private final Random random = new Random();
    private ScannedRobotEvent last;
    boolean enemyVisible = false;

    public void run() {
        if (!qInitialized) {
            loadKnowledge();
            qInitialized = true;
        }
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
            if (experimentRate > minExperimentRate) {
                experimentRate -= 0.0001;
            }
        }
    }

    private void loadKnowledge() {
        File file = getDataFile(KNOWLEDGE_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
                Q = (Map<Observation, Map<RobotAction, Double>>) ois.readObject();
            } catch (Exception e) {
                Q = new HashMap<>();
            }
        }
    }

    private void saveKnowledge() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(
                new RobocodeFileOutputStream(getDataFile(KNOWLEDGE_FILE))))) {
            oos.writeObject(Q);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private double performAction(RobotAction action) {
        action.invoke(this);
        double totalReward = 0;
        for (EventRewardWrapper event : events) {
            totalReward += event.reward;
        }
        if (enemyVisible) {
            totalReward -= Math.abs(normalizeBearing(getHeading() - getGunHeading() + last.getBearing()) / 180) * 20;
            totalReward += 20;
        }
        if (last != null) {
            totalReward += Math.max(0, 1 - Math.abs(last.getDistance() - 75) / 40) * 20;
        }
        events.clear();
        out.println("Executed: " + action.getClass().getSimpleName() + " | Reward: " + totalReward);
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
            out.println("closest one " + minDistance);
        } else {
            out.println("not random");
        }

        if (random.nextDouble() < experimentRate || rewards == null || rewards.isEmpty()) {
            out.println("random");
            if (Math.abs(obs.getOriginalRelativeGunHeading()) < 2) {
                return new Fire(random.nextDouble(MIN_BULLET_POWER, MAX_BULLET_POWER));
            }
            return switch (random.nextInt(6)) {
                case 0 -> new GoAhead(random.nextInt(50));
                case 1 -> new TurnGunLeft(random.nextInt(45));
                case 2 -> new TurnGunRight(random.nextInt(45));
                case 3 -> new TurnLeft(random.nextInt(45));
                case 4 -> new TurnRight(random.nextInt(45));
                case 5 -> new GoBack(random.nextInt(50));
                default -> null;
            };
        }

        return Collections.max(rewards.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    private Observation observe() {
        ScannedRobotEvent closest = null;
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
        while (angle >  180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    public void onHitByBullet(HitByBulletEvent e) {
        events.add(new EventRewardWrapper(e));
    }

    public void onBulletHit(BulletHitEvent e) {
        events.add(new EventRewardWrapper(e));
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        scannedRobotEvents.add(e);
    }

    public void onBattleEnded(BattleEndedEvent e) {
        saveKnowledge();
    }
}