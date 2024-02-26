package sensor_network;

import fr.sorbonne_u.cps.sensor_network.interfaces.PositionI;
import request.ast.Direction;

public class Position implements PositionI {
    private static final long serialVersionUID = 1L;

    private double x;
    private double y;

    // init Position
    public Position(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // distance par rapport à un autre position
    @Override
    public double distance(PositionI p) {
        if (p instanceof Position) {
            Position other = (Position) p;
            return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
        }
        throw new IllegalArgumentException("Unsupported PositionI implementation.");
    }
    
    @Override
    public fr.sorbonne_u.cps.sensor_network.interfaces.Direction directionFrom(PositionI p) {     
    	 return null;
    }

    // 根据相对位置返回方向
    //正西属于西北
    //正南属于西南
    //正东属于东南
    //正北属于东北
    public Direction directionFrom_ast(PositionI p) {     
    	  Position other = (Position) p;
          // 判断两点在x轴和y轴的相对位置，并考虑正方向归属的改变
          boolean north = this.y > other.y;
          boolean south = this.y < other.y;
          boolean east = this.x > other.x;
          boolean west = this.x < other.x;

          // 东西方向上的对比需要结合南北方向来决定具体归属
          if (north && !east && !west) {
              return Direction.NE; // 正北属于东北
          } else if (south && !east && !west) {
              return Direction.SW; // 正南属于西南
          } else if (west && !north && !south) {
              return Direction.NW; // 正西属于西北
          } else if (east && !north && !south) {
              return Direction.SE; // 正东属于东南
          } else if (north && east) {
              return Direction.NE; // 东北
          } else if (north && west) {
              return Direction.NW; // 西北
          } else if (south && east) {
              return Direction.SE; // 东南
          } else if (south && west) {
              return Direction.SW; // 西南
          }
        throw new IllegalArgumentException("Unsupported PositionI implementation.");
    }

    // 判断相对位置
    @Override
    public boolean northOf(PositionI p) {
    	Position other = (Position) p;
        return this.y > other.y;
    }

    @Override
    public boolean southOf(PositionI p) {
    	Position other = (Position) p;
        return this.y < other.y;
    }

    @Override
    public boolean eastOf(PositionI p) {
    	Position other = (Position) p;
        return this.x > other.x;
    }

    @Override
    public boolean westOf(PositionI p) {
    	Position other = (Position) p;
        return this.x < other.x;
    }

    // Getter方法，如果需要
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
    
    @Override
    public String toString() {
        return "Position{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}