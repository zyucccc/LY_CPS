package request.ast.astRand;

import request.ast.Rand;
import request.ast.interfaces.IASTvisitor;

public class SRand extends Rand{
	private String sensorId;
	
	public SRand (String id) {
		this.sensorId = id;
	}
	
	public String getSensorId() {
		return this.sensorId;
	}
	
	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }

}
