package request;

import java.util.HashSet;
import java.util.Set;

import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import request.ast.Query;
//import request.interfaces.ExecutionStateI;

public class RequestContinuation  extends Request implements RequestContinuationI {
    private static final long serialVersionUID = 1L;
	private ExecutionState executionState; // 执行状态
	private Set<NodeInfoI> visitedNodes = new HashSet<>();

    public RequestContinuation(String requestURI, Query<?> query, boolean isAsynchronous, ConnectionInfoI clientConnectionInfo, ExecutionState executionState) {
        super(requestURI, query, isAsynchronous, clientConnectionInfo);
        this.executionState = executionState;
//         this.visitedNodes = new HashSet<>();
    }
    
    public RequestContinuation(RequestI request,ExecutionState data) {
    	super(((Request)request).requestURI(), ((Request)request).getQueryCode(), ((Request)request).isAsynchronous(), ((Request)request).clientConnectionInfo());
    	this.executionState = data;
    }
    
    //copie constructeur
    public RequestContinuation(RequestContinuation other) {
        super(other.requestURI(), other.getQueryCode(), other.isAsynchronous(), other.clientConnectionInfo());
        this.executionState = new ExecutionState(other.executionState);
        
        this.visitedNodes = new HashSet<>();
        for (NodeInfoI node : other.visitedNodes) {
            this.visitedNodes.add(node);
        }
    }
    
    @Override
    public ExecutionStateI getExecutionState() {
//        if (!this.isAsynchronous()) {
//            throw new IllegalStateException("getExecutionState() can only be called on asynchronous requests.");
//        }
        return (ExecutionStateI) this.executionState;
    }
    
    public void setExecutionState(ExecutionState executionState) {
        this.executionState = executionState;
    }
 
    public Set<NodeInfoI> getVisitedNodes() {
        return visitedNodes;
    }

    public void addVisitedNode(NodeInfoI nodeInfo) {
        visitedNodes.add(nodeInfo);
    }
}
