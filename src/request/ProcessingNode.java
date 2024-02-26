package request;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;
import nodes.sensor.Sensor;

public class ProcessingNode implements ProcessingNodeI {
    private String nodeId;
    private PositionI position;
    private Set<NodeInfoI> neighbours;
    private HashMap<String, Sensor> sensorsData;

    public ProcessingNode(String nodeId, PositionI position) {
        this.nodeId = nodeId;
        this.position = position;
        this.neighbours = new HashSet<>();
        this.sensorsData = new HashMap<>();
    }

    @Override
    public String getNodeIdentifier() {
        return this.nodeId;
    }

    @Override
    public PositionI getPosition() {
        return this.position;
    }

    @Override
    public Set<NodeInfoI> getNeighbours() {
        return this.neighbours;
    }

    @Override
    public SensorDataI getSensorData(String sensorIdentifier) {
        return this.sensorsData.get(sensorIdentifier);
    }

    
    public void addNeighbour(NodeInfoI neighbour) {
        this.neighbours.add(neighbour);
    }

    public void addSensorData(String sensorIdentifier, Sensor SensorData) {
        this.sensorsData.put(sensorIdentifier, SensorData);
    }
}