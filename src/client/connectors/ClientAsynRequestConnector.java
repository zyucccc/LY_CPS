package client.connectors;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;

/**
 * The class <code>ClientAsynRequestConnector</code> implements the connector
 * for the client component to send a request to the server component.

 */
public class ClientAsynRequestConnector extends AbstractConnector implements RequestResultCI {

    @Override
    public void acceptRequestResult(String requestURI, QueryResultI result) throws Exception {
        ((RequestResultCI)this.offering).acceptRequestResult(requestURI, result);
    }
}
