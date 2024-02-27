package sensor_network;

import fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI;

public class EndPointDescriptor implements EndPointDescriptorI {
    private static final long serialVersionUID = 1L;
	private String uri;

    public EndPointDescriptor(String uri) {
        this.uri = uri;
    }

    public String getURI() {
        return uri;
    }
    
    @Override
    public String toString() {
        return "EndPointDescriptor{" +
                "uri='" + uri + '\'' +
                '}';
    }
    
}
