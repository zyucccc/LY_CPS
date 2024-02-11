package nodes;

import java.util.ArrayList;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.ports.PortI;
import fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
//import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PImplI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import nodes.sensor.Sensor;
import ports.SensorNodeInboundPort;
import request.ast.Query;
import request.ast.interfaces.IASTvisitor;
import request.ast.interpreter.Interpreter;

@OfferedInterfaces(offered = {RequestingCI.class})
public class SensorNodeComponent extends AbstractComponent {
	protected NodeInfoI nodeinfo;
	protected String uriPrefix;
	protected ArrayList<Sensor> sensorlist;
//	protected IASTvisitor<Object, ExecutionStateI, Exception> interpreter;
	
	protected static void	checkInvariant(SensorNodeComponent c)
	{
		assert	c.uriPrefix != null :
					new InvariantException("The URI prefix is null!");
		assert	c.isOfferedInterface(RequestingCI.class) :
					new InvariantException("The URI component should "
							+ "offer the interface URIProviderI!");
	}
	
//	public SensorNodeComponent(String nodeId, PositionI position, double range, EndPointDescriptorI p2pEndPoint, EndPointDescriptorI clientEndPoint) throws Exception {
//        super(1, 0); // 1 schedule thread,0 task
//        this.nodeinfo = new NodeInfo(nodeId, position, range, p2pEndPoint, clientEndPoint);
//    }
	public SensorNodeComponent(
            NodeInfoI nodeInfo,
            ArrayList<Sensor> sensorlist,
            String uriPrefix,
            String sensorNodeInboundPortURI
            ) throws Exception {
		super(uriPrefix, 1, 0) ;
		assert nodeInfo != null : "NodeInfo cannot be null!";
	    assert sensorNodeInboundPortURI != null && !sensorNodeInboundPortURI.isEmpty() : "InboundPort URI cannot be null or empty!";
	    this.nodeinfo = nodeInfo;
	    this.sensorlist = sensorlist;
	    this.uriPrefix = uriPrefix;
        PortI p = new SensorNodeInboundPort(sensorNodeInboundPortURI, this);
		// publish the port
		p.publishPort();
		
		if (AbstractCVM.isDistributed) {
			this.getLogger().setDirectory(System.getProperty("user.dir"));
		} else {
			this.getLogger().setDirectory(System.getProperty("user.home"));
		}
		this.getTracer().setTitle("SensorNode");
		this.getTracer().setRelativePosition(1, 0);
		
		//le methode de class courant
		SensorNodeComponent.checkInvariant(this);
		AbstractComponent.checkImplementationInvariant(this);
		AbstractComponent.checkInvariant(this);
	
		assert	this.uriPrefix.equals(uriPrefix) :
			new PostconditionException("The URI prefix has not "
										+ "been initialised!");
assert	this.isPortExisting(sensorNodeInboundPortURI) :
			new PostconditionException("The component must have a "
					+ "port with URI " + sensorNodeInboundPortURI);
assert	this.findPortFromURI(sensorNodeInboundPortURI).
			getImplementedInterface().equals(RequestingCI.class) :
			new PostconditionException("The component must have a "
					+ "port with implemented interface URIProviderI");
assert	this.findPortFromURI(sensorNodeInboundPortURI).isPublished() :
			new PostconditionException("The component must have a "
					+ "port published with URI " + sensorNodeInboundPortURI);
	}
	
	
	//life
	@Override
    public void start() throws ComponentStartException {
		this.logMessage("SensorNodeComponent started.");
        super.start();
    }
	
	@Override
    public void execute() throws Exception {
        super.execute();
    }
	
	 @Override
	    public void finalise() throws Exception {
	        // 清理资源，断开连接等...
	        this.logMessage("SensorNodeComponent [" + this.nodeinfo.nodeIdentifier() + "] stopping...");
	        super.finalise();
	    }
	 
	 @Override
	    public void shutdown() throws ComponentShutdownException {
	        super.shutdown();
	    }
	 
	public QueryResultI processRequest(RequestI request) throws Exception{
		Interpreter interpreter = new Interpreter();
		Query<?> query = (Query<?>) request.getQueryCode();
		QueryResultI result;
		result = (QueryResultI) interpreter.visit(query, null);

		return result;
	 }
	
}
