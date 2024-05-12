package nodes.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;

/**
 * The class <code>SensorNodeAsynRequestOutboundPort</code> implements the outbound port
 * for the sensor node component to send results of request Async to the client component.
 */
public class SensorNodeAsynRequestOutboundPort extends AbstractOutboundPort implements RequestResultCI {
    private static final long serialVersionUID = 1L;

    public SensorNodeAsynRequestOutboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, RequestResultCI.class, owner);
        assert uri != null && owner != null;
    }

    public SensorNodeAsynRequestOutboundPort(ComponentI owner) throws Exception {
        super(RequestResultCI.class, owner);
        assert owner != null;
    }

    @Override
    public void acceptRequestResult(String requestURI, QueryResultI result) throws Exception {
        ((RequestResultCI)this.getConnector()).acceptRequestResult(requestURI, result);
    }
}
