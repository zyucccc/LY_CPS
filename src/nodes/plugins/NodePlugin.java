package nodes.plugins;

import fr.sorbonne_u.components.AbstractPlugin;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PImplI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import nodes.NodeInfo;
import nodes.SensorNodeComponent;
import nodes.ports.NodeNodeOutboundPort;
import nodes.ports.SensorNodeRegistreOutboundPort;
import registre.interfaces.RegistrationCI;
import request.ast.Direction;
import sensor_network.Position;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class NodePlugin extends AbstractPlugin{
    private static final long serialVersionUID = 1L;

    //outbound port for Registre
    protected SensorNodeRegistreOutboundPort node_registre_port;
    protected String node_Registre_outboundPortURI;

    //outbound port for Node2Node
    protected NodeNodeOutboundPort node_node_NE_Outport;
    protected NodeNodeOutboundPort node_node_NW_Outport;
    protected NodeNodeOutboundPort node_node_SE_Outport;
    protected NodeNodeOutboundPort node_node_SW_Outport;

    //gestion Concurrence
    //proteger neighbours avec OutBoundPorts
    protected final ReentrantLock neighbours_OutPorts_lock = new ReentrantLock();


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
        this.addOfferedInterface(SensorNodeP2PCI.class);
    }

    @Override
    public void	initialise() throws Exception
    {
        // ---------------------------------------------------------------------
        // publish des ports
        // ---------------------------------------------------------------------
        this.node_registre_port = new SensorNodeRegistreOutboundPort(this.node_Registre_outboundPortURI, this.getOwner());
        this.node_registre_port.localPublishPort();

        this.node_node_NE_Outport = new NodeNodeOutboundPort(this.getOwner());
        this.node_node_NE_Outport.localPublishPort();
        this.node_node_NW_Outport = new NodeNodeOutboundPort(this.getOwner());
        this.node_node_NW_Outport.localPublishPort();
        this.node_node_SE_Outport = new NodeNodeOutboundPort(this.getOwner());
        this.node_node_SE_Outport.localPublishPort();
        this.node_node_SW_Outport = new NodeNodeOutboundPort(this.getOwner());
        this.node_node_SW_Outport.localPublishPort();
        super.initialise();
    }

    @Override
    public void	finalise() throws Exception
    {
        if(this.node_registre_port.connected()) {
            this.node_registre_port.doDisconnection();
        }

        if (this.node_node_NE_Outport.connected()) {
            this.node_node_NE_Outport.doDisconnection();
        }

        if (this.node_node_NW_Outport.connected()) {
            this.node_node_NW_Outport.doDisconnection();
        }

        if (this.node_node_SE_Outport.connected()) {
            this.node_node_SE_Outport.doDisconnection();
        }

        if (this.node_node_SW_Outport.connected()) {
            this.node_node_SW_Outport.doDisconnection();
        }
        super.finalise();
    }

    @Override
    public void	uninstall() throws Exception
    {
        //depublish les ports
        this.node_node_NE_Outport.unpublishPort();
        this.node_node_NW_Outport.unpublishPort();
        this.node_node_SE_Outport.unpublishPort();
        this.node_node_SW_Outport.unpublishPort();

        this.node_registre_port.unpublishPort();



        this.removeRequiredInterface(RegistrationCI.class);
        this.removeOfferedInterface(SensorNodeP2PCI.class);

        super.uninstall();
    }
    // -------------------------------------------------------------------------
    // Plug-in methods : getters
    // -------------------------------------------------------------------------
    public ReentrantLock getNeighbours_OutPorts_lock() {
        return this.neighbours_OutPorts_lock;
    }

    // -------------------------------------------------------------------------
    // Plug-in methods sur register et connecter
    // -------------------------------------------------------------------------
    public void sendNodeInfoToRegistre(NodeInfoI nodeInfo) throws Exception {
        ConcurrentHashMap<Direction, NodeInfoI> self_neighbours = ((SensorNodeComponent)this.getOwner()).getNeighbours();
        NodeInfo nodeinfo = ((SensorNodeComponent)this.getOwner()).getNodeinfo();

        this.logMessage("----------------Register------------------");
        this.logMessage("SensorNodeComponent sendNodeInfo to Registre " );

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

    public void connecterNeighbours() throws Exception {
        //ici on utilise neighbours_OutPorts_lock parce que on veut proteger la processus de connecter les neighbours
        //ca veut dire que la processus de connecter les neighbours ne peut pas etre interrompu par ask4connection ou ask4disconnection from d'autre nodes
        //en bref: on mettre connecterNeighours et ask4connection en mutuellement exclusif
        this.neighbours_OutPorts_lock.lock();
        try {
            NodeInfo nodeinfo = ((SensorNodeComponent)this.getOwner()).getNodeinfo();
            ConcurrentHashMap<Direction, NodeInfoI> self_neighbours = ((SensorNodeComponent)this.getOwner()).getNeighbours();

            this.logMessage("----------------Connecter Neighbours-----------------");
            this.logMessage("SensorNodeComponent [" + nodeinfo.nodeIdentifier() + "] start connecter ses neighbours");

            for (Map.Entry<Direction, NodeInfoI> neighbour : self_neighbours.entrySet()) {

                Direction direction = neighbour.getKey();
                NodeInfoI neighbourInfo = neighbour.getValue();
                NodeNodeOutboundPort selectedOutboundPort = this.getOutboundPortByDirection(direction);
                if (selectedOutboundPort != null) {
                    try {
                        if (selectedOutboundPort.connected()) {
                            this.getOwner().doPortDisconnection(selectedOutboundPort.getPortURI());
                        }
                        ((SensorNodeComponent)this.getOwner()).connecter(selectedOutboundPort, neighbourInfo);
                        selectedOutboundPort.ask4Connection(nodeinfo);
                    } catch (Exception e) {
                        System.err.println("connecterNeighbours: " + nodeinfo.nodeIdentifier() + " Failed to connect to neighbour at direction " + direction + ": " + e.getMessage());
                    }
                } else {
                    System.err.println("No outbound port selected for direction " + direction);
                }
            }
        }finally {
            this.neighbours_OutPorts_lock.unlock();
        }
        this.logMessage("----------------------------------------------");
    }

    //retourne les outport correspondant selon le direction donnee
    public NodeNodeOutboundPort getOutboundPortByDirection(Direction direction) {
        // choose correct outport by direction
        switch (direction) {
            case NE:
                return this.node_node_NE_Outport;
            case NW:
                return this.node_node_NW_Outport;
            case SE:
                return this.node_node_SE_Outport;
            case SW:
                return this.node_node_SW_Outport;
            default:
                return null;
        }
    }


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

    // -------------------------------------------------------------------------
    // SensorNodeP2PCI.class offered methodes
    // -------------------------------------------------------------------------
    public void ask4Disconnection(NodeInfoI neighbour) throws Exception {
        //proteger neighbours avec OutBoundPorts,on s'assure que les operations sur les neighbours et les OutBoundPorts ne vont pas etre interrompu
        NodeInfo nodeinfo = ((SensorNodeComponent)this.getOwner()).getNodeinfo();
        this.neighbours_OutPorts_lock.lock();
        try {
            ConcurrentHashMap<Direction, NodeInfoI> self_neighbours = ((SensorNodeComponent)this.getOwner()).getNeighbours();

            Direction direction = ((Position)nodeinfo.nodePosition()).directionTo_ast(neighbour.nodePosition());
            //plugin:
            NodeNodeOutboundPort selectedOutboundPort = this.getOutboundPortByDirection(direction);
            if (selectedOutboundPort.connected()) {
                this.logMessage(nodeinfo.nodeIdentifier() + "essayer de disconnect:" + neighbour.nodeIdentifier());
                this.getOwner().doPortDisconnection(selectedOutboundPort.getPortURI());
            }
            self_neighbours.remove(direction);
        }catch (Exception e) {
            System.err.println("ask4Disconnection: "+ nodeinfo.nodeIdentifier() + " Failed to Disconnect to neighbour : " + e.getMessage());
        }finally {
            this.neighbours_OutPorts_lock.unlock();
        }

    }

    public void ask4Connection(NodeInfoI newNeighbour) throws Exception {
        NodeInfo nodeinfo = ((SensorNodeComponent)this.getOwner()).getNodeinfo();
        //proteger neighbours avec OutBoundPorts,on s'assure que les operations sur les neighbours et les OutBoundPorts ne vont pas etre interrompu
        this.neighbours_OutPorts_lock.lock();
        try {
            this.logMessage(nodeinfo.nodeIdentifier()+" receive ask4Connection from "+newNeighbour.nodeIdentifier());
            ConcurrentHashMap<Direction, NodeInfoI> self_neighbours = ((SensorNodeComponent)this.getOwner()).getNeighbours();
            // check direction de newNeighbour
            Direction direction = ((Position)nodeinfo.nodePosition()).directionTo_ast(newNeighbour.nodePosition());
            //plugin:
            NodeNodeOutboundPort selectedOutboundPort = this.getOutboundPortByDirection(direction);
            // check if deja connected
            if (selectedOutboundPort.connected()) {
                //disconnter d'abord
                this.getOwner().doPortDisconnection(selectedOutboundPort.getPortURI());
                // connecter
                ((SensorNodeComponent)this.getOwner()).connecter(selectedOutboundPort, newNeighbour);
            } else {
                // si pas de connection pour le outport,on fait connecter
                ((SensorNodeComponent)this.getOwner()).connecter(selectedOutboundPort, newNeighbour);
            }
            self_neighbours.put(direction, newNeighbour);
        }catch (Exception e) {
            System.err.println("ask4Connection: "+nodeinfo.nodeIdentifier() + " Failed to connect to neighbour " + newNeighbour.nodeIdentifier() + ": " + e.getMessage());
        }finally {
            this.neighbours_OutPorts_lock.unlock();
        }
    }





}
