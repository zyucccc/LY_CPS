package sensor_network;

import fr.sorbonne_u.cps.sensor_network.interfaces.GeographicalZoneI;
import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;

public class GeographicalZone implements GeographicalZoneI {
    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    public GeographicalZone(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    public boolean in(PositionI p) {
        if (p instanceof Position) {
            Position position = (Position) p;
            return position.getX() >= minX && position.getX() <= maxX
                && position.getY() >= minY && position.getY() <= maxY;
        }
        return false;
    }
}
