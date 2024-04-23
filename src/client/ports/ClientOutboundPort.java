package client.ports;

import java.lang.reflect.Constructor;

import client.ClientComponent;
import fr.sorbonne_u.components.AbstractPort;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.connectors.ConnectorI;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cvm.AbstractDistributedCVM;
import fr.sorbonne_u.components.exceptions.ConnectionException;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.components.ports.PortI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.exceptions.PostconditionException;
import fr.sorbonne_u.exceptions.PreconditionException;
import nodes.SensorNodeComponent;
import sensor_network.QueryResult;

public class ClientOutboundPort extends	AbstractOutboundPort implements RequestingCI{
	
	private static final long serialVersionUID = 1L;
	
	public ClientOutboundPort (String uri,ComponentI owner) throws Exception{
		super(uri,RequestingCI.class, owner) ;
		assert	uri != null && owner instanceof  ClientComponent;
	}
	
	public ClientOutboundPort (ComponentI owner) throws Exception{
		super(RequestingCI.class, owner) ;
		assert	owner != null ;
	}

	@Override
	public QueryResultI execute(RequestI request) throws Exception {
		QueryResult result = (QueryResult) ((RequestingCI)this.getConnector()).execute(request);
		return result;
	}

	@Override
	public void executeAsync(RequestI request) throws Exception {
	  ((RequestingCI)this.getConnector()).executeAsync(request);
	}

}
