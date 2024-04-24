package nodes.plugins;

import fr.sorbonne_u.components.AbstractPlugin;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import nodes.NodeInfo;
import nodes.SensorNodeComponent;
import nodes.ports.SensorNodeRegistreOutboundPort;
import registre.interfaces.RegistrationCI;
import request.ast.Direction;
import sensor_network.Position;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class NodePlugin extends AbstractPlugin {
    private static final long serialVersionUID = 1L;

    //outbound port for Registre
    protected SensorNodeRegistreOutboundPort node_registre_port;
    protected String node_Registre_outboundPortURI;

        public NodePlugin(String node_Registre_outboundPortURI) {
            super();
            this.node_Registre_outboundPortURI = node_Registre_outboundPortURI;
        }

    // -------------------------------------------------------------------------
    // Plug-in life-cycle
    // -------------------------------------------------------------------------
    @Override
    public void	installOn(ComponentI owner) throws Exception
    {
        super.installOn(owner);
        this.addRequiredInterface(RegistrationCI.class);
//        this.addOfferedInterface(RequestingCI.class);
//        this.addOfferedInterface(SensorNodeP2PCI.class);
    }

    @Override
    public void	initialise() throws Exception
    {
        // ---------------------------------------------------------------------
        // publish des ports
        // ---------------------------------------------------------------------
        this.node_registre_port = new SensorNodeRegistreOutboundPort(this.node_Registre_outboundPortURI, this.getOwner());
        this.node_registre_port.localPublishPort();
        super.initialise();
    }

    @Override
    public void	finalise() throws Exception
    {
        if(this.node_registre_port.connected()) {
            this.node_registre_port.doDisconnection();
        }
        super.finalise();
    }

    @Override
    public void	uninstall() throws Exception
    {
        this.node_registre_port.unpublishPort();

        this.removeRequiredInterface(RegistrationCI.class);
//        this.removeOfferedInterface(RequestingCI.class);
//        this.removeOfferedInterface(SensorNodeP2PCI.class);

        super.uninstall();
    }
    // -------------------------------------------------------------------------
    // Plug-in methods
    // -------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // RegistrationCI.class methodes
    // -------------------------------------------------------------------------
    public boolean	registered(String nodeIdentifier)throws Exception{
        return this.node_registre_port.registered(nodeIdentifier);
    }

    public Set<NodeInfoI> register(NodeInfoI nodeInfo) throws Exception{
        return this.node_registre_port.register(nodeInfo);
    }

    public NodeInfoI findNewNeighbour(NodeInfoI nodeInfo, Direction d) throws Exception{
        return this.node_registre_port.findNewNeighbour(nodeInfo,d);
    }

    public void	unregister(String nodeIdentifier) throws Exception{
        this.node_registre_port.unregister(nodeIdentifier);
    }

    public void sendNodeInfoToRegistre(NodeInfoI nodeInfo) throws Exception {
        this.logMessage("----------------Register------------------");
        this.logMessage("SensorNodeComponent sendNodeInfo to Registre " );
        ConcurrentHashMap<Direction, NodeInfoI> self_neighbours = ((SensorNodeComponent)this.getOwner()).getNeighbours();
        NodeInfo nodeinfo = ((SensorNodeComponent)this.getOwner()).getNodeinfo();

//		     Boolean registed_before = this.node_registre_port.registered(nodeInfo.nodeIdentifier());
//		     this.logMessage("Registered before register? " + nodeInfo.nodeIdentifier()+"Boolean:"+registed_before);
        Set<NodeInfoI> neighbours = this.register(nodeInfo);
        for (NodeInfoI neighbour : neighbours) {
            Direction dir = ((Position)nodeinfo.nodePosition()).directionTo_ast(neighbour.nodePosition());
            self_neighbours.put(dir, neighbour);
        }
        this.logMessage("--------------Register() neighbours:--------------");
        this.logMessage("neighbours:");
        for (Map.Entry<Direction, NodeInfoI> neighbour : self_neighbours.entrySet()) {
            this.logMessage("neighbour de driection "+neighbour.getKey()+" :"+((NodeInfo)neighbour.getValue()).toString());
        }

//		     Boolean registed_after = this.node_registre_port.registered(nodeInfo.nodeIdentifier());
//		     this.logMessage("Registered after register? " + nodeInfo.nodeIdentifier()+"Boolean:"+registed_after);
        this.logMessage("----------------------------------------");

    }




}
