package agh.reinforced.actions;

import agh.reinforced.ParametrizedAction;
import robocode.Robot;

public class GoBack extends ParametrizedAction {
    public GoBack(double dist) { super(dist); }
    @Override
    public void invoke(Robot robot) { robot.back(value); }
}
