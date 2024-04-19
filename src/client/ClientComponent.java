package client;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import client.ports.ClientAsynRequestInboundPort;
import client.ports.ClientOutboundPort;
import client.ports.ClientRegistreOutboundPort;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.ports.PortI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.utils.aclocks.AcceleratedClock;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import fr.sorbonne_u.utils.aclocks.ClocksServerCI;
import fr.sorbonne_u.utils.aclocks.ClocksServerConnector;
import fr.sorbonne_u.utils.aclocks.ClocksServerOutboundPort;
import nodes.SensorNodeComponent;
import request.Request;
import request.ast.Direction;
import request.ast.astBase.RBase;
import request.ast.astBexp.SBExp;
import request.ast.astCont.DCont;
import request.ast.astCont.FCont;
import request.ast.astDirs.FDirs;
import request.ast.astGather.FGather;
import request.ast.astQuery.GQuery;
import request.ast.astQuery.BQuery;
import sensor_network.ConnectionInfo;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;
import sensor_network.EndPointDescriptor;
import sensor_network.QueryResult;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;
import nodes.connectors.NodeClientConnector;

@RequiredInterfaces(required = {RequestingCI.class,LookupCI.class,ClocksServerCI.class})
@OfferedInterfaces(offered = {RequestResultCI.class})
public class ClientComponent extends AbstractComponent {
	protected ClientOutboundPort client_node_port;
	protected ClientRegistreOutboundPort client_registre_port;
	protected String ClientAsyncInboundPortURI = null;
	//accelerated clock
	protected AcceleratedClock ac;
    //Max waiting time for un ASync request
	protected long Max_WaitingTime = 6000;
	//map qui stocke ASync requete resultats recu
	protected Map<String, QueryResult> requestResults;
	//map qui stocke ASync requete temps de debut
	protected Map<String,Instant> requestTimes;

	//gestion Concurrence
	protected final ReentrantReadWriteLock requestResults_lock = new ReentrantReadWriteLock();
	protected final ReentrantReadWriteLock requestTimes_lock = new ReentrantReadWriteLock();

	//introduire les pools threads
	//pool thread pour recevoir les resultats des requete Async from Node
	protected int index_poolthread_receiveAsync;
	protected String uri_pool_receiveAsync = "client-pool-thread-receiveAsync";
	protected int nbThreads_poolReceiveAsync = 40;
	//pool thread pour envoyer les requetes async aux nodes
	protected int index_poolthread_sendAsync;
	protected String uri_pool_sendAsync = "client-pool-thread-sendAsync";
	protected int nbThreads_poolSendAsync = 5;
    //le node id on va connecter
	protected String NodeId = "";
	
	protected ClientComponent(String uri, String Client_Node_outboundPortURI,String Client_Registre_outboundPortURI,String Client_AsynRequest_inboundPortURI,String CLOCK_URI,String NodeId) throws Exception {
		super(uri, 1, 2);

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
        //pool thread pour les requete Async from Node
		this.index_poolthread_receiveAsync = this.createNewExecutorService(this.uri_pool_receiveAsync, this.nbThreads_poolReceiveAsync,false);
		this.index_poolthread_sendAsync = this.createNewExecutorService(this.uri_pool_sendAsync,this.nbThreads_poolSendAsync,true);
		// ---------------------------------------------------------------------
		// Gestion des Port
		// ---------------------------------------------------------------------
		//publish InboundPort
		 PortI InboundPort_AsynRequest = new ClientAsynRequestInboundPort(Client_AsynRequest_inboundPortURI, this);
		 InboundPort_AsynRequest.publishPort();
		
        // init port (required interface)
        this.client_node_port = new ClientOutboundPort(Client_Node_outboundPortURI, this);
        //publish port(an outbound port is always local)
        this.client_node_port.localPublishPort();
        
        //client - registre - LookupCI
        this.client_registre_port = new ClientRegistreOutboundPort(Client_Registre_outboundPortURI,this);
        this.client_registre_port.localPublishPort();
        
        //Async port URI
        this.ClientAsyncInboundPortURI = Client_AsynRequest_inboundPortURI;
        
        this.getTracer().setTitle("client") ;
		this.getTracer().setRelativePosition(0, 0) ;
		
		this.requestResults = new HashMap<>();
		this.requestTimes = new HashMap<>();

		this.NodeId = NodeId;
        
		
        AbstractComponent.checkImplementationInvariant(this);
		AbstractComponent.checkInvariant(this);
    }
	// ---------------------------------------------------------------------
	// Partie getters pour les pools threads
	// ---------------------------------------------------------------------
	//pool de thread pour les requetes async (renvoyer resultats) from nodes
    public int getIndex_poolthread_receiveAsync() {
		return index_poolthread_receiveAsync;
	}
	//pool de thread pour les requetes async (envoyer requetes) to nodes
	public int getIndex_poolthread_sendAsync() {
		return index_poolthread_sendAsync;
	}

