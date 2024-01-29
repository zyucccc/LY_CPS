package ast;

import ast.interfaces.IASTvisitable;
import ast.interfaces.IASTvisitor;

public abstract class Gather implements IASTvisitable{
	
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
