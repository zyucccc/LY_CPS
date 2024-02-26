package request;

import java.util.HashSet;
import java.util.Set;

import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
//import fr.sorbonne_u.cps.sensor_network.interfaces.*;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;
import request.ast.Direction;
import sensor_network.QueryResult;

public class ExecutionState implements ExecutionStateI {
    private ProcessingNode processingNode;
    private QueryResult currentResult;
    private boolean isContinuationSet;
    private boolean isDirectional;
    private Set<Direction> directions;
    private int hops;
    private boolean isFlooding;
    private double maximalDistance;
    private PositionI floodingBasePosition;

    public ExecutionState() {
        this.processingNode = null;
        this.currentResult = null;
        this.isContinuationSet = false;
        this.isDirectional = false;
        this.directions = new HashSet<>();
        this.hops = 0;
        this.isFlooding = false;
        this.maximalDistance = 0.0;
        this.floodingBasePosition = null;
    }

    @Override
    public ProcessingNodeI getProcessingNode() {
        return this.processingNode;
    }

    @Override
    public void updateProcessingNode(ProcessingNodeI pn) {
        this.processingNode = (ProcessingNode) pn;
    }

    @Override
    public QueryResultI getCurrentResult() {
        return this.currentResult;
    }

    @Override
    public void addToCurrentResult(QueryResultI result) {
    	// 如果当前没有结果，则直接将新结果设置为当前结果
        if (this.currentResult == null) {
            this.currentResult = (QueryResult) result;
        } else {
            // Bool query
            if (result.isBooleanRequest()) {
                this.currentResult.positiveSensorNodes().addAll(result.positiveSensorNodes());
            }
            
            // Gather query
            if (result.isGatherRequest()) {
                this.currentResult.gatheredSensorsValues().addAll(result.gatheredSensorsValues());
            }
            
            // mise a jour type de resultRequest
            if( this.currentResult.isBooleanRequest() || result.isBooleanRequest()) {
            	this.currentResult.SetBooleanRequest();
            };
            if( this.currentResult.isGatherRequest() || result.isGatherRequest()) {
            	this.currentResult.SetGatherRequest();
            };
        }
    }

    @Override
    public boolean isContinuationSet() {
    	return this.isContinuationSet;
//        return this.isDirectional || this.isFlooding;
    }

    @Override
    public boolean isDirectional() {
        return this.isDirectional;
    }
    
    @Override
    public Set<fr.sorbonne_u.cps.sensor_network.interfaces.Direction> getDirections() {
        return null;
    }

  
    public Set<Direction> getDirections_ast() {
        return this.directions;
    }

    @Override
    public boolean noMoreHops() {
        // 假设最大跳数为10，这个值根据需要调整
        return this.hops >= 10;
    }

    @Override
    public void incrementHops() {
        this.hops++;
    }

    @Override
    public boolean isFlooding() {
        return this.isFlooding;
    }

    @Override
    public boolean withinMaximalDistance(PositionI p) {
        // 这里需要实现距离计算逻辑，以下仅为示例
//        if (this.floodingBasePosition != null) {
//            return this.floodingBasePosition.distanceTo(p) <= this.maximalDistance;
//        }
        return false;
    }
    
    // check continuation Vide
    public boolean isVide() {
        return this.isContinuationSet && (!this.isDirectional) && (!this.isFlooding) ;
    }

    // set direction//flooding //
    public void setDirectional(boolean isDirectional, Set<Direction> directions,int saut) {
    	this.isContinuationSet = true;
        this.isDirectional = isDirectional;
        this.directions = directions;
        this.hops = saut;
    }

    public void setFlooding(boolean isFlooding, double maximalDistance, PositionI basePosition) {
    	this.isContinuationSet = true;
    	this.isFlooding = isFlooding;
        this.maximalDistance = maximalDistance;
        this.floodingBasePosition = basePosition;
    }
    
    public void setIsContinuationSet() {
    	this.isContinuationSet = true;
    }
    
    
    
}