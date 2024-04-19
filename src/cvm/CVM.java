package cvm;
//uri plus automatique

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.helpers.CVMDebugModes;

import fr.sorbonne_u.utils.aclocks.ClocksServer;
import nodes.NodeInfo;
import nodes.SensorNodeComponent;
import nodes.connectors.NodeClientConnector;
import nodes.connectors.NodeRegistreConnector;
import nodes.sensor.Sensor;
import registre.RegistreComponent;
import sensor_network.EndPointDescriptor;
import sensor_network.Position;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import client.ClientComponent;
import client.connectors.ClientRegistreConnector;

public class CVM extends AbstractCVM {
	// ---------------------------------------------------------------------
	// URIs statiques
	// ---------------------------------------------------------------------
	private int nbNodes = 49;
	private int nbClients = 2;
    String[] uris_clientComposant = new String[nbClients];
	String[] uris_client_Node_outbound = new String[nbClients];
	String[] uris_client_Registre_outbound = new String[nbClients];
    //Clock
	protected static final String CLOCK_URI = "global-clock";
	public static final Instant START_INSTANT = Instant.now().plusSeconds(1);
	protected static final long START_DELAY = 3000L;
	public static final double ACCELERATION_FACTOR = 1.0;

	//component uri
	protected static final String Registre_COMPONENT_URI = "my-registre-uri";
	//Registre inbound port
	protected static final String Registre_LookupCI_INBOUND_PORT_URI = "registre-LookupCI-inbound-uri";
	protected static final String Registre_RegistrationCI_INBOUND_PORT_URI = "registre-RegistrationCI-inbound-uri";

	private static final AtomicInteger idCounter_Client = new AtomicInteger(0);
    protected String[] generateClientAndPortsURIs() {
		int baseId = idCounter_Client.incrementAndGet();
		//Client component URI
		String CLIENT_COMPONENT_URI = "my-client-uri" + baseId;
		//Client outbound port pour des Nodes
		String CLIENT_Node_OUTBOUND_PORT_URI = "client-outbound-uri" + baseId;
		//Client outbound port pour Registre
		String CLIENT_Registre_OUTBOUND_PORT_URI = "client-registre-outbound-uri" + baseId;
		//Client asyn Request InboundPort
		String Client_AsynReqest_Inbound_PORT_URI="client-asynRequest-inbound-uri" + baseId;
		return new String[] {CLIENT_COMPONENT_URI, CLIENT_Node_OUTBOUND_PORT_URI, CLIENT_Registre_OUTBOUND_PORT_URI,Client_AsynReqest_Inbound_PORT_URI};
	}
//    protected static final String CLIENT_COMPONENT_URI = "my-client-uri";
//    //Client asyn Request InboundPort
//    String Client_AsynReqest_Inbound_PORT_URI="client-asynRequest-inbound-uri";
//	//Client outbound port pour des Nodes
//    protected static final String CLIENT_Node_OUTBOUND_PORT_URI = "client-outbound-uri";
//    //Client outbound port pour Registre
//    protected static final String CLIENT_Registre_OUTBOUND_PORT_URI = "client-registre-outbound-uri";

