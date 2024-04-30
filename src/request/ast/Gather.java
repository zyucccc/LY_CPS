package request.ast;

import request.ast.interfaces.IASTvisitable;
import request.ast.interfaces.IASTvisitor;

import java.io.Serializable;

public abstract class Gather implements IASTvisitable, Serializable {
	private static final long serialVersionUID = 1L;
    protected String sensorId;
	
	public Gather(String sensorId) {
		this.sensorId = sensorId;
		
	}
	public String getSensorID() {
		return sensorId;
	}
	
	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }
	
}
