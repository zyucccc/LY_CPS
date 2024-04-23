package nodes;


import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import client.ClientComponent;
import client.connectors.ClientAsynRequestConnector;
import client.ports.ClientRegistreOutboundPort;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.examples.basic_cs.interfaces.URIProviderCI;
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
import fr.sorbonne_u.utils.aclocks.*;
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

@RequiredInterfaces(required = {RegistrationCI.class,SensorNodeP2PCI.class,RequestResultCI.class,ClocksServerCI.class})
@OfferedInterfaces(offered = {RequestingCI.class,SensorNodeP2PCI.class})
public class SensorNodeComponent extends AbstractComponent {
	protected AcceleratedClock ac;
	protected NodeInfo nodeinfo;
	protected ProcessingNode processingNode;
	//inbound port for client et node2node
	protected SensorNodeInboundPort InboundPort_toClient;
	protected NodeNodeInboundPort InboundPort_P2PtoNode;
	//outbound port for Registre
	protected SensorNodeRegistreOutboundPort node_registre_port;
	//outbound port for Node2Node
	protected NodeNodeOutboundPort node_node_NE_Outport;
	protected NodeNodeOutboundPort node_node_NW_Outport;
	protected NodeNodeOutboundPort node_node_SE_Outport;
	protected NodeNodeOutboundPort node_node_SW_Outport;
	//outbound port for AsynRequest
	protected SensorNodeAsynRequestOutboundPort node_asynRequest_Outport;

	//pool thread pour traiter les requetes async provenant des nodes
	protected int index_poolthread_receiveAsync;
	protected String uri_pool_receiveAsync = "-pool-thread-receiveAsync";
	protected int nbThreads_poolReceiveAsync = 10;

	//pool thread pour traiter les requetes async provenant du client
	protected int index_poolthread_receiveAsync_Client;
	protected String uri_pool_receiveAsync_Client = "-pool-thread-receiveAsync-Client";
	protected int nbThreads_poolReceiveAsync_Client = 10;

	//pool thread pour traiter les requetes de connection(askConnection,askDisconnection
	protected int index_poolthread_Receiveconnection;
	protected String uri_pool_Receiveconnection = "-pool-thread-Receiveconnection";
	protected int nbThreads_Receiveconnection = 4;
	//gestion Concurrence
	//proteger les donnees de sensors
	protected final ReentrantReadWriteLock sensorData_lock = new ReentrantReadWriteLock();
	//proteger neighbours avec OutBoundPorts
	protected final ReentrantLock neighbours_OutPorts_lock = new ReentrantLock();

	//on utilise les pools de threads par defaut pour traiter les fonctions register,connecter
	//on introduit 2 pools de threads distincts pour traiter les requetes recus
	
	protected ConcurrentHashMap<Direction,NodeInfoI> neighbours;
	
