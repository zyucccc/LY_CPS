package ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import nodes.ClientComponent;
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
				
	}

}
