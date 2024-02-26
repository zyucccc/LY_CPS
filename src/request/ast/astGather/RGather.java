package request.ast.astGather;

import request.ast.Gather;
import request.ast.interfaces.IASTvisitor;

public class RGather extends Gather{

	private final Gather gather;

	public RGather(String sensorId,Gather gather) {
		super(sensorId);
		this.gather=gather;
	}
	
	public Gather getGather() {
		return gather;
	}
	
	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }

}
