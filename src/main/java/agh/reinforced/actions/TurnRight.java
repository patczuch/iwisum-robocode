package agh.reinforced.actions;

import agh.reinforced.ParametrizedAction;
import robocode.Robot;

public class TurnRight extends ParametrizedAction {
    public TurnRight(double angle) { super(angle); }
    @Override
    public void invoke(Robot robot) { robot.turnRight(value); }
}
