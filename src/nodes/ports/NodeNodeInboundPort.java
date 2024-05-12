package nodes.ports;

import client.ClientComponent;
import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PImplI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import nodes.SensorNodeComponent;
import nodes.plugins.NodePlugin;

/**
 * The class <code>NodeNodeInboundPort</code> implements the inbound port
 * for the sensor node component to receive the result of a request.
 */
public class NodeNodeInboundPort extends	AbstractInboundPort  implements SensorNodeP2PCI{
	private static final long serialVersionUID = 1L;
	
	public NodeNodeInboundPort (String uri,ComponentI owner) throws Exception{
		super(uri,SensorNodeP2PCI.class, owner) ;
		assert	uri != null && owner instanceof SensorNodeComponent ;
	}
	
	public NodeNodeInboundPort (ComponentI owner) throws Exception{
		super(SensorNodeP2PCI.class, owner) ;
		assert owner instanceof SensorNodeComponent ;
	}

	public NodeNodeInboundPort (String uri,ComponentI owner,String pluginURI) throws Exception{
		super(uri,SensorNodeP2PCI.class, owner,pluginURI,null);
		assert	owner.isInstalled(pluginURI);
	}

	public NodeNodeInboundPort (ComponentI owner,String pluginURI) throws Exception{
          super(SensorNodeP2PCI.class, owner,pluginURI,null);
		  assert	owner.isInstalled(pluginURI);
	}

@Override
public void ask4Disconnection(NodeInfoI neighbour) throws Exception {
	this.getOwner().runTask(((SensorNodeComponent)owner).getIndex_poolthread_ReceiveConnection(),new AbstractComponent.AbstractTask(this.getPluginURI()) {
		@Override
		public void run() {
			try {
				((SensorNodeP2PImplI)this.getTaskProviderReference()).ask4Disconnection(neighbour);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	});
}

@Override
public void ask4Connection(NodeInfoI newNeighbour) throws Exception {
	this.getOwner().runTask(((SensorNodeComponent)owner).getIndex_poolthread_ReceiveConnection(),new AbstractComponent.AbstractTask(this.getPluginURI()) {
		@Override
		public void run() {
			try {
				((SensorNodeP2PImplI)this.getTaskProviderReference()).ask4Connection(newNeighbour);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	});
}


@Override
public QueryResultI execute(RequestContinuationI request) throws Exception {
	return this.getOwner().handleRequest(
			new AbstractComponent.AbstractService<QueryResultI>(this.getPluginURI()){
				@Override
				public QueryResultI call() throws Exception {
					return ((SensorNodeP2PImplI )this.getServiceProviderReference()).execute(request);
				}
	});
}

//ici,nous appelons le pool de thread distinct pour traiter les requêtes async provenant des noeuds
@Override
public void executeAsync(RequestContinuationI requestContinuation) throws Exception {
	this.getOwner().runTask(((SensorNodeComponent)owner).getIndex_poolthread_receiveAsync(),new AbstractComponent.AbstractTask(this.getPluginURI()) {
		@Override
		public void run() {
			try {
				((SensorNodeP2PImplI )this.getTaskProviderReference()).executeAsync(requestContinuation);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	});
}
}
