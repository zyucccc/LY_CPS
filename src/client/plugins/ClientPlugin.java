package client.plugins;

import client.ports.ClientOutboundPort;
import client.ports.ClientRegistreOutboundPort;
import fr.sorbonne_u.components.AbstractPlugin;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.GeographicalZoneI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;
import nodes.connectors.NodeClientConnector;

import java.util.Set;

//@RequiredInterfaces(required = {RequestingCI.class, LookupCI.class})
public class ClientPlugin extends AbstractPlugin {
    private static final long serialVersionUID = 1L;
    protected ClientOutboundPort client_node_port;
    protected ClientRegistreOutboundPort client_registre_port;
//    protected String registreInboundPortURI;
    protected String nodeInboundPortURI;

    protected String client_node_OutboundPortUri;
    protected String client_registre_OutboundPortUri;

    public ClientPlugin(String client_node_OutPortUri,String client_registre_OutPortUri) throws Exception {
        super();
        this.client_node_OutboundPortUri = client_node_OutPortUri;
        this.client_registre_OutboundPortUri = client_registre_OutPortUri;
    }

    // -------------------------------------------------------------------------
    // Plug-in life-cycle
    // -------------------------------------------------------------------------
    @Override
    public void	installOn(ComponentI owner) throws Exception
    {
        super.installOn(owner);
        this.addRequiredInterface(RequestingCI.class);
        this.addRequiredInterface(LookupCI.class);
    }
    @Override
    public void	initialise() throws Exception
    {
        super.initialise();
        this.client_node_port =
                new ClientOutboundPort(this.client_node_OutboundPortUri,this.getOwner());
        this.client_node_port.localPublishPort();
        this.client_registre_port =
                new ClientRegistreOutboundPort(this.client_registre_OutboundPortUri,this.getOwner());
        this.client_registre_port.localPublishPort();

    }

    @Override
    public void	finalise() throws Exception
    {
        if (this.client_node_port.connected()) {
            this.client_node_port.doDisconnection();
        }
        this.client_node_port.unpublishPort();
        if (this.client_registre_port.connected()) {
            this.client_registre_port.doDisconnection();
        }
        this.client_registre_port.unpublishPort();
        super.finalise();
    }

    @Override
    public void	uninstall() throws Exception
    {
        this.client_node_port.unpublishPort();
        this.client_registre_port.unpublishPort();
        this.removeRequiredInterface(RequestingCI.class);
        this.removeRequiredInterface(LookupCI.class);
        super.uninstall();
    }

    // -------------------------------------------------------------------------
    // Plug-in methods
    // -------------------------------------------------------------------------

//    public void createAndPublishOutboundPort(String client_node_portUri,String client_registre_portUri) throws Exception {
//        this.client_node_port =
//                new ClientOutboundPort(client_node_portUri,this.getOwner());
//        this.client_node_port.localPublishPort();
//        this.client_registre_port =
//                new ClientRegistreOutboundPort(client_registre_portUri,this.getOwner());
//        this.client_registre_port.localPublishPort();
//    }

//    public void setRegistreInboundPortURI(String uri) {
//       this.registreInboundPortURI = uri;
//    }

    public void setNodeInboundPortURI(String uri) {
        this.nodeInboundPortURI = uri;
    }

    public void connectNode() throws Exception {
        this.getOwner().doPortConnection(
                this.client_registre_port.getPortURI(),
                this.nodeInboundPortURI,
                NodeClientConnector.class.getCanonicalName()
        );
    }
    // -------------------------------------------------------------------------
    // Look-up CI methods
    // -------------------------------------------------------------------------
    public ConnectionInfoI findByIdentifier(String sensorNodeId) throws Exception
    {
       return this.client_registre_port.findByIdentifier(sensorNodeId);
    }

    public Set<ConnectionInfoI> findByZone(GeographicalZoneI z) throws Exception {
        return this.client_registre_port.findByZone(z);
    }

    // -------------------------------------------------------------------------
    // RequestingCI methods
    // -------------------------------------------------------------------------
    public QueryResultI execute(RequestI request) throws Exception {
        return this.client_node_port.execute(request);
    }

    public void executeAsync(RequestI request) throws Exception {
        this.client_node_port.executeAsync(request);
    }


}
