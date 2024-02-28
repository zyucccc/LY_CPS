package request;

import java.util.HashSet;
import java.util.Set;

import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
//import fr.sorbonne_u.cps.sensor_network.interfaces.*;

import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;
import request.ast.Direction;
import request.interfaces.ExecutionStateI;
import sensor_network.Position;
import sensor_network.QueryResult;

public class ExecutionState implements ExecutionStateI {
    private static final long serialVersionUID = 1L;
	private ProcessingNode processingNode;
    private QueryResult currentResult;
    private boolean isContinuationSet;
    private boolean isDirectional;
    private Set<Direction> directions;
    private int hops;
    private int current_hops;
    private boolean isFlooding;
    private double maximalDistance;
    private Position floodingBasePosition;

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
    public Set<Direction> getDirections() {
        return this.directions;
    }

    @Override
    public boolean noMoreHops() {
        return this.current_hops >= this.hops;
    }
    
    public int Hops_Left() {
    	return this.hops-this.current_hops;
    }

    @Override
    public void incrementHops() {
        this.current_hops++;
    }

    @Override
    public boolean isFlooding() {
        return this.isFlooding;
    }

    @Override
    public boolean withinMaximalDistance(PositionI p) {
        if (this.floodingBasePosition != null) {
            return this.floodingBasePosition.distance(p) <= this.maximalDistance;
        }
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
        this.floodingBasePosition = (Position) basePosition;
    }
    
    public void setIsContinuationSet() {
    	this.isContinuationSet = true;
    }
    
    public String toStringContinuation() {
        StringBuilder sb = new StringBuilder("Continuation Info: ");
        if (!this.isContinuationSet) {
            sb.append("None set.");
        } else {
            if (this.isDirectional) {
                sb.append("Directional with directions ");
                this.directions.forEach(dir -> sb.append(dir).append(" "));
                sb.append("and hops left ").append(10 - this.hops).append(".");
            } else if (this.isFlooding) {
                sb.append("Flooding with maximal distance ").append(this.maximalDistance);
                if (this.floodingBasePosition != null) {
                    sb.append(" from base position ").append(this.floodingBasePosition.toString());
                }
                sb.append(".");
            } else {
                sb.append("Type de contionuation : ECont");
            }
        }
        return sb.toString();
    }
    
    
}
