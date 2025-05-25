package agh.reinforced.actions;

import agh.reinforced.ParametrizedAction;
import robocode.Robot;

public class TurnGunRight extends ParametrizedAction {
    public TurnGunRight(double angle) { super(angle); }
    @Override
    public void invoke(Robot robot) { robot.turnGunRight(value); }
}
