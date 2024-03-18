package cvm;
//uri 会自动生成
// plus automatique

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.helpers.CVMDebugModes;

import nodes.NodeInfo;
import nodes.SensorNodeComponent;
import nodes.connectors.NodeClientConnector;
import nodes.connectors.NodeRegistreConnector;
import nodes.sensor.Sensor;
import registre.RegistreComponent;
import sensor_network.EndPointDescriptor;
import sensor_network.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import client.ClientComponent;
import client.connectors.ClientRegistreConnector;

public class CVM extends AbstractCVM {
	//component uri
	protected static final String Registre_COMPONENT_URI = "my-registre-uri";
    protected static final String CLIENT_COMPONENT_URI = "my-client-uri";
    
   
    //Client asyn Request InboundPort
    String Client_AsynReqest_Inbound_PORT_URI="client-asynRequest-inbound-uri";

    protected static final String CLIENT_Node_OUTBOUND_PORT_URI = "client-outbound-uri";
    //Registre inbound port
    protected static final String Registre_LookupCI_INBOUND_PORT_URI = "registre-LookupCI-inbound-uri";
    protected static final String Registre_RegistrationCI_INBOUND_PORT_URI = "registre-RegistrationCI-inbound-uri";
    
    //Client outbound port pour Registre
    protected static final String CLIENT_Registre_OUTBOUND_PORT_URI = "client-registre-outbound-uri";

    private static final AtomicInteger idCounter = new AtomicInteger(0);
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
        String SensorNode_Node_NE_OUTBOUND_PORT_URI="node-node-NE-outbound-uri" + baseId;
        String SensorNode_Node_NW_OUTBOUND_PORT_URI = "node-node-NW-outbound-uri" +baseId;
        String SensorNode_Node_SE_OUTBOUND_PORT_URI = "node-node-SE-outbound-uri1"+baseId;
        String SensorNode_Node_SW_OUTBOUND_PORT_URI = "node-node-SW-outbound-uri1"+baseId;
       
