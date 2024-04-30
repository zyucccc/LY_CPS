package request.ast;

import request.ast.interfaces.IASTvisitable;
import request.ast.interfaces.IASTvisitor;

import java.io.Serializable;

public enum Direction implements IASTvisitable, Serializable {

	 NE, NW, SE, SW;

	private static final long serialVersionUID = 1L;
	public Direction getDirection() {
        return this;
    }

	public Direction calcul_inverse_Direction() {
		switch(this) {
		case NE:
			return SW;
		case NW:
			return SE;
		case SE:
			return NW;
		case SW:
			return NE;
		default:
			return null;
		}
	}
	
	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }
}
