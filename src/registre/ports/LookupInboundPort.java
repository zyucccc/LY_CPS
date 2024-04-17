package registre.ports;

import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.GeographicalZoneI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;
import registre.RegistreComponent;

import java.util.Set;

public class LookupInboundPort extends AbstractInboundPort implements LookupCI {

    private static final long serialVersionUID = 1L;

    public LookupInboundPort(String uri, ComponentI owner) throws Exception {
        super(uri, LookupCI.class, owner);
        assert uri != null && owner instanceof RegistreComponent;
    }

    public LookupInboundPort(ComponentI owner) throws Exception {
        super(LookupCI.class, owner);
        assert owner instanceof RegistreComponent;
    }

    //ici,nous appelons le pool de thread distinct pour les clients

    @Override
    public ConnectionInfoI findByIdentifier(String sensorNodeId) throws Exception {
    	return this.getOwner().handleRequest(((RegistreComponent)owner).getIndex_poolthread_client(),owner -> {
            return ((RegistreComponent)owner).findByIdentifier(sensorNodeId);
    	});
        }
    

    @Override
    public Set<ConnectionInfoI> findByZone(GeographicalZoneI z) throws Exception {
        return this.getOwner().handleRequest(((RegistreComponent)owner).getIndex_poolthread_client(),owner -> {
            return ((RegistreComponent)owner).findByZone(z);
        });
        }
    
}