	// ---------------------------------------------------------------------
	// URIs automatiques pour les noeuds
	// ---------------------------------------------------------------------
    private static final AtomicInteger idCounter = new AtomicInteger(0);
	//generer les URIS pour les Nodes
    protected String[] generateNodeAndPortsURIs() {
        int baseId = idCounter.incrementAndGet(); 
        //SensorNode component URI
        String SENSORNODE_COMPONENT_URI = "my-sensornode-uri" + baseId;
        //SensorNode node P2P
        String NODE_P2P_INBOUND_PORT_URI = "node"+ baseId +"-inbound-uri" + baseId;
        //sensor inbound port pour Client
        String SENSORNODE_INBOUND_PORT_URI="sensornode-inbound-uri" + baseId;
        //SensorNode outbound port pour Registre
        String SensorNode_Registre_OUTBOUND_PORT_URI = "node-registre-outbound-uri" + baseId;
        //NWES
        String SensorNode_Node_NE_OUTBOUND_PORT_URI = "node-node-NE-outbound-uri" + baseId;
        String SensorNode_Node_NW_OUTBOUND_PORT_URI = "node-node-NW-outbound-uri" +baseId;
        String SensorNode_Node_SE_OUTBOUND_PORT_URI = "node-node-SE-outbound-uri1"+baseId;
        String SensorNode_Node_SW_OUTBOUND_PORT_URI = "node-node-SW-outbound-uri1"+baseId;
        //asyn outbound port uri for Sensor node
        String sensorNodeAsyn_OutboundPortURI = "sensornode-async-outbound-uri" + baseId;
        
        return new String[] {SENSORNODE_COMPONENT_URI
        		, NODE_P2P_INBOUND_PORT_URI
        		, SENSORNODE_INBOUND_PORT_URI
        		,SensorNode_Registre_OUTBOUND_PORT_URI
        		,SensorNode_Node_NE_OUTBOUND_PORT_URI
        		,SensorNode_Node_NW_OUTBOUND_PORT_URI
        		,SensorNode_Node_SE_OUTBOUND_PORT_URI
        		,SensorNode_Node_SW_OUTBOUND_PORT_URI
        		,sensorNodeAsyn_OutboundPortURI};
    }

    public CVM() throws Exception {
        super();
    }
    
    /** Reference to the sensor component to share between deploy
	 *  and shutdown.														*/
	protected String	uriSensorNodeURI;
	/** Reference to the client component to share between deploy
	 *  and shutdown.														*/
	protected String	uriClientURI;
	
	protected String	uriRegistreURI;

	
//	instantiate the components, publish their port and interconnect them.
    @Override
    public void deploy() throws Exception {
        assert !this.deploymentDone();
        
            // ---------------------------------------------------------------------
     		// Configuration phase
     		// ---------------------------------------------------------------------
     		// debugging mode configuration; comment and uncomment the line to see
     		// the difference
     		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.LIFE_CYCLE);
     		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.INTERFACES);
     		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.PORTS);
     		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.CONNECTING);
     		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.CALLING);
     		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.EXECUTOR_SERVICES);

     		
     	    // ---------------------------------------------------------------------
    		// Creation phase
    		// ---------------------------------------------------------------------
     		//clock
		long unixEpochStartTimeInNanos = TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis() + START_DELAY);
		AbstractComponent.createComponent(
				ClocksServer.class.getCanonicalName(),
				new Object[]{
						CLOCK_URI, // URI attribuée à l’horloge
						unixEpochStartTimeInNanos, // moment du démarrage en temps réel Unix
						START_INSTANT, // instant de démarrage du scénario
						ACCELERATION_FACTOR}); // facteur d’acccélération

		// creer registre Component

		this.uriRegistreURI =
				AbstractComponent.createComponent(
						RegistreComponent.class.getCanonicalName(),
						new Object[]{Registre_COMPONENT_URI,Registre_RegistrationCI_INBOUND_PORT_URI, Registre_LookupCI_INBOUND_PORT_URI});
		assert this.isDeployedComponent(this.uriRegistreURI);
		this.toggleTracing(this.uriRegistreURI);
		this.toggleLogging(this.uriRegistreURI);
            
            // creer client Component
			for(int i = 0; i < nbClients; i++) {
				//les uris de client
				String[] uris_client = generateClientAndPortsURIs();
				//Client component URI
				String CLIENT_COMPONENT_URI = uris_client[0];
				//Client outbound port pour des Nodes
				String CLIENT_Node_OUTBOUND_PORT_URI = uris_client[1];
				uris_client_Node_outbound[i] = CLIENT_Node_OUTBOUND_PORT_URI;
				//Client outbound port pour Registre
				String CLIENT_Registre_OUTBOUND_PORT_URI = uris_client[2];
				uris_client_Registre_outbound[i] = CLIENT_Registre_OUTBOUND_PORT_URI;
				//Client asyn Request InboundPort
				String Client_AsynReqest_Inbound_PORT_URI=uris_client[3];
				//creer des Client Component
				this.uriClientURI =
						AbstractComponent.createComponent(
							ClientComponent.class.getCanonicalName(),
							new Object[]{CLIENT_COMPONENT_URI, CLIENT_Node_OUTBOUND_PORT_URI,CLIENT_Registre_OUTBOUND_PORT_URI,Client_AsynReqest_Inbound_PORT_URI,CLOCK_URI});
				assert this.isDeployedComponent(this.uriClientURI);
				this.toggleTracing(this.uriClientURI);
				this.toggleLogging(this.uriClientURI);

				uris_clientComposant[i] = this.uriClientURI;

				//connection client to registre
				this.doPortConnection(
						this.uriClientURI,
						CLIENT_Registre_OUTBOUND_PORT_URI,
						Registre_LookupCI_INBOUND_PORT_URI,
						ClientRegistreConnector.class.getCanonicalName());
			}//boucle pour creer des Clients
