package sensor_network;

import java.io.Serializable;
import java.util.ArrayList;

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
    
//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        if (isBooleanRequest) {
//            sb.append("Boolean Query Result: \n");
//            if (positiveSensorNodes.isEmpty()) {
//                sb.append("No positive sensor nodes.");
//            } else {
//                sb.append("Positive Sensor Nodes: ");
//                sb.append(String.join(", ", positiveSensorNodes));
//            }
//        }
//        if (isGatherRequest) {
//            if (sb.length() > 0) {
//                sb.append("\n"); // 如果已经有内容，添加换行符
//            }
//            sb.append("Gather Query Result: \n");
//            if (gatheredSensorsValues.isEmpty()) {
//                sb.append("No sensor data gathered.");
//            } else {
//                sb.append("Gathered Sensors Values: [");
//                ArrayList<String> sensorValues = new ArrayList<>();
//                for (SensorDataI sensorData : gatheredSensorsValues) {
//                    sensorValues.add(sensorData.toString()); // 假设SensorDataI也重写了toString方法
//                }
//                sb.append(String.join(", ", sensorValues));
//                sb.append("]");
//            }
//        }
//        return sb.toString();
//    }
    
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
                sb.append("\n"); // 如果已经有内容，添加换行符
            }
            sb.append("Gather Query Result: \n");
            if (gatheredSensorsValues.isEmpty()) {
                sb.append("No sensor data gathered.");
            } else {
                sb.append("Gathered Sensors Values: ");
                ArrayList<String> sensorValues = new ArrayList<>();
                for (SensorDataI sensorData : gatheredSensorsValues) {
                    // 修改此处以满足新的格式要求
                    String sensorValueString = String.format("[Node ID: %s, Sensor ID: %s, Value: %s]", 
                        sensorData.getNodeIdentifier(), 
                        sensorData.getSensorIdentifier(), 
                        sensorData.getValue().toString());
                    sensorValues.add(sensorValueString);
                }
                // 使用";"作为分隔符
                sb.append("[" + String.join(";", sensorValues) + "]");
            }
        }
        return sb.toString();
    }


}
