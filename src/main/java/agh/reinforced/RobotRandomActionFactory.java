package agh.reinforced;

import agh.reinforced.actions.*;

import java.util.Random;

public class RobotRandomActionFactory {

    private static final int MAX_DISTANCE = 400;
    private static final int MAX_ENERGY = 100;
    private static final int MAX_ANGLE = 360;

    private final Random random = new Random();

    public RobotAction randomAction() {
        return switch (random.nextInt(7)) {
            case 0 -> new GoAhead(randomDistance());
            case 1 -> new Fire(randomPower());
            case 2 -> new TurnGunLeft(randomAngle());
            case 3 -> new TurnGunRight(randomAngle());
            case 4 -> new TurnLeft(randomAngle());
            case 5 -> new TurnRight(randomAngle());
            case 6 -> new GoBack(randomDistance());
            default -> new GoAhead(20);
        };
    }

    private double randomPower() {
        return random.nextDouble() * MAX_ENERGY;
    }

    private double randomDistance() {
        return random.nextDouble() * MAX_DISTANCE;
    }

    private double randomAngle() {
        return random.nextInt(MAX_ANGLE + 1);
    }
}