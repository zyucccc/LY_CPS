package cvm;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.helpers.CVMDebugModes;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import nodes.ClientComponent;
import nodes.NodeInfo;
import nodes.SensorNodeComponent;
import nodes.sensor.Sensor;
import sensor_network.Position;
import connectors.URIServiceConnector;

import java.util.ArrayList;
import java.util.HashMap;

public class CVM extends AbstractCVM {
	//component uri
    protected static final String SENSORNODE_COMPONENT_URI = "my-sensornode-uri";
    protected static final String CLIENT_COMPONENT_URI = "my-client-uri";
    //port
    protected static final String SENSORNODE_INBOUND_PORT_URI = "sensornode-inbound-uri";
    protected static final String CLIENT_OUTBOUND_PORT_URI = "client-outbound-uri";

    public CVM() throws Exception {
        super();
    }
    
    /** Reference to the sensor component to share between deploy
	 *  and shutdown.														*/
	protected String	uriSensorNodeURI;
	/** Reference to the client component to share between deploy
	 *  and shutdown.														*/
	protected String	uriClientURI;

	
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
     	//nodeinfo
     		Position position = new Position(0.0,0.0);
     		NodeInfo nodeinfo = new NodeInfo("node1",position,0.0,null,null);
//     		Sensor sensor_temperature = new Sensor("sensor1","temperature",int.class,35);
//     		ArrayList<Sensor> sensorlist = new ArrayList<Sensor>();
//     		sensorlist.add(sensor_temperature);
            Sensor SensorData_temperature = new Sensor("node1","temperature",double.class,35.0);
            Sensor SensorData_fumée = new Sensor("node1","fumée",Boolean.class,true);
     		
            HashMap<String, Sensor> sensorsData = new HashMap<String, Sensor>();
            sensorsData.put("temperature",SensorData_temperature);
            sensorsData.put("fumée",SensorData_fumée);
     		
        // creer sensorNode Component
     		this.uriSensorNodeURI =
            AbstractComponent.createComponent(
                SensorNodeComponent.class.getCanonicalName(),
                new Object[]{nodeinfo,SENSORNODE_COMPONENT_URI, SENSORNODE_INBOUND_PORT_URI,sensorsData});
        assert this.isDeployedComponent(this.uriSensorNodeURI);
        this.toggleTracing(this.uriSensorNodeURI);
        this.toggleLogging(this.uriSensorNodeURI);

        // creer client Component
        this.uriClientURI =
            AbstractComponent.createComponent(
                ClientComponent.class.getCanonicalName(),
                new Object[]{CLIENT_COMPONENT_URI, CLIENT_OUTBOUND_PORT_URI});
        assert this.isDeployedComponent(this.uriClientURI);
        this.toggleTracing(this.uriClientURI);
        this.toggleLogging(this.uriClientURI);

		// ---------------------------------------------------------------------
		// Connection phase
		// ---------------------------------------------------------------------
        
        this.doPortConnection(
        	this.uriClientURI,
            CLIENT_OUTBOUND_PORT_URI,
            SENSORNODE_INBOUND_PORT_URI,
            URIServiceConnector.class.getCanonicalName());

        super.deploy();
        assert this.deploymentDone();
    }
    
    @Override
	public void	finalise() throws Exception
	{
		// Port disconnections can be done here for static architectures
		// otherwise, they can be done in the finalise methods of components.
		this.doPortDisconnection(this.uriClientURI,
				CLIENT_OUTBOUND_PORT_URI);

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