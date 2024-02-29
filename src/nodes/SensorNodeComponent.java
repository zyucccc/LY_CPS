package nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;

//import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PImplI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import registre.interfaces.RegistrationCI;
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
import request.Request;
import request.RequestContinuation;
import request.ast.Direction;
import request.ast.Gather;
import request.ast.Query;
import request.ast.astQuery.BQuery;
import request.ast.astQuery.GQuery;
import request.ast.interfaces.IASTvisitor;
import request.ast.interpreter.Interpreter;
import sensor_network.EndPointDescriptor;
import sensor_network.Position;
import sensor_network.QueryResult;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;

@RequiredInterfaces(required = {RegistrationCI.class,SensorNodeP2PCI.class})
@OfferedInterfaces(offered = {RequestingCI.class,SensorNodeP2PCI.class})
public class SensorNodeComponent extends AbstractComponent {
	protected NodeInfo nodeinfo;
	protected String uriPrefix;
	protected ProcessingNode processingNode;
	//outbound port for Registre
	protected SensorNodeRegistreOutboundPort node_registre_port;
	//outbound port for Node2Node
	protected NodeNodeOutboundPort node_node_NE_Outport;
	protected NodeNodeOutboundPort node_node_NW_Outport;
	protected NodeNodeOutboundPort node_node_SE_Outport;
	protected NodeNodeOutboundPort node_node_SW_Outport;
//	protected Set<NodeInfoI> neighbours;
	protected Map<Direction,NodeInfoI> neighbours;
	
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
            String node_node_NE_OutboundPortURI,//P2P node to node outbound Port
            String node_node_NW_OutboundPortURI,
            String node_node_SE_OutboundPortURI,
            String node_node_SW_OutboundPortURI,
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
		this.neighbours = new HashMap<>();
		
		this.processingNode = new ProcessingNode(NodeID,position,sensorsData);
		
		//Inbound Port publish
		//node - client - requestingI
	    //lier URI
        PortI p = new SensorNodeInboundPort(sensorNodeInboundPortURI, this);
		p.publishPort();
        //node - node - SensorNodeP2PCI
        PortI p2p=new NodeNodeInboundPort(node_node_InboundPortURI,this);
        p2p.publishPort();
		
		//Outbound Port pubulish
		//node - registre - RegistrationCI
        this.node_registre_port = new SensorNodeRegistreOutboundPort(node_Registre_outboundPortURI,this);
        this.node_registre_port.localPublishPort();
        //node - node - SensorNodeP2PCI
        this.node_node_NE_Outport = new NodeNodeOutboundPort(node_node_NE_OutboundPortURI,this);
        this.node_node_NE_Outport.localPublishPort();
        this.node_node_NW_Outport = new NodeNodeOutboundPort(node_node_NW_OutboundPortURI,this);
        this.node_node_NW_Outport.localPublishPort();
        this.node_node_SE_Outport = new NodeNodeOutboundPort(node_node_SE_OutboundPortURI,this);
        this.node_node_SE_Outport.localPublishPort();
        this.node_node_SW_Outport = new NodeNodeOutboundPort(node_node_SW_OutboundPortURI,this);
        this.node_node_SW_Outport.localPublishPort();
		
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
        
//        this.logMessage("Actuel position: " + this.processingNode.getPosition());
//        this.logMessage("Actuel ProcessingNode" + this.processingNode);
        
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
//        data.addToCurrentResult(result);
        //si cest un requestContinuation,propager le request
        if(data.isDirectional() || data.isFlooding()) {
        	RequestContinuation requestCont = new RequestContinuation(request,data);
        	this.propagerQuery(requestCont);
        }
        
		this.logMessage("Calcul Fini: ");
		
		this.logMessage("Resultat du query: " + result);
		this.logMessage("--------------------------------------");

