package request.ast.astBase;

import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import request.ast.Base;

public class ABase extends Base{
	
	public ABase(PositionI position) {
        this.position = position;
    }
	

    public PositionI getPosition() {
        return this.position;
    }

}
