package request;

import request.ast.Query;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;


import java.io.Serializable;

public class Request implements RequestI {
    private static final long serialVersionUID = 1L;
	private final String requestURI;
    private final Query<?> query;
    private final boolean isAsynchronous;
    private final ConnectionInfoI clientConnectionInfo;
    

    public Request(String requestURI, Query<?> query, boolean isAsynchronous, ConnectionInfoI clientConnectionInfo) {
        this.requestURI = requestURI;
        this.query = query;
        this.isAsynchronous = isAsynchronous;
        this.clientConnectionInfo = clientConnectionInfo;
    }
    //copie constructeur
    public Request(Request other) {
        this.requestURI = other.requestURI;
        this.query = other.query; 
        this.isAsynchronous = other.isAsynchronous;
        this.clientConnectionInfo = other.clientConnectionInfo; 
    }

    @Override
    public String requestURI() {
        return this.requestURI;
    }

    @Override
    public Query<?> getQueryCode() {
        return this.query;
    }

    @Override
    public boolean isAsynchronous() {
        return this.isAsynchronous;
    }

    @Override
    public ConnectionInfoI clientConnectionInfo() {
        return this.clientConnectionInfo;
    }
    
    
    
}
