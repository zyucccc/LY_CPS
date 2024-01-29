package ast.astRand;

import ast.Rand;

public class SRand extends Rand{
	private String sensorId;
	
	public SRand (String id) {
		this.sensorId = id;
	}
	
	public String getSensorId() {
		return this.sensorId;
	}

}
