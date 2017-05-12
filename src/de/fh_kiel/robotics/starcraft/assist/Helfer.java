package de.fh_kiel.robotics.starcraft.assist;

import bwapi.Position;
import bwapi.TilePosition;
import bwapi.Unit;

public class Helfer {
	
	public static boolean istGleich( Unit aErsteEinheit, Unit aZweiteEinheit ){
		return aErsteEinheit.getID() == aZweiteEinheit.getID();
	}
	
	public static boolean istGleich( Position aPositionEins, Position aPositionZwei ){
		return aPositionEins.getX() == aPositionZwei.getX() && aPositionEins.getY() == aPositionZwei.getY();
	}
	
	public static boolean istGleich( TilePosition aKachelPositionEins, TilePosition aKachelPositionZwei ){
		return aKachelPositionEins.getX() == aKachelPositionZwei.getX() && aKachelPositionEins.getY() == aKachelPositionZwei.getY();
	}

}
