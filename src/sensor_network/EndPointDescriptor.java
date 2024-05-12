package sensor_network;

import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.cps.sensor_network.interfaces.BCM4JavaEndPointDescriptorI;

/**
 * The class <code>EndPointDescriptor</code> implements the end point descriptor information.
 */
public class EndPointDescriptor implements BCM4JavaEndPointDescriptorI {
    private static final long serialVersionUID = 1L;
	private String uri;

    public EndPointDescriptor(String uri) {
        this.uri = uri;
    }

    @Override
    public String getInboundPortURI() {
        return uri;
    }

    @Override
    public boolean isOfferedInterface(Class<? extends OfferedCI> aClass) {
        return true;
    }

    @Override
    public String toString() {
        return "EndPointDescriptor{" +
                "uri='" + uri + '\'' +
                '}';
    }
}