	// ---------------------------------------------------------------------
	// Partie Sync
	// ---------------------------------------------------------------------
	public void sendRequest_direction() throws Exception{
		this.logMessage("----------------- Query Resultat Sync (Direction) ------------------");
		 this.logMessage("ClientComponent Sending request Sync Direction....");
		 int nb_saut = 1;
		GQuery test = new GQuery(new FGather("temperature"),new DCont(new FDirs(Direction.NE),nb_saut));
        String requestURI = "gather-request-uri";	      
        RequestI request = new Request(requestURI,test,false,null);
        QueryResult result = (QueryResult) this.client_node_port.execute(request);
        this.logMessage("ClientComponentr Receive resultat de request:");
        this.logMessage("" + result);
        this.logMessage("--------------------------------------");
	}
        
	
	public void sendRequest_flooding() throws Exception{
		this.logMessage("----------------- Query Resultat Sync (Flooding)------------------");
		 this.logMessage("ClientComponent Sending request Sync Flooding....");
		double max_distance =8.0;
		BQuery test = new BQuery(new SBExp("fumée"),new FCont(new RBase(),max_distance));
        String requestURI = "flood-request-uri";	      
        RequestI request = new Request(requestURI,test,false,null);
        QueryResult result = (QueryResult) this.client_node_port.execute(request);
        this.logMessage("ClientComponentr Receive resultat de request:");
        this.logMessage("" + result);
        this.logMessage("-------------------------------------");
	}

	// ---------------------------------------------------------------------
	// Obtenir connection info from Registre et connecter
	// ---------------------------------------------------------------------
	
	public void findEtConnecterByIdentifer(String NodeID) throws Exception {
		this.logMessage("---------------Connect to Node-------------------");
		this.logMessage("ClientComponent search and connect Node :" + NodeID);
		ConnectionInfo connectionInfo = (ConnectionInfo) this.client_registre_port.findByIdentifier(NodeID);
		if (connectionInfo != null) {
		String InboundPortURI = ((EndPointDescriptor)connectionInfo.endPointInfo()).getInboundPortURI();
		this.doPortConnection(this.client_node_port.getPortURI(), InboundPortURI,NodeClientConnector.class.getCanonicalName());

		this.logMessage("ClientComponent : Connection established with Node: " + NodeID);
		}else {
			this.logMessage("Node " + NodeID + " not found or cannot connect.");
		}
		this.logMessage("----------------------------------");
	}

	// ---------------------------------------------------------------------
	// Partie Async
	// ---------------------------------------------------------------------

	//Request Direction Async
	public void sendRequest_direction_Asyn(String requestURI,Direction dir,int nb_saut)throws Exception{
        if(this.ClientAsyncInboundPortURI!=null) {
    	this.logMessage("-----------------Client Send Requete Async (Direction) ------------------");
		EndPointDescriptor endpointDescriptor = new EndPointDescriptor(ClientAsyncInboundPortURI);
        ConnectionInfo connection_info = new ConnectionInfo("client",endpointDescriptor);
		GQuery test = new GQuery(new FGather("temperature"),new DCont(new FDirs(dir),nb_saut));
		this.logMessage("ClientComponent Sending Request Async Direction: "+requestURI);
        RequestI request = new Request(requestURI,test,true,connection_info);
        this.client_node_port.executeAsync(request);
        
        }else {
        	System.err.println("ClientAsyncInboundPortURI NULL");
        }	
	}

