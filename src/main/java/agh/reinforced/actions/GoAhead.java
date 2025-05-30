package agh.reinforced.actions;

import agh.reinforced.RobotAction;
import robocode.Robot;

import java.io.Serial;

public class GoAhead implements RobotAction {
    @Serial
    private static final long serialVersionUID = 1L;
    private final double value;
    public GoAhead(double value) {
        this.value = value;
    }
    @Override
    public void invoke(Robot robot) { robot.ahead(value); }
}