		return result;
	 }
	
	public void propagerQuery(RequestContinuationI request) throws Exception {
		this.logMessage("-----------------Propager Query------------------");
		ExecutionState data = (ExecutionState) request.getExecutionState();
		if(data.isDirectional()) {
		Set<Direction> dirs = data.getDirections_ast();
		  if(!data.noMoreHops()) {
			  for (Direction dir : dirs) {
				  NodeNodeOutboundPort selectedOutboundPort = getOutboundPortByDirection(dir);
				  if(selectedOutboundPort.connected()) {
					  selectedOutboundPort.execute(request);
				  }
				}
		  }
		}else if(data.isFlooding()){
	      for(Direction dir : Direction.values()) {
	    	  NodeNodeOutboundPort selectedOutboundPort = getOutboundPortByDirection(dir);
	    	  if(selectedOutboundPort.connected()) {
				  selectedOutboundPort.execute(request);
			  }
	      }
		} else {
			System.err.println("Erreur type de Cont");
		}
//		this.node_node_port.execute(request);
	}
	
	public QueryResultI processRequestContinuation(RequestContinuationI request) throws Exception{
		this.logMessage("---------------Receive Query Continuation---------------");
		this.logMessage("SensorNodeComponent "+this.nodeinfo.nodeIdentifier()+" : receive request");	
		Interpreter interpreter = new Interpreter();
		Query<?> query = (Query<?>) request.getQueryCode();
		ExecutionState data = (ExecutionState) request.getExecutionState();
        data.updateProcessingNode(this.processingNode);  
        QueryResultI result = (QueryResult) query.eval(interpreter, data);	
        QueryResultI result_All = data.getCurrentResult();
		
		this.logMessage("Calcul Fini Cont: ");
		
		this.logMessage("Res Cont de node actuel: " + result);
		this.logMessage("Res All: " + result_All);
		this.logMessage("------------------------------------");

		return result;
	 }
	
	
	public void sendNodeInfoToRegistre(NodeInfoI nodeInfo) throws Exception {
		     this.logMessage("----------------Register------------------");
		     this.logMessage("SensorNodeComponent sendNodeInfo to Registre " );
		     // 调用注册表组件的注册方法
		     Boolean registed_before = this.node_registre_port.registered(nodeInfo.nodeIdentifier());
		     this.logMessage("Registered before register? " + nodeInfo.nodeIdentifier()+"Boolean:"+registed_before);
		     Set<NodeInfoI> neighbours = this.node_registre_port.register(nodeInfo);
		     for (NodeInfoI neighbour : neighbours) {
		    	 Direction dir = ((Position)this.nodeinfo.nodePosition()).directionTo_ast(neighbour.nodePosition());
		    	 this.neighbours.put(dir, neighbour);
		     }
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
	    //mettre neighbours dans this.neighbours (HashMap) 
		for (NodeInfoI neighbour : actuel_neighbours) {
	    	 Direction dir = ((Position)this.nodeinfo.nodePosition()).directionTo_ast(neighbour.nodePosition());
	    	 this.neighbours.put(dir, neighbour);
	     }
	    this.logMessage("neighbours:");
	     for (Map.Entry<Direction, NodeInfoI> neighbour : this.neighbours.entrySet()) {
	         this.logMessage("neighbour de driection "+neighbour.getKey()+" :"+((NodeInfo)neighbour.getValue()).toString());
	     }
	     this.logMessage("----------------------------------");
	}
	
	public void ask4Connection(NodeInfoI newNeighbour) throws Exception {
	    // check direction de newNeighbour
	    Direction direction = ((Position)this.nodeinfo.nodePosition()).directionTo_ast(newNeighbour.nodePosition());
	    NodeNodeOutboundPort selectedOutboundPort = getOutboundPortByDirection(direction);
	    // 检查当前方向的出站端口是否已经连接
	    if (selectedOutboundPort.connected()) {
	        // si on trouve que cet node est deja connecte au newNeighbour,on fait rien et sort fonction
	        if (!selectedOutboundPort.getConnectedNodePortURI().equals( ((EndPointDescriptor)((NodeInfo)newNeighbour).p2pEndPointInfo()).getURI()  )) {
	            //disconnter d'abord
	        	selectedOutboundPort.ask4Disconnection(newNeighbour);
//	            ask4Disconnection();
	            // connecter
	            connecter(direction, selectedOutboundPort, newNeighbour);
	        }
	    } else {
	        // si pas de connection pour le outport,on fait connecter
	        connecter(direction, selectedOutboundPort, newNeighbour);
	    }
	}

	public void ask4Disconnection(NodeInfoI neighbour) throws Exception {

	    Direction direction = ((Position)this.nodeinfo.nodePosition()).directionTo_ast(neighbour.nodePosition());
	    NodeNodeOutboundPort selectedOutboundPort = getOutboundPortByDirection(direction);

	    if (selectedOutboundPort.connected()) {
	    	this.logMessage(this.nodeinfo.nodeIdentifier()+"essayer de deconnect:"+neighbour.nodeIdentifier());
	        selectedOutboundPort.doDisconnection();
	    }
	}

	private NodeNodeOutboundPort getOutboundPortByDirection(Direction direction) {
	    // choose  correct outport by direction
	    switch (direction) {
	        case NE:
	            return this.node_node_NE_Outport;
	        case NW:
	            return this.node_node_NW_Outport;
	        case SE:
	            return this.node_node_SE_Outport;
	        case SW:
	            return this.node_node_SW_Outport;
	        default:
	            return null; 
	    }
	}
	
	public void connecter(Direction direction,NodeNodeOutboundPort selectedOutboundPort,NodeInfoI newNeighbour) throws Exception {
	    EndPointDescriptor endpointDescriptor = (EndPointDescriptor) newNeighbour.p2pEndPointInfo();
	    if (endpointDescriptor != null) {
	        String neighbourInboundPortURI = endpointDescriptor.getURI();
	        this.logMessage("SensorNodeComponent :"+ this.nodeinfo.nodeIdentifier() +" start connect : Uri obtenue to connect :" + neighbourInboundPortURI);
//	        this.logMessage("Test name class: "+NodeNodeConnector.class.getCanonicalName());
	        selectedOutboundPort.doConnection(neighbourInboundPortURI,NodeNodeConnector.class.getCanonicalName());  
//	        selectedOutboundPort.ask4Connection(this.nodeinfo);
	       this.logMessage("SensorNodeComponent : "+ this.nodeinfo.nodeIdentifier() + ": Connection established with Node :" + newNeighbour.nodeIdentifier());
	    } else {
	        throw new Exception("p2pEndPointInfo() did not return an instance of EndPointDescriptor");
	    }
	}
	
	public void connecterNeighbours() throws Exception {
		this.logMessage("----------------Connecter Neighbours-----------------");
		this.logMessage("SensorNodeComponent [" + this.nodeinfo.nodeIdentifier() + "] start connecter ses neighbours");
	     for (Map.Entry<Direction, NodeInfoI> neighbour : this.neighbours.entrySet()) {
//	    	 this.ask4Connection(neighbour.getKey(),neighbour.getValue());
	    	 Direction direction = neighbour.getKey();
	    	    NodeInfoI neighbourInfo = neighbour.getValue();
	    	    NodeNodeOutboundPort selectedOutboundPort = this.getOutboundPortByDirection(direction);
	    	    if (selectedOutboundPort != null) {
	    	        try {
                     this.connecter(direction,selectedOutboundPort, neighbourInfo);
	    	        } catch (Exception e) {
	    	            System.err.println(this.nodeinfo.nodeIdentifier() + "Failed to connect to neighbour at direction " + direction + ": " + e.getMessage());
	    	        }
	    	    } else {
	    	        System.err.println("No outbound port selected for direction " + direction);
	    	    } 
	     }
	     this.logMessage("----------------------------------------------");
	}

	
}