	//Request Flooding Async
	public void sendRequest_flooding_Asyn(String requestURI,double max_distance) throws Exception{
		this.logMessage("-----------------Client Send Requete Async (Flooding)------------------");
		
		EndPointDescriptor endpointDescriptor = new EndPointDescriptor(ClientAsyncInboundPortURI);
	    ConnectionInfo connection_info = new ConnectionInfo("client",endpointDescriptor);
		 
//		double max_distance =8.0;
		BQuery test = new BQuery(new SBExp("fumée"),new FCont(new RBase(),max_distance));
		this.logMessage("ClientComponent Sending request Async Flooding: "+requestURI);
        RequestI request = new Request(requestURI,test,true,connection_info);
       
        this.client_node_port.executeAsync(request);
	}

	
	public void	acceptRequestResult(String requestURI,QueryResultI result) throws Exception{
		this.logMessage("-----------------Receive Request Async Resultat ------------------\n"+
				"Receive resultat du request: "+requestURI + "\n Query Result: " + result );
		//fusionner les res,stocker dans hashmap
		//si deja exist,merge res . Sinon inserer le res dans hashmap
		requestResults_lock.readLock().lock();
			if (this.requestResults.containsKey(requestURI)) {
				requestResults_lock.readLock().unlock();
				requestResults_lock.writeLock().lock();
				//check si cette requete est deja terminee
				requestTimes_lock.readLock().lock();
				try{
				if (!this.requestTimes.containsKey(requestURI)) {
					requestTimes_lock.readLock().unlock();
					this.logMessage("Request " + requestURI + " has already been completed.Resultat rejete.");
					return;
				}
				}finally {
					if(requestTimes_lock.getReadHoldCount()>0)
					 requestTimes_lock.readLock().unlock();
				}
				// merge res
				QueryResult existingResult = this.requestResults.get(requestURI);
				existingResult.mergeRes(result);
				this.requestResults.put(requestURI, existingResult);
				requestResults_lock.writeLock().unlock();
			} else {
				requestResults_lock.readLock().unlock();
				requestResults_lock.writeLock().lock();
				requestTimes_lock.writeLock().lock();
				// insert
				this.requestResults.put(requestURI, (QueryResult) result);
				//nous stockons le start instant pour chaque requete
				this.requestTimes.put(requestURI, this.ac.currentInstant());

				requestResults_lock.writeLock().unlock();
				requestTimes_lock.writeLock().unlock();
			}
	}

