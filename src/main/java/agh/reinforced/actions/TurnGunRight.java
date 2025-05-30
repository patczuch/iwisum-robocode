package agh.reinforced.actions;

import agh.reinforced.RobotAction;
import robocode.Robot;

import java.io.Serial;

public class TurnGunRight implements RobotAction {
    @Serial
    private static final long serialVersionUID = 1L;
    private final double value;
    public TurnGunRight(double value) {
        this.value = value;
    }
    @Override
    public void invoke(Robot robot) { robot.turnGunRight(value); }
}
