package nodes.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractOutboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import registre.interfaces.RegistrationCI;
import request.ast.Direction;

import java.util.Set;

/**
 * The class <code>SensorNodeRegistreOutboundPort</code> implements the outbound port
 * for the sensor node component to send a request of register,unregister to the registration component.
 *
 */
public class SensorNodeRegistreOutboundPort extends AbstractOutboundPort implements RegistrationCI {
    private static final long serialVersionUID = 1L;

    public SensorNodeRegistreOutboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, RegistrationCI.class, owner);
    }

    public SensorNodeRegistreOutboundPort(ComponentI owner) throws Exception {
        super(RegistrationCI.class, owner);
    }

    @Override
    public boolean registered(String nodeIdentifier) throws Exception {
        return ((RegistrationCI) this.getConnector()).registered(nodeIdentifier);
    }

    @Override
    public Set<NodeInfoI> register(NodeInfoI nodeInfo) throws Exception {
        return ((RegistrationCI) this.getConnector()).register(nodeInfo);
    }


    @Override
    public NodeInfoI findNewNeighbour(NodeInfoI nodeInfo, Direction d) throws Exception {
        return ((RegistrationCI) this.getConnector()).findNewNeighbour(nodeInfo, d);
    }

    @Override
    public void unregister(String nodeIdentifier) throws Exception {
        ((RegistrationCI) this.getConnector()).unregister(nodeIdentifier);
    }
}
