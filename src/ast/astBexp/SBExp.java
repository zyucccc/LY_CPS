package ast.astBexp;

import ast.Bexp;

public class SBExp extends Bexp{
	
	private final int sensorId;
	
	public SBExp(int sensorId) {
		this.sensorId = sensorId;
		
	}
	public int getSensorID() {
		return sensorId;
	}
}
