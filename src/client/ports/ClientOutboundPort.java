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
				
	}
	
	@Override
	public synchronized void	doConnection(
		String otherPortURI,
		String ccname
		) throws Exception
	{
		System.out.println("Client Connect to Node start: ");
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");
		assert	this.isPublished() && !this.connected() :
					new PreconditionException("isPublished() && "
													+ "!connected()");
		assert	otherPortURI != null && ccname != null :
					new PreconditionException("otherPortURI != null && "
													+ "ccname != null");
//		assert	this.isPublished()  :
//			new PreconditionException("isPublished() && "
//											+ "!connected()");
////		assert	 !this.connected() :
//			new PreconditionException("isPublished() && "
//											+ "!connected()");
		System.out.println("Test connected: " + this.connected());
		System.out.println("Test1 ");

		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		Class<?> cc = Class.forName(ccname);
		Constructor<?> c = cc.getConstructor(new Class<?>[]{});
		ConnectorI connector = (ConnectorI) c.newInstance();
		System.out.println("Test2 ");
		this.doConnection(otherPortURI, connector);
	}
	
	@Override
	public synchronized void	doConnection(
		String otherPortURI,
		ConnectorI connector
		) throws	Exception
	{
		assert	!this.isDestroyed() :
					new PreconditionException("!isDestroyed()");
		assert	this.isPublished() && !this.connected() :
					new PreconditionException("isPublished() && "
													+ "!connected()");
		assert	otherPortURI != null && connector != null :
					new PreconditionException("otherPortURI != null && "
													+ "connector != null");

		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		// In a simple client/server connection, where a plain outbound port is
		// connected to a plain inbound port, be it remote or local, the
		// connection is done one way on the client (outbound port) side,
		// so we need only to connect this side.
		System.out.println("Test3 ");
		this.doMyConnection(otherPortURI, connector);
		System.out.println("Test4 ");
		AbstractOutboundPort.checkImplementationInvariant(this);
		AbstractOutboundPort.checkInvariant(this);
		AbstractPort.checkImplementationInvariant(this);
		AbstractPort.checkInvariant(this);
		assert	this.connected() :
					new PostconditionException("connected()");
		System.out.println("Test5 ");
	}

	/**
	 * @see fr.sorbonne_u.components.AbstractPort#doMyConnection(java.lang.String, fr.sorbonne_u.components.connectors.ConnectorI)
	 */
	@Override
	protected synchronized void	doMyConnection(
		String otherPortURI,
		ConnectorI connector
		) throws Exception
	{
		// FIXME: should use a proper state machine model to implement the
		// connection and disconnection protocol

		this.setConnector(connector);
		this.setServerPortURI(otherPortURI);
		PortI serverPort =
				AbstractCVM.getFromLocalRegistry(this.getServerPortURI());
		if (serverPort == null && AbstractCVM.isDistributed) {
			this.isRemotelyConnected.set(true);
			serverPort = (PortI)AbstractDistributedCVM.getCVM().
									getRemoteReference(this.getServerPortURI());
		} else {
			this.isRemotelyConnected.set(false);
		}
		assert	serverPort != null :
					new ConnectionException("Unknown server port URI: " +
													this.getServerPortURI());

		this.getConnector().connect((OfferedCI)serverPort, this);
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#obeyConnection(java.lang.String, java.lang.String)
	 */
	@Override
	public void			obeyConnection(String otherPortURI, String ccname)
	throws	Exception
	{
		throw new ConnectionException("Can't call obeyConnection on simple"
														+ " outbound ports.");
	}

	/**
	 * @see fr.sorbonne_u.components.ports.PortI#obeyConnection(java.lang.String, fr.sorbonne_u.components.connectors.ConnectorI)
	 */
	@Override
	public void			obeyConnection(String otherPortURI, ConnectorI connector)
	throws	Exception
	{
		throw new ConnectionException("Can't call obeyConnection on simple"
														+ " outbound ports.");
	}


}
