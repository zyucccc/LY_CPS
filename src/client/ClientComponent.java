package client;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import client.plugins.ClientPlugin;
import client.ports.ClientAsynRequestInboundPort;
import client.ports.ClientOutboundPort;
import client.ports.ClientRegistreOutboundPort;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.AbstractPort;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
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
import nodes.plugins.NodePlugin;
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

@RequiredInterfaces(required = {ClocksServerCI.class})
@OfferedInterfaces(offered = {RequestResultCI.class})
public class ClientComponent extends AbstractComponent {
	protected ClientAsynRequestInboundPort InboundPort_AsynRequest;
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

	//le node id on va connecter
	protected String NodeId = "";

    //pour les tests de performance
    private AtomicLong totalTime = new AtomicLong(0);
    private AtomicInteger responseCount = new AtomicInteger(0);
    private ConcurrentHashMap<String, Instant> request_send_time = new ConcurrentHashMap<>();

	//plugin
	protected String ClientPluginURI;
	
	protected ClientComponent(String Client_Registre_outboundPortURI,String CLOCK_URI,String NodeId) throws Exception {
		super(1, 2);
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
		ClientPlugin clientPlugin = new ClientPlugin(Client_Registre_outboundPortURI);
		this.ClientPluginURI = AbstractPort.generatePortURI();
		clientPlugin.setPluginURI(this.ClientPluginURI);
		this.installPlugin(clientPlugin);

		// ---------------------------------------------------------------------
		// Gestion des Port
		// ---------------------------------------------------------------------
		//Async port URI
		 this.ClientAsyncInboundPortURI = AbstractPort.generatePortURI();
		//publish InboundPort
		 this.InboundPort_AsynRequest = new ClientAsynRequestInboundPort(this.ClientAsyncInboundPortURI, this);
		 this.InboundPort_AsynRequest.publishPort();
        
        this.getTracer().setTitle("client") ;
		this.getTracer().setRelativePosition(0, 0) ;
		
		this.requestResults = new HashMap<>();
		this.requestTimes = new HashMap<>();

		this.NodeId = NodeId;

        AbstractComponent.checkImplementationInvariant(this);
		AbstractComponent.checkInvariant(this);
    }
	// ---------------------------------------------------------------------
	// Partie getters pour les pools des threads defini dans plug-in
	// ---------------------------------------------------------------------
     	public int get_Index_poolthread_receiveAsync() {
		return ((ClientPlugin)this.getPlugin(this.ClientPluginURI)).get_Index_poolthread_receiveAsync();
		}

		public int get_Index_poolthread_sendAsync() {
		return ((ClientPlugin)this.getPlugin(this.ClientPluginURI)).get_Index_poolthread_sendAsync();
		}

		public String get_Uri_pool_sendAsync() {
		return ((ClientPlugin)this.getPlugin(this.ClientPluginURI)).get_Uri_pool_sendAsync();
		}

		public String get_Uri_pool_receiveAsync() {
		return ((ClientPlugin)this.getPlugin(this.ClientPluginURI)).get_Uri_pool_receiveAsync();
		}

	// ---------------------------------------------------------------------
	// Partie getters pour fermer les pools des threads
	// ---------------------------------------------------------------------
		public void fermer_poolthread(String uri_pool) {
		  this.shutdownExecutorService(uri_pool);
		}

