package de.fh_kiel.robotics.starcraft.assist;

public class TilePosition extends bwapi.TilePosition {

	public TilePosition(int x, int y) {
		super(x, y);
	}
	
	@Override
	public boolean equals(Object o) {
		if( !(o instanceof TilePosition) ){
			return false;
		}
		return ((TilePosition)o).getX() == getX() && ((TilePosition)o).getY() == getY();
	}
	
	@Override
	public int hashCode() {
		return getX()*10000+getY();
	}
	
};
