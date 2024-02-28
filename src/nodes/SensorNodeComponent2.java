package nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import client.ports.ClientRegistreOutboundPort;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
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
import registre.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import nodes.ports.SensorNodeInboundPort;
import nodes.ports.SensorNodeRegistreOutboundPort;
import nodes.sensor.Sensor;
import request.ExecutionState;
import request.ProcessingNode;
import request.ast.Gather;
import request.ast.Query;
import request.ast.astQuery.BQuery;
import request.ast.astQuery.GQuery;
import request.ast.interfaces.IASTvisitor;
import request.ast.interpreter.Interpreter;
import sensor_network.Position;
import sensor_network.QueryResult;

@RequiredInterfaces(required = {RegistrationCI.class})
@OfferedInterfaces(offered = {RequestingCI.class})
public class SensorNodeComponent2 extends AbstractComponent {
	protected NodeInfo nodeinfo;
	protected String uriPrefix;
//	protected ArrayList<Sensor> sensorlist;
	protected ProcessingNode processingNode;
	protected SensorNodeRegistreOutboundPort node_registre_port;
	
//	protected IASTvisitor<Object, ExecutionStateI, Exception> interpreter;
	
	protected static void	checkInvariant(SensorNodeComponent2 c)
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
	protected SensorNodeComponent2(
            NodeInfoI nodeInfo,
//            ArrayList<Sensor> sensorlist,
            String uriPrefix,
            String sensorNodeInboundPortURI,
            String Node_Registre_outboundPortURI,
            HashMap<String, Sensor> sensorsData
            ) throws Exception {
		super(uriPrefix, 1, 0) ;
		assert nodeInfo != null : "NodeInfo cannot be null!";
	    assert sensorNodeInboundPortURI != null && !sensorNodeInboundPortURI.isEmpty() : "InboundPort URI cannot be null or empty!";
	    this.nodeinfo = (NodeInfo) nodeInfo;
//	    this.sensorlist = sensorlist;
	    this.uriPrefix = uriPrefix;
	    
	    String NodeID = this.nodeinfo.nodeIdentifier();
		Position position = (Position) this.nodeinfo.nodePosition();
		 System.out.println("Test Position: " + position);
//		this.logMessage("Actuel position: " + position);
		
		this.processingNode = new ProcessingNode(NodeID,position,sensorsData);
		
	    //lier URI
        PortI p = new SensorNodeInboundPort(sensorNodeInboundPortURI, this);
		// publish the port
		p.publishPort();
		
		//node - registre - RegistrationCI
        this.node_registre_port = new SensorNodeRegistreOutboundPort(Node_Registre_outboundPortURI,this);
        this.node_registre_port.localPublishPort();
		
		
		
		if (AbstractCVM.isDistributed) {
			this.getLogger().setDirectory(System.getProperty("user.dir"));
		} else {
			this.getLogger().setDirectory(System.getProperty("user.home"));
		}
		this.getTracer().setTitle("SensorNode");
		this.getTracer().setRelativePosition(0, 2);
		
		//le methode de class courant
		SensorNodeComponent2.checkInvariant(this);
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
		this.logMessage("SensorNodeComponent "+ this.nodeinfo.nodeIdentifier() +" started.");
        super.start();
    }
	
	@Override
    public void execute() throws Exception {
		this.logMessage("SensorNodeComponent executed.");
//        super.execute();
        this.runTask(
        	    new AbstractComponent.AbstractTask() {
        	     @Override
        	     public void run() {
        	      try {    
        	       ((SensorNodeComponent2)this.getTaskOwner()).sendNodeInfoToRegistre(nodeinfo) ;
        	      } catch (Exception e) {
        	       e.printStackTrace();
        	      }
        	     }
        	    }) ;  
    }
	
	 @Override
	    public void finalise() throws Exception {
	        this.logMessage("SensorNodeComponent [" + this.nodeinfo.nodeIdentifier() + "] stopping...");
	        super.finalise();
	    }
	 
	 @Override
	    public void shutdown() throws ComponentShutdownException {
	        super.shutdown();
	    }
	 
	public QueryResultI processRequest(RequestI request) throws Exception{
		this.logMessage("SensorNodeComponent receive request");
		
		
		Interpreter interpreter = new Interpreter();
		Query<?> query = (Query<?>) request.getQueryCode();
//		if(query instanceof BQuery) {
//			this.logMessage("Query : BQuery");
//		} else if(query instanceof GQuery) {
//			this.logMessage("Query : GQuery");
//		}
		ExecutionState data = new ExecutionState(); 
        
        data.updateProcessingNode(this.processingNode);
        
        this.logMessage("Actuel position: " + this.processingNode.getPosition());
        this.logMessage("Actuel ProcessingNode" + this.processingNode);
        
//        ProcessingNode processingnode2 = (ProcessingNode) data.getProcessingNode();
        
//        this.logMessage("Test ProcessingNode" + this.processingNode);
        
//        this.logMessage("Test1");

//        if(((GQuery) query).getGather() != null) {
//        	 this.logMessage("Test_Gather");
//        }
//        Gather test_RGather = ((GQuery) query).getGather();
//        ArrayList<String> result_test_RGather = (ArrayList<String>)interpreter.visit(test_RGather, data);
//        this.logMessage("Res Gather: " + result_test_RGather);
        
        QueryResult result = (QueryResult) query.eval(interpreter, data);	
		
		this.logMessage("Calcul Fini: ");
		
		this.logMessage("Res: " + result);

		return result;
	 }
	
	public void sendNodeInfoToRegistre(NodeInfoI nodeInfo) throws Exception {
		  this.logMessage("SensorNodeComponent sendNodeInfo to Registre " );
		     // 调用注册表组件的注册方法
		     Boolean registed_before = this.node_registre_port.registered(nodeInfo.nodeIdentifier());
		     this.logMessage("Registered before register? " + nodeInfo.nodeIdentifier()+"Boolean:"+registed_before);
		     Set<NodeInfoI> neighbours = this.node_registre_port.register(nodeInfo);
		     Boolean registed_after = this.node_registre_port.registered(nodeInfo.nodeIdentifier());
		     this.logMessage("Registered after register? " + nodeInfo.nodeIdentifier()+"Boolean:"+registed_after);
		     
//		     this.logMessage("neighbours: " + neighbours);
		     this.logMessage("neighbours:");
		     for (NodeInfoI neighbour : neighbours) {
		         this.logMessage(((NodeInfo)neighbour).toString());
		     }
	}

	
}