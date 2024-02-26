package request.ast.astBexp;

import request.ast.Bexp;
import request.ast.interfaces.IASTvisitor;

public class SBExp extends Bexp{
	
	private final String sensorId;
	
	public SBExp(String sensorId) {
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