	//Vérifier régulièrement si les requetes ont atteint le temps d'attente maximum
	public void checkRequestResults() {
		// check every 3 secs
		long delay = 3_000;
		this.scheduleTaskWithFixedDelay(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
//						((ClientComponent)this.getTaskOwner()).logMessage("check start time");
						Instant currentInstant = ac.currentInstant();
						ArrayList<String> toRemove = new ArrayList<>();
						requestTimes_lock.readLock().lock();
						for (Map.Entry<String, Instant> entry : requestTimes.entrySet()) {
							String requestURI = entry.getKey();
							Instant start_instant = entry.getValue();
							QueryResult result = requestResults.get(requestURI);
							if (Duration.between(start_instant, currentInstant).toMillis() > Max_WaitingTime) {
							((ClientComponent)this.getTaskOwner()).logMessage("-----------------Total Async Resultat (Fusionner)------------------");
			                ((ClientComponent)this.getTaskOwner()).logMessage("Request " + requestURI + " has reached the maximum wait time. Final result fusionné: " + result);
							toRemove.add(requestURI);
							}
						}
						requestTimes_lock.readLock().unlock();
						requestTimes_lock.writeLock().lock();
						for (String requestURI : toRemove) {
							requestTimes.remove(requestURI);
						}
						requestTimes_lock.writeLock().unlock();
					 }
					},
					delay,
					delay,
					TimeUnit.MILLISECONDS);
	}


	// ---------------------------------------------------------------------
	// Cycle de Vie
	// ---------------------------------------------------------------------
	@Override
    public void start() throws ComponentStartException {
		this.logMessage("ClientComponent started.");
        super.start();
    }
	
	 @Override
	    public void execute() throws Exception {
		Instant start_instant = this.ac.getStartInstant();
		//3
		Instant instant_findConnecter = start_instant.plusSeconds(8);
		//5,7
		Instant instant_sendAsync = start_instant.plusSeconds(10);
		Instant instant_sendAsync2 = start_instant.plusSeconds(10);
		long delay = 1L;
		long delay_send = 1L;
		long delay_send2 = 1L;

		if(instant_findConnecter.isAfter(this.ac.currentInstant()))
		delay = ac.nanoDelayUntilInstant(instant_findConnecter);
		if(instant_sendAsync.isAfter(this.ac.currentInstant()))
		delay_send = ac.nanoDelayUntilInstant(instant_sendAsync);
		if(instant_sendAsync2.isAfter(this.ac.currentInstant()))
		delay_send2 = ac.nanoDelayUntilInstant(instant_sendAsync2);

		    this.logMessage("ClientComponent executed.");

			this.runTask(new AbstractComponent.AbstractTask() {
				@Override
				public void run() {
					try {
				 ((ClientComponent)this.getTaskOwner()).checkRequestResults();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

		    this.scheduleTask(new AbstractComponent.AbstractTask() {
			 @Override
			 public void run() {
				 try {
					 // essayer de connecter de node indiquee
//					 String NodeID = "node1";
					 ((ClientComponent)this.getTaskOwner()).findEtConnecterByIdentifer(((ClientComponent)this.getTaskOwner()).NodeId);

					 //request Sync
					 ((ClientComponent)this.getTaskOwner()).sendRequest_direction() ;
					 ((ClientComponent)this.getTaskOwner()).sendRequest_flooding() ;

				 } catch (Exception e) {
					 e.printStackTrace();
				 }
			 }
		 }, delay, TimeUnit.NANOSECONDS);

			//(en premier temps)send async requete en utilisant pool thread distinct (pool thread_sendAsync)
			this.scheduleTask(this.index_poolthread_sendAsync,new AbstractComponent.AbstractTask() {
				@Override
				public void run() {
					try {
						//request Async
						((ClientComponent)this.getTaskOwner()).sendRequest_direction_Asyn("direction-requete-Async-uri",Direction.NE,5) ;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, delay_send, TimeUnit.NANOSECONDS);

		 this.scheduleTask(this.index_poolthread_sendAsync,new AbstractComponent.AbstractTask() {
			 @Override
			 public void run() {
				 try {
					 //request Async
					 ((ClientComponent)this.getTaskOwner()).sendRequest_flooding_Asyn("flooding-requete-Async-uri",8.0) ;
				 } catch (Exception e) {
					 e.printStackTrace();
				 }
			 }
		 }, delay_send, TimeUnit.NANOSECONDS);

		//(deuxieme temps)send async requete en utilisant pool thread distinct (pool thread_sendAsync)
		 this.scheduleTask(this.index_poolthread_sendAsync,new AbstractComponent.AbstractTask() {
			 @Override
			 public void run() {
				 try {
					 //request Async
					 ((ClientComponent)this.getTaskOwner()).sendRequest_direction_Asyn("direction-requete-Async-uri2",Direction.NW,5) ;
				 } catch (Exception e) {
					 e.printStackTrace();
				 }
			 }
		 }, delay_send2, TimeUnit.NANOSECONDS);

		 this.scheduleTask(this.index_poolthread_sendAsync,new AbstractComponent.AbstractTask() {
			 @Override
			 public void run() {
				 try {
					 //request Async
					 ((ClientComponent)this.getTaskOwner()).sendRequest_flooding_Asyn("flooding-requete-Async-uri2",12.0) ;
				 } catch (Exception e) {
					 e.printStackTrace();
				 }
			 }
		 }, delay_send2, TimeUnit.NANOSECONDS);
		 this.scheduleTask(this.index_poolthread_sendAsync,new AbstractComponent.AbstractTask() {
			 @Override
			 public void run() {
				 try {
					 //request Async
//					 ((ClientComponent)this.getTaskOwner()).sendRequest_direction_Asyn("direction-requete-Async-uri3",Direction.SW,6) ;
				 } catch (Exception e) {
					 e.printStackTrace();
				 }
			 }
		 }, delay_send2, TimeUnit.NANOSECONDS);

		 this.scheduleTask(this.index_poolthread_sendAsync,new AbstractComponent.AbstractTask() {
			 @Override
			 public void run() {
				 try {
					 //request Async
//					 ((ClientComponent)this.getTaskOwner()).sendRequest_flooding_Asyn("flooding-requete-Async-uri3",16.0) ;
				 } catch (Exception e) {
					 e.printStackTrace();
				 }
			 }
		 }, delay_send2, TimeUnit.NANOSECONDS);

	 }
	 
	 @Override
	    public void finalise() throws Exception {
		 this.logMessage("stopping Client component.") ;
		 this.printExecutionLogOnFile("Client");
		 
		 if (this.client_node_port.connected()) {
	            this.client_node_port.doDisconnection();
	        }
	     this.client_node_port.unpublishPort();
	     if (this.client_registre_port.connected()) {
	            this.client_registre_port.doDisconnection();
	        }
	     this.client_registre_port.unpublishPort();
	     super.finalise();
	    }
	
	
	
}
