package nodes.ports;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingImplI;
import nodes.SensorNodeComponent;
import nodes.plugins.NodePlugin;

/**
 * The class <code>SensorNodeInboundPort</code> implements the inbound port
 * for the sensor node component to treat the request from the client component.
 */
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

	public SensorNodeInboundPort (ComponentI owner,String pluginURI) throws Exception{
		super(RequestingCI.class, owner,pluginURI,null);
		assert	owner.isInstalled(pluginURI);
	}

	public SensorNodeInboundPort (String uri,ComponentI owner,String pluginURI) throws Exception{
		super(uri,RequestingCI.class, owner,pluginURI,null);
		assert	owner.isInstalled(pluginURI);
	}

@Override
public QueryResultI execute(RequestI request) throws Exception {
	return this.getOwner().handleRequest(
			new AbstractComponent.AbstractService<QueryResultI>(this.getPluginURI()){
				@Override
				public QueryResultI call() throws Exception {
					return ((RequestingImplI)this.getServiceProviderReference()).execute(request);
				}
			});
}

//ici,nous appelons le pool de thread distinct pour traiter les requêtes async provenant du client
@Override
public void executeAsync(RequestI request) throws Exception {
	this.getOwner().runTask(((SensorNodeComponent)owner).getIndex_poolthread_receiveAsync_Client(),new AbstractComponent.AbstractTask(this.getPluginURI()) {
		@Override
		public void run() {
			try {
				((RequestingImplI)this.getTaskProviderReference()).executeAsync(request);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	});
}

	

}
