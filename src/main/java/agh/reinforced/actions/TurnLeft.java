package agh.reinforced.actions;

import agh.reinforced.ParametrizedAction;
import robocode.Robot;

public class TurnLeft extends ParametrizedAction {
    public TurnLeft(double angle) { super(angle); }
    @Override
    public void invoke(Robot robot) { robot.turnLeft(value); }
}
