package request.ast;


import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import request.ast.interfaces.IASTvisitable;
import request.ast.interfaces.IASTvisitor;
import sensor_network.Position;

import java.io.Serializable;

public abstract class Base implements IASTvisitable, Serializable {
	private static final long serialVersionUID = 1L;
	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }

}
