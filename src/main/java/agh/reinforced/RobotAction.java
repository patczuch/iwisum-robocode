package agh.reinforced;

import robocode.Robot;
import java.io.Serializable;

public interface RobotAction extends Serializable {
    void invoke(Robot robot);
}
