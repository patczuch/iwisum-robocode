package agh.reinforced;

import robocode.*;

public class EventRewardWrapper {

    public final Event event;
    public final double reward;

    public EventRewardWrapper(HitByBulletEvent e, int multiplier) {
        this.event = e;
        this.reward = -e.getPower() * multiplier;
    }

    public EventRewardWrapper(BulletHitEvent e, int multiplier) {
        this.event = e;
        this.reward = e.getBullet().getPower() * multiplier;
    }

    public EventRewardWrapper(WinEvent e) {
        this.event = e;
        this.reward = 2000;
    }

    public EventRewardWrapper(DeathEvent e) {
        this.event = e;
        this.reward = -2000;
    }
}