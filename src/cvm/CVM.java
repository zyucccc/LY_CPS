package cvm;
//uri plus automatique

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.AbstractPort;
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
	private int nbNodes = 50;
	private int nbClients = 5;
    //Clock
	protected static final String CLOCK_URI = "global-clock";
	public static final Instant START_INSTANT = Instant.now().plusSeconds(1);
	protected static final long START_DELAY = 3000L;
	public static final double ACCELERATION_FACTOR = 1.0;
	// ---------------------------------------------------------------------
	// URIs pour le registre
	// ---------------------------------------------------------------------
	//Registre inbound port
	protected static final String Registre_LookupCI_INBOUND_PORT_URI = "registre-LookupCI-inbound-uri";
	protected static final String Registre_RegistrationCI_INBOUND_PORT_URI = "registre-RegistrationCI-inbound-uri";

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
							new Object[]{Registre_RegistrationCI_INBOUND_PORT_URI, Registre_LookupCI_INBOUND_PORT_URI});
			assert this.isDeployedComponent(this.uriRegistreURI);
			this.toggleTracing(this.uriRegistreURI);
			this.toggleLogging(this.uriRegistreURI);

		    //node ids lesquels les clients veulent connecter
			String[] NodeIDs = {"node1","node13","node44","node26","node38"};
//        String[] NodeIDs = {"node1","node1","node1","node1","node1"};
            // creer client Component
			for(int i = 0; i < nbClients; i++) {
				//Client outbound port pour Registre
				String CLIENT_Registre_OUTBOUND_PORT_URI = AbstractPort.generatePortURI();
				//creer des Client Component
				this.uriClientURI =
						AbstractComponent.createComponent(
							ClientComponent.class.getCanonicalName(),
							new Object[]{
									CLIENT_Registre_OUTBOUND_PORT_URI,
									CLOCK_URI,
									NodeIDs[i]});

				assert this.isDeployedComponent(this.uriClientURI);
				this.toggleTracing(this.uriClientURI);
				this.toggleLogging(this.uriClientURI);

				//connection client to registre
				this.doPortConnection(
						this.uriClientURI,
						CLIENT_Registre_OUTBOUND_PORT_URI,
						Registre_LookupCI_INBOUND_PORT_URI,
						ClientRegistreConnector.class.getCanonicalName());
			}//boucle pour creer des Clients

     		 Position[] positions = {
					 //node 1-10
     		         new Position(0.0, 0.0),
					 //direction：Nord
	 		         new Position(0.0, 10.0),
					 //direction: Sud
					 new Position(0.0, -10.0),
					 //direction: Est
					 new Position(10.0, 0.0),
					 //direction: Ouest
					 new Position(-10.0, 0.0),
					 //direction: NE
					 new Position(5.0, 5.0),
					 new Position(10.0, 10.0),
					 //direction: SE
					 new Position(5.0,-5.0),
					 new Position(10.0,-10.0),
					 //direction: NW
					 new Position(-5.0, 5.0),
//					 10-20
					 new Position(-10.0, 10.0),
					 //direction: SW
					 new Position(-5.0,-5.0),
					 new Position(-10.0,-10.0),
					 new Position(0.0, 20.0),
	 		         new Position(0.0, -20.0),
					 new Position(20.0, 0.0),
					 new Position(-20.0, 0.0),
					 new Position(15.0, 15.0),
					 new Position(20.0, 20.0),
					 new Position(25.0,25.0),
//					 20-30
					 new Position(5.0,15.0),
					 new Position(15.0,5.0),
					 new Position(10.0,20.0),
					 new Position(20.0,10.0),
					 new Position(25.0,5.0),
					 new Position(15.0,-15.0),
					 new Position(20.0,-20.0),
					 new Position(25.0,-25.0),
					 new Position(5.0,-15.0),
					 new Position(15.0,-5.0),
//					 30-40
					 new Position(10.0,-20.0),
					 new Position(20.0,-10.0),
					 new Position(25.0,-5.0),
				     new Position(-15.0, 15.0),
				     new Position(-20.0, 20.0),
					 new Position(-25.0,25.0),
					 new Position(-5.0,15.0),
					 new Position(-15.0,5.0),
					 new Position(-10.0,20.0),
					 new Position(-20.0,10.0),
//					 40-50
					 new Position(-25.0,5.0),
					 new Position(-15.0,-15.0),
					 new Position(-20.0,-20.0),
					 new Position(-25.0,-25.0),
					 new Position(-5.0,-15.0),
					 new Position(-15.0,-5.0),
					 new Position(-10.0,-20.0),
					 new Position(-20.0,-10.0),
					 new Position(-25.0,-5.0),
					 new Position(-25.0, -15.0)
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
//		    Arrays.fill(ranges, 50.0);
			//definir les situations de fumée
			boolean[] smokes = new boolean[nbNodes];
			for (int i = 0; i < nbNodes; i++) {
//				smokes[i] = rand.nextBoolean();
				smokes[i] = true;
			}

     	    // creer des Nodes
     		 for(int i = 0; i < nbNodes; i++) {
     			//les uris de noeuds
				String NODE_P2P_INBOUND_PORT_URI = AbstractPort.generatePortURI();
				String SENSORNODE_INBOUND_PORT_URI = AbstractPort.generatePortURI();
				String SensorNode_Registre_OUTBOUND_PORT_URI = AbstractPort.generatePortURI();
				//les sensor data predefinis
     			Position position = positions[i];
     			double temperature = temperatures[i];
     			boolean smoke = smokes[i];
     			double range = ranges[i];
     			EndPointDescriptor clientEndPoint = new EndPointDescriptor(SENSORNODE_INBOUND_PORT_URI);
         		EndPointDescriptor p2pEndPoint = new EndPointDescriptor(NODE_P2P_INBOUND_PORT_URI);
         		NodeInfo nodeinfo = new NodeInfo("node"+(i+1),position,range,p2pEndPoint,clientEndPoint);
         		Sensor SensorData_temperature = new Sensor("node"+(i+1),"temperature",double.class,temperature);
                Sensor SensorData_fumée = new Sensor("node"+(i+1),"fumée",Boolean.class,smoke);
                HashMap<String, Sensor> sensorsData = new HashMap<>();
                sensorsData.put("temperature",SensorData_temperature);
                sensorsData.put("fumée",SensorData_fumée);
                
                //creer des sensorNode Component
                this.uriSensorNodeURI=
                        AbstractComponent.createComponent(
                            SensorNodeComponent.class.getCanonicalName(),
                            new Object[]{nodeinfo,
									SENSORNODE_INBOUND_PORT_URI,
									SensorNode_Registre_OUTBOUND_PORT_URI,
									NODE_P2P_INBOUND_PORT_URI,
                            		sensorsData,
									CLOCK_URI});
                    assert this.isDeployedComponent(this.uriSensorNodeURI);
                    this.toggleTracing(this.uriSensorNodeURI);
                    this.toggleLogging(this.uriSensorNodeURI);
                    
                  //conection node to registre
                    this.doPortConnection(
                        	this.uriSensorNodeURI,
							SensorNode_Registre_OUTBOUND_PORT_URI,
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
            cvm.startStandardLifeCycle(2000000L);
            Thread.sleep(1000000L); // wait some time to see the traces
            System.exit(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}