package ast;

public abstract class Gather {
	
    protected String sensorId;
	
	public Gather(String sensorId) {
		this.sensorId = sensorId;
		
	}
	public String getSensorID() {
		return sensorId;
	}
}
