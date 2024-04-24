package nodes;


import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import client.connectors.ClientAsynRequestConnector;
import client.ports.ClientRegistreOutboundPort;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.AbstractPort;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PImplI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.utils.aclocks.*;
import nodes.plugins.NodePlugin;
import fr.sorbonne_u.exceptions.InvariantException;
import fr.sorbonne_u.exceptions.PostconditionException;
import nodes.ports.SensorNodeAsynRequestOutboundPort;
import nodes.sensor.Sensor;
import request.ExecutionState;
import request.ProcessingNode;
import request.ast.Direction;
import sensor_network.ConnectionInfo;
import sensor_network.EndPointDescriptor;
import sensor_network.Position;
import sensor_network.QueryResult;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;

//@OfferedInterfaces(offered = {RequestingCI.class,SensorNodeP2PCI.class})
//@RequiredInterfaces(required = {RegistrationCI.class,SensorNodeP2PCI.class,RequestResultCI.class,ClocksServerCI.class})
@RequiredInterfaces(required = {RequestResultCI.class,ClocksServerCI.class})
public class SensorNodeComponent extends AbstractComponent {
	protected AcceleratedClock ac;
	protected NodeInfo nodeinfo;
	protected ProcessingNode processingNode;

	//outbound port for AsynRequest
	protected SensorNodeAsynRequestOutboundPort node_asynRequest_Outport;

	//plugin URI
	protected String NodePluginURI;

	//pool thread pour traiter les requetes async provenant des nodes
	protected int index_poolthread_receiveAsync;
	protected String uri_pool_receiveAsync = AbstractPort.generatePortURI();
	protected int nbThreads_poolReceiveAsync = 10;

	//pool thread pour traiter les requetes async provenant du client
	protected int index_poolthread_receiveAsync_Client;
	protected String uri_pool_receiveAsync_Client = AbstractPort.generatePortURI();
	protected int nbThreads_poolReceiveAsync_Client = 10;

	//pool thread pour traiter les requetes de connection(askConnection,askDisconnection
	protected int index_poolthread_Receiveconnection;
	protected String uri_pool_Receiveconnection = AbstractPort.generatePortURI();
	protected int nbThreads_Receiveconnection = 4;
	//gestion Concurrence
	//proteger les donnees de sensors
	protected final ReentrantReadWriteLock sensorData_lock = new ReentrantReadWriteLock();
	//proteger la processus pour renvoyer les resultats aux clients
	protected final ReentrantLock envoyer_res_lock = new ReentrantLock();

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
            String sensorNodeInboundPortURI,
            String node_Registre_outboundPortURI,//Node_registre_outboundPortURI
            String node_node_InboundPortURI,//P2P node to node Inbound Port
            HashMap<String, Sensor> sensorsData,
			String CLOCK_URI
            ) throws Exception {
		
		super(7, 7) ;
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
		// Gestion des plugins
		// ---------------------------------------------------------------------
        NodePlugin nodePlugin = new NodePlugin(node_Registre_outboundPortURI,sensorNodeInboundPortURI,node_node_InboundPortURI);
		this.NodePluginURI = AbstractPort.generatePortURI();
        nodePlugin.setPluginURI(this.NodePluginURI);
		this.installPlugin(nodePlugin);

		// ---------------------------------------------------------------------
		// Configuration des pools de threads
		// ---------------------------------------------------------------------
		this.index_poolthread_receiveAsync = this.createNewExecutorService(this.uri_pool_receiveAsync, this.nbThreads_poolReceiveAsync,false);
		this.index_poolthread_receiveAsync_Client = this.createNewExecutorService(this.uri_pool_receiveAsync_Client, this.nbThreads_poolReceiveAsync_Client,false);
        this.index_poolthread_Receiveconnection = this.createNewExecutorService(this.uri_pool_Receiveconnection, this.nbThreads_Receiveconnection,false);

		this.nodeinfo = (NodeInfo) nodeInfo;
	    
	    String NodeID = this.nodeinfo.nodeIdentifier();
		Position position = (Position) this.nodeinfo.nodePosition();
		this.neighbours = new ConcurrentHashMap<>();
		this.processingNode = new ProcessingNode(NodeID,position,sensorsData);
        
        //node async request outbound port
        this.node_asynRequest_Outport = new SensorNodeAsynRequestOutboundPort(this);
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
					+ "port with implemented interface RequestingCI");
