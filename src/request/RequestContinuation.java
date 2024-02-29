package request;

import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import request.ast.Query;
//import request.interfaces.ExecutionStateI;

public class RequestContinuation  extends Request implements RequestContinuationI {
    private static final long serialVersionUID = 1L;
	private ExecutionState executionState; // 执行状态

    public RequestContinuation(String requestURI, Query<?> query, boolean isAsynchronous, ConnectionInfoI clientConnectionInfo, ExecutionState executionState) {
        super(requestURI, query, isAsynchronous, clientConnectionInfo);
        this.executionState = executionState;
    }
    
    public RequestContinuation(RequestI request,ExecutionState data) {
    	super(((Request)request).requestURI(), ((Request)request).getQueryCode(), ((Request)request).isAsynchronous(), ((Request)request).clientConnectionInfo());
    	this.executionState = data;
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
}
