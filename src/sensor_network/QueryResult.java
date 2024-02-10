package sensor_network;

import java.io.Serializable;
import java.util.ArrayList;

import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.SensorDataI;

public class QueryResult implements QueryResultI,Serializable{	
	
	private ArrayList<String> positiveSensorNodes;
	private ArrayList<SensorDataI> gatheredSensorsValues;

	private boolean isBooleanRequest;
    private boolean isGatherRequest;

    public QueryResult(boolean isBooleanRequest, ArrayList<String> positiveSensorNodes, boolean isGatherRequest, ArrayList<SensorDataI> gatheredSensorsValues) {
        this.isBooleanRequest = isBooleanRequest;
        this.positiveSensorNodes = positiveSensorNodes;
        this.isGatherRequest = isGatherRequest;
        this.gatheredSensorsValues = gatheredSensorsValues;
    }

    @Override
    public boolean isBooleanRequest() {
        return isBooleanRequest;
    }

    @Override
    public ArrayList<String> positiveSensorNodes() {
        return positiveSensorNodes;
    }

    @Override
    public boolean isGatherRequest() {
        return isGatherRequest;
    }

    @Override
    public ArrayList<SensorDataI> gatheredSensorsValues() {
        return gatheredSensorsValues;
    }

    // 这里你可以添加一个构造函数来初始化一个空的查询结果
    public QueryResult() {
        this.isBooleanRequest = false;
        this.positiveSensorNodes = new ArrayList<>();
        this.isGatherRequest = false;
        this.gatheredSensorsValues = new ArrayList<>();
    }

}
