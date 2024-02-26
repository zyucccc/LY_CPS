package nodes;

import fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import sensor_network.Position;

public class NodeInfo implements NodeInfoI {
    private static final long serialVersionUID = 1L;
    
	private final String nodeId; // 节点的唯一标识符
    private final Position position; // 节点的位置
    private final double range; // 节点的通信范围
    private final EndPointDescriptorI p2pEndPoint; // 用于P2P连接的端点信息
    private final EndPointDescriptorI clientEndPoint; // 用于客户端连接的端点信息

    // 构造函数
    public NodeInfo(String nodeId, PositionI position, double range, EndPointDescriptorI p2pEndPoint, EndPointDescriptorI clientEndPoint) {
//        assert nodeId != null && !nodeId.isEmpty() : "NodeInfo.constructor: nodeId cannot be null or empty";
//        assert position != null : "NodeInfo.constructor: null position";
//        assert range > 0.0 : "NodeInfo.constructor: range must be positive";
//        assert p2pEndPoint != null : "NodeInfo.constructor: null p2pEndPoint";
//        assert clientEndPoint != null : "NodeInfo.constructor: null clientEndPoint";

        this.nodeId = nodeId;
        this.position = (Position) position;
        this.range = range;
        this.p2pEndPoint = p2pEndPoint;
        this.clientEndPoint = clientEndPoint;
    }

    // 实现 NodeInfoI 和 ConnectionInfoI 接口的方法
    @Override
    public String nodeIdentifier() {
        return this.nodeId;
    }

    @Override
    public PositionI nodePosition() {
        return this.position;
    }

    @Override
    public double nodeRange() {
        return this.range;
    }

    @Override
    public EndPointDescriptorI p2pEndPointInfo() {
        return this.p2pEndPoint;
    }

    @Override
    public EndPointDescriptorI endPointInfo() {
        return this.clientEndPoint;
    }
}