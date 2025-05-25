package agh.reinforced.actions;

import agh.reinforced.ParametrizedAction;
import robocode.Robot;

public class Fire extends ParametrizedAction {
    public Fire(double power) { super(power); }
    @Override
    public void invoke(Robot robot) { robot.fire(value); }
}