package nodes.sensor;

import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;

import java.io.Serializable;
import java.time.Instant;

public class Sensor implements SensorDataI {
    private final String nodeIdentifier;
    private final String sensorIdentifier;
    private final Class<? extends Serializable> type;
    private final Serializable value;
    private final Instant timestamp;

    public Sensor(String nodeIdentifier, String sensorIdentifier,
                      Class<? extends Serializable> type, Serializable value) {
        this.nodeIdentifier = nodeIdentifier;
        this.sensorIdentifier = sensorIdentifier;
        this.type = type;
        this.value = value;
        this.timestamp = Instant.now(); // Capture the current instant as the timestamp
    }

    @Override
    public String getNodeIdentifier() {
        return nodeIdentifier;
    }

    @Override
    public String getSensorIdentifier() {
        return sensorIdentifier;
    }

    @Override
    public Class<? extends Serializable> getType() {
        return type;
    }

    @Override
    public Serializable getValue() {
        return value;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }
}