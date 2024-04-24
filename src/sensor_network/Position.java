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
    
    //ici,nous redefinissons les 2 fonctions_ast nous-meme,
    //c'est parce que nous utilisons la class Direction cree par nous-meme
    //mais pas celui du package fourni(Direction dans sorbonne_u.cps.sensor_network.interfaces)

    //retouner la direction selon la position
    //d'ailleurs,nous imposons:
    //ouest est dans le nord-ouest
    //sud est dans le sud-ouest
    //est est dans le sud-est
    //nord est dans le nord-est
    public Direction directionFrom_ast(PositionI p) {     
    	  Position other = (Position) p;

          boolean north = this.y > other.y;
          boolean south = this.y < other.y;
          boolean east = this.x > other.x;
          boolean west = this.x < other.x;

          if (north && !east && !west) {
              return Direction.NE;
          } else if (south && !east && !west) {
              return Direction.SW;
          } else if (west && !north && !south) {
              return Direction.NW;
          } else if (east && !north && !south) {
              return Direction.SE;
          } else if (north && east) {
              return Direction.NE;
          } else if (north && west) {
              return Direction.NW;
          } else if (south && east) {
              return Direction.SE;
          } else if (south && west) {
              return Direction.SW;
          }
        throw new IllegalArgumentException("Unsupported PositionI implementation.");
    }
    
    //inverse du directionfrom
    public Direction directionTo_ast(PositionI p) {
        Position other = (Position) p;
        // inverse logique du comparaison
        boolean north = this.y < other.y; 
        boolean south = this.y > other.y;
        boolean east = this.x < other.x;
        boolean west = this.x > other.x; 

        if (north && !east && !west) {
            return Direction.NE; 
        } else if (south && !east && !west) {
            return Direction.SW; 
        } else if (west && !north && !south) {
            return Direction.NW; 
        } else if (east && !north && !south) {
            return Direction.SE; 
        } else if (north && east) {
            return Direction.NE; 
        } else if (north && west) {
            return Direction.NW; 
        } else if (south && east) {
            return Direction.SE; 
        } else if (south && west) {
            return Direction.SW;
        }
        throw new IllegalArgumentException("Unsupported PositionI implementation.");
    }

    //distinguer les cas de position
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

    // Getter
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

	@Override
	public fr.sorbonne_u.cps.sensor_network.interfaces.Direction directionFrom(PositionI p) {
		// TODO Auto-generated method stub
		return null;
	}
}