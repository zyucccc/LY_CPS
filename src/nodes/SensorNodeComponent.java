package nodes;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import client.ClientComponent;
import client.connectors.ClientAsynRequestConnector;
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
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;
//import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PImplI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import registre.interfaces.RegistrationCI;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import nodes.connectors.NodeClientConnector;
import nodes.connectors.NodeNodeConnector;
import nodes.ports.NodeNodeInboundPort;
import nodes.ports.NodeNodeOutboundPort;
import nodes.ports.SensorNodeAsynRequestOutboundPort;
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
import sensor_network.ConnectionInfo;
import sensor_network.EndPointDescriptor;
import sensor_network.Position;
import sensor_network.QueryResult;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;

@RequiredInterfaces(required = {RegistrationCI.class,SensorNodeP2PCI.class,RequestResultCI.class})
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
	//outbound port for AsynRequest
	protected SensorNodeAsynRequestOutboundPort node_asynRequest_Outport;
	
	
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
            HashMap<String, Sensor> sensorsData,
            String sensorNodeAsyn_OutboundPortURI
            ) throws Exception {
		
		super(uriPrefix, 4, 2) ;
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
        
        //node async request outbound port
        this.node_asynRequest_Outport = new SensorNodeAsynRequestOutboundPort(sensorNodeAsyn_OutboundPortURI,this);
        this.node_asynRequest_Outport.localPublishPort();
		
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
	//mise a jour les donnees de sensors
	protected void startSensorDataUpdateTask() {
	    // mise a jour par 8 secs
	    long delay = 8_000; // milliseconde

	    this.scheduleTaskWithFixedDelay(
	        new AbstractComponent.AbstractTask() {
	            @Override
	            public void run() {
	                // mise a jour temperature
	                String sensorIdentifier = "temperature";
	                // generer new val temperature
	                int newTemperature = generateNewTemperatureValue();
	                //Mettre en commentaire pour tester
	                ((SensorNodeComponent)this.getTaskOwner()).processingNode.updateSensorData_int(sensorIdentifier, (int) newTemperature);
//	                logMessage("Temperature sensor updated with new value: " + newTemperature);
	            }
	        },
	        delay, // init delay
	        delay, // delay entre 2 task
	        TimeUnit.MILLISECONDS);
	}

	protected int generateNewTemperatureValue() {
	    // rand val entre -2 et 2
	    int change = (int) ((Math.random() * 4) - 2);
	    // init val 35
	    return 35 + change;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////
	//Cycle life
	@Override
    public void start() throws ComponentStartException {
		this.logMessage("SensorNodeComponent "+ this.nodeinfo.nodeIdentifier() +" started.");
        super.start();
        //exectuer parallels
        //2 threads
        this.runTask(new AbstractComponent.AbstractTask() {
            @Override
            public void run() {
                try {    
                    ((SensorNodeComponent)this.getTaskOwner()).startSensorDataUpdateTask();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        this.runTask(new AbstractComponent.AbstractTask() {
            @Override
            public void run() {
                try {    
                    ((SensorNodeComponent)this.getTaskOwner()).sendNodeInfoToRegistre(((SensorNodeComponent)this.getTaskOwner()).nodeinfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
	
	@Override
    public void execute() throws Exception {
		this.logMessage("SensorNodeComponent executed.");
        super.execute();
		this.scheduleTask(new AbstractComponent.AbstractTask() {
            @Override
            public void run() {
                try {
                	
//                ((SensorNodeComponent)this.getTaskOwner()).refraichir_neighbours(((SensorNodeComponent)this.getTaskOwner()).nodeinfo);
                ((SensorNodeComponent)this.getTaskOwner()).connecterNeighbours();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 2000, TimeUnit.MILLISECONDS);
        
    }
	
	 @Override
	    public void finalise() throws Exception {
	        this.logMessage("SensorNodeComponent [" + this.nodeinfo.nodeIdentifier() + "] stopping...");
	        if (this.node_node_NE_Outport.connected()) {
	            this.node_node_NE_Outport.doDisconnection();
	        }
	        this.node_node_NE_Outport.unpublishPort();

	        if (this.node_node_NW_Outport.connected()) {
	            this.node_node_NW_Outport.doDisconnection();
	        }
	        this.node_node_NW_Outport.unpublishPort();

	        if (this.node_node_SE_Outport.connected()) {
	            this.node_node_SE_Outport.doDisconnection();
	        }
	        this.node_node_SE_Outport.unpublishPort();

	        if (this.node_node_SW_Outport.connected()) {
	            this.node_node_SW_Outport.doDisconnection();
	        }
	        this.node_node_SW_Outport.unpublishPort();

	        if (this.node_registre_port.connected()) {
	            this.node_registre_port.doDisconnection();
	        }
	        this.node_registre_port.unpublishPort();
	        super.finalise();
	    }
	 
	 @Override
	    public void shutdown() throws ComponentShutdownException {
	        super.shutdown();
	    }
	 
	 //////////////////////////////////////////////////////////////////////////////////////
	 //deal with les request recu par le client
	public QueryResultI processRequest(RequestI request) throws Exception{
		this.logMessage("----------------Receive Query------------------");
		this.logMessage("SensorNodeComponent "+this.nodeinfo.nodeIdentifier()+" : receive request");	
		Interpreter interpreter = new Interpreter();
		Query<?> query = (Query<?>) request.getQueryCode();
		ExecutionState data = new ExecutionState(); 
        data.updateProcessingNode(this.processingNode);
        
        QueryResult result = (QueryResult) query.eval(interpreter, data);
		this.logMessage("----------------Res Actuel----------------------");
		this.logMessage("Resultat du query: " + result);
         
         if(data.isDirectional()||data.isFlooding()) {
        	 RequestContinuation requestCont = new RequestContinuation(request,data);
        	 //pour enregister les nodes deja traite pour ce request
        	 requestCont.addVisitedNode(this.nodeinfo);
        	 this.propagerQuery(requestCont);
         }
        
        QueryResult result_all = (QueryResult) data.getCurrentResult();
        
//		this.logMessage("Calcul Fini: ");
		this.logMessage("------------------Res ALL----------------------");
		this.logMessage("Resultat du query: " + result_all);
		this.logMessage("--------------------------------------");

		return result_all;
	 }
	 //////////////////////////////////////////////////////////////////////////////////////
	 //deal with les request Async recu par le client
	public void processRequest_Async(RequestI request) throws Exception{
		this.logMessage("----------------Receive Query Async------------------");
		this.logMessage("SensorNodeComponent "+this.nodeinfo.nodeIdentifier()+" : receive request Async");	
		Interpreter interpreter = new Interpreter();
		Query<?> query = (Query<?>) request.getQueryCode();
		ExecutionState data = new ExecutionState(); 
        data.updateProcessingNode(this.processingNode);
        
        QueryResult result = (QueryResult) query.eval(interpreter, data);
		this.logMessage("----------------Res Actuel----------------------");
		this.logMessage("Resultat du query: " + result);
         
         if(data.isDirectional()||data.isFlooding()) {
        	 //if cest un request continuation
        	 RequestContinuation requestCont = new RequestContinuation(request,data);
        	 //pour enregister les nodes deja traite pour ce request
        	 requestCont.addVisitedNode(this.nodeinfo);
        	 this.propagerQuery(requestCont);
         }else{
        	 //if cest un request ECont,renvoyer directement le res a Client
        	 ConnectionInfo co_info = (ConnectionInfo) request.clientConnectionInfo();
        	 String InboundPortUri = ((EndPointDescriptor)co_info.endPointInfo()).getURI();
        	 if(this.node_asynRequest_Outport.connected()) {
        		 this.node_asynRequest_Outport.doDisconnection();
        	 }
        	 this.node_asynRequest_Outport.doConnection(InboundPortUri,ClientAsynRequestConnector.class.getCanonicalName());
        	 QueryResult result_all = (QueryResult) data.getCurrentResult();
        	 this.node_asynRequest_Outport.acceptRequestResult(request.requestURI(),result_all);
         }    
	}
	
	//continuer a propager les request continuation
	public void propagerQuery(RequestContinuationI request) throws Exception {
		this.logMessage("-----------------Propager Query------------------");
		ExecutionState data = (ExecutionState) request.getExecutionState();
		//deal with direction
		if(data.isDirectional()) {
		Set<Direction> dirs = data.getDirections_ast();
		//if noMoreHops , on stop propage Request Direction
		  if(!data.noMoreHops()) {
			  for (Direction dir : dirs) {
				  NodeNodeOutboundPort selectedOutboundPort = getOutboundPortByDirection(dir);
				  if(selectedOutboundPort.connected()) {
					  if(!request.isAsynchronous()) {
					   selectedOutboundPort.execute(request);}
					  else {
						  //if async,on fait un copie du request
						  RequestContinuation copie_request = (RequestContinuation) request;
						  selectedOutboundPort.executeAsync(copie_request);
					  }
				  }
				}
		  }
		}else 
			//deal with fooding
			if(data.isFlooding()){
				//propager flooding query a tous les directions
	      for(Direction dir : Direction.values()) {
	    	  
	    	  NodeNodeOutboundPort selectedOutboundPort = getOutboundPortByDirection(dir); 
	    	  if(selectedOutboundPort.connected()) {	
	    		  if(!request.isAsynchronous()) {
	    		  this.logMessage(this.nodeinfo.nodeIdentifier()+" Sending request flooding dir :"+dir);
	    		   selectedOutboundPort.execute(request);
	    		  }else {
		    		  this.logMessage(this.nodeinfo.nodeIdentifier()+" Sending request Async flooding dir :"+dir);
		    		  //if async,on fait un copie du request
					  RequestContinuation copie_request = (RequestContinuation) request;
		    		   selectedOutboundPort.executeAsync(copie_request);
	    		  }
			  }
	      }
		} else {
			System.err.println("Erreur type de Cont");
		}
	}
	
	//deal with les request continuation recu par les nodes
	public QueryResultI processRequestContinuation(RequestContinuationI requestCont) throws Exception{
		//si cette node actuel est deja traite par cette request recu,on ignorer et return direct
		//pour eviter le Probleme: deadlock caused by Call_back
		//ex: node 1 send request flooding to node2,node2 send encore request to node1
		if (((RequestContinuation)requestCont).getVisitedNodes().contains(this.nodeinfo)) {
            return null;
        }
		((RequestContinuation) requestCont).addVisitedNode(this.nodeinfo);
		Interpreter interpreter = new Interpreter();
		ExecutionState data = (ExecutionState) requestCont.getExecutionState();
		//chaque fois on recevoit un request de flooding
		//on check si ce node est dans max_distance de propager ce request
		//si oui ,on continue a collecter les infos de node actuel
		//si non,on return le res precedent
		if(data.isFlooding()) {
			this.logMessage(this.nodeinfo.nodeIdentifier()+" receive flooding request");
			Position actuel_position = (Position) this.nodeinfo.nodePosition();
			if(!data.withinMaximalDistance(actuel_position)) {
				this.logMessage("Hors distance");
				return data.getCurrentResult();
			}
//			this.logMessage(this.nodeinfo.nodeIdentifier()+"passe test 2");
		}
		 if(data.isDirectional()){
				data.incrementHops();
			}
		 
		this.logMessage("---------------Receive Query Continuation---------------");
		this.logMessage("SensorNodeComponent "+this.nodeinfo.nodeIdentifier()+" : receive request");	
		Query<?> query = (Query<?>) requestCont.getQueryCode();
        data.updateProcessingNode(this.processingNode);  
        QueryResultI result = (QueryResult) query.eval(interpreter, data);	
		this.logMessage("----------------Res Actuel----------------------");
		this.logMessage("Res Cont de node actuel: " + result);
//        QueryResultI result_All = data.getCurrentResult();
       
        
		this.propagerQuery(requestCont);     
		
//		this.logMessage("Calcul Fini Cont: ");
//		this.logMessage("------------------Res ALL----------------------");
//		this.logMessage("Res All: " + result_All);
		this.logMessage("------------------------------------");

		return result;
	 }
	
	//process Request Continuation Async 
	public void processRequestContinuation_Asyn(RequestContinuationI requestCont) throws Exception{
		//si cette node actuel est deja traite par cette request recu,on ignorer et return direct
		//pour eviter le Probleme: deadlock caused by Call_back
		//ex: node 1 send request flooding to node2,node2 send encore request to node1
		if (!((RequestContinuation)requestCont).getVisitedNodes().contains(this.nodeinfo)) {
			
      
		((RequestContinuation) requestCont).addVisitedNode(this.nodeinfo);
		Interpreter interpreter = new Interpreter();
		ExecutionState data = (ExecutionState) requestCont.getExecutionState();
		//chaque fois on recevoit un request de flooding
		//on check si ce node est dans max_distance de propager ce request
		//si oui ,on continue a collecter les infos de node actuel
		//si non,on return le res precedent
		if(data.isFlooding()) {
			this.logMessage(this.nodeinfo.nodeIdentifier()+" receive flooding request asyn");
			Position actuel_position = (Position) this.nodeinfo.nodePosition();
			if(!data.withinMaximalDistance(actuel_position)) {
				this.logMessage("Hors distance");
				//connecter Client et renvoyer les res async
 				//renvoyerAsyncRes(requestCont);
				return;
			}
		}
		 if(data.isDirectional()){
				data.incrementHops();
			}
		 
		this.logMessage("---------------Receive Query Continuation Async---------------");
		this.logMessage("SensorNodeComponent "+this.nodeinfo.nodeIdentifier()+" : receive request async");	
		Query<?> query = (Query<?>) requestCont.getQueryCode();
        data.updateProcessingNode(this.processingNode);  
        
     
		this.logMessage("----------------Res Actuel----------------------");
		QueryResultI result = (QueryResult) query.eval(interpreter, data);	
		this.logMessage("Res Cont de node actuel: " + result);
        
		//traiter la fin du request:
		if(data.isDirectional()){
			if(data.noMoreHops()) {
				this.logMessage(this.nodeinfo.nodeIdentifier() + " envoyer Res du request Async Direction: "+requestCont.getExecutionState().getCurrentResult() );
				renvoyerAsyncRes(requestCont);
			}
		}else if(data.isFlooding()) {
			if(checkNeighboursDansPortee(requestCont)) {
				this.logMessage(this.nodeinfo.nodeIdentifier() + " envoyer Res du request Async Flooding: "+requestCont.getExecutionState().getCurrentResult() );
				renvoyerAsyncRes(requestCont);
			}
		}
		
		this.propagerQuery(requestCont);     
		this.logMessage("------------------------------------");
		
		
		}
	 }
	public Boolean checkNeighboursDansPortee (RequestContinuationI requestCont) {
		// verifier si tous les neighbours ne sont pas dans le portee de request flooding (sauf les neighbous deja visite)
	    for (Map.Entry<Direction, NodeInfoI> entry : neighbours.entrySet()) {
	        NodeInfoI neighbour = entry.getValue();
	        //si ce neighbours est deja visite
	        if (((RequestContinuation) requestCont).getVisitedNodes().contains(neighbour)) {
	            continue;
	        }
	        // check les autres neighbours
	        ExecutionState data = (ExecutionState) requestCont.getExecutionState();
	        if (data.withinMaximalDistance(neighbour.nodePosition())) {
	            // si au moins 1 neighbours non visite est dans portee,return false
	            return false;
	        }
	    }
	    //tous les neighbours non visitees est dehors le portee:
	    return true;
	}
	
	//deleguer les operations de connecter Client via Port Async et appeler acceptRequest pour renvoyer res
	public void renvoyerAsyncRes (RequestContinuationI request) throws Exception {
		 ConnectionInfo co_info = (ConnectionInfo) request.clientConnectionInfo();
    	 String InboundPortUri = ((EndPointDescriptor)co_info.endPointInfo()).getURI();
    	 if(this.node_asynRequest_Outport.connected()) {
    		 this.node_asynRequest_Outport.doDisconnection();
    	 }
    	 this.node_asynRequest_Outport.doConnection(InboundPortUri,ClientAsynRequestConnector.class.getCanonicalName());
    	 ExecutionState data = (ExecutionState) request.getExecutionState();
    	 QueryResult result_all = (QueryResult) data.getCurrentResult();
    	 this.node_asynRequest_Outport.acceptRequestResult(request.requestURI(),result_all);
	}
	
	public void sendNodeInfoToRegistre(NodeInfoI nodeInfo) throws Exception {
		     this.logMessage("----------------Register------------------");
		     this.logMessage("SensorNodeComponent sendNodeInfo to Registre " );

		     Boolean registed_before = this.node_registre_port.registered(nodeInfo.nodeIdentifier());
		     this.logMessage("Registered before register? " + nodeInfo.nodeIdentifier()+"Boolean:"+registed_before);
		     Set<NodeInfoI> neighbours = this.node_registre_port.register(nodeInfo);
		     
		     for (NodeInfoI neighbour : neighbours) {
		    	 Direction dir = ((Position)this.nodeinfo.nodePosition()).directionTo_ast(neighbour.nodePosition());
		    	 this.neighbours.put(dir, neighbour);
		     }
		     this.logMessage("--------------Register() neighbours:--------------");
		     this.logMessage("neighbours:");
		     for (Map.Entry<Direction, NodeInfoI> neighbour : this.neighbours.entrySet()) {
		         this.logMessage("neighbour de driection "+neighbour.getKey()+" :"+((NodeInfo)neighbour.getValue()).toString());
		     }
		     
		     Boolean registed_after = this.node_registre_port.registered(nodeInfo.nodeIdentifier());
		     this.logMessage("Registered after register? " + nodeInfo.nodeIdentifier()+"Boolean:"+registed_after);
		     this.logMessage("----------------------------------------");

	}
	
	//apres que tous les nodes se register dans Registre
	//on regraichir pour obetnir les infos de neighbours
	//car au debut,les infos de neighbours retourne par register() sont incomplete
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
	     this.logMessage("------------------------------------");
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	// fonctions Pour connection entre Nodes
	public void ask4Connection(NodeInfoI newNeighbour) throws Exception {
		this.logMessage(this.nodeinfo.nodeIdentifier()+" receive ask4Connection from "+newNeighbour.nodeIdentifier());
	    // check direction de newNeighbour
		System.out.println("TestPosition actuel: "+this.nodeinfo.nodePosition() );
		System.out.println("TestPosition neighbours: "+newNeighbour.nodePosition() );
	    Direction direction = ((Position)this.nodeinfo.nodePosition()).directionTo_ast(newNeighbour.nodePosition());
	    NodeNodeOutboundPort selectedOutboundPort = getOutboundPortByDirection(direction);

	    // check if deja connected
	    System.out.println("Test1 ");
	    if (selectedOutboundPort.connected()) {
	    	System.out.println("Test2 ");
	        // si on trouve que cet node est deja connecte au newNeighbour,on fait rien et sort fonction
	            //disconnter d'abord
	    	    selectedOutboundPort.doDisconnection();
	            // connecter
	        	System.out.println("Test3 ");
	            this.connecter(direction, selectedOutboundPort, newNeighbour);
	    } else {
	        // si pas de connection pour le outport,on fait connecter
	        connecter(direction, selectedOutboundPort, newNeighbour);
	    }
	}

	public void ask4Disconnection(NodeInfoI neighbour) throws Exception {

	    Direction direction = ((Position)this.nodeinfo.nodePosition()).directionTo_ast(neighbour.nodePosition());
	    NodeNodeOutboundPort selectedOutboundPort = getOutboundPortByDirection(direction);

	    if (selectedOutboundPort.connected()) {
	    	this.logMessage(this.nodeinfo.nodeIdentifier()+"essayer de disconnect:"+neighbour.nodeIdentifier());
	        selectedOutboundPort.doDisconnection();
	    }
	}

	//retourne les outport correspondant selon le direction donnee
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
	
	//deleguer les operation de connecter un outport choisi a un neighbour
	public void connecter(Direction direction,NodeNodeOutboundPort selectedOutboundPort,NodeInfoI newNeighbour) throws Exception {
	    EndPointDescriptor endpointDescriptor = (EndPointDescriptor) newNeighbour.p2pEndPointInfo();
	    if (endpointDescriptor != null) {
	        String neighbourInboundPortURI = endpointDescriptor.getURI();
	        this.logMessage("SensorNodeComponent :"+ this.nodeinfo.nodeIdentifier() +" start connect : Uri obtenue to connect :" + neighbourInboundPortURI);

	        selectedOutboundPort.doConnection(neighbourInboundPortURI,NodeNodeConnector.class.getCanonicalName());  

	       this.logMessage("SensorNodeComponent : "+ this.nodeinfo.nodeIdentifier() + ": Connection established with Node :" + newNeighbour.nodeIdentifier());
	    } else {
	        throw new Exception("p2pEndPointInfo() did not return an instance of EndPointDescriptor");
	    }
	}
	
	public void connecterNeighbours() throws Exception {
		this.logMessage("----------------Connecter Neighbours-----------------");
		this.logMessage("SensorNodeComponent [" + this.nodeinfo.nodeIdentifier() + "] start connecter ses neighbours");
	     for (Map.Entry<Direction, NodeInfoI> neighbour : this.neighbours.entrySet()) {

	    	    Direction direction = neighbour.getKey();
	    	    NodeInfoI neighbourInfo = neighbour.getValue();
	    	    NodeNodeOutboundPort selectedOutboundPort = this.getOutboundPortByDirection(direction);
	    	    if (selectedOutboundPort != null) {
	    	        try {
	    	        	nodeinfo = this.nodeinfo;
	    	           if(selectedOutboundPort.connected()) {
	    	        	   selectedOutboundPort.doDisconnection();
	    	           }
                       this.connecter(direction,selectedOutboundPort, neighbourInfo);
	    	           selectedOutboundPort.ask4Connection(nodeinfo);
	    	        } catch (Exception e) {
	    	            System.err.println("connecterNeighbours: "+this.nodeinfo.nodeIdentifier() + " Failed to connect to neighbour at direction " + direction + ": " + e.getMessage());
	    	        }
	    	    } else {
	    	        System.err.println("No outbound port selected for direction " + direction);
	    	    } 
	     }
	     this.logMessage("----------------------------------------------");
	}

	
}
