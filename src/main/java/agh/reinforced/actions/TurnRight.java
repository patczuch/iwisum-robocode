package agh.reinforced.actions;

import agh.reinforced.RobotAction;
import robocode.Robot;

public class TurnRight implements RobotAction {
    private final double value;
    public TurnRight(double value) {
        this.value = value;
    }
    @Override
    public void invoke(Robot robot) { robot.turnRight(value); }
}