//			String[]
//            this.uriClientURI =
//                AbstractComponent.createComponent(
//                    ClientComponent.class.getCanonicalName(),
//                    new Object[]{CLIENT_COMPONENT_URI, CLIENT_Node_OUTBOUND_PORT_URI,CLIENT_Registre_OUTBOUND_PORT_URI,Client_AsynReqest_Inbound_PORT_URI,CLOCK_URI});
//            assert this.isDeployedComponent(this.uriClientURI);
//            this.toggleTracing(this.uriClientURI);
//            this.toggleLogging(this.uriClientURI);
                
              //connection client to registre
//                this.doPortConnection(
//                    	this.uriClientURI,
//                    	CLIENT_Registre_OUTBOUND_PORT_URI,
//                    	Registre_LookupCI_INBOUND_PORT_URI,
//                        ClientRegistreConnector.class.getCanonicalName());


                
        //node  
     	//nodeinfo
//		     int nbNodes = 49;
     		 Position[] positions = {
     		        new Position(0.0, 0.0),
					//direction：Nord
	 		        new Position(0.0, 10.0),
	 		        new Position(0.0, 20.0),
					//direction: Sud
					new Position(0.0, -10.0),
	 		        new Position(0.0, -20.0),
					//direction: Est
					new Position(10.0, 0.0),
					new Position(20.0, 0.0),
					//direction: Ouest
					new Position(-10.0, 0.0),
					new Position(-20.0, 0.0),
					//direction: NE
     		        new Position(5.0, 5.0),
     		        new Position(10.0, 10.0),
					new Position(15.0, 15.0),
					new Position(20.0, 20.0),
					new Position(25.0,25.0),
					new Position(5.0,15.0),
					new Position(15.0,5.0),
					new Position(10.0,20.0),
					new Position(20.0,10.0),
					new Position(25.0,5.0),

					//direction: SE
     		        new Position(5.0,-5.0),
					new Position(10.0,-10.0),
					new Position(15.0,-15.0),
					new Position(20.0,-20.0),
					 new Position(25.0,-25.0),
					 new Position(5.0,-15.0),
					 new Position(15.0,-5.0),
					 new Position(10.0,-20.0),
					 new Position(20.0,-10.0),
					 new Position(25.0,-5.0),
					//direction: NW
					new Position(-5.0, 5.0),
				    new Position(-10.0, 10.0),
				    new Position(-15.0, 15.0),
				    new Position(-20.0, 20.0),
					 new Position(-25.0,25.0),
					 new Position(-5.0,15.0),
					 new Position(-15.0,5.0),
					 new Position(-10.0,20.0),
					 new Position(-20.0,10.0),
					 new Position(-25.0,5.0),
					//direction: SW
					new Position(-5.0,-5.0),
					new Position(-10.0,-10.0),
					new Position(-15.0,-15.0),
					new Position(-20.0,-20.0),
					 new Position(-25.0,-25.0),
					 new Position(-5.0,-15.0),
					 new Position(-15.0,-5.0),
					 new Position(-10.0,-20.0),
					 new Position(-20.0,-10.0),
					 new Position(-25.0,-5.0),

     		    };
	 		//definir les temperatures
			double[] temperatures = new double[nbNodes];
			Random rand = new Random();
			for (int i = 0; i < nbNodes; i++) {
				temperatures[i] = rand.nextInt(41);
			}
			//definir les ranges
     		double[] ranges = new double[nbNodes];
			Arrays.fill(ranges, 9.0);
			//definir les situations de fumée
			boolean[] smokes = new boolean[nbNodes];
			for (int i = 0; i < nbNodes; i++) {
//				smokes[i] = Math.random() > 0.5;
				smokes[i] = rand.nextBoolean();
//				smokes[i] = true;
			}

     	    // creer des Nodes
     		 for(int i = 0; i < nbNodes; i++) {
     			//les uris de noeuds
				String[] uris = generateNodeAndPortsURIs();
     			Position position = positions[i];
     			double tempera = temperatures[i];
     			boolean smoke = smokes[i];
     			double range = ranges[i];
				 //Position position = new Position(Math.random() * 100, Math.random() * 100);
     			EndPointDescriptor uriinfo = new EndPointDescriptor(uris[2]);
         		EndPointDescriptor p2pEndPoint = new EndPointDescriptor(uris[1]);
         		NodeInfo nodeinfo = new NodeInfo("node"+(i+1),position,range,p2pEndPoint,uriinfo);
         		Sensor SensorData_temperature = new Sensor("node"+(i+1),"temperature",double.class,tempera);
                Sensor SensorData_fumée = new Sensor("node"+(i+1),"fumée",Boolean.class,smoke);
                HashMap<String, Sensor> sensorsData = new HashMap<String, Sensor>();
                sensorsData.put("temperature",SensorData_temperature);
                sensorsData.put("fumée",SensorData_fumée);
                
                //creer des sensorNode Component
                this.uriSensorNodeURI=
                        AbstractComponent.createComponent(
                            SensorNodeComponent.class.getCanonicalName(),
                            new Object[]{nodeinfo,uris[0], 
                            		uris[2],
                            		uris[3],
                            		uris[4],
                            		uris[5],
                            		uris[6],
                            		uris[7],
                            		uris[1] ,
                            		sensorsData,
                            		uris[8],
									CLOCK_URI});
                    assert this.isDeployedComponent(this.uriSensorNodeURI);
                    this.toggleTracing(this.uriSensorNodeURI);
                    this.toggleLogging(this.uriSensorNodeURI);
                    
                  //conection node1 to registre
                    this.doPortConnection(
                        	this.uriSensorNodeURI,
                        	uris[3],
                        	Registre_RegistrationCI_INBOUND_PORT_URI,
                            NodeRegistreConnector.class.getCanonicalName());
     		 }//boucle pour creer des Nodes

        super.deploy();
        assert this.deploymentDone();
    }
    
    @Override
	public void	finalise() throws Exception
	{
		// Port disconnections can be done here for static architectures
		// otherwise, they can be done in the finalise methods of components.
		for(int i = 0; i < nbClients; i++) {
			this.doPortDisconnection(uris_clientComposant[i],
					uris_client_Registre_outbound[i]);
			this.doPortDisconnection(uris_clientComposant[i],
					uris_client_Node_outbound[i]);
		}

//		this.doPortDisconnection(this.uriClientURI,
//				CLIENT_Node_OUTBOUND_PORT_URI);
//		this.doPortDisconnection(this.uriClientURI,
//				CLIENT_Registre_OUTBOUND_PORT_URI);

		super.finalise();
	}
    
    @Override
	public void	shutdown() throws Exception
	{
		assert	this.allFinalised();
		// any disconnection not done yet can be performed here

		super.shutdown();
	}

    public static void main(String[] args) {
        try {
        	// Create an instance of the defined component virtual machine.
            CVM cvm = new CVM();
            //execute
            cvm.startStandardLifeCycle(200000L);
            Thread.sleep(100000L); // 等待一段时间以观察输出
            System.exit(0); // 退出程序
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}