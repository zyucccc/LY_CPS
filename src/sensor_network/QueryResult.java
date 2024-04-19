package sensor_network;

import java.io.Serializable;
import java.util.*;

import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;

public class QueryResult implements QueryResultI,Serializable{	
	
	private static final long serialVersionUID = 1L;
	
	private ArrayList<String> positiveSensorNodes;
	private ArrayList<SensorDataI> gatheredSensorsValues;

	private boolean isBooleanRequest;
	private boolean isGatherRequest;
	
    public QueryResult() {
        this.isBooleanRequest = false;
        this.positiveSensorNodes = new ArrayList<>();
        this.isGatherRequest = false;
        this.gatheredSensorsValues = new ArrayList<>();
    }

    public QueryResult(boolean isBooleanRequest, ArrayList<String> positiveSensorNodes, boolean isGatherRequest, ArrayList<SensorDataI> gatheredSensorsValues) {
        this.isBooleanRequest = isBooleanRequest;
        this.positiveSensorNodes = positiveSensorNodes;
        this.isGatherRequest = isGatherRequest;
        this.gatheredSensorsValues = gatheredSensorsValues;
    }
    
    //copie constructeur
    public QueryResult(QueryResult other) {
        this.isBooleanRequest = other.isBooleanRequest;
        this.positiveSensorNodes = new ArrayList<>(other.positiveSensorNodes);
        this.isGatherRequest = other.isGatherRequest;
        this.gatheredSensorsValues = new ArrayList<>(other.gatheredSensorsValues);
    }

    public void mergeRes(QueryResultI otherResult) {
        if (otherResult.isBooleanRequest()) {
            //traiter les doublons en utilisant hashset
            Set<String> uniqueSensorNodes = new HashSet<>(this.positiveSensorNodes);
            uniqueSensorNodes.addAll(otherResult.positiveSensorNodes());
            this.positiveSensorNodes = new ArrayList<>(uniqueSensorNodes);
        }
        if (otherResult.isGatherRequest()) {
            //traiter les doublons en utilisant hashmap
            Map<String, SensorDataI> uniqueSensors = new HashMap<>();
            for (SensorDataI sensorData : this.gatheredSensorsValues) {
                String key = sensorData.getNodeIdentifier() + ":" + sensorData.getSensorIdentifier();
                uniqueSensors.put(key, sensorData);
            }
            for (SensorDataI newSensorData : otherResult.gatheredSensorsValues()) {
                String key = newSensorData.getNodeIdentifier() + ":" + newSensorData.getSensorIdentifier();
                uniqueSensors.put(key, newSensorData);
            }
            this.gatheredSensorsValues = new ArrayList<>(uniqueSensors.values());
//            this.gatheredSensorsValues.addAll(otherResult.gatheredSensorsValues());
        }
    }
    
    @Override
    public boolean isBooleanRequest() {
        return isBooleanRequest;
    }
    
    public void SetBooleanRequest() {
        this.isBooleanRequest =true;
    }

    @Override
    public ArrayList<String> positiveSensorNodes() {
        return positiveSensorNodes;
    }

    @Override
    public boolean isGatherRequest() {
        return isGatherRequest;
    }
    
    public void SetGatherRequest() {
        this.isGatherRequest =true;
    }

    @Override
    public ArrayList<SensorDataI> gatheredSensorsValues() {
        return gatheredSensorsValues;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isBooleanRequest) {
            sb.append("Boolean Query Result: \n");
            if (positiveSensorNodes.isEmpty()) {
                sb.append("No positive sensor nodes.");
            } else {
                sb.append("Positive Sensor Nodes: ");
                sb.append(String.join(", ", positiveSensorNodes));
            }
        }
        if (isGatherRequest) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append("Gather Query Result: \n");
            if (gatheredSensorsValues.isEmpty()) {
                sb.append("No sensor data gathered.");
            } else {
                sb.append("Gathered Sensors Values: ");
                ArrayList<String> sensorValues = new ArrayList<>();
                for (SensorDataI sensorData : gatheredSensorsValues) {
                    String sensorValueString = String.format("[Node ID: %s, Sensor ID: %s, Value: %s]", 
                        sensorData.getNodeIdentifier(), 
                        sensorData.getSensorIdentifier(), 
                        sensorData.getValue().toString());
                    sensorValues.add(sensorValueString);
                }
                sb.append("[" + String.join(";", sensorValues) + "]");
            }
        }
        return sb.toString();
    }


}
