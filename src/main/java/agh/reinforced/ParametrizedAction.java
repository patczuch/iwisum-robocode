package agh.reinforced;

import java.util.Objects;

public abstract class ParametrizedAction implements RobotAction {

    protected final double value;

    protected ParametrizedAction(double value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParametrizedAction that = (ParametrizedAction) o;
        return Double.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, getClass().getName());
    }
}
