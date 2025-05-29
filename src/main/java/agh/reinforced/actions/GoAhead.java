package agh.reinforced.actions;

import agh.reinforced.RobotAction;
import robocode.Robot;

public class GoAhead implements RobotAction {
    private final double value;
    public GoAhead(double value) {
        this.value = value;
    }
    @Override
    public void invoke(Robot robot) { robot.ahead(value); }
}
