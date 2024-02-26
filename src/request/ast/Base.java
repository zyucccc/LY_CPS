package request.ast;

import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import request.ast.interfaces.IASTvisitable;
import request.ast.interfaces.IASTvisitor;
import sensor_network.Position;

public abstract class Base implements IASTvisitable{
	protected PositionI position;
	
//	public abstract PositionI getPosition() ;
	
	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }

}
