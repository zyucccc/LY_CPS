package request.ast.astRand;

import request.ast.Rand;

public class SRand extends Rand{
	private String sensorId;
	
	public SRand (String id) {
		this.sensorId = id;
	}
	
	public String getSensorId() {
		return this.sensorId;
	}

}
