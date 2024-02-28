package nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import client.ClientComponent;
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
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;
//import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PImplI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import registre.interfaces.RegistrationCI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ExecutionStateI;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import nodes.connectors.NodeClientConnector;
import nodes.connectors.NodeNodeConnector;
import nodes.ports.NodeNodeInboundPort;
import nodes.ports.NodeNodeOutboundPort;
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
import sensor_network.EndPointDescriptor;
import sensor_network.Position;
import sensor_network.QueryResult;

@RequiredInterfaces(required = {RegistrationCI.class,SensorNodeP2PCI.class})
@OfferedInterfaces(offered = {RequestingCI.class,SensorNodeP2PCI.class})
public class SensorNodeComponent extends AbstractComponent {
	protected NodeInfo nodeinfo;
	protected String uriPrefix;
	protected ProcessingNode processingNode;
	//outbound port for Registre
	protected SensorNodeRegistreOutboundPort node_registre_port;
	//outbound port for Node2Node
	protected NodeNodeOutboundPort node_node_port;
	protected Set<NodeInfoI> neighbours;
	
	protected static void	checkInvariant(SensorNodeComponent c)
	{
		assert	c.uriPrefix != null :
					new InvariantException("The URI prefix is null!");
		assert	c.isOfferedInterface(RequestingCI.class) :
					new InvariantException("The URI component should "
							+ "offer the interface URIProviderI!");
	}
	
	protected SensorNodeComponent(
            NodeInfoI nodeInfo,
            String uriPrefix,
            String sensorNodeInboundPortURI,
            String node_Registre_outboundPortURI,//Node_registre_outboundPortURI
            String node_node_OutboundPortURI,//P2P node to node outbound Port
            String node_node_InboundPortURI,//P2P node to node Inbound Port
            HashMap<String, Sensor> sensorsData
            ) throws Exception {
		
		super(uriPrefix, 1, 1) ;
		assert nodeInfo != null : "NodeInfo cannot be null!";
		//inboudporturi for client
	    assert sensorNodeInboundPortURI != null && !sensorNodeInboundPortURI.isEmpty() : "InboundPort URI cannot be null or empty!";
	    //inboudporturi for node2node
	    assert node_node_InboundPortURI != null && !node_node_InboundPortURI.isEmpty() : "InboundPortN2 URI cannot be null or empty!";
	    this.nodeinfo = (NodeInfo) nodeInfo;
	    this.uriPrefix = uriPrefix;
	    
	    String NodeID = this.nodeinfo.nodeIdentifier();
		Position position = (Position) this.nodeinfo.nodePosition();
		this.neighbours = new HashSet<>();
		
		this.processingNode = new ProcessingNode(NodeID,position,sensorsData);
		
		//Inbound Port publish
		//node - client - requestingI
	    //lier URI
        PortI p = new SensorNodeInboundPort(sensorNodeInboundPortURI, this);
		//publish the port
		p.publishPort();
        //node - node - SensorNodeP2PCI
        PortI p2p=new NodeNodeInboundPort(node_node_InboundPortURI,this);
        p2p.publishPort();
		
		//Outbound Port pubulish
		//node - registre - RegistrationCI
        this.node_registre_port = new SensorNodeRegistreOutboundPort(node_Registre_outboundPortURI,this);
        this.node_registre_port.localPublishPort();
        //node - node - SensorNodeP2PCI
        this.node_node_port = new NodeNodeOutboundPort(node_node_OutboundPortURI,this);
        this.node_node_port.localPublishPort();
		
        if (AbstractCVM.isDistributed) {
			this.getLogger().setDirectory(System.getProperty("user.dir"));
		} else {
			this.getLogger().setDirectory(System.getProperty("user.home"));
		}
		this.getTracer().setTitle("SensorNode");
		this.getTracer().setRelativePosition(0, 2);
		
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
		this.logMessage("SensorNodeComponent "+ this.nodeinfo.nodeIdentifier() +" started.");
        super.start();
        this.runTask(
        	    new AbstractComponent.AbstractTask() {
        	     @Override
        	     public void run() {
        	      try {    
        	       ((SensorNodeComponent)this.getTaskOwner()).sendNodeInfoToRegistre(((SensorNodeComponent)this.getTaskOwner()).nodeinfo) ;
        	      } catch (Exception e) {
        	       e.printStackTrace();
        	      }
        	     }
        	    }) ;  
    }
	
