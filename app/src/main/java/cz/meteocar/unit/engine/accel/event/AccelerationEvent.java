package cz.meteocar.unit.engine.accel.event;

import cz.meteocar.unit.engine.event.AppEvent;
import cz.meteocar.unit.engine.event.EventType;

/**
 * Event that carries information about acceleration in time.
 */
public class AccelerationEvent extends AppEvent {

    private double x;
    private double y;
    private double z;

    /**
     * @param x acceleration on x axis
     * @param y acceleration on y axis
     * @param z acceleration on z axis
     */
    public AccelerationEvent(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * @return acceleration on x axis
     */
    public double getX() {
        return x;
    }

    /**
     * @return acceleration on y axis
     */
    public double getY() {
        return y;
    }

    /**
     * @return acceleration on Z axis
     */
    public double getZ() {
        return z;
    }


    @Override
    public EventType getType() {
        return EventType.EVENT_ACCEL;
    }
}
