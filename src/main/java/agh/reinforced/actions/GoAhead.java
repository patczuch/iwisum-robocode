package agh.reinforced.actions;

import agh.reinforced.ParametrizedAction;
import robocode.Robot;

public class GoAhead extends ParametrizedAction {
    public GoAhead(double dist) { super(dist); }
    @Override
    public void invoke(Robot robot) { robot.ahead(value); }
}
