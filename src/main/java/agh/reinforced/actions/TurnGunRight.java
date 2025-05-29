package agh.reinforced.actions;

import agh.reinforced.RobotAction;
import robocode.Robot;

public class TurnGunRight implements RobotAction {
    private final double value;
    public TurnGunRight(double value) {
        this.value = value;
    }
    @Override
    public void invoke(Robot robot) { robot.turnGunRight(value); }
}
