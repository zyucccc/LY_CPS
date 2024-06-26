package nodes.connectors;

import fr.sorbonne_u.components.connectors.AbstractConnector;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import registre.interfaces.RegistrationCI;
import request.ast.Direction;

import java.util.Set;

/**
 * The class <code>NodeRegistreConnector</code> implements the connector between
 * the sensor node component and the registration component.
 *
 * <p><strong>Description</strong></p>
 *
 * <p>
 * The connector implements the <code>RegistrationCI</code> interface and
 * delegates the calls to the registration component.
 * </p>
 *
 */
public class NodeRegistreConnector extends AbstractConnector implements RegistrationCI {
    @Override
    public boolean registered(String nodeIdentifier) throws Exception {
       
        return ((RegistrationCI)this.offering).registered(nodeIdentifier);
    }

    @Override
    public Set<NodeInfoI> register(NodeInfoI nodeInfo) throws Exception {
        
        return ((RegistrationCI)this.offering).register(nodeInfo);
    }

    
	@Override
    public NodeInfoI findNewNeighbour(NodeInfoI nodeInfo, Direction d) throws Exception {
        return ((RegistrationCI)this.offering).findNewNeighbour(nodeInfo, d);
    }
	

    @Override
    public void unregister(String nodeIdentifier) throws Exception {
        ((RegistrationCI)this.offering).unregister(nodeIdentifier);
    }

}
