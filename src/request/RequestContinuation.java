package request;

import java.util.HashSet;
import java.util.Set;

import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import request.ast.Direction;
import request.ast.Query;
//import request.interfaces.ExecutionStateI;

public class RequestContinuation  extends Request implements RequestContinuationI {
    private static final long serialVersionUID = 1L;
	private ExecutionState executionState;
    //visitedNodes pour traiter les requete flooding, on ne veut pas visiter un noeud plus d'une fois
    private Set<String> visitedNodes = new HashSet<>();



    public RequestContinuation(String requestURI, Query<?> query, boolean isAsynchronous, ConnectionInfoI clientConnectionInfo, ExecutionState executionState) {
        super(requestURI, query, isAsynchronous, clientConnectionInfo);
        this.executionState = executionState;
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
        for (String node : other.visitedNodes) {
            this.visitedNodes.add(node);
        }

    }
    
    @Override
    public ExecutionStateI getExecutionState() {
        return (ExecutionStateI) this.executionState;
    }

    public Set<String> getVisitedNodes() {
        return visitedNodes;
    }

    public void addVisitedNode(String nodeInfo) {
        visitedNodes.add(nodeInfo);
    }
}
