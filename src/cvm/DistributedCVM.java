package cvm;

import client.ClientComponent;
import client.connectors.ClientRegistreConnector;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.AbstractPort;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.cvm.AbstractDistributedCVM;
import fr.sorbonne_u.utils.aclocks.ClocksServer;
import nodes.NodeInfo;
import nodes.SensorNodeComponent;
import nodes.connectors.NodeRegistreConnector;
import nodes.sensor.Sensor;
import registre.RegistreComponent;
import sensor_network.EndPointDescriptor;
import sensor_network.Position;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class DistributedCVM extends AbstractDistributedCVM {
    protected static final String JVM_URI1 = "jvm1";
    protected static final String JVM_URI2 = "jvm2";
    protected static final String JVM_URI3 = "jvm3";
    protected static final String JVM_URI4 = "jvm4";
    protected static final String JVM_URI5 = "jvm5";

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

    protected String	 uriSensorNodeURI;
    protected String[]   SensorNode_URIs = new String[nbNodes];
    protected String[]   SENSOR_Node_P2P_INBOUND_PORT_URI = new String[nbNodes];
    protected String[]   SENSOR_Node_INBOUND_PORT_URI = new String[nbNodes];
    protected String[]   SENSOR_Node_OUTBOUND_PORT_Registre_URI = new String[nbNodes];

    //client
    protected String	  uriClientURI;
    protected String[]    Client_URIs = new String[nbClients];
    protected String[]    CLIENT_Registre_OUTBOUND_PORT_URI = new String[nbClients];

    protected String	uriRegistreURI;

    //configuration
    protected Position[] positions = {
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



    public DistributedCVM(String[] args)throws Exception {
        super(args);
    }

    @Override
    public void instantiateAndPublish() throws Exception {
        // ---------------------------------------------------------------------
        // JVM1
        // ---------------------------------------------------------------------
        if(AbstractCVM.getThisJVMURI().equals(JVM_URI1)){
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

            // creer client Component
            this.createClient(0,"node1");
            // creer des Nodes
            this.createSensorNodes(0,10);
            }
        // ---------------------------------------------------------------------
        // JVM2
        // ---------------------------------------------------------------------
        else if (AbstractCVM.getThisJVMURI().equals(JVM_URI2)){
            // creer client Component
            this.createClient(1,"node13");
            // creer des Nodes
            this.createSensorNodes(10,20);
        }
        // ---------------------------------------------------------------------
        // JVM3
        // ---------------------------------------------------------------------
        else if (AbstractCVM.getThisJVMURI().equals(JVM_URI3)){
            //creer client
            this.createClient(2,"node44");
            //creer des Nodes
            this.createSensorNodes(20,30);
        }
        // ---------------------------------------------------------------------
        // JVM4
        // ---------------------------------------------------------------------
        else if (AbstractCVM.getThisJVMURI().equals(JVM_URI4)){
            //creer client
            this.createClient(3,"node26");
            //creer des Nodes
            this.createSensorNodes(30,40);
        }
        // ---------------------------------------------------------------------
        // JVM5
        // ---------------------------------------------------------------------
        else if (AbstractCVM.getThisJVMURI().equals(JVM_URI5)){
            //creer client
            this.createClient(4,"node38");
            //creer des Nodes
            this.createSensorNodes(40,50);
        }
        else {
            System.out.println("Unknown JVM URI... " + AbstractCVM.getThisJVMURI());
        }
        super.instantiateAndPublish();
    }

    @Override
    public void interconnect() throws Exception {
        if(AbstractCVM.getThisJVMURI().equals(JVM_URI1)){
            //connecter client et registre
            this.connectClientToRegistre(0);
            for(int i = 0;i<10;i++) {
                this.connectSensorNodeToRegistre(i);
            }
        }
        else if (AbstractCVM.getThisJVMURI().equals(JVM_URI2)){
            this.connectClientToRegistre(1);
            for(int i = 10;i<20;i++) {
                this.connectSensorNodeToRegistre(i);
            }

        }
        else if (AbstractCVM.getThisJVMURI().equals(JVM_URI3)){
            this.connectClientToRegistre(2);
            for(int i = 20;i<30;i++) {
                this.connectSensorNodeToRegistre(i);
            }
        }
        else if (AbstractCVM.getThisJVMURI().equals(JVM_URI4)){
            this.connectClientToRegistre(3);
            for(int i = 30;i<40;i++) {
                this.connectSensorNodeToRegistre(i);
            }
        }
        else if (AbstractCVM.getThisJVMURI().equals(JVM_URI5)){
            this.connectClientToRegistre(4);
            for(int i = 40;i<50;i++) {
                this.connectSensorNodeToRegistre(i);
            }
        }
        else {
            System.out.println("Unknown JVM URI... " + AbstractCVM.getThisJVMURI());
        }
        super.interconnect();
    }

    public static void main(String[] args) {
        try {
            DistributedCVM dcvm = new DistributedCVM(args);
            dcvm.startStandardLifeCycle(2000000L);
            Thread.sleep(1000000L);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
    }
    }

    // ---------------------------------------------------------------------
    // Creer des clients
    // ---------------------------------------------------------------------
    public void createClient(int index,String node_id) throws Exception{
        // creer client Component
        //Client outbound port pour Registre
        String CLIENT_Registre_OUTBOUND_PORT_URI = AbstractPort.generatePortURI();
        this.CLIENT_Registre_OUTBOUND_PORT_URI[index] = CLIENT_Registre_OUTBOUND_PORT_URI;
        //creer des Client Component
        this.uriClientURI =
                AbstractComponent.createComponent(
                        ClientComponent.class.getCanonicalName(),
                        new Object[]{
                                CLIENT_Registre_OUTBOUND_PORT_URI,
                                CLOCK_URI,
                                node_id});
        this.Client_URIs[index] = this.uriClientURI;

        assert this.isDeployedComponent(this.uriClientURI);
        this.toggleTracing(this.uriClientURI);
        this.toggleLogging(this.uriClientURI);
    }

    // ---------------------------------------------------------------------
    // Connection client et registre
    // ---------------------------------------------------------------------
    public void connectClientToRegistre(int index) throws Exception {
        this.doPortConnection(
                this.Client_URIs[index],
                this.CLIENT_Registre_OUTBOUND_PORT_URI[index],
                Registre_LookupCI_INBOUND_PORT_URI,
                ClientRegistreConnector.class.getCanonicalName());
    }

    // ---------------------------------------------------------------------
    // Creer des SensorNodes
    // ---------------------------------------------------------------------
    public void createSensorNodes(int index1,int index2) throws Exception {
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
    //			smokes[i] = rand.nextBoolean();
                smokes[i] = true;
            }
        for(int i = index1; i < index2; i++) {
            //les uris de noeuds
            String NODE_P2P_INBOUND_PORT_URI = AbstractPort.generatePortURI();
            this.SENSOR_Node_P2P_INBOUND_PORT_URI[i] = NODE_P2P_INBOUND_PORT_URI;
            String SENSORNODE_INBOUND_PORT_URI = AbstractPort.generatePortURI();
            this.SENSOR_Node_INBOUND_PORT_URI[i] = SENSORNODE_INBOUND_PORT_URI;
            String SensorNode_Registre_OUTBOUND_PORT_URI = AbstractPort.generatePortURI();
            this.SENSOR_Node_OUTBOUND_PORT_Registre_URI[i] = SensorNode_Registre_OUTBOUND_PORT_URI;
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
            this.SensorNode_URIs[i]=
                    AbstractComponent.createComponent(
                            SensorNodeComponent.class.getCanonicalName(),
                            new Object[]{nodeinfo,
                                    SENSORNODE_INBOUND_PORT_URI,
                                    SensorNode_Registre_OUTBOUND_PORT_URI,
                                    NODE_P2P_INBOUND_PORT_URI,
                                    sensorsData,
                                    CLOCK_URI});
            assert this.isDeployedComponent(this.SensorNode_URIs[i]);
            this.toggleTracing(this.SensorNode_URIs[i]);
            this.toggleLogging(this.SensorNode_URIs[i]);
        }
    }

    // ---------------------------------------------------------------------
    // Connection SensorNode et registre
    // ---------------------------------------------------------------------
    public void connectSensorNodeToRegistre(int index) throws Exception {
        this.doPortConnection(
                this.SensorNode_URIs[index],
                this.SENSOR_Node_OUTBOUND_PORT_Registre_URI[index],
                Registre_RegistrationCI_INBOUND_PORT_URI,
                NodeRegistreConnector.class.getCanonicalName());
    }

}