	@Override
    public void execute() throws Exception {
		this.logMessage("SensorNodeComponent executed.");
        super.execute();
		this.scheduleTask(new AbstractComponent.AbstractTask() {
            @Override
            public void run() {
                try {
                ((SensorNodeComponent)this.getTaskOwner()).refraichir_neighbours(((SensorNodeComponent)this.getTaskOwner()).nodeinfo);
                ((SensorNodeComponent)this.getTaskOwner()).connecterNeighbours();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1000, TimeUnit.MILLISECONDS);
        
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
		this.logMessage("----------------Receive Query------------------");
		this.logMessage("SensorNodeComponent "+this.nodeinfo.nodeIdentifier()+" : receive request");	
		Interpreter interpreter = new Interpreter();
		Query<?> query = (Query<?>) request.getQueryCode();
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
		this.logMessage("----------------------------------");

		return result;
	 }
	
	public void sendNodeInfoToRegistre(NodeInfoI nodeInfo) throws Exception {
		     this.logMessage("----------------Register------------------");
		     this.logMessage("SensorNodeComponent sendNodeInfo to Registre " );
		     // 调用注册表组件的注册方法
		     Boolean registed_before = this.node_registre_port.registered(nodeInfo.nodeIdentifier());
		     this.logMessage("Registered before register? " + nodeInfo.nodeIdentifier()+"Boolean:"+registed_before);
		     Set<NodeInfoI> neighbours = this.node_registre_port.register(nodeInfo);
		     this.neighbours = neighbours;
		     Boolean registed_after = this.node_registre_port.registered(nodeInfo.nodeIdentifier());
		     this.logMessage("Registered after register? " + nodeInfo.nodeIdentifier()+"Boolean:"+registed_after);
		     this.logMessage("----------------------------------");
//		     this.logMessage("neighbours:");
//		     for (NodeInfoI neighbour : neighbours) {
//		         this.logMessage("neighbour :"+((NodeInfo)neighbour).toString());
//		     }
	}
	
	public void refraichir_neighbours(NodeInfoI nodeInfo) throws Exception {
		this.logMessage("--------------Actuel neighbours:--------------");
		this.logMessage("SensorNodeComponent"+ nodeInfo.nodeIdentifier() +" : actuel neighbours: " );
		Set<NodeInfoI> actuel_neighbours = this.node_registre_port.refraichir_neighbours(nodeInfo);
		this.neighbours = actuel_neighbours;
	    this.logMessage("neighbours:");
	     for (NodeInfoI neighbour : neighbours) {
	         this.logMessage("neighbour :"+((NodeInfo)neighbour).toString());
	     }
	     this.logMessage("----------------------------------");
	}
	
	public void ask4Connection(NodeInfoI newNeighbour) throws Exception {
	    EndPointDescriptor endpointDescriptor = (EndPointDescriptor) newNeighbour.p2pEndPointInfo();
	    if (endpointDescriptor != null) {
	        String neighbourInboundPortURI = endpointDescriptor.getURI();
	        this.logMessage("SensorNodeComponent :"+ this.nodeinfo.nodeIdentifier() +" start connect : Uri obtenue to connect :" + neighbourInboundPortURI);
	        this.logMessage("Test name class: "+NodeNodeConnector.class.getCanonicalName());
	       this.node_node_port.doConnection(neighbourInboundPortURI,NodeNodeConnector.class.getCanonicalName());        
	       this.logMessage("SensorNodeComponent : "+ this.nodeinfo.nodeIdentifier() + ": Connection established with Node :" + newNeighbour.nodeIdentifier());
	    } else {
	        throw new Exception("p2pEndPointInfo() did not return an instance of EndPointDescriptor");
	    }
	}
	
	public void connecterNeighbours() throws Exception {
		this.logMessage("SensorNodeComponent [" + this.nodeinfo.nodeIdentifier() + "] start connecter ses neighbours");
		 for (NodeInfoI neighbour : this.neighbours) {
			 this.ask4Connection(neighbour);
		 }
	}

	public QueryResultI execute(RequestContinuationI request) {
		return null;
	}

	
}