	// ---------------------------------------------------------------------
	// Partie Sync
	// ---------------------------------------------------------------------
	public void sendRequest_direction() throws Exception{
		this.logMessage("----------------- Query Resultat Sync (Direction) ------------------");
		 this.logMessage("ClientComponent Sending request Sync Direction....");
		 int nb_saut = 5;
		GQuery test = new GQuery(new FGather("temperature"),new DCont(new FDirs(Direction.NE),nb_saut));
        String requestURI = "gather-request-uri";	      
        RequestI request = new Request(requestURI,test,false,null);

		//plugin:
		QueryResult result = (QueryResult) ((ClientPlugin)this.getPlugin(this.ClientPluginURI)).execute(request);

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

        //plugin:
		QueryResult result = (QueryResult) ((ClientPlugin)this.getPlugin(this.ClientPluginURI)).execute(request);

		this.logMessage("ClientComponentr Receive resultat de request:");
        this.logMessage("" + result);
        this.logMessage("-------------------------------------");
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

		//plugin:
		((ClientPlugin)this.getPlugin(this.ClientPluginURI)).executeAsync(request);
        
        }else {
        	System.err.println("ClientAsyncInboundPortURI NULL");
        }	
	}

	//Request Flooding Async
	public void sendRequest_flooding_Asyn(String requestURI,double max_distance) throws Exception{
		this.logMessage("-----------------Client Send Requete Async (Flooding)------------------");
		
		EndPointDescriptor endpointDescriptor = new EndPointDescriptor(ClientAsyncInboundPortURI);
	    ConnectionInfo connection_info = new ConnectionInfo("client",endpointDescriptor);

		BQuery test = new BQuery(new SBExp("fumée"),new FCont(new RBase(),max_distance));
		this.logMessage("ClientComponent Sending request Async Flooding: "+requestURI);
        RequestI request = new Request(requestURI,test,true,connection_info);

		//plugin:
		((ClientPlugin)this.getPlugin(this.ClientPluginURI)).executeAsync(request);
	}


	
	public void	acceptRequestResult(String requestURI,QueryResultI result) throws Exception{
        //pour les tests de performance
        Instant currentInstant = ac.currentInstant();
        Instant send_instant = request_send_time.get(requestURI);
        if(send_instant != null) {
            long responseTime = Duration.between(send_instant, currentInstant).toMillis();
            if (responseTime > 0) {
                totalTime.addAndGet(responseTime);
                responseCount.incrementAndGet();
            }
        }

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
		// check chaque secs
		long delay = 1_000;
		this.scheduleTaskWithFixedDelay(
				new AbstractComponent.AbstractTask() {
					@Override
					public void run() {
						Instant currentInstant = ac.currentInstant();
						ArrayList<String> toRemove = new ArrayList<>();
						requestTimes_lock.readLock().lock();
						try{
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
                        //pour les tests de performance
//                        ((ClientComponent)this.getTaskOwner()).logMessage("-----------------Test Performance: Temps Moyenne------------------");
//                        ((ClientComponent)this.getTaskOwner()).logMessage("Temps moyenne actuel (Milli Second) : " + ((ClientComponent)this.getTaskOwner()).getAverageResponseTime());
                        }finally {
							requestTimes_lock.readLock().unlock();
						}
						requestTimes_lock.writeLock().lock();
						try{
						for (String requestURI : toRemove) {
							requestTimes.remove(requestURI);
						}}
						finally {
							requestTimes_lock.writeLock().unlock();
						}
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
		Instant instant_findConnecter = start_instant.plusSeconds(9);
		//5,7
		Instant instant_sendAsync = start_instant.plusSeconds(10);
//		Instant instant_sendAsync2 = start_instant.plusSeconds(10);
		long delay_connect = 1L;
		long delay_send = 1L;
//		long delay_send2 = 1L;
		long delay_entre_2_requete = 30_000_000_000L;//4 second entre 2 requetes



		if(instant_findConnecter.isAfter(this.ac.currentInstant()))
		delay_connect = ac.nanoDelayUntilInstant(instant_findConnecter);
		if(instant_sendAsync.isAfter(this.ac.currentInstant()))
		delay_send = ac.nanoDelayUntilInstant(instant_sendAsync);
//		if(instant_sendAsync2.isAfter(this.ac.currentInstant()))
//		delay_send2 = ac.nanoDelayUntilInstant(instant_sendAsync2);

		    this.logMessage("ClientComponent executed.");

			this.runTask(new AbstractComponent.AbstractTask(this.ClientPluginURI) {
				@Override
				public void run() {
					try {
				 ((ClientComponent)this.getTaskOwner()).checkRequestResults();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

		    this.scheduleTask(new AbstractComponent.AbstractTask(this.ClientPluginURI) {
			 @Override
			 public void run() {
				 try {
					 // essayer de connecter de node indiquee
					 ((ClientPlugin)this.getTaskProviderReference()).findEtConnecterByIdentifer(NodeId);
					 //request Sync
					 ((ClientComponent)this.getTaskOwner()).sendRequest_direction() ;
					 ((ClientComponent)this.getTaskOwner()).sendRequest_flooding() ;

				 } catch (Exception e) {
					 e.printStackTrace();
				 }
			 }
		 }, delay_connect, TimeUnit.NANOSECONDS);


		 //(en premier temps)send async requete en utilisant pool thread distinct (pool thread_sendAsync)
		 //uri1
		 this.scheduleTaskWithFixedDelay(
				 this.get_Index_poolthread_sendAsync(),new AbstractComponent.AbstractTask(this.ClientPluginURI) {
					 @Override
					 public void run() {
						 try {
							 //request Async
                             String requestURI = "direction-requete-Async-uri - "+AbstractPort.generatePortURI();
                             //nous enregistrons le temps d'envoi de la requete
                             Instant currentInstant = ac.currentInstant();
                             request_send_time.put(requestURI, currentInstant);
							 ((ClientComponent)this.getTaskOwner()).sendRequest_direction_Asyn(requestURI,Direction.NE,5) ;
						 } catch (Exception e) {
							 e.printStackTrace();
						 }
					 }},delay_send,delay_entre_2_requete,TimeUnit.NANOSECONDS);


		 this.scheduleTaskWithFixedDelay(
				 this.get_Index_poolthread_sendAsync(),new AbstractComponent.AbstractTask(this.ClientPluginURI) {
					 @Override
					 public void run() {
						 try {
							 //request Async
                             String requestURI = "flooding-requete-Async-uri - "+AbstractPort.generatePortURI();
                             Instant currentInstant = ac.currentInstant();
                             request_send_time.put(requestURI, currentInstant);
							 ((ClientComponent)this.getTaskOwner()).sendRequest_flooding_Asyn(requestURI,8.0) ;
						 } catch (Exception e) {
							 e.printStackTrace();
						 }
					 }},delay_send,delay_entre_2_requete,TimeUnit.NANOSECONDS);
         //uri2
		 this.scheduleTaskWithFixedDelay(
				 this.get_Index_poolthread_sendAsync(),new AbstractComponent.AbstractTask(this.ClientPluginURI) {
					 @Override
					 public void run() {
						 try {
							 //request Async
                             String requestURI = "direction-requete-Async-uri2 - "+AbstractPort.generatePortURI();
                             Instant currentInstant = ac.currentInstant();
                             request_send_time.put(requestURI, currentInstant);
							 ((ClientComponent)this.getTaskOwner()).sendRequest_direction_Asyn(requestURI,Direction.NW,5) ;
                         } catch (Exception e) {
							 e.printStackTrace();
						 }
					 }},delay_send,delay_entre_2_requete,TimeUnit.NANOSECONDS);

		 this.scheduleTaskWithFixedDelay(
				 this.get_Index_poolthread_sendAsync(),new AbstractComponent.AbstractTask(this.ClientPluginURI) {
					 @Override
					 public void run() {
						 try {
							 //request Async
                             String requestURI = "flooding-requete-Async-uri2 - "+AbstractPort.generatePortURI();
                             Instant currentInstant = ac.currentInstant();
                             request_send_time.put(requestURI, currentInstant);
							 ((ClientComponent)this.getTaskOwner()).sendRequest_flooding_Asyn(requestURI,12.0) ;
						 } catch (Exception e) {
							 e.printStackTrace();
						 }
					 }},delay_send,delay_entre_2_requete,TimeUnit.NANOSECONDS);
		 //uri3
		 this.scheduleTaskWithFixedDelay(
				 this.get_Index_poolthread_sendAsync(),new AbstractComponent.AbstractTask(this.ClientPluginURI) {
					 @Override
					 public void run() {
						 try {
							 //request Async
                             String requestURI = "direction-requete-Async-uri3 - "+AbstractPort.generatePortURI();
                             Instant currentInstant = ac.currentInstant();
                             request_send_time.put(requestURI, currentInstant);
                             ((ClientComponent)this.getTaskOwner()).sendRequest_direction_Asyn(requestURI,Direction.SW,7) ;
                         } catch (Exception e) {
							 e.printStackTrace();
						 }
					 }},delay_send,delay_entre_2_requete,TimeUnit.NANOSECONDS);

         this.scheduleTaskWithFixedDelay(
                 this.get_Index_poolthread_sendAsync(),new AbstractComponent.AbstractTask(this.ClientPluginURI) {
                     @Override
                     public void run() {
                         try {
                             //request Async
                             String requestURI = "flooding-requete-Async-uri3 - "+AbstractPort.generatePortURI();
                             Instant currentInstant = ac.currentInstant();
                             request_send_time.put(requestURI, currentInstant);
                             ((ClientComponent)this.getTaskOwner()).sendRequest_flooding_Asyn(requestURI,15.0) ;
                         } catch (Exception e) {
                             e.printStackTrace();
                         }
                     }},delay_send,delay_entre_2_requete,TimeUnit.NANOSECONDS);

	 }
	 
	 @Override
	    public void finalise() throws Exception {
		 this.logMessage("stopping Client component.") ;
		 this.printExecutionLogOnFile("Client");

	     super.finalise();
	    }

		@Override
	public void shutdown() throws ComponentShutdownException {
		try {
			this.InboundPort_AsynRequest.unpublishPort();
//			this.shutdownExecutorService(this.get_Uri_pool_receiveAsync());
//			this.shutdownExecutorService(this.get_Uri_pool_sendAsync());
		}
		catch (Exception e) {
			throw new ComponentShutdownException(e);
		}
			super.shutdown();
	    }

    // ---------------------------------------------------------------------
    // les test de performance
    // ---------------------------------------------------------------------
    public double getAverageResponseTime() {
        if (responseCount.get() > 0) {
            return (double) totalTime.get() / responseCount.get();
        }
        return 0.0;
    }
	
	
	
}
