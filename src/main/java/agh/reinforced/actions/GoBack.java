package agh.reinforced.actions;

import agh.reinforced.RobotAction;
import robocode.Robot;

public class GoBack implements RobotAction {
    private final double value;
    public GoBack(double value) {
        this.value = value;
    }
    @Override
    public void invoke(Robot robot) { robot.back(value); }
}
