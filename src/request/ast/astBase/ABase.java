package request.ast.astBase;


import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import request.ast.Base;
import request.ast.interfaces.IASTvisitor;
import sensor_network.Position;

public class ABase extends Base{
	
	public ABase(PositionI position) {
        this.position = position;
    }
	

    public PositionI getPosition() {
        return this.position;
    }

    @Override
	public <Result, Data, Anomaly extends Throwable> 
    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
            throws Anomaly {
        return visitor.visit(this, data);
    }
}
