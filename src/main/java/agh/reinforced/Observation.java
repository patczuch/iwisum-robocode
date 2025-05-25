package agh.reinforced;

import robocode.Robot;
import java.io.Serializable;
import java.util.Objects;

public class Observation implements Serializable {

    private static final int BUCKETS = 10;
    private static final int MIN_ENERGY = 0;
    private static final int MAX_ENERGY = 100;
    private static final int MIN_ANGLE = 0;
    private static final int MAX_ANGLE = 360;

    private final double posX;
    private final double posY;
    private final double energy;
    private final double heading;
    private final double gunHeading;
    private final Boolean zeroBearing;

    private transient final HashCalculator hashCalculator;

    public Observation(Robot robot, double posX, double posY, double energy, double heading, double gunHeading, Boolean zeroBearing) {
        this.posX = posX;
        this.posY = posY;
        this.energy = energy;
        this.heading = heading;
        this.gunHeading = gunHeading;
        this.zeroBearing = zeroBearing;
        this.hashCalculator = new HashCalculator(robot);
    }

    @Override
    public int hashCode() {
        return hashCalculator.hash(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Observation other)) return false;
        return Double.compare(posX, other.posX) == 0
                && Double.compare(posY, other.posY) == 0
                && Double.compare(energy, other.energy) == 0
                && Double.compare(heading, other.heading) == 0
                && Double.compare(gunHeading, other.gunHeading) == 0
                && Objects.equals(zeroBearing, other.zeroBearing);
    }

    private static class HashCalculator implements Serializable {
        private final int minX = 0;
        private final int maxX;
        private final int minY = 0;
        private final int maxY;
        private final int buckets = BUCKETS;

        public HashCalculator(Robot robot) {
            maxX = (int) robot.getBattleFieldWidth();
            maxY = (int) robot.getBattleFieldHeight();
        }

        public int hash(Observation obs) {
            if (obs.zeroBearing != null) {
                return Objects.hash(
                        bucket(minX, maxX, obs.posX),
                        bucket(minY, maxY, obs.posY),
                        bucket(MIN_ENERGY, MAX_ENERGY, obs.energy),
                        bucket(MIN_ANGLE, MAX_ANGLE, obs.heading),
                        bucket(MIN_ANGLE, MAX_ANGLE, obs.gunHeading),
                        obs.zeroBearing
                );
            }
            return Objects.hash(
                    bucket(minX, maxX, obs.posX),
                    bucket(minY, maxY, obs.posY),
                    bucket(MIN_ENERGY, MAX_ENERGY, obs.energy),
                    bucket(MIN_ANGLE, MAX_ANGLE, obs.heading),
                    bucket(MIN_ANGLE, MAX_ANGLE, obs.gunHeading)
            );
        }

        private int bucket(int min, int max, double value) {
            int span = max - min;
            int bucketSpan = span / buckets;
            int offset = (int) (value - min);
            return offset / bucketSpan;
        }
    }
}