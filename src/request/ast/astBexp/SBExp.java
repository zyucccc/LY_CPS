package request.ast.astBexp;

import request.ast.Bexp;

public class SBExp extends Bexp{
	
	private final String sensorId;
	
	public SBExp(String sensorId) {
		this.sensorId = sensorId;
		
	}
	public String getSensorID() {
		return sensorId;
	}
}
