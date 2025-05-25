package agh.reinforced;

import agh.reinforced.actions.GoAhead;
import robocode.*;
import java.awt.Color;
import java.io.*;
import java.util.*;

public class ReinforcedLearningRobot extends AdvancedRobot {

    private static final String KNOWLEDGE_FILE = "q.ser";

    private static Map<Observation, Map<RobotAction, Double>> Q = new HashMap<>();
    private static boolean qInitialized = false;

    private final double alpha = 0.1;
    private final double discountFactor = 0.1;
    private final double experimentRate = 0.1;

    private final Set<EventWrapper> events = new HashSet<>();
    private final Set<ScannedRobotEvent> scannedRobotEvents = new HashSet<>();
    private final Random random = new Random();

    private final RobotRandomActionFactory actionFactory = new RobotRandomActionFactory();

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
        }
    }

    private void loadKnowledge() {
        File file = getDataFile(KNOWLEDGE_FILE);
        System.out.println(file);
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
        for (EventWrapper event : events) {
            totalReward += event.reward;
        }
        events.clear();
        out.println("Executed: " + action.getClass().getSimpleName() + " | Reward: " + totalReward);
        return totalReward;
    }

    private void learn(RobotAction action, Observation oldObs, Observation newObs, double reward) {
        Map<RobotAction, Double> rewards = Q.computeIfAbsent(oldObs, k -> new HashMap<>());
        double oldValue = rewards.getOrDefault(action, 0.0);
        double updatedValue = (1 - alpha) * oldValue + alpha * (reward + discountFactor * maxReward(newObs));
        rewards.put(action, updatedValue);
    }

    private double maxReward(Observation obs) {
        Map<RobotAction, Double> rewards = Q.get(obs);
        if (rewards == null || rewards.isEmpty()) {
            return 0;
        }
        return Collections.max(rewards.values());
    }

    private RobotAction chooseAction(Observation obs) {
        if (random.nextDouble() < experimentRate) {
            return actionFactory.randomAction();
        }
        Map<RobotAction, Double> rewards = Q.get(obs);
        if (rewards != null && !rewards.isEmpty()) {
            return Collections.max(rewards.entrySet(), Map.Entry.comparingByValue()).getKey();
        }
        return new GoAhead(20);
    }

    private Observation observe() {
        ScannedRobotEvent closest = null;
        for (ScannedRobotEvent e : scannedRobotEvents) {
            if (closest == null || e.getDistance() < closest.getDistance()) {
                closest = e;
            }
        }
        scannedRobotEvents.clear();
        Boolean zeroBearing = closest != null ? closest.getBearing() == 0 : null;
        return new Observation(this, getX(), getY(), getEnergy(), getHeading(), getGunHeading(), zeroBearing);
    }

    public void onHitByBullet(HitByBulletEvent e) {
        events.add(new EventWrapper(e));
    }

    public void onBulletHit(BulletHitEvent e) {
        events.add(new EventWrapper(e));
    }

    public void onDeath(DeathEvent e) {
        events.add(new EventWrapper(e));
    }

    public void onWin(WinEvent e) {
        events.add(new EventWrapper(e));
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        scannedRobotEvents.add(e);
    }

    public void onBattleEnded(BattleEndedEvent e) {
        saveKnowledge();
    }
}