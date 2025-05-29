package agh.reinforced.actions;

import agh.reinforced.RobotAction;
import robocode.Robot;

public class TurnLeft implements RobotAction {
    private final double value;
    public TurnLeft(double value) {
        this.value = value;
    }
    @Override
    public void invoke(Robot robot) { robot.turnLeft(value); }
}
