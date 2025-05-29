package agh.reinforced.actions;

import agh.reinforced.RobotAction;
import robocode.Robot;

public class TurnGunLeft implements RobotAction {
    private final double value;
    public TurnGunLeft(double value) {
        this.value = value;
    }
    @Override
    public void invoke(Robot robot) { robot.turnGunLeft(value); }
}