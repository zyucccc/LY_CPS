package nodes.connectors;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;

public class NodeClientConnector extends AbstractConnector implements RequestingCI{

	@Override
    public QueryResultI execute(RequestI request) throws Exception {
		//deleguer a sensor node
        // 将调用转发给服务端实现
        return ((RequestingCI)this.offering).execute(request);
    }

    @Override
    public void executeAsync(RequestI request) throws Exception {
    	//deleguer a sensor node
        // 将异步调用转发给服务端实现
        ((RequestingCI)this.offering).executeAsync(request);
    }
    
}
