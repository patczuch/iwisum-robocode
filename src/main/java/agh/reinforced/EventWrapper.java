package agh.reinforced;

import robocode.*;

public class EventWrapper {

    public final Event event;
    public final double reward;

    public EventWrapper(HitByBulletEvent e) {
        this.event = e;
        this.reward = e.getPower();
    }

    public EventWrapper(BulletHitEvent e) {
        this.event = e;
        this.reward = e.getBullet().getPower();
    }

    public EventWrapper(WinEvent e) {
        this.event = e;
        this.reward = 200;
    }

    public EventWrapper(DeathEvent e) {
        this.event = e;
        this.reward = -200;
    }
}