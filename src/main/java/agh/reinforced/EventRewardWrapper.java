package agh.reinforced;

import robocode.*;

public class EventRewardWrapper {

    public final Event event;
    public final double reward;

    public EventRewardWrapper(HitByBulletEvent e) {
        this.event = e;
        this.reward = -e.getPower() * 300;
    }

    public EventRewardWrapper(BulletHitEvent e) {
        this.event = e;
        this.reward = e.getBullet().getPower() * 500;
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