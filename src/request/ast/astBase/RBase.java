package request.ast.astBase;

import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import fr.sorbonne_u.cps.sensor_network.requests.interfaces.ProcessingNodeI;
import request.ast.Base;

public class RBase extends Base{

	private ProcessingNodeI node;

    public RBase(ProcessingNodeI node) {
        this.node = node;
    }

    @Override
    public PositionI getPosition() {
        return node.getPosition(); 
    }
}
