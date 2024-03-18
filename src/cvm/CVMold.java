//package cvm;
////uri 会自动生成
//// plus automatique
//
//import fr.sorbonne_u.components.AbstractComponent;
//import fr.sorbonne_u.components.cvm.AbstractCVM;
//import fr.sorbonne_u.components.helpers.CVMDebugModes;
//
//import nodes.NodeInfo;
//import nodes.SensorNodeComponent;
//import nodes.connectors.NodeClientConnector;
//import nodes.connectors.NodeRegistreConnector;
//import nodes.sensor.Sensor;
//import registre.RegistreComponent;
//import sensor_network.EndPointDescriptor;
//import sensor_network.Position;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//
//import client.ClientComponent;
//import client.connectors.ClientRegistreConnector;
//
//public class CVM extends AbstractCVM {
//	//component uri
//	protected static final String Registre_COMPONENT_URI = "my-registre-uri";
//    protected static final String CLIENT_COMPONENT_URI = "my-client-uri";
//    
//    //sensor inbound port pour Client
//    protected static final String SENSORNODE_INBOUND_PORT_URI = "sensornode-inbound-uri";
//    protected static final String SENSORNODE_INBOUND_PORT_URI2 = "sensornode-inbound-uri2";
//    protected static final String SENSORNODE_INBOUND_PORT_URI3 = "sensornode-inbound-uri3";
//    protected static final String SENSORNODE_INBOUND_PORT_URI4 = "sensornode-inbound-uri4";
//
//    protected static final String CLIENT_Node_OUTBOUND_PORT_URI = "client-outbound-uri";
//    //Registre inbound port
//    protected static final String Registre_LookupCI_INBOUND_PORT_URI = "registre-LookupCI-inbound-uri";
//    protected static final String Registre_RegistrationCI_INBOUND_PORT_URI = "registre-RegistrationCI-inbound-uri";
//    
//    //Client outbound port pour Registre
//    protected static final String CLIENT_Registre_OUTBOUND_PORT_URI = "client-registre-outbound-uri";
//
//    //SensorNode component URI
//    protected static final String SENSORNODE_COMPONENT_URI = "my-sensornode-uri";
//    protected static final String SENSORNODE_COMPONENT_URI2 = "my-sensornode-uri2";
//    protected static final String SENSORNODE_COMPONENT_URI3 = "my-sensornode-uri3";
//    protected static final String SENSORNODE_COMPONENT_URI4 = "my-sensornode-uri4";
//    
//    //SensorNode outbound port pour Registre
//    protected static final String SensorNode_Registre_OUTBOUND_PORT_URI = "node-registre-outbound-uri";
//    protected static final String SensorNode_Registre_OUTBOUND_PORT_URI2 = "node-registre-outbound-uri2";
//    protected static final String SensorNode_Registre_OUTBOUND_PORT_URI3 = "node-registre-outbound-uri3";
//    protected static final String SensorNode_Registre_OUTBOUND_PORT_URI4 = "node-registre-outbound-uri4";
//    
//    //SensorNode node
//    protected static final String NODE_P2P_INBOUND_PORT_URI1  = "node1-inbound-uri1";
//    protected static final String NODE_P2P_INBOUND_PORT_URI2  = "node2-inbound-uri2";
//    protected static final String NODE_P2P_INBOUND_PORT_URI3  = "node3-inbound-uri3";
//    protected static final String NODE_P2P_INBOUND_PORT_URI4  = "node3-inbound-uri4";
//    
//    protected static final String SensorNode_Node_NE_OUTBOUND_PORT_URI1 = "node-node-NE-outbound-uri1";
//    protected static final String SensorNode_Node_NW_OUTBOUND_PORT_URI1 = "node-node-NW-outbound-uri1";
//    protected static final String SensorNode_Node_SE_OUTBOUND_PORT_URI1 = "node-node-SE-outbound-uri1";
//    protected static final String SensorNode_Node_SW_OUTBOUND_PORT_URI1 = "node-node-SW-outbound-uri1";
//    
//    protected static final String SensorNode_Node_NE_OUTBOUND_PORT_URI2 = "node-node-NE-outbound-uri2";
//    protected static final String SensorNode_Node_NW_OUTBOUND_PORT_URI2 = "node-node-NW-outbound-uri2";
//    protected static final String SensorNode_Node_SE_OUTBOUND_PORT_URI2 = "node-node-SE-outbound-uri2";
//    protected static final String SensorNode_Node_SW_OUTBOUND_PORT_URI2 = "node-node-SW-outbound-uri2";
//    
//    protected static final String SensorNode_Node_NE_OUTBOUND_PORT_URI3 = "node-node-NE-outbound-uri3";
//    protected static final String SensorNode_Node_NW_OUTBOUND_PORT_URI3 = "node-node-NW-outbound-uri3";
//    protected static final String SensorNode_Node_SE_OUTBOUND_PORT_URI3 = "node-node-SE-outbound-uri3";
//    protected static final String SensorNode_Node_SW_OUTBOUND_PORT_URI3 = "node-node-SW-outbound-uri3";
//    
//    protected static final String SensorNode_Node_NE_OUTBOUND_PORT_URI4 = "node-node-NE-outbound-uri4";
//    protected static final String SensorNode_Node_NW_OUTBOUND_PORT_URI4 = "node-node-NW-outbound-uri4";
//    protected static final String SensorNode_Node_SE_OUTBOUND_PORT_URI4 = "node-node-SE-outbound-uri4";
//    protected static final String SensorNode_Node_SW_OUTBOUND_PORT_URI4 = "node-node-SW-outbound-uri4";
//    
//    public CVM() throws Exception {
//        super();
//    }
//    
//    /** Reference to the sensor component to share between deploy
//	 *  and shutdown.														*/
//	protected String	uriSensorNodeURI;
//	protected String	uriSensorNodeURI2;
//	protected String	uriSensorNodeURI3;
//	protected String	uriSensorNodeURI4;
//	/** Reference to the client component to share between deploy
//	 *  and shutdown.														*/
//	protected String	uriClientURI;
//	
//	protected String	uriRegistreURI;
//
//	
////	instantiate the components, publish their port and interconnect them.
//    @Override
//    public void deploy() throws Exception {
//        assert !this.deploymentDone();
//        
//            // ---------------------------------------------------------------------
//     		// Configuration phase
//     		// ---------------------------------------------------------------------
//     		// debugging mode configuration; comment and uncomment the line to see
//     		// the difference
//     		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.LIFE_CYCLE);
//     		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.INTERFACES);
//     		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.PORTS);
//     		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.CONNECTING);
//     		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.CALLING);
//     		AbstractCVM.DEBUG_MODE.add(CVMDebugModes.EXECUTOR_SERVICES);
//
//     		
//     	    // ---------------------------------------------------------------------
//    		// Creation phase
//    		// ---------------------------------------------------------------------
//     	//nodeinfo
//     		Position position = new Position(0.0,0.0);
////     		Sensor sensor_temperature = new Sensor("sensor1","temperature",int.class,35);
////     		ArrayList<Sensor> sensorlist = new ArrayList<Sensor>();
////     		sensorlist.add(sensor_temperature);
//     		EndPointDescriptor uriinfo = new EndPointDescriptor(SENSORNODE_INBOUND_PORT_URI);
//     		EndPointDescriptor p2pEndPoint = new EndPointDescriptor(NODE_P2P_INBOUND_PORT_URI1);
//     		NodeInfo nodeinfo = new NodeInfo("node1",position,50.0,p2pEndPoint,uriinfo);
//            Sensor SensorData_temperature = new Sensor("node1","temperature",double.class,35.0);
//            Sensor SensorData_fumée = new Sensor("node1","fumée",Boolean.class,false);
//     		
//            HashMap<String, Sensor> sensorsData = new HashMap<String, Sensor>();
//            
//            sensorsData.put("temperature",SensorData_temperature);
//            sensorsData.put("fumée",SensorData_fumée);
//     		
//        // creer sensorNode Component
//		
////		  this.uriSensorNodeURI = AbstractComponent.createComponent(
////		  SensorNodeComponent.class.getCanonicalName(), new
////		  Object[]{nodeinfo,SENSORNODE_COMPONENT_URI,
////		  SENSORNODE_INBOUND_PORT_URI,SensorNode_Registre_OUTBOUND_PORT_URI,sensorsData
////		  }); assert this.isDeployedComponent(this.uriSensorNodeURI);
////		  this.toggleTracing(this.uriSensorNodeURI);
////		  this.toggleLogging(this.uriSensorNodeURI);
//		 
//        this.uriSensorNodeURI =
//                AbstractComponent.createComponent(
//                    SensorNodeComponent.class.getCanonicalName(),
//                    new Object[]{nodeinfo,SENSORNODE_COMPONENT_URI, 
//                    		SENSORNODE_INBOUND_PORT_URI,
//                    		SensorNode_Registre_OUTBOUND_PORT_URI,
//                    		SensorNode_Node_NE_OUTBOUND_PORT_URI1,
//                    		SensorNode_Node_NW_OUTBOUND_PORT_URI1,
//                    		SensorNode_Node_SE_OUTBOUND_PORT_URI1,
//                    		SensorNode_Node_SW_OUTBOUND_PORT_URI1,
//                    		NODE_P2P_INBOUND_PORT_URI1 ,sensorsData});
//            assert this.isDeployedComponent(this.uriSensorNodeURI);
//            this.toggleTracing(this.uriSensorNodeURI);
//            this.toggleLogging(this.uriSensorNodeURI);
//        
//        //init donnees for 2ieme node
//    	Position position2 = new Position(5,5);
//    	EndPointDescriptor uriinfo2 = new EndPointDescriptor(SENSORNODE_INBOUND_PORT_URI2);
//    	EndPointDescriptor p2pEndPoint2 = new EndPointDescriptor(NODE_P2P_INBOUND_PORT_URI2);
//
// 		NodeInfo nodeinfo2 = new NodeInfo("node2",position2,50.0,p2pEndPoint2,uriinfo2);
//        Sensor SensorData_temperature2 = new Sensor("node2","temperature",double.class,40.0);
//        Sensor SensorData_fumée2 = new Sensor("node2","fumée",Boolean.class,true);
// 		
//        HashMap<String, Sensor> sensorsData2 = new HashMap<String, Sensor>();
//        
//        sensorsData2.put("temperature",SensorData_temperature2);
//        sensorsData2.put("fumée",SensorData_fumée2);
//        
//        // creer 2 ieme sensorNode Component
//        
//    this.uriSensorNodeURI2 =
//            AbstractComponent.createComponent(
//                SensorNodeComponent.class.getCanonicalName(),
//                new Object[]{nodeinfo2,SENSORNODE_COMPONENT_URI2, 
//                		SENSORNODE_INBOUND_PORT_URI2,
//                		SensorNode_Registre_OUTBOUND_PORT_URI2,
//                		SensorNode_Node_NE_OUTBOUND_PORT_URI2,
//                		SensorNode_Node_NW_OUTBOUND_PORT_URI2,
//                		SensorNode_Node_SE_OUTBOUND_PORT_URI2,
//                		SensorNode_Node_SW_OUTBOUND_PORT_URI2,
//                		NODE_P2P_INBOUND_PORT_URI2 
//                		,sensorsData2});
//        assert this.isDeployedComponent(this.uriSensorNodeURI2);
//        this.toggleTracing(this.uriSensorNodeURI2);
//        this.toggleLogging(this.uriSensorNodeURI2);
//        
//        //init donnees for 3ieme node
//    	Position position3 = new Position(10,10);
//    	EndPointDescriptor uriinfo3 = new EndPointDescriptor(SENSORNODE_INBOUND_PORT_URI3);
//    	EndPointDescriptor p2pEndPoint3 = new EndPointDescriptor(NODE_P2P_INBOUND_PORT_URI3);
//
// 		NodeInfo nodeinfo3 = new NodeInfo("node3",position3,50.0,p2pEndPoint3,uriinfo3);
//        Sensor SensorData_temperature3 = new Sensor("node3","temperature",double.class,10.0);
//        Sensor SensorData_fumée3 = new Sensor("node3","fumée",Boolean.class,true);
// 		
//        HashMap<String, Sensor> sensorsData3 = new HashMap<String, Sensor>();
//        
//        sensorsData3.put("temperature",SensorData_temperature3);
//        sensorsData3.put("fumée",SensorData_fumée3);
//        
//        // creer 3 ieme sensorNode Component
//        
//    this.uriSensorNodeURI3 =
//            AbstractComponent.createComponent(
//                SensorNodeComponent.class.getCanonicalName(),
//                new Object[]{nodeinfo3,SENSORNODE_COMPONENT_URI3, 
//                		SENSORNODE_INBOUND_PORT_URI3,
//                		SensorNode_Registre_OUTBOUND_PORT_URI3,
//                		SensorNode_Node_NE_OUTBOUND_PORT_URI3,
//                		SensorNode_Node_NW_OUTBOUND_PORT_URI3,
//                		SensorNode_Node_SE_OUTBOUND_PORT_URI3,
//                		SensorNode_Node_SW_OUTBOUND_PORT_URI3,
//                		NODE_P2P_INBOUND_PORT_URI3 
//                		,sensorsData3});
//        assert this.isDeployedComponent(this.uriSensorNodeURI3);
//        this.toggleTracing(this.uriSensorNodeURI3);
//        this.toggleLogging(this.uriSensorNodeURI3);
//        
//      //init donnees for 4ieme node
//    	Position position4 = new Position(1,-1);
//    	EndPointDescriptor uriinfo4 = new EndPointDescriptor(SENSORNODE_INBOUND_PORT_URI4);
//    	EndPointDescriptor p2pEndPoint4 = new EndPointDescriptor(NODE_P2P_INBOUND_PORT_URI4);
//
// 		NodeInfo nodeinfo4 = new NodeInfo("node4",position4,3.0,p2pEndPoint4,uriinfo4);
//        Sensor SensorData_temperature4 = new Sensor("node4","temperature",double.class,90.0);
//        Sensor SensorData_fumée4 = new Sensor("node4","fumée",Boolean.class,true);
// 		
//        HashMap<String, Sensor> sensorsData4 = new HashMap<String, Sensor>();
//        
//        sensorsData4.put("temperature",SensorData_temperature4);
//        sensorsData4.put("fumée",SensorData_fumée4);
//        
//        // creer 4 ieme sensorNode Component
//        
//    this.uriSensorNodeURI4 =
//            AbstractComponent.createComponent(
//                SensorNodeComponent.class.getCanonicalName(),
//                new Object[]{nodeinfo4,SENSORNODE_COMPONENT_URI4, 
//                		SENSORNODE_INBOUND_PORT_URI4,
//                		SensorNode_Registre_OUTBOUND_PORT_URI4,
//                		SensorNode_Node_NE_OUTBOUND_PORT_URI4,
//                		SensorNode_Node_NW_OUTBOUND_PORT_URI4,
//                		SensorNode_Node_SE_OUTBOUND_PORT_URI4,
//                		SensorNode_Node_SW_OUTBOUND_PORT_URI4,
//                		NODE_P2P_INBOUND_PORT_URI4 
//                		,sensorsData4});
//        assert this.isDeployedComponent(this.uriSensorNodeURI4);
//        this.toggleTracing(this.uriSensorNodeURI4);
//        this.toggleLogging(this.uriSensorNodeURI4);
//        
//        
//        // creer client Component
//        this.uriClientURI =
//            AbstractComponent.createComponent(
//                ClientComponent.class.getCanonicalName(),
//                new Object[]{CLIENT_COMPONENT_URI, CLIENT_Node_OUTBOUND_PORT_URI,CLIENT_Registre_OUTBOUND_PORT_URI});
//        assert this.isDeployedComponent(this.uriClientURI);
//        this.toggleTracing(this.uriClientURI);
//        this.toggleLogging(this.uriClientURI);
//        
//        // creer registre Component
//        
//        this.uriRegistreURI =
//                AbstractComponent.createComponent(
//                    RegistreComponent.class.getCanonicalName(),
//                    new Object[]{Registre_COMPONENT_URI,Registre_RegistrationCI_INBOUND_PORT_URI, Registre_LookupCI_INBOUND_PORT_URI});
//            assert this.isDeployedComponent(this.uriRegistreURI);
//            this.toggleTracing(this.uriRegistreURI);
//            this.toggleLogging(this.uriRegistreURI);
//
//		// ---------------------------------------------------------------------
//		// Connection phase
//		// ---------------------------------------------------------------------
//        
////        this.doPortConnection(
////        	this.uriClientURI,
////            CLIENT_Node_OUTBOUND_PORT_URI,
////            SENSORNODE_INBOUND_PORT_URI,
////            NodeClientConnector.class.getCanonicalName());
//        //connection client to registre
//        this.doPortConnection(
//            	this.uriClientURI,
//            	CLIENT_Registre_OUTBOUND_PORT_URI,
//            	Registre_LookupCI_INBOUND_PORT_URI,
//                ClientRegistreConnector.class.getCanonicalName());
//        //conection node1 to registre
//        this.doPortConnection(
//            	this.uriSensorNodeURI,
//            	SensorNode_Registre_OUTBOUND_PORT_URI,
//            	Registre_RegistrationCI_INBOUND_PORT_URI,
//                NodeRegistreConnector.class.getCanonicalName());
//      //conection node2 to registre
//        this.doPortConnection(
//            	this.uriSensorNodeURI2,
//            	SensorNode_Registre_OUTBOUND_PORT_URI2,
//            	Registre_RegistrationCI_INBOUND_PORT_URI,
//                NodeRegistreConnector.class.getCanonicalName());
//      //conection node3 to registre
//        this.doPortConnection(
//            	this.uriSensorNodeURI3,
//            	SensorNode_Registre_OUTBOUND_PORT_URI3,
//            	Registre_RegistrationCI_INBOUND_PORT_URI,
//                NodeRegistreConnector.class.getCanonicalName());
//        //conection node4 to registre
//        this.doPortConnection(
//            	this.uriSensorNodeURI4,
//            	SensorNode_Registre_OUTBOUND_PORT_URI4,
//            	Registre_RegistrationCI_INBOUND_PORT_URI,
//                NodeRegistreConnector.class.getCanonicalName());
//
//
//        
//
//        super.deploy();
//        assert this.deploymentDone();
//    }
//    
//    @Override
//	public void	finalise() throws Exception
//	{
//		// Port disconnections can be done here for static architectures
//		// otherwise, they can be done in the finalise methods of components.
//		this.doPortDisconnection(this.uriClientURI,
//				CLIENT_Node_OUTBOUND_PORT_URI);
//		this.doPortDisconnection(this.uriClientURI,
//				CLIENT_Registre_OUTBOUND_PORT_URI);
//
//		super.finalise();
//	}
//    
//    @Override
//	public void	shutdown() throws Exception
//	{
//		assert	this.allFinalised();
//		// any disconnection not done yet can be performed here
//
//		super.shutdown();
//	}
//
//    public static void main(String[] args) {
//        try {
//        	// Create an instance of the defined component virtual machine.
//            CVM cvm = new CVM();
//            //execute
//            cvm.startStandardLifeCycle(200000L);
//            Thread.sleep(100000L); // 等待一段时间以观察输出
//            System.exit(0); // 退出程序
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
//}