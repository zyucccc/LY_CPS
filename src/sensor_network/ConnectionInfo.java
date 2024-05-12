package sensor_network;

import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI;

/**
 * The class <code>ConnectionInfo</code> implements the connection information.
 * for the sensor node component
 */
public class ConnectionInfo implements ConnectionInfoI {
    private static final long serialVersionUID = 1L;
    private String nodeIdentifier;
    private EndPointDescriptorI endPointDescriptor;

    public ConnectionInfo(String nodeIdentifier, EndPointDescriptorI endPointDescriptor) {
        this.nodeIdentifier = nodeIdentifier;
        this.endPointDescriptor = endPointDescriptor;
    }

    @Override
    public String nodeIdentifier() {
        return this.nodeIdentifier;
    }

    @Override
    public EndPointDescriptorI endPointInfo() {
        return this.endPointDescriptor;
    }
}
