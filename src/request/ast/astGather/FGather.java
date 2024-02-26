package request.ast.astGather;

import request.ast.Gather;
import request.ast.interfaces.IASTvisitor;

public class FGather extends Gather{

	public FGather(String sensorId) {
		super(sensorId);
	}
	
	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }
	
}
