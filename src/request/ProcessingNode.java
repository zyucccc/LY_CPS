package request;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;
import nodes.sensor.Sensor;
import sensor_network.Position;

public class ProcessingNode implements ProcessingNodeI {
    private String nodeId;
    private Position position;
    private Set<NodeInfoI> neighbours;
    private HashMap<String, Sensor> sensorsData;

    public ProcessingNode(String nodeId, PositionI position) {
        this.nodeId = nodeId;
        this.position = (Position)position;
        this.neighbours = new HashSet<>();
        this.sensorsData = new HashMap<>();
    }
    public ProcessingNode(String nodeId, PositionI position,HashMap<String, Sensor> sensorsData) {
        this.nodeId = nodeId;
        this.position = (Position)position;
        this.neighbours = new HashSet<>();
        this.sensorsData = sensorsData;
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
    
    public void updateSensorData_int(String sensorIdentifier, int newValue) {
        Sensor oldSensor = this.sensorsData.get(sensorIdentifier);
        if (oldSensor != null) {
            Sensor newSensor = new Sensor(oldSensor.getNodeIdentifier(), 
                                          oldSensor.getSensorIdentifier(), 
                                          oldSensor.getType(), 
                                          newValue);
            this.sensorsData.put(sensorIdentifier, newSensor);
        } 
    }

    
    public void addNeighbour(NodeInfoI neighbour) {
        this.neighbours.add(neighbour);
    }

    public void addSensorData(String sensorIdentifier, Sensor SensorData) {
        this.sensorsData.put(sensorIdentifier, SensorData);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Node ID: ").append(nodeId).append("\n");
        sb.append("Position: ").append(position.getX()).append(", ").append(position.getY()).append("\n");
        sb.append("Sensor Data: \n");
        for (Map.Entry<String, Sensor> entry : sensorsData.entrySet()) {
            sb.append("    ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}