package nodes;

import fr.sorbonne_u.cps.sensor_network.interfaces.EndPointDescriptorI;
import fr.sorbonne_u.cps.sensor_network.interfaces.NodeInfoI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import sensor_network.EndPointDescriptor;
import sensor_network.Position;

public class NodeInfo implements NodeInfoI {
    private static final long serialVersionUID = 1L;
    
	private final String nodeId;
    private  Position position;
    private final double range;
    private  EndPointDescriptorI p2pEndPoint;
    private  EndPointDescriptorI clientEndPoint;

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
    public void Setp2pendPointInfo(EndPointDescriptor uriinfo) {
        this.p2pEndPoint = uriinfo;
    }
    
    public void SetendPointInfo(EndPointDescriptor uriinfo) {
        this.clientEndPoint = uriinfo;
    }
    @Override
    public String toString() {
        return "NodeInfo{" +
                "nodeId='" + nodeId + '\'' +
                ", position=" + position +
                ", range=" + range +   
                ", clientEndPoint=" + clientEndPoint +
                '}';
    }
    
//    @Override
//    public String toString() {
//        return "NodeInfo{" +
//                "nodeId='" + nodeId + '\'' +
//                ", position=" + position +
//                ", range=" + range +
//                ", p2pEndPoint=" + p2pEndPoint +
//                ", clientEndPoint=" + clientEndPoint +
//                '}';
//    }


}