        return new String[] {SENSORNODE_COMPONENT_URI
        		, NODE_P2P_INBOUND_PORT_URI
        		, SENSORNODE_INBOUND_PORT_URI
        		,SensorNode_Registre_OUTBOUND_PORT_URI
        		,SensorNode_Node_NE_OUTBOUND_PORT_URI
        		,SensorNode_Node_NW_OUTBOUND_PORT_URI
        		,SensorNode_Node_SE_OUTBOUND_PORT_URI
        		,SensorNode_Node_SW_OUTBOUND_PORT_URI};
    }

    public CVM() throws Exception {
        super();
    }
    
    /** Reference to the sensor component to share between deploy
	 *  and shutdown.														*/
	protected String	uriSensorNodeURI;
	protected String	uriSensorNodeURI2;
	protected String	uriSensorNodeURI3;
	protected String	uriSensorNodeURI4;
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
     		
     		
     		
            
            // creer client Component
            this.uriClientURI =
                AbstractComponent.createComponent(
                    ClientComponent.class.getCanonicalName(),
                    new Object[]{CLIENT_COMPONENT_URI, CLIENT_Node_OUTBOUND_PORT_URI,CLIENT_Registre_OUTBOUND_PORT_URI,Client_AsynReqest_Inbound_PORT_URI});
            assert this.isDeployedComponent(this.uriClientURI);
            this.toggleTracing(this.uriClientURI);
            this.toggleLogging(this.uriClientURI);
            
            // creer registre Component
            
            this.uriRegistreURI =
                    AbstractComponent.createComponent(
                        RegistreComponent.class.getCanonicalName(),
                        new Object[]{Registre_COMPONENT_URI,Registre_RegistrationCI_INBOUND_PORT_URI, Registre_LookupCI_INBOUND_PORT_URI});
                assert this.isDeployedComponent(this.uriRegistreURI);
                this.toggleTracing(this.uriRegistreURI);
                this.toggleLogging(this.uriRegistreURI);
                
              //connection client to registre
                this.doPortConnection(
                    	this.uriClientURI,
                    	CLIENT_Registre_OUTBOUND_PORT_URI,
                    	Registre_LookupCI_INBOUND_PORT_URI,
                        ClientRegistreConnector.class.getCanonicalName());
                
        //node  
     	//nodeinfo
     		 Position[] positions = {
     		        new Position(0.0, 0.0),
     		        new Position(5.0, 5.0),
     		        new Position(10.0, 10.0),
     		        new Position(1.0, -1.0)
     		    };
     		double[] temperatures = {35.0, 40.0, 10.0, 90.0};
     	    boolean[] smokes = {false, true, true, true};
     		 for(int i = 0; i < 4; i++) {
     			 //les uris de noeud
     			 String[] uris = generateNodeAndPortsURIs();
     			Position position = positions[i];
     			double tempera = temperatures[i];
     			boolean smoke = smokes[i];
     			 //Position position = new Position(Math.random() * 100, Math.random() * 100); // 示例：随机生成位置
     			EndPointDescriptor uriinfo = new EndPointDescriptor(uris[2]);
         		EndPointDescriptor p2pEndPoint = new EndPointDescriptor(uris[1]);
         		NodeInfo nodeinfo = new NodeInfo("node"+(i+1),position,50.0,p2pEndPoint,uriinfo);
         		Sensor SensorData_temperature = new Sensor("node"+(i+1),"temperature",double.class,tempera);
                Sensor SensorData_fumée = new Sensor("node"+(i+1),"fumée",Boolean.class,smoke);
                HashMap<String, Sensor> sensorsData = new HashMap<String, Sensor>();
                sensorsData.put("temperature",SensorData_temperature);
                sensorsData.put("fumée",SensorData_fumée);
                
                //creer sensorNode Component
//                
//                this.uriSensorNodeURI =
//                        AbstractComponent.createComponent(
//                            SensorNodeComponent.class.getCanonicalName(),
//                            new Object[]{nodeinfo,SENSORNODE_COMPONENT_URI, 
//                            		SENSORNODE_INBOUND_PORT_URI,
//                            		SensorNode_Registre_OUTBOUND_PORT_URI,
//                            		SensorNode_Node_NE_OUTBOUND_PORT_URI1,
//                            		SensorNode_Node_NW_OUTBOUND_PORT_URI1,
//                            		SensorNode_Node_SE_OUTBOUND_PORT_URI1,
//                            		SensorNode_Node_SW_OUTBOUND_PORT_URI1,
//                            		NODE_P2P_INBOUND_PORT_URI1 ,sensorsData});
//                    assert this.isDeployedComponent(this.uriSensorNodeURI);
//                    this.toggleTracing(this.uriSensorNodeURI);
//                    this.toggleLogging(this.uriSensorNodeURI);
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
                            		uris[1] ,sensorsData});
                    assert this.isDeployedComponent(this.uriSensorNodeURI);
                    this.toggleTracing(this.uriSensorNodeURI);
                    this.toggleLogging(this.uriSensorNodeURI);
                    
                  //conection node1 to registre
                    this.doPortConnection(
                        	this.uriSensorNodeURI,
                        	uris[3],
                        	Registre_RegistrationCI_INBOUND_PORT_URI,
                            NodeRegistreConnector.class.getCanonicalName());
     		 }
     		 
     		 

     		
        // creer sensorNode Component
		
//		  this.uriSensorNodeURI = AbstractComponent.createComponent(
//		  SensorNodeComponent.class.getCanonicalName(), new
//		  Object[]{nodeinfo,SENSORNODE_COMPONENT_URI,
//		  SENSORNODE_INBOUND_PORT_URI,SensorNode_Registre_OUTBOUND_PORT_URI,sensorsData
//		  }); assert this.isDeployedComponent(this.uriSensorNodeURI);
//		  this.toggleTracing(this.uriSensorNodeURI);
//		  this.toggleLogging(this.uriSensorNodeURI);
		 


        




		// ---------------------------------------------------------------------
		// Connection phase
		// ---------------------------------------------------------------------
        
     		 
        //conection node2 to node1
//        this.doPortConnection(
//            	this.uriSensorNodeURI2,
//            	SensorNode_Node_OUTBOUND_PORT_URI2,
//            	NODE_P2P_INBOUND_PORT_URI ,
//                NodeRegistreConnector.class.getCanonicalName());
        
    

       

        super.deploy();
        assert this.deploymentDone();
    }
    
    @Override
	public void	finalise() throws Exception
	{
		// Port disconnections can be done here for static architectures
		// otherwise, they can be done in the finalise methods of components.
		this.doPortDisconnection(this.uriClientURI,
				CLIENT_Node_OUTBOUND_PORT_URI);
		this.doPortDisconnection(this.uriClientURI,
				CLIENT_Registre_OUTBOUND_PORT_URI);

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