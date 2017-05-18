package de.fh_kiel.robotics.starcraft.assist;

public class Position extends bwapi.Position {

	public Position(int x, int y) {
		super(x, y);
	}
	
	public Position add( Position aPosition ){
		return new Position(getX() + aPosition.getX(), getY() + aPosition.getY());
	}
	
	public Position add( bwapi.Position aPosition ){
		return new Position(getX() + aPosition.getX(), getY() + aPosition.getY());
	}
	
	@Override
	public boolean equals(Object o) {
		if( !(o instanceof Position) ){
			return false;
		}
		return ((Position)o).getX() == getX() && ((Position)o).getY() == getY();
	}
	
	@Override
	public int hashCode() {
		return getX()*10000+getY();
	}
	
};
