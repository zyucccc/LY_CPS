package registre;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
import fr.sorbonne_u.cps.sensor_network.interfaces.ConnectionInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.GeographicalZoneI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.registry.interfaces.LookupCI;

import nodes.NodeInfo;
import registre.ports.LookupInboundPort;
import registre.interfaces.RegistrationCI;
//import registry.ports.RegistrationInboundPort;
//import registry.ports.LookupInboundPort;
import registre.ports.RegistrationInboundPort;
import request.ast.Direction;
import sensor_network.Position;
import sensor_network.ConnectionInfo;


@OfferedInterfaces(offered = {RegistrationCI.class, LookupCI.class})
public class RegistreComponent extends AbstractComponent {
    protected String registrationInboundPortURI;
    protected String lookupInboundPortURI;
    //pool thread client
    protected int index_poolthread_client;
    protected String uri_pool_client = "registre-pool-thread-client";
    protected int nbThreads_poolClient = 5;
    //pool thread node
    //plus rapide pour des node,moins rapide pour des clients
    protected int index_poolthread_node;
    protected String uri_pool_node = "registre-pool-thread-node";
    protected int nbThreads_poolNode = 50;

    //port
    protected RegistrationInboundPort registrationPort;
    protected LookupInboundPort lookupPort;

    //Gestion Concurrence : Technique concurrentHashMap
    //hashmap pour stocker les noeuds deja register
    private ConcurrentHashMap<String, NodeInfo> registeredNodes;

    protected RegistreComponent(String uriPrefix,String registrationInboundPortURI, String lookupInboundPortURI) throws Exception {
        super(uriPrefix , 1, 0); // 1 schedule thread
        this.registrationInboundPortURI = registrationInboundPortURI;
        this.lookupInboundPortURI = lookupInboundPortURI;
        
        assert registrationInboundPortURI != null && !registrationInboundPortURI.isEmpty() : "InboundPort URI cannot be null or empty!";
        assert lookupInboundPortURI != null && !lookupInboundPortURI.isEmpty() : "InboundPort URI cannot be null or empty!";
        
        //init hashmap
        this.registeredNodes = new ConcurrentHashMap<>();

        //introduire nouveau pool de thread
        this.index_poolthread_client = this.createNewExecutorService(uri_pool_client, nbThreads_poolClient, false);
        this.index_poolthread_node = this.createNewExecutorService(uri_pool_node, nbThreads_poolNode, false);
        
        this.registrationPort = new RegistrationInboundPort(registrationInboundPortURI, this);
        this.registrationPort.publishPort();

        this.lookupPort = new LookupInboundPort(lookupInboundPortURI, this);
        this.lookupPort.publishPort();
        
		if (AbstractCVM.isDistributed) {
			this.getLogger().setDirectory(System.getProperty("user.dir"));
		} else {
			this.getLogger().setDirectory(System.getProperty("user.home"));
		}
		
		this.getTracer().setTitle("Registre");
		this.getTracer().setRelativePosition(2, 0);
    }
    // ---------------------------------------------------------------------
    // Partie getter index pool thread
    // ---------------------------------------------------------------------
    public int getIndex_poolthread_client() {
        return this.index_poolthread_client;
    }
    public int getIndex_poolthread_node() {
        return this.index_poolthread_node;
    }
    // ---------------------------------------------------------------------
    // Partie Registration.CI  for Sensor Node
    // ---------------------------------------------------------------------
    public boolean registered(String nodeIdentifier) throws Exception {
           return registeredNodes.containsKey(nodeIdentifier);
       }
    
    public Set<NodeInfoI> register(NodeInfoI nodeInfo) throws Exception {
//    	 this.logMessage("------------Receive Register Request---------------");
    	 this.logMessage("RegistreComponent receive request: register() by: "+ nodeInfo.nodeIdentifier());
        if (nodeInfo == null || registered(nodeInfo.nodeIdentifier())) {
            throw new IllegalArgumentException("NodeInfo cannot be null and must not be already registered.");
        }
        
        // ajoute new nodeid - nodeinfo dans hashmao
        this.registeredNodes.put(nodeInfo.nodeIdentifier(), (NodeInfo) nodeInfo);
        
        Set<NodeInfoI> neighbours_possible = findNeighboursInAllDirections((NodeInfo) nodeInfo);
       
        //check range
        Set<NodeInfoI> neighbours = new HashSet<>();
        for (NodeInfoI possibleNeighbour : neighbours_possible) {
            if (isConnectable(nodeInfo, possibleNeighbour)) {
                neighbours.add((NodeInfo) possibleNeighbour);
            }
        }
       
        
        assert neighbours.stream().allMatch(n -> n != null) : "All returned nodes must be non-null.";
        assert neighbours.stream().noneMatch(n -> n.nodeIdentifier().equals(nodeInfo.nodeIdentifier())) : "The new node must not be included in the returned set.";
        assert neighbours.stream().allMatch(n -> 
            nodeInfo.nodePosition().distance(n.nodePosition()) <= nodeInfo.nodeRange() ||
            nodeInfo.nodePosition().distance(n.nodePosition()) <= n.nodeRange())
            : "All returned nodes must be within the communication range of the new node or vice versa.";
        
        this.logMessage("RegistreComponent :"+nodeInfo.nodeIdentifier()+" register() Succes!");
        return neighbours;
    }
    
