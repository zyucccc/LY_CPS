package nodes.connectors;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;

/**
 * The class <code>NodeClientConnector</code> implements the connector between
 * the client component and the sensor node component.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * The connector implements the <code>RequestingCI</code> interface and
 * delegates the calls to the sensor node component.
 * </p>
 *
 * @see fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI
 * @see fr.sorbonne_u.cps.sensor_network.interfaces.RequestI
 * @see fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI
 *
 */
public class NodeClientConnector extends AbstractConnector implements RequestingCI{

	@Override
    public QueryResultI execute(RequestI request) throws Exception {
		//deleguer a sensor node
        return ((RequestingCI)this.offering).execute(request);
    }

    @Override
    public void executeAsync(RequestI request) throws Exception {
    	//deleguer a sensor node
        ((RequestingCI)this.offering).executeAsync(request);
    }
    
}
