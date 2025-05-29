package agh.reinforced;

import robocode.*;

public class EventRewardWrapper {

    public final Event event;
    public final double reward;

    public EventRewardWrapper(HitByBulletEvent e) {
        this.event = e;
        this.reward = -e.getPower() * 100;
    }

    public EventRewardWrapper(BulletHitEvent e) {
        this.event = e;
        this.reward = e.getBullet().getPower() * 100;
    }
}