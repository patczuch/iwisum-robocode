package agh.reinforced.actions;

import agh.reinforced.RobotAction;
import robocode.Robot;

public class Fire implements RobotAction {
    private final double value;
    public Fire(double value) {
        this.value = value;
    }
    @Override
    public void invoke(Robot robot) { robot.fire(value); }
}