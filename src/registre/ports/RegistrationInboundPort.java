package registre.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import registre.RegistreComponent;
import registre.interfaces.RegistrationCI;
import request.ast.Direction;

import java.util.Set;

/**
 * The class <code>RegistrationInboundPort</code> implements the inbound port
 * for the registration component to receive requests from the client component.

 */
public class RegistrationInboundPort extends AbstractInboundPort implements RegistrationCI {
    
    private static final long serialVersionUID = 1L;

    public RegistrationInboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, RegistrationCI.class, owner);
        assert uri != null && owner instanceof RegistreComponent;
    }

    public RegistrationInboundPort(ComponentI owner) throws Exception {
        super(RegistrationCI.class, owner);
        assert owner instanceof RegistreComponent;
    }

    //ici,nous appelons le pool de thread distinct pour les noeuds

    @Override
    public boolean registered(String nodeIdentifier) throws Exception {
        return this.getOwner().handleRequest(((RegistreComponent)owner).getIndex_poolthread_node(),owner -> {
            return ((RegistreComponent) owner).registered(nodeIdentifier);
            });
        }
    

    @Override
    public Set<NodeInfoI> register(NodeInfoI nodeInfo) throws Exception {
        return  this.getOwner().handleRequest(((RegistreComponent)owner).getIndex_poolthread_node(),owner -> {
            return ((RegistreComponent) owner).register(nodeInfo);
        });
    }

    
    @Override
    public NodeInfoI findNewNeighbour(NodeInfoI nodeInfo, Direction d) throws Exception {
        return this.getOwner().handleRequest(((RegistreComponent)owner).getIndex_poolthread_node(),owner -> {
      	  return (NodeInfoI)((RegistreComponent)owner).findNewNeighbour(nodeInfo,d);
        }
      		  );
  }

    @Override
    public void unregister(String nodeIdentifier) throws Exception {
    	this.getOwner().handleRequest(((RegistreComponent)owner).getIndex_poolthread_node(),owner -> {
            return ((RegistreComponent) owner).unregister(nodeIdentifier);
        });
            
}

}
