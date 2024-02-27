package client.ports;

import client.ClientComponent;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.GeographicalZoneI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;

import java.util.Set;

public class ClientRegistreOutboundPort extends AbstractOutboundPort implements LookupCI {
    
    private static final long serialVersionUID = 1L;

    public ClientRegistreOutboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, LookupCI.class, owner);
        assert uri != null && owner instanceof ClientComponent;
    }

    public ClientRegistreOutboundPort(ComponentI owner) throws Exception {
        super(LookupCI.class, owner);
        assert owner instanceof ClientComponent;
    }

    @Override
    public ConnectionInfoI findByIdentifier(String sensorNodeId) throws Exception {
        return ((LookupCI) this.getConnector()).findByIdentifier(sensorNodeId);
    }

    @Override
    public Set<ConnectionInfoI> findByZone(GeographicalZoneI z) throws Exception {
        return ((LookupCI) this.getConnector()).findByZone(z);
    }
}
