package client.ports;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import client.ClientComponent;

public class ClientAsynRequestInboundPort extends AbstractInboundPort implements RequestResultCI {
    private static final long serialVersionUID = 1L;

    public ClientAsynRequestInboundPort(String uri, AbstractComponent owner) throws Exception {
        super(uri, RequestResultCI.class, owner);
        assert uri != null && owner instanceof ClientComponent;
    }

    public ClientAsynRequestInboundPort(AbstractComponent owner) throws Exception {
        super(RequestResultCI.class, owner);
        assert owner instanceof ClientComponent;
    }

    @Override
    public void acceptRequestResult(String requestURI, QueryResultI result) throws Exception {
//        this.getOwner().handleRequest(owner -> {
//            ((ClientComponent)owner).handleReceivedRequestResult(requestURI, result);
//            return null; // Since the method does not return anything.
//        });
    }
}
