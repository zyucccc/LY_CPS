package registre;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import fr.sorbonne_u.components.AbstractComponent;
import fr.sorbonne_u.components.annotations.OfferedInterfaces;
import fr.sorbonne_u.components.cvm.AbstractCVM;
import fr.sorbonne_u.components.exceptions.ComponentStartException;
import fr.sorbonne_u.components.exceptions.ComponentShutdownException;
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
    	  this.logMessage("RegistreComponent receive request: registed()");
           return registeredNodes.containsKey(nodeIdentifier);
       }
    
    public Set<NodeInfoI> register(NodeInfoI nodeInfo) throws Exception {
        // 检查前置条件
        if (nodeInfo == null || registered(nodeInfo.nodeIdentifier())) {
            throw new IllegalArgumentException("NodeInfo cannot be null and must not be already registered.");
        }
        
        // 将新节点加入到注册表中
        this.registeredNodes.put(nodeInfo.nodeIdentifier(), (NodeInfo) nodeInfo);
        
        // 找到新节点可以连接的已注册节点
//        Set<NodeInfo> connectableNodes = new HashSet<>();
        
        Set<NodeInfoI> neighbours_possible = findNeighboursInAllDirections((NodeInfo) nodeInfo);
        Set<NodeInfoI> neighbours = new HashSet<>();
        
        //check range
        for (NodeInfoI possibleNeighbour : neighbours_possible) {
            if (isConnectable(nodeInfo, possibleNeighbour)) {
                neighbours.add((NodeInfo) possibleNeighbour);
            }
        }
       
        
        // 检查后置条件
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
                neighbours.add((NodeInfo) neighbour);
            }
        }
        return neighbours;
    }
    
    
    
    public NodeInfoI findNewNeighbour(NodeInfoI nodeInfo, Direction d) throws Exception {
//    	Position position = new Position(0.0,0.0);
//    	NodeInfoI neighbour = new NodeInfo("test", position, 10.0, null, null);
//        return neighbour; // 示例代码，需要具体实现
    	return null;
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

    
    

}