    private Set<NodeInfoI> findNeighboursInAllDirections(NodeInfoI nodeInfo) throws Exception {
        Set<NodeInfoI> neighbours = new HashSet<>();
        for (Direction d : Direction.values()) {
            NodeInfoI neighbour = findNewNeighbour(nodeInfo, d);
            if (neighbour != null) {
                neighbours.add(neighbour);
            }
        }
  
        return neighbours;
    }
    
    
    
    public NodeInfoI findNewNeighbour(NodeInfoI nodeInfo, Direction d) throws Exception {
    	  NodeInfo closestNeighbour = null;
          double closestDistance = Double.MAX_VALUE;

          for (NodeInfoI potentialNeighbour : registeredNodes.values()) {
              // assurer que on compare pas le node it-self
              if (!potentialNeighbour.nodeIdentifier().equals(nodeInfo.nodeIdentifier())) {
                  Direction potentialDirection = ((Position)nodeInfo.nodePosition()).directionTo_ast(potentialNeighbour.nodePosition());
                 //on trouve un noeud dans la direction indique
                  if (potentialDirection == d) {
                      double distance = nodeInfo.nodePosition().distance(potentialNeighbour.nodePosition());
                      //&& distance <= nodeInfo.nodeRange() Ici on check pas le contraint de range,on le fait dans register boucle
                      //on check si la distance est dans le portee des 2 ranges
                      if (distance < closestDistance && isConnectable(nodeInfo,potentialNeighbour)) {
                          closestNeighbour = (NodeInfo) potentialNeighbour;
                          closestDistance = distance;
                      }
                  }
              }
          }

          return closestNeighbour;
    }

    private boolean isConnectable(NodeInfoI newNode, NodeInfoI existingNode) {
        double distance = newNode.nodePosition().distance(existingNode.nodePosition());
        return distance <= newNode.nodeRange() && distance <= existingNode.nodeRange();
    }
    
    public boolean unregister(String nodeIdentifier) throws Exception {
        if (!registered(nodeIdentifier)) {
            throw new Exception("Node is not registered: " + nodeIdentifier);
        }
        registeredNodes.remove(nodeIdentifier);
        return true;
    }


    // ---------------------------------------------------------------------
    // Partie Lookup.CI   for client
    // ---------------------------------------------------------------------
    public ConnectionInfoI findByIdentifier(String sensorNodeId) throws Exception {
    	this.logMessage("------------Receive Client Request (By Id)---------------\n"+"RegistreComponent receive request de Client : findByIdentifer() avec NodeID :"+ sensorNodeId);
    	NodeInfo nodeInfo = registeredNodes.get(sensorNodeId);
        if (nodeInfo != null) {
        	this.logMessage("RegistreComponent : findByIdentifer() NodeID :"+ sensorNodeId + "NodeInfo : " + nodeInfo);
            return new ConnectionInfo(sensorNodeId, nodeInfo.endPointInfo());
        }
        return null;
    }

   
    public Set<ConnectionInfoI> findByZone(GeographicalZoneI z) throws Exception {
        Set<ConnectionInfoI> result = new HashSet<>();
        for (NodeInfo nodeInfo : registeredNodes.values()) {
            if (z.in(nodeInfo.nodePosition())) {
                result.add(new ConnectionInfo(nodeInfo.nodeIdentifier(), nodeInfo.endPointInfo()));
            }
        }
        return result;
    }

    // ---------------------------------------------------------------------
    // Partie Cycle de vie
    // ---------------------------------------------------------------------

    @Override
    public void start() throws ComponentStartException {
        super.start();
        this.logMessage("RegistreComponent started.");
    }

    @Override
    public void finalise() throws Exception {
        super.finalise();
        this.logMessage("RegistreComponent finalising...");
    }

    @Override
    public void shutdown() throws ComponentShutdownException {
        try{
             this.registrationPort.unpublishPort();
             this.lookupPort.unpublishPort();
             this.shutdownExecutorService(this.uri_pool_client);
             this.shutdownExecutorService(this.uri_pool_node);
        }catch (Exception e) {
            throw new ComponentShutdownException(e);
        }
        super.shutdown();
        this.logMessage("RegistreComponent shutting down.");
    }

}
