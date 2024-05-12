package client.plugins;

import client.ClientComponent;
import client.ports.ClientOutboundPort;
import client.ports.ClientRegistreOutboundPort;
import fr.sorbonne_u.components.AbstractPlugin;
import fr.sorbonne_u.components.AbstractPort;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.annotations.RequiredInterfaces;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.GeographicalZoneI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;
import nodes.connectors.NodeClientConnector;
import sensor_network.ConnectionInfo;
import sensor_network.EndPointDescriptor;

import java.util.Set;

/**
 * The class <code>ClientPlugin</code> implements a plug-in for the client
 * component that provides the client with the services to connect to the
 * sensor network and to send requests to the nodes in the network.
 */
public class ClientPlugin extends AbstractPlugin {
    private static final long serialVersionUID = 1L;
    protected ClientOutboundPort client_node_port;
    protected ClientRegistreOutboundPort client_registre_port;
    protected String nodeInboundPortURI;

    protected String client_node_OutboundPortUri = AbstractPort.generatePortURI();
    protected String client_registre_OutboundPortUri;

    //Introduire les pools threads
    //pool thread pour recevoir les resultats des requete Async from Node
    protected int index_poolthread_receiveAsync;
    protected String uri_pool_receiveAsync = AbstractPort.generatePortURI();
    protected int nbThreads_poolReceiveAsync = 40;
    //pool thread pour envoyer les requetes async aux nodes
    protected int index_poolthread_sendAsync;
    protected String uri_pool_sendAsync = AbstractPort.generatePortURI();
    protected int nbThreads_poolSendAsync = 5;

    //nous transmettons les URI des ports ici car nous fait la connection entre client et registre dans CVM
    public ClientPlugin(String client_registre_OutPortUri) throws Exception {
        super();
        this.client_registre_OutboundPortUri = client_registre_OutPortUri;
    }

    // -------------------------------------------------------------------------
    // Plug-in life-cycle
    // -------------------------------------------------------------------------
    @Override
    public void	installOn(ComponentI owner) throws Exception
    {
        super.installOn(owner);
        try{
        this.addRequiredInterface(RequestingCI.class);
        this.addRequiredInterface(LookupCI.class);
        }catch (Exception e){
            System.err.println("Erreur Client-Plugin : installOn "+e.toString());
        }
    }

    @Override
    public void	initialise() throws Exception
    {
        super.initialise();
        // ---------------------------------------------------------------------
        // publish des ports
        // ---------------------------------------------------------------------
        this.client_node_port =
                new ClientOutboundPort(this.client_node_OutboundPortUri,this.getOwner());
        this.client_node_port.localPublishPort();
        this.client_registre_port =
                new ClientRegistreOutboundPort(this.client_registre_OutboundPortUri,this.getOwner());
        this.client_registre_port.localPublishPort();

        // ---------------------------------------------------------------------
        // Configuration des pools de threads
        // ---------------------------------------------------------------------
        //pool thread pour les requete Async from Node
        this.index_poolthread_receiveAsync = this.createNewExecutorService(this.uri_pool_receiveAsync, this.nbThreads_poolReceiveAsync,false);
        //pool thread pour les requete Async from Node
        this.index_poolthread_sendAsync = this.createNewExecutorService(this.uri_pool_sendAsync,this.nbThreads_poolSendAsync,true);

    }

    @Override
    public void	finalise() throws Exception
    {
        if (this.client_node_port.connected()) {
            this.client_node_port.doDisconnection();
        }
        if (this.client_registre_port.connected()) {
            this.client_registre_port.doDisconnection();
        }
        super.finalise();
    }

    @Override
    public void	uninstall() throws Exception
    {
        this.client_node_port.unpublishPort();
        this.client_registre_port.unpublishPort();

        this.removeRequiredInterface(RequestingCI.class);
        this.removeRequiredInterface(LookupCI.class);

        //fermer pool de thread
//        ((ClientComponent)this.getOwner()).fermer_poolthread(this.uri_pool_receiveAsync);
//        ((ClientComponent)this.getOwner()).fermer_poolthread(this.uri_pool_sendAsync);


        super.uninstall();
    }

    // -------------------------------------------------------------------------
    // Plug-in methods
    // -------------------------------------------------------------------------
    //publier les infos des pools de threads
    public int get_Index_poolthread_sendAsync(){
       return this.index_poolthread_sendAsync;
    }

    public String get_Uri_pool_sendAsync(){
        return this.uri_pool_sendAsync;
    }

    public int get_Index_poolthread_receiveAsync(){
        return this.index_poolthread_receiveAsync;
    }

    public String get_Uri_pool_receiveAsync(){
        return this.uri_pool_receiveAsync;
    }

    //Soit on fait set uri puis connectNode() ,soit on connect directement avec connectNode(uri)
    public void setNodeInboundPortURI(String uri) {
        this.nodeInboundPortURI = uri;
    }

    public void connectNode() throws Exception {
        try{
        this.getOwner().doPortConnection(
                this.client_node_port.getPortURI(),
                this.nodeInboundPortURI,
                NodeClientConnector.class.getCanonicalName()
        );}
        catch (Exception e){
            System.err.println("Erreur connectNode:"+e.toString());
        }
    }

    public void connectNode(String uri) throws Exception {
        this.getOwner().doPortConnection(
                this.client_node_port.getPortURI(),
                uri,
                NodeClientConnector.class.getCanonicalName()
        );
    }

    // ---------------------------------------------------------------------
    // Obtenir connection info from Registre et connecter
    // ---------------------------------------------------------------------

    public void findEtConnecterByIdentifer(String NodeID) throws Exception {
        this.logMessage("---------------Connect to Node-------------------");
        this.logMessage("ClientComponent search and connect Node :" + NodeID);

        ConnectionInfo connectionInfo = (ConnectionInfo) this.findByIdentifier(NodeID);

        if (connectionInfo != null) {
            String InboundPortURI = ((EndPointDescriptor)connectionInfo.endPointInfo()).getInboundPortURI();
            this.connectNode(InboundPortURI);
            this.logMessage("ClientComponent : Connection established with Node: " + NodeID);
        }else {
            this.logMessage("Node " + NodeID + " not found or cannot connect.");
        }
        this.logMessage("----------------------------------");
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
