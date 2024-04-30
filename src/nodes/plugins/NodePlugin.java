package nodes.plugins;

import client.connectors.ClientAsynRequestConnector;
import fr.sorbonne_u.components.AbstractPlugin;
import fr.sorbonne_u.components.AbstractPort;
import fr.sorbonne_u.components.ComponentI;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.components.interfaces.OfferedCI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.QueryResultI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestContinuationI;
import fr.sorbonne_u.cps.sensor_network.interfaces.RequestI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PCI;
import fr.sorbonne_u.cps.sensor_network.network.interfaces.SensorNodeP2PImplI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingCI;
import fr.sorbonne_u.cps.sensor_network.nodes.interfaces.RequestingImplI;
import nodes.NodeInfo;
import nodes.SensorNodeComponent;
import nodes.connectors.NodeNodeConnector;
import nodes.ports.*;
import registre.interfaces.RegistrationCI;
import request.ExecutionState;
import request.ProcessingNode;
import request.RequestContinuation;
import request.ast.Direction;
import request.ast.Query;
import request.ast.interpreter.Interpreter;
import sensor_network.ConnectionInfo;
import sensor_network.EndPointDescriptor;
import sensor_network.Position;
import sensor_network.QueryResult;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class NodePlugin extends AbstractPlugin implements SensorNodeP2PImplI, RequestingImplI {
    private static final long serialVersionUID = 1L;

    //outbound port for Registre
    protected SensorNodeRegistreOutboundPort node_registre_port;
    protected String node_Registre_outboundPortURI;

    //outbound port for Node2Node
    protected NodeNodeOutboundPort node_node_NE_Outport;
    protected NodeNodeOutboundPort node_node_NW_Outport;
    protected NodeNodeOutboundPort node_node_SE_Outport;
    protected NodeNodeOutboundPort node_node_SW_Outport;

    //inbound port for client et node2node
    protected SensorNodeInboundPort InboundPort_toClient;
    protected NodeNodeInboundPort InboundPort_P2PtoNode;
    //inbound port URI
    protected String sensorNodeInboundPortURI;
    protected String node_node_InboundPortURI;

    //pool thread pour traiter les requetes async provenant des nodes
    protected int index_poolthread_receiveAsync;
    protected String uri_pool_receiveAsync = AbstractPort.generatePortURI();
    protected int nbThreads_poolReceiveAsync = 10;

    //pool thread pour traiter les requetes async provenant du client
    protected int index_poolthread_receiveAsync_Client;
    protected String uri_pool_receiveAsync_Client = AbstractPort.generatePortURI();
    protected int nbThreads_poolReceiveAsync_Client = 10;

    //pool thread pour traiter les requetes de connection(askConnection,askDisconnection
    protected int index_poolthread_ReceiveConnection;
    protected String uri_pool_ReceiveConnection = AbstractPort.generatePortURI();
    protected int nbThreads_Receiveconnection = 4;

    //gestion Concurrence
    //proteger neighbours avec 4 OutBoundPorts (NE,NW,SE,SW
    protected final ReentrantLock neighbours_OutPorts_lock = new ReentrantLock();

        public NodePlugin(String node_Registre_outboundPortURI,String sensorNodeInboundPortURI,String node_node_InboundPortURI) {
            super();
            this.node_Registre_outboundPortURI = node_Registre_outboundPortURI;
            this.sensorNodeInboundPortURI = sensorNodeInboundPortURI;
            this.node_node_InboundPortURI = node_node_InboundPortURI;
        }

    // -------------------------------------------------------------------------
    // Plug-in life-cycle
    // -------------------------------------------------------------------------
    @Override
    public void	installOn(ComponentI owner) throws Exception
    {
        super.installOn(owner);

        this.addRequiredInterface(RegistrationCI.class);

        this.addRequiredInterface(SensorNodeP2PCI.class);
        this.addOfferedInterface(SensorNodeP2PCI.class);

        this.addOfferedInterface(RequestingCI.class);
    }

    @Override
    public void	initialise() throws Exception
    {
        // ---------------------------------------------------------------------
        // publish des ports
        // ---------------------------------------------------------------------
        this.InboundPort_P2PtoNode = new NodeNodeInboundPort(this.node_node_InboundPortURI,this.getOwner(),this.getPluginURI());
        this.InboundPort_P2PtoNode.publishPort();

        this.InboundPort_toClient = new SensorNodeInboundPort(this.sensorNodeInboundPortURI,this.getOwner(),this.getPluginURI());
        this.InboundPort_toClient.publishPort();

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

        // ---------------------------------------------------------------------
        // Configuration des pools de threads
        // ---------------------------------------------------------------------
        this.index_poolthread_receiveAsync = this.createNewExecutorService(this.uri_pool_receiveAsync, this.nbThreads_poolReceiveAsync,false);
        this.index_poolthread_receiveAsync_Client = this.createNewExecutorService(this.uri_pool_receiveAsync_Client, this.nbThreads_poolReceiveAsync_Client,false);
        this.index_poolthread_ReceiveConnection = this.createNewExecutorService(this.uri_pool_ReceiveConnection, this.nbThreads_Receiveconnection,false);
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

        if(this.InboundPort_P2PtoNode.connected()) {
            this.InboundPort_P2PtoNode.doDisconnection();
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

        try{
        this.InboundPort_P2PtoNode.unpublishPort();
        this.InboundPort_toClient.unpublishPort();
        }catch (Exception e){
//            throw new ComponentShutdownException(e);
            e.printStackTrace();
        }

        this.removeRequiredInterface(RegistrationCI.class);

        this.removeRequiredInterface(SensorNodeP2PCI.class);
        this.removeOfferedInterface(SensorNodeP2PCI.class);

        this.removeOfferedInterface(RequestingCI.class);

        super.uninstall();
    }
    // -------------------------------------------------------------------------
    // Plug-in methods : getters
    // -------------------------------------------------------------------------
    public ReentrantLock getNeighbours_OutPorts_lock() {
        return this.neighbours_OutPorts_lock;
    }

    //pools de threads:
    public int get_Index_poolthread_receiveAsync(){
        return this.index_poolthread_receiveAsync;
    }
    public int get_Index_poolthread_receiveAsync_Client(){
        return this.index_poolthread_receiveAsync_Client;
    }
    public int get_Index_poolthread_ReceiveConnection(){
        return this.index_poolthread_ReceiveConnection;
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
                        this.connecter(selectedOutboundPort, neighbourInfo);
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
    // SensorNodeP2PCI.class required methodes
    // -------------------------------------------------------------------------


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
                this.connecter(selectedOutboundPort, newNeighbour);
            } else {
                // si pas de connection pour le outport,on fait connecter
                this.connecter(selectedOutboundPort, newNeighbour);
            }
            self_neighbours.put(direction, newNeighbour);
        }catch (Exception e) {
            System.err.println("ask4Connection: "+nodeinfo.nodeIdentifier() + " Failed to connect to neighbour " + newNeighbour.nodeIdentifier() + ": " + e.getMessage());
        }finally {
            this.neighbours_OutPorts_lock.unlock();
        }
    }

    //deleguer les operation de connecter un outport choisi a un neighbour
    public void connecter(NodeNodeOutboundPort selectedOutboundPort,NodeInfoI newNeighbour) throws Exception {
        NodeInfo nodeinfo = ((SensorNodeComponent)this.getOwner()).getNodeinfo();
        EndPointDescriptor endpointDescriptor = (EndPointDescriptor) newNeighbour.p2pEndPointInfo();
        if (endpointDescriptor != null) {
            String neighbourInboundPortURI = endpointDescriptor.getInboundPortURI();
            this.logMessage("SensorNodeComponent :"+ nodeinfo.nodeIdentifier() +" start connect : Uri obtenue to connect :" + neighbourInboundPortURI);

            this.getOwner().doPortConnection(selectedOutboundPort.getPortURI(),neighbourInboundPortURI, NodeNodeConnector.class.getCanonicalName());

            this.logMessage("SensorNodeComponent : "+ nodeinfo.nodeIdentifier() + ": Connection established with Node :" + newNeighbour.nodeIdentifier());
        } else {
            throw new Exception("p2pEndPointInfo() did not return an instance of EndPointDescriptor");
        }
    }

    // ---------------------------------------------------------------------
    // continuer a propager les request continuation
    // ---------------------------------------------------------------------
    public void propagerQuery(RequestContinuationI request) throws Exception {
        this.logMessage("-----------------Propager Query------------------");
        NodeInfo nodeinfo = ((SensorNodeComponent)this.getOwner()).getNodeinfo();

        ExecutionState data = (ExecutionState) request.getExecutionState();
        //deal with direction
        if(data.isDirectional()) {
            Set<Direction> dirs = data.getDirections_ast();
            //if noMoreHops , on stop propager Request Direction
            if(!data.noMoreHops()) {
                for (Direction dir : dirs) {
                    //plugin:
                    NodeNodeOutboundPort selectedOutboundPort = this.getOutboundPortByDirection(dir);
                    if(selectedOutboundPort.connected()) {
                        if(!request.isAsynchronous()) {
                            this.logMessage(nodeinfo.nodeIdentifier()+" Sending request Sync Directional: "+request.requestURI()+" dir to :"+dir);
                            selectedOutboundPort.execute(request);}
                        else {
                            //if async,on fait un copie du request
                            RequestContinuation copie_request = new RequestContinuation((RequestContinuation) request);
                            this.logMessage(nodeinfo.nodeIdentifier()+" Sending request ASync Directional: "+request.requestURI()+" dir to:"+dir);
                            selectedOutboundPort.executeAsync(copie_request);
                        }
                    }
                }
            }
        }else
            //deal with fooding
            if(data.isFlooding()){
                for(Direction dir : Direction.values()) {
                    NodeNodeOutboundPort selectedOutboundPort = this.getOutboundPortByDirection(dir);
                    if(selectedOutboundPort.connected()) {
                        if(!request.isAsynchronous()) {
                            this.logMessage(nodeinfo.nodeIdentifier()+" Sending request Sync flooding: "+request.requestURI()+" dir :"+dir);
                            selectedOutboundPort.execute(request);
                        }else {
                            this.logMessage(nodeinfo.nodeIdentifier()+" Sending request Async flooding: "+request.requestURI()+" dir :"+dir);
                            //if async,on fait un copie du request
                            RequestContinuation copie_request = new RequestContinuation((RequestContinuation) request);
                            selectedOutboundPort.executeAsync(copie_request);
                        }
                    }
                }
            } else {
                System.err.println("Erreur type de Cont");
            }
    }

    // ---------------------------------------------------------------------
    // Traiter les requetes Sync from Client
    // ---------------------------------------------------------------------
    public QueryResultI execute(RequestI request) throws Exception{
        NodeInfo nodeinfo = ((SensorNodeComponent)this.getOwner()).getNodeinfo();
        ReentrantReadWriteLock sensorData_lock = ((SensorNodeComponent)this.getOwner()).getSensorData_lock();

        this.logMessage("----------------Receive Query Sync------------------");
        this.logMessage("SensorNodeComponent "+nodeinfo.nodeIdentifier()+" : receive request Sync");
        Interpreter interpreter = new Interpreter();
        Query<?> query = (Query<?>) request.getQueryCode();
        ExecutionState data = new ExecutionState();
        data.updateProcessingNode(((SensorNodeComponent)this.getOwner()).getProcessingNode());

        sensorData_lock.readLock().lock();
        QueryResult result = (QueryResult) query.eval(interpreter, data);
        sensorData_lock.readLock().unlock();

        this.logMessage("----------------Res Actuel----------------------");
        this.logMessage("Resultat du requete Sync: " + result);

        if(data.isDirectional()||data.isFlooding()) {
            RequestContinuation requestCont = new RequestContinuation(request,data);
            //pour enregister les nodes deja traite pour ce request(Flooding)
//            requestCont.addVisitedNode(nodeinfo);
            requestCont.addVisitedNode(nodeinfo.nodeIdentifier());
            this.propagerQuery(requestCont);
        }

        QueryResult result_all = (QueryResult) data.getCurrentResult();

        this.logMessage("------------------Res ALL----------------------");
        this.logMessage("Resultat du requete Sync: " + result_all);
        this.logMessage("--------------------------------------");
        return result_all;
    }

    // ---------------------------------------------------------------------
    // Traiter les requetes Sync from Nodes
    // ---------------------------------------------------------------------
    public QueryResultI execute(RequestContinuationI requestCont) throws Exception{
        NodeInfo nodeinfo = ((SensorNodeComponent)this.getOwner()).getNodeinfo();
        ProcessingNode processingNode = ((SensorNodeComponent)this.getOwner()).getProcessingNode();
        ReentrantReadWriteLock sensorData_lock = ((SensorNodeComponent)this.getOwner()).getSensorData_lock();
        //si cette node actuel est deja traite par cette request recu,on ignorer et return direct
        //pour eviter le Probleme: deadlock caused by Call_back
        //ex: node 1 send request flooding to node2,node2 send encore request to node1
        if (((RequestContinuation)requestCont).getVisitedNodes().contains(nodeinfo.nodeIdentifier())) {
            return null;
        }
//        ((RequestContinuation) requestCont).addVisitedNode(nodeinfo);
        ((RequestContinuation) requestCont).addVisitedNode(nodeinfo.nodeIdentifier());
        Interpreter interpreter = new Interpreter();
        ExecutionState data = (ExecutionState) requestCont.getExecutionState();
        //chaque fois on recevoit un request de flooding
        //on check si ce node est dans max_distance de propager ce request
        //si oui ,on continue a collecter les infos de node actuel
        //si non,on return le res precedent
        if(data.isFlooding()) {
            this.logMessage(nodeinfo.nodeIdentifier()+" receive flooding request Sync: "+requestCont.requestURI());
            Position actuel_position = (Position) nodeinfo.nodePosition();
            if(!data.withinMaximalDistance(actuel_position)) {
				this.logMessage("Hors distance");
                return data.getCurrentResult();
            }
        }
        if(data.isDirectional()){
            this.logMessage(nodeinfo.nodeIdentifier()+" receive directional request Sync");
            data.incrementHops();
        }

        this.logMessage("---------------Receive Query Continuation Sync---------------");
        this.logMessage("SensorNodeComponent "+nodeinfo.nodeIdentifier()+" : receive request sync: "+requestCont.requestURI());
        Query<?> query = (Query<?>) requestCont.getQueryCode();
        data.updateProcessingNode(processingNode);

        sensorData_lock.readLock().lock();
        QueryResultI result;
        try {
            result = (QueryResult) query.eval(interpreter, data);
        }finally {
            sensorData_lock.readLock().unlock();
        }

        this.logMessage("----------------Resultat Actuel Sync----------------------");
        this.logMessage("Resultat Continuational de node actuel: " + result);

        this.propagerQuery(requestCont);
        this.logMessage("------------------------------------");
        return result;
    }

    // ---------------------------------------------------------------------
    // Traiter les requetes Async from Client
    // ---------------------------------------------------------------------
    public void executeAsync(RequestI request) throws Exception{
        NodeInfo nodeinfo = ((SensorNodeComponent)this.getOwner()).getNodeinfo();
        ProcessingNode processingNode = ((SensorNodeComponent)this.getOwner()).getProcessingNode();
        ReentrantReadWriteLock sensorData_lock = ((SensorNodeComponent)this.getOwner()).getSensorData_lock();
        SensorNodeAsynRequestOutboundPort node_asynRequest_Outport = ((SensorNodeComponent)this.getOwner()).getNode_asynRequest_Outport();

        this.logMessage("----------------Receive Query Async------------------");
        this.logMessage("SensorNodeComponent "+nodeinfo.nodeIdentifier()+" : receive request Async: "+request.requestURI());
        Interpreter interpreter = new Interpreter();
        Query<?> query = (Query<?>) request.getQueryCode();
        ExecutionState data = new ExecutionState();
        data.updateProcessingNode(processingNode);

        sensorData_lock.readLock().lock();
        QueryResult result = (QueryResult) query.eval(interpreter, data);
        sensorData_lock.readLock().unlock();

        this.logMessage("----------------Resultat Async Actuel----------------------");
        this.logMessage("Resultat du quete Async: " + result);

        if(data.isDirectional()||data.isFlooding()) {
            //if cest un request continuation
            RequestContinuation requestCont = new RequestContinuation(request,data);
            //pour enregister les nodes deja traite pour ce request
//            requestCont.addVisitedNode(nodeinfo);
            requestCont.addVisitedNode(nodeinfo.nodeIdentifier());
            //traiter la fin du request:
            if(data.isDirectional()){
                //si no more hops ou pas de neighbours pour la direction demandé,on renvoie le resultat au client
                if(data.noMoreHops()) {
                    this.logMessage("Requete directionnelle fini "+requestCont.requestURI()+" : No more hops");
                    this.logMessage(nodeinfo.nodeIdentifier() + " envoyer Res du request Async Direction: "+requestCont.getExecutionState().getCurrentResult() );
                    ((SensorNodeComponent)this.getOwner()).renvoyerAsyncRes(requestCont);
                    return;
                } else if (checkNeighboursDansDirection(requestCont)) {
                    this.logMessage("Requete directionnelle fini "+requestCont.requestURI()+" : No neighbours dans la direction");
                    this.logMessage(nodeinfo.nodeIdentifier() + " envoyer Res du request Async Direction: "+requestCont.getExecutionState().getCurrentResult() );
                    ((SensorNodeComponent)this.getOwner()).renvoyerAsyncRes(requestCont);
                    return;
                }
            }else if(data.isFlooding()) {
                // verifier si tous les neighbours ne sont pas dans le portee de request flooding (sauf les neighbous deja visite)
                //si cest le cas,renvoyer le res actuel au client
                if(checkNeighboursDansPortee(requestCont)) {
                    this.logMessage("Requete flooding fini "+requestCont.requestURI()+" : No neighbours dans le portee");
                    this.logMessage(nodeinfo.nodeIdentifier() + " envoyer Res du request Async Flooding: "+requestCont.getExecutionState().getCurrentResult() );
                    ((SensorNodeComponent)this.getOwner()).renvoyerAsyncRes(requestCont);
                    return;
                }
            }
            this.propagerQuery(requestCont);
        }else{
            //if cest un request ECont,renvoyer directement le res a Client
            ConnectionInfo co_info = (ConnectionInfo) request.clientConnectionInfo();
            String InboundPortUri = ((EndPointDescriptor)co_info.endPointInfo()).getInboundPortURI();
            if(node_asynRequest_Outport.connected()) {
                node_asynRequest_Outport.doDisconnection();
            }
            node_asynRequest_Outport.doConnection(InboundPortUri, ClientAsynRequestConnector.class.getCanonicalName());
            QueryResult result_all = (QueryResult) data.getCurrentResult();
            node_asynRequest_Outport.acceptRequestResult(request.requestURI(),result_all);
        }
    }

    // ---------------------------------------------------------------------
    // Traiter les requetes Async from Nodes
    // ---------------------------------------------------------------------
    public void executeAsync(RequestContinuationI requestCont) throws Exception{
        NodeInfo nodeinfo = ((SensorNodeComponent)this.getOwner()).getNodeinfo();
        ProcessingNode processingNode = ((SensorNodeComponent)this.getOwner()).getProcessingNode();
        ReentrantReadWriteLock sensorData_lock = ((SensorNodeComponent)this.getOwner()).getSensorData_lock();

        //si cette node actuel est deja traite par cette request recu,on ignorer et return direct
        //pour eviter le Probleme: deadlock caused by Call_back
        //ex: node 1 send request flooding to node2,node2 send encore request to node1
        if (!((RequestContinuation)requestCont).getVisitedNodes().contains(nodeinfo.nodeIdentifier())) {


//            ((RequestContinuation) requestCont).addVisitedNode(nodeinfo);
            ((RequestContinuation) requestCont).addVisitedNode(nodeinfo.nodeIdentifier());
            Interpreter interpreter = new Interpreter();
            ExecutionState data = (ExecutionState) requestCont.getExecutionState();
            //chaque fois on recevoit un request de flooding
            //on check si ce node est dans max_distance de propager ce request
            //si oui ,on continue a collecter les infos de node actuel
            //si non,on return le res precedent
            if(data.isFlooding()) {
                this.logMessage(nodeinfo.nodeIdentifier()+" receive flooding request Async: "+requestCont.requestURI());
                Position actuel_position = (Position) nodeinfo.nodePosition();
                if(!data.withinMaximalDistance(actuel_position)) {
				this.logMessage("Hors distance");
                    return;
                }
            }
            if(data.isDirectional()){
                this.logMessage(nodeinfo.nodeIdentifier()+" receive directional request Async: "+requestCont.requestURI());
                data.incrementHops();
            }

            this.logMessage("---------------Receive Query Continuation Async---------------");
            this.logMessage("SensorNodeComponent "+nodeinfo.nodeIdentifier()+" : receive request Async");
            Query<?> query = (Query<?>) requestCont.getQueryCode();
            data.updateProcessingNode(processingNode);


            this.logMessage("----------------Res Actuel Async----------------------");

            sensorData_lock.readLock().lock();
            QueryResultI result;
            try {
                result = (QueryResult) query.eval(interpreter, data);
            }finally {
                sensorData_lock.readLock().unlock();
            }

            this.logMessage("Res Cont de node actuel: " + result);

            //traiter la fin du request:
            if(data.isDirectional()){
                //si no more hops ou pas de neighbours pour la direction demandé,on renvoie le resultat au client
                if(data.noMoreHops()) {
                    this.logMessage("Requete directionnelle fini "+requestCont.requestURI()+" : No more hops");
                    this.logMessage(nodeinfo.nodeIdentifier() + " envoyer Res du request Async Direction: "+requestCont.getExecutionState().getCurrentResult() );
                    ((SensorNodeComponent)this.getOwner()).renvoyerAsyncRes(requestCont);
                    return;
                } else if (checkNeighboursDansDirection(requestCont)) {
                    this.logMessage("Requete directionnelle fini "+requestCont.requestURI()+" : No neighbours dans la direction");
                    this.logMessage(nodeinfo.nodeIdentifier() + " envoyer Res du request Async Direction: "+requestCont.getExecutionState().getCurrentResult() );
                    ((SensorNodeComponent)this.getOwner()).renvoyerAsyncRes(requestCont);
                    return;
                }
            }else if(data.isFlooding()) {
                // verifier si tous les neighbours ne sont pas dans le portee de request flooding (sauf les neighbous deja visite)
                //si cest le cas,renvoyer le res actuel au client
                if(checkNeighboursDansPortee(requestCont)) {
                    this.logMessage("Requete flooding fini "+requestCont.requestURI()+" : No neighbours dans le portee");
                    this.logMessage(nodeinfo.nodeIdentifier() + " envoyer Res du request Async Flooding: "+requestCont.getExecutionState().getCurrentResult() );
                    ((SensorNodeComponent)this.getOwner()).renvoyerAsyncRes(requestCont);
                    return;
                }
            }

            this.propagerQuery(requestCont);
            this.logMessage("------------------------------------");

        }
    }

    //verifier si il y a pas de neighbours dans la direction de request Directional
    public Boolean checkNeighboursDansDirection (RequestContinuationI requestCont) {
        ConcurrentHashMap<Direction, NodeInfoI> self_neighbours = ((SensorNodeComponent)this.getOwner()).getNeighbours();
        ExecutionState data = (ExecutionState) requestCont.getExecutionState();
        Set<Direction> dirs = data.getDirections_ast();
        for (Direction dir : dirs) {
            NodeInfoI neighbour = self_neighbours.get(dir);
            if (neighbour != null) {
                return false;
            }
        }
        return true;
    }

    // verifier si tous les neighbours ne sont pas dans le portee de request flooding (sauf les neighbous deja visite)
    public Boolean checkNeighboursDansPortee (RequestContinuationI requestCont) {
        ConcurrentHashMap<Direction, NodeInfoI> self_neighbours = ((SensorNodeComponent)this.getOwner()).getNeighbours();
        for (Map.Entry<Direction, NodeInfoI> entry : self_neighbours.entrySet()) {
            NodeInfoI neighbour = entry.getValue();
            //si ce neighbours est deja visite
            if (((RequestContinuation) requestCont).getVisitedNodes().contains(neighbour.nodeIdentifier())) {
                continue;
            }
            // check les autres neighbours
            ExecutionState data = (ExecutionState) requestCont.getExecutionState();
            if (data.withinMaximalDistance(neighbour.nodePosition())) {
                // si au moins 1 neighbours non visite est dans portee,return false
                return false;
            }
        }
        //tous les neighbours non visitees est dehors le portee:
        return true;
    }






}
