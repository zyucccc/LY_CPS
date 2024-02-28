package nodes.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import nodes.SensorNodeComponent;

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

	@Override
	public void ask4Disconnection(NodeInfoI neighbour) throws Exception {
		/*
		 * this.getOwner().handleRequest(owner -> { return ((SensorNodeComponent)
		 * owner).ask4Disconnection(neighbours); });
		 */
	}

	@Override
	public void ask4Connection(NodeInfoI newNeighbour) throws Exception {
		/*
		 * this.getOwner().handleRequest(owner -> { return ((SensorNodeComponent)
		 * owner).ask4Connection(newNeighbour); });
		 */		
	}

	@Override
	public QueryResultI execute(RequestContinuationI request) throws Exception {
		/*
		 * return this.getOwner().handleRequest(owner -> { return
		 * (QueryResultI)((SensorNodeComponent)owner).execute(request); } );
		 */
		return null;
	}

	@Override
	public void executeAsync(RequestContinuationI requestContinuation) throws Exception {
		/*this.getOwner().handleRequest(owner -> {
            return ((SensorNodeComponent) owner).executeAsync(requestContinuation);
        });
	*/	
	}
}
