package agh.reinforced;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class Observation implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final double enemyDistance;
    private final double energy;
    private final double heading;
    private final double relativeGunHeading;
    private final double enemyBearing;
    private final boolean enemyEnergyLoss;
    private final boolean zeroBearing;

    public Observation(double enemyDistance, double energy, double heading, double relativeGunHeading, double enemyBearing,
                       boolean enemyEnergyLoss) {
        this.enemyDistance = bucket(0, 1, 5, enemyDistance);
        this.energy = bucket(0, 100, 3, energy);
        this.heading = bucket(0, 360, 4, heading);
        this.relativeGunHeading = bucket(-180, 180, 8, relativeGunHeading);
        this.enemyBearing = bucket(-180, 180, 8, enemyBearing);
        this.enemyEnergyLoss = enemyEnergyLoss;
        this.zeroBearing = Math.abs(relativeGunHeading) < 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Observation that = (Observation) o;
        return Double.compare(that.enemyDistance, enemyDistance) == 0 &&
                Double.compare(that.energy, energy) == 0 &&
                Double.compare(that.heading, heading) == 0 &&
                Double.compare(that.relativeGunHeading, relativeGunHeading) == 0 &&
                Double.compare(that.enemyBearing, enemyBearing) == 0 &&
                that.enemyEnergyLoss == enemyEnergyLoss &&
                that.zeroBearing == zeroBearing;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enemyDistance, energy, heading, relativeGunHeading, enemyBearing, enemyEnergyLoss, zeroBearing);
    }

    public double distanceTo(Observation other) {
        double d1 = (this.enemyDistance - other.enemyDistance) / 5;
        double d2 = (this.energy - other.energy) / 3;
        double d3 = (this.heading - other.heading) / 4;
        double d4 = (this.relativeGunHeading - other.relativeGunHeading) / 8;
        double d5 = (this.enemyBearing - other.enemyBearing) / 8;
        double enemyEnergyLoss = other.enemyEnergyLoss == this.enemyEnergyLoss ? 0 : 1;
        return Math.sqrt(d1 * d1 + d2 * d2 + d3 * d3 + d4 * d4 + d5 * d5 + enemyEnergyLoss);
    }

    private int bucket(int min, int max, int buckets, double value) {
        return (int) ((value - min) * buckets / (max - min));
    }

    public boolean getZeroBearing() {
        return zeroBearing;
    }
}
