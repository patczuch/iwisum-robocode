package agh.reinforced.actions;

import agh.reinforced.ParametrizedAction;
import robocode.Robot;

public class TurnGunLeft extends ParametrizedAction {
    public TurnGunLeft(double angle) { super(angle); }
    @Override
    public void invoke(Robot robot) { robot.turnGunLeft(value); }
}