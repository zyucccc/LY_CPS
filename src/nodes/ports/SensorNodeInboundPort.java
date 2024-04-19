package nodes.ports;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import nodes.SensorNodeComponent;

public class SensorNodeInboundPort extends	AbstractInboundPort implements RequestingCI{
	
	private static final long serialVersionUID = 1L;
	
	public SensorNodeInboundPort (String uri,ComponentI owner) throws Exception{
		super(uri,RequestingCI.class, owner) ;
		assert	uri != null && owner instanceof SensorNodeComponent ;
	}
	
	public SensorNodeInboundPort (ComponentI owner) throws Exception{
		super(RequestingCI.class, owner) ;
		assert owner instanceof SensorNodeComponent ;
	}

	@Override
	public QueryResultI execute(RequestI request) throws Exception {
		return this.getOwner().handleRequest(((SensorNodeComponent)owner).getIndex_poolthread_receiveSync(),owner -> ((SensorNodeComponent)owner).processRequest(request));
	}

	@Override
	public void executeAsync(RequestI request) throws Exception {
	    this.getOwner().runTask(((SensorNodeComponent)owner).getIndex_poolthread_receiveAsync(),new AbstractComponent.AbstractTask() {
	        @Override
	        public void run() {
	            try {
	                ((SensorNodeComponent)getOwner()).processRequest_Async(request);
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	    });
	}

	

}