assert	this.findPortFromURI(sensorNodeInboundPortURI).isPublished() :
			new PostconditionException("The component must have a "
					+ "port published with URI " + sensorNodeInboundPortURI);
	}

	// ---------------------------------------------------------------------
	// Partie getters pour les pools threads
	// ---------------------------------------------------------------------
	public int getIndex_poolthread_receiveAsync() {
		return this.index_poolthread_receiveAsync;
	}

	public int getIndex_poolthread_receiveAsync_Client(){
		return this.index_poolthread_receiveAsync_Client;
	}

	public int getIndex_poolthread_Receiveconnection() {
		return this.index_poolthread_Receiveconnection;
	}

	// ---------------------------------------------------------------------
	// Partie getters pour neighbours
	// ---------------------------------------------------------------------
    public ConcurrentHashMap<Direction, NodeInfoI> getNeighbours() {
		return this.neighbours;
	}

	// ---------------------------------------------------------------------
	// Partie getters pour nodeinfo
	// ---------------------------------------------------------------------
    public NodeInfo getNodeinfo() {
		return this.nodeinfo;
	}

	public ProcessingNode getProcessingNode() {
		return this.processingNode;
	}

	// ---------------------------------------------------------------------
	// Partie getters pour locks
	// ---------------------------------------------------------------------
    public ReentrantReadWriteLock getSensorData_lock() {
		return this.sensorData_lock;
	}

	public ReentrantLock getEnvoyer_res_lock() {
		return this.envoyer_res_lock;
	}

	// ---------------------------------------------------------------------
	// Partie getters pour async port qui renvoyer les res aux clients
	// ---------------------------------------------------------------------
	public SensorNodeAsynRequestOutboundPort getNode_asynRequest_Outport() {
		return this.node_asynRequest_Outport;
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
		this.scheduleTask(new AbstractComponent.AbstractTask(this.NodePluginURI) {
			@Override
			public void run() {
				try {
					((NodePlugin)this.getTaskProviderReference()).sendNodeInfoToRegistre(((SensorNodeComponent)this.getTaskOwner()).nodeinfo);
					((NodePlugin)this.getTaskProviderReference()).connecterNeighbours();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 1L, TimeUnit.NANOSECONDS);
    }
	
	 @Override
	    public void finalise() throws Exception {
	        this.logMessage("SensorNodeComponent [" + this.nodeinfo.nodeIdentifier() + "] stopping...");

			if(this.node_asynRequest_Outport.connected()) {
				this.node_asynRequest_Outport.doDisconnection();
			}

		    super.finalise();
	    }
	 
	 @Override
	    public void shutdown() throws ComponentShutdownException {
		 try {
			 //depublish les ports
			 this.node_asynRequest_Outport.unpublishPort();

			 //fermer les pools des threads explicitement
			 this.shutdownExecutorService(this.uri_pool_receiveAsync);
             this.shutdownExecutorService(this.uri_pool_receiveAsync_Client);
			 this.shutdownExecutorService(this.uri_pool_Receiveconnection);

		 } catch (Exception e) {
			 throw new ComponentShutdownException(e);
		 }
		 super.shutdown();
	    }
	
	//deleguer les operations de connecter Client via Port Async et appeler acceptRequest pour renvoyer res
	public void renvoyerAsyncRes (RequestContinuationI request) throws Exception {
		envoyer_res_lock.lock();
		//proteger les operations de renvoyer res,en meme temps il ne faut que un seul thread pour renvoyer res aux clients
		try {
			ConnectionInfo co_info = (ConnectionInfo) request.clientConnectionInfo();
			String InboundPortUri = ((EndPointDescriptor) co_info.endPointInfo()).getInboundPortURI();
			if (this.node_asynRequest_Outport.connected()) {
				this.doPortDisconnection(this.node_asynRequest_Outport.getPortURI());
			}
			this.doPortConnection(this.node_asynRequest_Outport.getPortURI(), InboundPortUri, ClientAsynRequestConnector.class.getCanonicalName());
			ExecutionState data = (ExecutionState) request.getExecutionState();
			QueryResult result_all = (QueryResult) data.getCurrentResult();
			this.logMessage("Connecter au Client "+InboundPortUri+" successfully! renvoyer Result.....");
			this.node_asynRequest_Outport.acceptRequestResult(request.requestURI(),result_all);
		}catch (Exception e) {
			System.err.println("renvoyerAsyncRes: "+this.nodeinfo.nodeIdentifier() + " Failed to send result to client: " + e.getMessage());
		}finally {
			envoyer_res_lock.unlock();
		}
	}
	
}
