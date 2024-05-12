package client.connectors;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.GeographicalZoneI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;

import java.util.Set;

/**
 * The class <code>ClientRegistreConnector</code> implements the connector
 * for the client component to send a request to the registration component.

 */
public class ClientRegistreConnector extends AbstractConnector implements LookupCI {

    @Override
    public ConnectionInfoI findByIdentifier(String sensorNodeId) throws Exception {
        return ((LookupCI)this.offering).findByIdentifier(sensorNodeId);
    }

    @Override
    public Set<ConnectionInfoI> findByZone(GeographicalZoneI z) throws Exception {
        return ((LookupCI)this.offering).findByZone(z);
    }
}
