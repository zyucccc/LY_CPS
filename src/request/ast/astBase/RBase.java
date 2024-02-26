package request.ast.astBase;

import request.ast.Base;
import request.ast.interfaces.IASTvisitor;

public class RBase extends Base{
	
	 @Override
		public <Result, Data, Anomaly extends Throwable> 
	    Result eval(IASTvisitor<Result, Data, Anomaly> visitor, Data data)
	            throws Anomaly {
	        return visitor.visit(this, data);
	    }

}
