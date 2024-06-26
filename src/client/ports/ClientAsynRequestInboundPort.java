package client.ports;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.ports.AbstractInboundPort;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestResultCI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import client.ClientComponent;

/**
 * The class <code>ClientAsynRequestInboundPort</code> implements the inbound port
 * for the client component to receive the result of a request.
 */
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
        this.getOwner().runTask(((ClientComponent)getOwner()).get_Index_poolthread_receiveAsync(),new AbstractComponent.AbstractTask() {
            @Override
            public void run() {
                try {
                    ((ClientComponent)getOwner()).acceptRequestResult(requestURI, result);
                } catch (Exception e) {
                    System.err.println("Erreur: Client accept result Async : "+e.toString());
                    e.printStackTrace();
                }
            }
        });
    }

}
