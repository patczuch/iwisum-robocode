package agh.reinforced;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class Observation implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final int enemyDistance;
    private final int energy;
    private final int heading;
    private final int relativeGunHeading;
    private final int enemyBearing;
    private final boolean enemyEnergyLoss;
    private final boolean zeroBearing;
    private static final int DISTANCE_BUCKETS = 5;
    private static final int ENERGY_BUCKETS = 3;
    private static final int HEADING_BUCKETS = 6;
    private static final int GUN_HEADING_BUCKETS = 10;
    private static final int ENEMY_BEARING_BUCKETS = 6;

    public Observation(double enemyDistance, double energy, double heading, double relativeGunHeading, double enemyBearing,
                       boolean enemyEnergyLoss) {
        this.enemyDistance = bucket(0, 1, DISTANCE_BUCKETS, enemyDistance);
        this.energy = bucket(0, 100, ENERGY_BUCKETS, energy);
        this.heading = bucket(0, 360, HEADING_BUCKETS, heading);
        this.relativeGunHeading = bucket(-180, 180, GUN_HEADING_BUCKETS, relativeGunHeading);
        this.enemyBearing = bucket(-180, 180, ENEMY_BEARING_BUCKETS, enemyBearing);
        this.enemyEnergyLoss = enemyEnergyLoss;
        this.zeroBearing = Math.abs(relativeGunHeading) < 3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Observation that = (Observation) o;
        return enemyDistance == that.enemyDistance && energy == that.energy && heading == that.heading && relativeGunHeading == that.relativeGunHeading && enemyBearing == that.enemyBearing && enemyEnergyLoss == that.enemyEnergyLoss && zeroBearing == that.zeroBearing;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enemyDistance, energy, heading, relativeGunHeading, enemyBearing, enemyEnergyLoss, zeroBearing);
    }

    public double distanceTo(Observation other) {
        if (other.enemyEnergyLoss != enemyEnergyLoss || other.zeroBearing != zeroBearing) {
            return 1000;
        }
        double d1 = (this.enemyDistance - other.enemyDistance) / (double) DISTANCE_BUCKETS;
        double d2 = (this.energy - other.energy) / (double) ENERGY_BUCKETS;
        double d3 = (this.heading - other.heading) / (double) HEADING_BUCKETS;
        double d4 = (this.relativeGunHeading - other.relativeGunHeading) / (double) GUN_HEADING_BUCKETS;
        double d5 = (this.enemyBearing - other.enemyBearing) / (double) ENEMY_BEARING_BUCKETS;
        return Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3 + d4 * d4 + d5 * d5);
    }

    private int bucket(int min, int max, int buckets, double value) {
        return (int) ((value - min) * buckets / (max - min));
    }

    public boolean getZeroBearing() {
        return zeroBearing;
    }
}