	protected static void	checkInvariant(SensorNodeComponent c)
	{
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
            String sensorNodeAsyn_OutboundPortURI,
			String CLOCK_URI
            ) throws Exception {
		
		super(uriPrefix, 7, 7) ;
		assert nodeInfo != null : "NodeInfo cannot be null!";
		//inboudporturi for client
	    assert sensorNodeInboundPortURI != null && !sensorNodeInboundPortURI.isEmpty() : "InboundPort URI cannot be null or empty!";
	    //inboudporturi for node2node
	    assert node_node_InboundPortURI != null && !node_node_InboundPortURI.isEmpty() : "InboundPortN2 URI cannot be null or empty!";

		// ---------------------------------------------------------------------
		// configuration clock
		// ---------------------------------------------------------------------
		ClocksServerOutboundPort p_clock = new ClocksServerOutboundPort(this);
		p_clock.publishPort();
		this.doPortConnection(
				p_clock.getPortURI(),
				ClocksServer.STANDARD_INBOUNDPORT_URI,
				ClocksServerConnector.class.getCanonicalName());
		this.ac = p_clock.getClock(CLOCK_URI);
		this.doPortDisconnection(p_clock.getPortURI());
		p_clock.unpublishPort();
		p_clock.destroyPort();
		if(this.ac.startTimeNotReached()) {
			this.ac.waitUntilStart();
		}

		// ---------------------------------------------------------------------
		// Configuration des pools de threads
		// ---------------------------------------------------------------------
		uri_pool_receiveAsync = uriPrefix+uri_pool_receiveAsync;
		uri_pool_receiveAsync_Client = uriPrefix+uri_pool_receiveAsync_Client;
//		uri_pool_receiveSync = uriPrefix+uri_pool_receiveSync;
		uri_pool_Receiveconnection = uriPrefix+uri_pool_Receiveconnection;
		this.index_poolthread_receiveAsync = this.createNewExecutorService(this.uri_pool_receiveAsync, this.nbThreads_poolReceiveAsync,false);
		this.index_poolthread_receiveAsync_Client = this.createNewExecutorService(this.uri_pool_receiveAsync_Client, this.nbThreads_poolReceiveAsync_Client,false);
//        this.index_poolthread_receiveSync = this.createNewExecutorService(this.uri_pool_receiveSync, this.nbThreads_poolReceiveSync,false);
        this.index_poolthread_Receiveconnection = this.createNewExecutorService(this.uri_pool_Receiveconnection, this.nbThreads_Receiveconnection,false);

		this.nodeinfo = (NodeInfo) nodeInfo;
	    
	    String NodeID = this.nodeinfo.nodeIdentifier();
		Position position = (Position) this.nodeinfo.nodePosition();
		this.neighbours = new ConcurrentHashMap<>();
		
		this.processingNode = new ProcessingNode(NodeID,position,sensorsData);
		
		//Inbound Port publish
		//node - client - requestingI
	    //lier URI
//        PortI p = new SensorNodeInboundPort(sensorNodeInboundPortURI, this);
//		p.publishPort();
		this.InboundPort_toClient = new SensorNodeInboundPort(sensorNodeInboundPortURI,this);
		this.InboundPort_toClient.publishPort();
        //node - node - SensorNodeP2PCI
//        PortI p2p=new NodeNodeInboundPort(node_node_InboundPortURI,this);
//        p2p.publishPort();
		this.InboundPort_P2PtoNode = new NodeNodeInboundPort(node_node_InboundPortURI,this);
		this.InboundPort_P2PtoNode.publishPort();
		
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

	// ---------------------------------------------------------------------
	// Partie getters pour les pools threads
	// ---------------------------------------------------------------------
	public int getIndex_poolthread_receiveAsync() {
		return index_poolthread_receiveAsync;
	}

	public int getIndex_poolthread_receiveAsync_Client(){
		return index_poolthread_receiveAsync_Client;
	}

	public int getIndex_poolthread_Receiveconnection() {
		return index_poolthread_Receiveconnection;
	}

	// ---------------------------------------------------------------------
	// Partie mise a jour les donnees de sensors
	// ---------------------------------------------------------------------
	protected void startSensorDataUpdateTask() {
	    // mise a jour par secs
	    long delay = 2_000; // milliseconde

	    this.scheduleTaskWithFixedDelay(
	        new AbstractComponent.AbstractTask() {
	            @Override
	            public void run() {
	                // mise a jour temperature
	                String sensorIdentifier = "temperature";
	                // generer new val temperature
	                double newTemperature = ((SensorNodeComponent)this.getTaskOwner()).generateNewTemperatureValue();
	                //Mettre en commentaire pour tester
					try {
						((SensorNodeComponent) this.getTaskOwner()).updateSensorData(sensorIdentifier, newTemperature);
					}catch (Exception e){
						System.err.println("Exception in updateSensorData scheduleTask: " + e.getMessage());
						e.printStackTrace();
					}
	            }
	        },
	        1L, // init delay
	        delay, // delay entre 2 task
	        TimeUnit.MILLISECONDS);

	}

	protected void updateSensorData(String sensorIdentifier, double newValue) {
		this.sensorData_lock.writeLock().lock();
		try {
			Sensor oldSensor = (Sensor) this.processingNode.getSensorData(sensorIdentifier);
			if (oldSensor != null) {
				Sensor newSensor = new Sensor(oldSensor.getNodeIdentifier(),
						oldSensor.getSensorIdentifier(),
						oldSensor.getType(),
						newValue);
				this.processingNode.addSensorData(sensorIdentifier, newSensor);
			}
		}catch (Exception e){
			System.err.println("Exception in updateSensorData: " + e.getMessage());
			e.printStackTrace();
		}finally {
			this.sensorData_lock.writeLock().unlock();
		}
	}

	protected double generateNewTemperatureValue() {
		sensorData_lock.readLock().lock();
		double temperature = 35.0;
		try {
			Class<? extends Serializable> type = this.processingNode.getSensorData("temperature").getType();
			if(type == double.class) {
				temperature = (double) this.processingNode.getSensorData("temperature").getValue();
			} else if (type == Integer.class) {
				temperature =  ((Integer)this.processingNode.getSensorData("temperature").getValue()).doubleValue();;
			} else {
				System.err.println("Type de temperature non supporté"+type);
			}
		}catch (Exception e) {
			System.err.println("Exception in generateNewTemperatureValue: " + e.getMessage());
			e.printStackTrace();
		}finally {
			sensorData_lock.readLock().unlock();
		}

		// rand val entre -2 et 2
		Random rand = new Random();
	    double change =  rand.nextInt(4) - 2;
	    // init val 35
	    return temperature + change;
	}

	// ---------------------------------------------------------------------
	// Cycle de Vie
	// ---------------------------------------------------------------------
	@Override
    public void start() throws ComponentStartException {
		this.logMessage("SensorNodeComponent "+ this.nodeinfo.nodeIdentifier() +" started.");
        super.start();
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
    }
	
	@Override
    public void execute() throws Exception {
		this.logMessage("SensorNodeComponent executed.");
		super.execute();
		Instant start_instant = this.ac.getStartInstant();
		Instant instant_register = start_instant.plusSeconds(1);
		long delay_register = 1L;
		if(instant_register.isAfter(this.ac.currentInstant())) {
			delay_register = this.ac.nanoDelayUntilInstant(instant_register);
		}

		//instant1:register+connecter
		this.scheduleTask(new AbstractComponent.AbstractTask() {
			@Override
			public void run() {
				try {
					((SensorNodeComponent)this.getTaskOwner()).sendNodeInfoToRegistre(((SensorNodeComponent)this.getTaskOwner()).nodeinfo);
					((SensorNodeComponent)this.getTaskOwner()).connecterNeighbours();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 1L, TimeUnit.NANOSECONDS);
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

			if(this.node_asynRequest_Outport.connected()) {
				this.node_asynRequest_Outport.doDisconnection();
			}
			this.node_asynRequest_Outport.unpublishPort();

			if(this.InboundPort_P2PtoNode.connected()){
				this.InboundPort_P2PtoNode.doDisconnection();
			}
            this.InboundPort_P2PtoNode.unpublishPort();

			if(this.InboundPort_toClient.connected()){
				this.InboundPort_toClient.doDisconnection();
			}
			this.InboundPort_toClient.unpublishPort();
		    super.finalise();
	    }
	 
	 @Override
	    public void shutdown() throws ComponentShutdownException {
		 try {
			 //fermer les pools des threads explicitement
			 this.shutdownExecutorService(this.uri_pool_receiveAsync);
             this.shutdownExecutorService(this.uri_pool_receiveAsync_Client);
			 this.shutdownExecutorService(this.uri_pool_Receiveconnection);

		 } catch (Exception e) {
			 throw new ComponentShutdownException(e);
		 }
		 super.shutdown();
	    }

	// ---------------------------------------------------------------------
	// Traiter les requetes from Client
	// ---------------------------------------------------------------------
	public QueryResultI processRequest(RequestI request) throws Exception{
		this.logMessage("----------------Receive Query Sync------------------");
		this.logMessage("SensorNodeComponent "+this.nodeinfo.nodeIdentifier()+" : receive request Sync");
		Interpreter interpreter = new Interpreter();
		Query<?> query = (Query<?>) request.getQueryCode();
		ExecutionState data = new ExecutionState(); 
        data.updateProcessingNode(this.processingNode);

		this.sensorData_lock.readLock().lock();
        QueryResult result = (QueryResult) query.eval(interpreter, data);
		this.sensorData_lock.readLock().unlock();

		this.logMessage("----------------Res Actuel----------------------");
		this.logMessage("Resultat du requete Sync: " + result);
         
         if(data.isDirectional()||data.isFlooding()) {
        	 RequestContinuation requestCont = new RequestContinuation(request,data);
        	 //pour enregister les nodes deja traite pour ce request
        	 requestCont.addVisitedNode(this.nodeinfo);
        	 this.propagerQuery(requestCont);
         }
        
        QueryResult result_all = (QueryResult) data.getCurrentResult();

		this.logMessage("------------------Res ALL----------------------");
		this.logMessage("Resultat du requete Sync: " + result_all);
		this.logMessage("--------------------------------------");
		return result_all;
	 }
	// ---------------------------------------------------------------------
	// Traiter les requetes Async from Client
	// ---------------------------------------------------------------------
	public void processRequest_Async(RequestI request) throws Exception{

		this.logMessage("----------------Receive Query Async------------------");
		this.logMessage("SensorNodeComponent "+this.nodeinfo.nodeIdentifier()+" : receive request Async");	
		Interpreter interpreter = new Interpreter();
		Query<?> query = (Query<?>) request.getQueryCode();
		ExecutionState data = new ExecutionState(); 
        data.updateProcessingNode(this.processingNode);

		this.sensorData_lock.readLock().lock();
        QueryResult result = (QueryResult) query.eval(interpreter, data);
		this.sensorData_lock.readLock().unlock();

		this.logMessage("----------------Resultat Async Actuel----------------------");
		this.logMessage("Resultat du quete Async: " + result);
         
         if(data.isDirectional()||data.isFlooding()) {
        	 //if cest un request continuation
        	 RequestContinuation requestCont = new RequestContinuation(request,data);
        	 //pour enregister les nodes deja traite pour ce request
        	 requestCont.addVisitedNode(this.nodeinfo);
			 //traiter la fin du request:
			 if(data.isDirectional()){
				 //si no more hops ou pas de neighbours pour la direction demandé,on renvoie le resultat au client
				 if(data.noMoreHops()) {
					 this.logMessage("Requete directionnelle fini "+requestCont.requestURI()+" : No more hops");
					 this.logMessage(this.nodeinfo.nodeIdentifier() + " envoyer Res du request Async Direction: "+requestCont.getExecutionState().getCurrentResult() );
					 renvoyerAsyncRes(requestCont);
					 return;
				 } else if (checkNeighboursDansDirection(requestCont)) {
					 this.logMessage("Requete directionnelle fini "+requestCont.requestURI()+" : No neighbours dans la direction");
					 this.logMessage(this.nodeinfo.nodeIdentifier() + " envoyer Res du request Async Direction: "+requestCont.getExecutionState().getCurrentResult() );
					 renvoyerAsyncRes(requestCont);
					 return;
				 }
			 }else if(data.isFlooding()) {
				 // verifier si tous les neighbours ne sont pas dans le portee de request flooding (sauf les neighbous deja visite)
				 //si cest le cas,renvoyer le res actuel au client
				 if(checkNeighboursDansPortee(requestCont)) {
					 this.logMessage("Requete flooding fini "+requestCont.requestURI()+" : No neighbours dans le portee");
					 this.logMessage(this.nodeinfo.nodeIdentifier() + " envoyer Res du request Async Flooding: "+requestCont.getExecutionState().getCurrentResult() );
					 renvoyerAsyncRes(requestCont);
					 return;
				 }
			 }
        	 this.propagerQuery(requestCont);
         }else{
        	 //if cest un request ECont,renvoyer directement le res a Client
        	 ConnectionInfo co_info = (ConnectionInfo) request.clientConnectionInfo();
        	 String InboundPortUri = ((EndPointDescriptor)co_info.endPointInfo()).getInboundPortURI();
        	 if(this.node_asynRequest_Outport.connected()) {
        		 this.node_asynRequest_Outport.doDisconnection();
        	 }
        	 this.node_asynRequest_Outport.doConnection(InboundPortUri,ClientAsynRequestConnector.class.getCanonicalName());
        	 QueryResult result_all = (QueryResult) data.getCurrentResult();
        	 this.node_asynRequest_Outport.acceptRequestResult(request.requestURI(),result_all);
         }    
	}

	// ---------------------------------------------------------------------
	// continuer a propager les request continuation
	// ---------------------------------------------------------------------
	public void propagerQuery(RequestContinuationI request) throws Exception {
		this.logMessage("-----------------Propager Query------------------");
		ExecutionState data = (ExecutionState) request.getExecutionState();
		//deal with direction
		if(data.isDirectional()) {
		Set<Direction> dirs = data.getDirections_ast();
		//if noMoreHops , on stop propager Request Direction
		  if(!data.noMoreHops()) {
			  for (Direction dir : dirs) {
				  NodeNodeOutboundPort selectedOutboundPort = getOutboundPortByDirection(dir);
				  if(selectedOutboundPort.connected()) {
					  if(!request.isAsynchronous()) {
					   selectedOutboundPort.execute(request);}
					  else {
						  //if async,on fait un copie du request
						  RequestContinuation copie_request = new RequestContinuation((RequestContinuation) request);
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
	    		  this.logMessage(this.nodeinfo.nodeIdentifier()+" Sending request Sync flooding dir :"+dir);
	    		   selectedOutboundPort.execute(request);
	    		  }else {
		    		  this.logMessage(this.nodeinfo.nodeIdentifier()+" Sending request Async flooding dir :"+dir);
		    		  //if async,on fait un copie du request
					  RequestContinuation copie_request = new RequestContinuation((RequestContinuation) request);
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
			this.logMessage(this.nodeinfo.nodeIdentifier()+" receive flooding request Sync");
			Position actuel_position = (Position) this.nodeinfo.nodePosition();
			if(!data.withinMaximalDistance(actuel_position)) {
//				this.logMessage("Hors distance");
				return data.getCurrentResult();
			}
		}
		 if(data.isDirectional()){
				data.incrementHops();
			}
		 
		this.logMessage("---------------Receive Query Continuation Sync---------------");
		this.logMessage("SensorNodeComponent "+this.nodeinfo.nodeIdentifier()+" : receive request sync");
		Query<?> query = (Query<?>) requestCont.getQueryCode();
        data.updateProcessingNode(this.processingNode);

		this.sensorData_lock.readLock().lock();
		QueryResultI result;
		try {
			result = (QueryResult) query.eval(interpreter, data);
		}finally {
			this.sensorData_lock.readLock().unlock();
		}

		this.logMessage("----------------Resultat Actuel Sync----------------------");
		this.logMessage("Resultat Continuational de node actuel: " + result);
        
		this.propagerQuery(requestCont);
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
			this.logMessage(this.nodeinfo.nodeIdentifier()+" receive flooding request Async");
			Position actuel_position = (Position) this.nodeinfo.nodePosition();
			if(!data.withinMaximalDistance(actuel_position)) {
//				this.logMessage("Hors distance");
				return;
			}
		}
		 if(data.isDirectional()){
				data.incrementHops();
			}
		 
		this.logMessage("---------------Receive Query Continuation Async---------------");
		this.logMessage("SensorNodeComponent "+this.nodeinfo.nodeIdentifier()+" : receive request Async");
		Query<?> query = (Query<?>) requestCont.getQueryCode();
        data.updateProcessingNode(this.processingNode);  
        
     
		this.logMessage("----------------Res Actuel Async----------------------");

		this.sensorData_lock.readLock().lock();
		QueryResultI result;
		try {
			result = (QueryResult) query.eval(interpreter, data);
		}finally {
			this.sensorData_lock.readLock().unlock();
		}

		this.logMessage("Res Cont de node actuel: " + result);
        
		//traiter la fin du request:
		if(data.isDirectional()){
			//si no more hops ou pas de neighbours pour la direction demandé,on renvoie le resultat au client
			if(data.noMoreHops()) {
                this.logMessage("Requete directionnelle fini "+requestCont.requestURI()+" : No more hops");
				this.logMessage(this.nodeinfo.nodeIdentifier() + " envoyer Res du request Async Direction: "+requestCont.getExecutionState().getCurrentResult() );
				renvoyerAsyncRes(requestCont);
				return;
			} else if (checkNeighboursDansDirection(requestCont)) {
				this.logMessage("Requete directionnelle fini "+requestCont.requestURI()+" : No neighbours dans la direction");
				this.logMessage(this.nodeinfo.nodeIdentifier() + " envoyer Res du request Async Direction: "+requestCont.getExecutionState().getCurrentResult() );
				renvoyerAsyncRes(requestCont);
				return;
			}
		}else if(data.isFlooding()) {
			// verifier si tous les neighbours ne sont pas dans le portee de request flooding (sauf les neighbous deja visite)
			//si cest le cas,renvoyer le res actuel au client
			if(checkNeighboursDansPortee(requestCont)) {
				this.logMessage("Requete flooding fini "+requestCont.requestURI()+" : No neighbours dans le portee");
				this.logMessage(this.nodeinfo.nodeIdentifier() + " envoyer Res du request Async Flooding: "+requestCont.getExecutionState().getCurrentResult() );
				renvoyerAsyncRes(requestCont);
				return;
			}
		}
		
		this.propagerQuery(requestCont);     
		this.logMessage("------------------------------------");
		
		
		}
	 }
	 //verifier si il y a pas de neighbours dans la direction de request Directional
	public Boolean checkNeighboursDansDirection (RequestContinuationI requestCont) {
	    ExecutionState data = (ExecutionState) requestCont.getExecutionState();
	    Set<Direction> dirs = data.getDirections_ast();
	    for (Direction dir : dirs) {
	        NodeInfoI neighbour = neighbours.get(dir);
	        if (neighbour != null) {
	            return false;
	        }
	    }
	    return true;
	}

	 // verifier si tous les neighbours ne sont pas dans le portee de request flooding (sauf les neighbous deja visite)
	public Boolean checkNeighboursDansPortee (RequestContinuationI requestCont) {
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
    	 String InboundPortUri = ((EndPointDescriptor)co_info.endPointInfo()).getInboundPortURI();
    	 if(this.node_asynRequest_Outport.connected()) {
			 this.doPortDisconnection(this.node_asynRequest_Outport.getPortURI());
    	 }
		 this.doPortConnection(this.node_asynRequest_Outport.getPortURI(),InboundPortUri,ClientAsynRequestConnector.class.getCanonicalName());
    	 ExecutionState data = (ExecutionState) request.getExecutionState();
    	 QueryResult result_all = (QueryResult) data.getCurrentResult();
    	 this.node_asynRequest_Outport.acceptRequestResult(request.requestURI(),result_all);
	}
	
	public void sendNodeInfoToRegistre(NodeInfoI nodeInfo) throws Exception {
		     this.logMessage("----------------Register------------------");
		     this.logMessage("SensorNodeComponent sendNodeInfo to Registre " );

//		     Boolean registed_before = this.node_registre_port.registered(nodeInfo.nodeIdentifier());
//		     this.logMessage("Registered before register? " + nodeInfo.nodeIdentifier()+"Boolean:"+registed_before);
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
		     
//		     Boolean registed_after = this.node_registre_port.registered(nodeInfo.nodeIdentifier());
//		     this.logMessage("Registered after register? " + nodeInfo.nodeIdentifier()+"Boolean:"+registed_after);
		     this.logMessage("----------------------------------------");

	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////
	// fonctions Pour connection entre Nodes
	public void ask4Connection(NodeInfoI newNeighbour) throws Exception {
		this.logMessage(this.nodeinfo.nodeIdentifier()+" receive ask4Connection from "+newNeighbour.nodeIdentifier());
		// check direction de newNeighbour
		Direction direction = ((Position)this.nodeinfo.nodePosition()).directionTo_ast(newNeighbour.nodePosition());
		NodeNodeOutboundPort selectedOutboundPort = getOutboundPortByDirection(direction);
		//proteger neighbours avec OutBoundPorts,on s'assure que les operations sur les neighbours et les OutBoundPorts ne vont pas etre interrompu
		this.neighbours_OutPorts_lock.lock();
		try {
			// check if deja connected
			if (selectedOutboundPort.connected()) {
				//disconnter d'abord
				this.doPortDisconnection(selectedOutboundPort.getPortURI());
				// connecter
				this.connecter(direction, selectedOutboundPort, newNeighbour);
			} else {
				// si pas de connection pour le outport,on fait connecter
				connecter(direction, selectedOutboundPort, newNeighbour);
			}
			this.neighbours.put(direction, newNeighbour);
		}catch (Exception e) {
			System.err.println("ask4Connection: "+this.nodeinfo.nodeIdentifier() + " Failed to connect to neighbour " + newNeighbour.nodeIdentifier() + ": " + e.getMessage());
		}finally {
			this.neighbours_OutPorts_lock.unlock();
		}
	}

	public void ask4Disconnection(NodeInfoI neighbour) throws Exception {

	    Direction direction = ((Position)this.nodeinfo.nodePosition()).directionTo_ast(neighbour.nodePosition());
	    NodeNodeOutboundPort selectedOutboundPort = getOutboundPortByDirection(direction);
		//proteger neighbours avec OutBoundPorts,on s'assure que les operations sur les neighbours et les OutBoundPorts ne vont pas etre interrompu
		this.neighbours_OutPorts_lock.lock();
        try {
			if (selectedOutboundPort.connected()) {
				this.logMessage(this.nodeinfo.nodeIdentifier() + "essayer de disconnect:" + neighbour.nodeIdentifier());
				this.doPortDisconnection(selectedOutboundPort.getPortURI());
			}
			this.neighbours.remove(direction);
		}finally {
			this.neighbours_OutPorts_lock.unlock();
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
	        String neighbourInboundPortURI = endpointDescriptor.getInboundPortURI();
	        this.logMessage("SensorNodeComponent :"+ this.nodeinfo.nodeIdentifier() +" start connect : Uri obtenue to connect :" + neighbourInboundPortURI);

	        this.doPortConnection(selectedOutboundPort.getPortURI(),neighbourInboundPortURI,NodeNodeConnector.class.getCanonicalName());

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
	    	           if(selectedOutboundPort.connected()) {
						   this.doPortDisconnection(selectedOutboundPort.getPortURI());
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
