package registre;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
    protected String uriPrefix;
    
    private HashMap<String, NodeInfo> registeredNodes;

    protected RegistreComponent(String uriPrefix,String registrationInboundPortURI, String lookupInboundPortURI) throws Exception {
        super(uriPrefix , 1, 0); // 假设有一个调度线程池，没有普通线程池
        this.registrationInboundPortURI = registrationInboundPortURI;
        this.lookupInboundPortURI = lookupInboundPortURI;
        
        assert registrationInboundPortURI != null && !registrationInboundPortURI.isEmpty() : "InboundPort URI cannot be null or empty!";
        assert lookupInboundPortURI != null && !lookupInboundPortURI.isEmpty() : "InboundPort URI cannot be null or empty!";
        
        //init hashmap
        this.registeredNodes = new HashMap<>();
        
        // 创建并发布Registration服务的入站端口
        RegistrationInboundPort registrationPort = new RegistrationInboundPort(registrationInboundPortURI, this);
        registrationPort.publishPort();

        // 创建并发布Lookup服务的入站端口
        LookupInboundPort lookupPort = new LookupInboundPort(lookupInboundPortURI, this);
        lookupPort.publishPort();
        
		if (AbstractCVM.isDistributed) {
			this.getLogger().setDirectory(System.getProperty("user.dir"));
		} else {
			this.getLogger().setDirectory(System.getProperty("user.home"));
		}
		
		this.getTracer().setTitle("Registre");
		this.getTracer().setRelativePosition(2, 0);
    }

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
        super.shutdown();
        this.logMessage("RegistreComponent shutting down.");
    }
    
    //service offered : Registration
    //new
    public boolean registered(String nodeIdentifier) throws Exception {
//    	  this.logMessage("RegistreComponent receive request: registed()");
           return registeredNodes.containsKey(nodeIdentifier);
       }
    
    public Set<NodeInfoI> register(NodeInfoI nodeInfo) throws Exception {
    	 this.logMessage("------------Receive Register Request---------------");
    	 this.logMessage("RegistreComponent receive request: register() by: "+ nodeInfo.nodeIdentifier());
        if (nodeInfo == null || registered(nodeInfo.nodeIdentifier())) {
            throw new IllegalArgumentException("NodeInfo cannot be null and must not be already registered.");
        }
        
        // ajoute new nodeid - nodeinfo dans hashmao
        this.registeredNodes.put(nodeInfo.nodeIdentifier(), (NodeInfo) nodeInfo);
        
        Set<NodeInfoI> neighbours_possible = findNeighboursInAllDirections((NodeInfo) nodeInfo);
        Set<NodeInfoI> neighbours = new HashSet<>();
        
        //check range
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
        
        this.logMessage("RegistreComponent : register() Succes!");
        return neighbours;
    }
    
    public Set<NodeInfoI> refraichir_neighbours(NodeInfoI nodeInfo) throws Exception {
//   	 this.logMessage("RegistreComponent receive request: refraichir_register()by: "+ nodeInfo.nodeIdentifier());
       if (nodeInfo == null || !registered(nodeInfo.nodeIdentifier())) {
           throw new IllegalArgumentException("NodeInfo cannot be null and must be already registered.");
       }
       
       Set<NodeInfoI> neighbours_possible = findNeighboursInAllDirections((NodeInfo) nodeInfo);
       Set<NodeInfoI> neighbours = new HashSet<>();
       
       //check range
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
                  Direction potentialDirection = ((Position)nodeInfo.nodePosition()).directionFrom_ast(potentialNeighbour.nodePosition());
                 //on trouve un noeud dans la direction indique
                  if (potentialDirection == d) {
                      double distance = nodeInfo.nodePosition().distance(potentialNeighbour.nodePosition());
                      //&& distance <= nodeInfo.nodeRange() Ici on check pas le contraint de range,on le fait dans register boucle
                      if (distance < closestDistance ) {
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
        return distance <= newNode.nodeRange() || distance <= existingNode.nodeRange();
    }
    
    public boolean unregister(String nodeIdentifier) throws Exception {
        if (!registered(nodeIdentifier)) {
            throw new Exception("Node is not registered: " + nodeIdentifier);
        }
        registeredNodes.remove(nodeIdentifier);
        return true;
        //这个节点移除后 它的邻居的行为逻辑需要添加
    }
    
    
//service offered : LookupCI
    
    public ConnectionInfoI findByIdentifier(String sensorNodeId) throws Exception {
    	this.logMessage("------------Receive Client Request---------------");
    	this.logMessage("RegistreComponent receive request de Client : findByIdentifer() avec NodeID :"+ sensorNodeId);
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
    
    

}
