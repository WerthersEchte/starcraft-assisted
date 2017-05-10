package de.fh_kiel.robotics.starcraft.assist;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bwapi.Color;
import bwapi.Pair;
import bwapi.Position;
import bwapi.Race;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;

public class Anzeige {

	public static boolean sBildschirmAusgabe = true;
	public static boolean sGeländeAusgabe = true;
	
	public static void anzeigen() {
		if( sBildschirmAusgabe ){
			informationenAnzeigen();
			
	    	möglicheEinheitenGebäudeUndForschung();
		}
		
		if( sGeländeAusgabe ){
			gelaendeAnzeigen();
		}
    	
    	sammelpunktAnzeigen();
	}
	
	public static void reset(){

		sBildschirmAusgabe = true;
		sGeländeAusgabe = true;
		sMinMaxOfRegion.clear();
		sAllTechs.clear();
		sAllUpgrades.clear();
		sAllUnits.clear();
		
	}

	private static void informationenAnzeigen() {
		BotKern.spiel().setTextSize(bwapi.Text.Size.Enum.Small);
    	BotKern.spiel().drawTextScreen(10, 10, BotKern.spiel().elapsedTime() + "s " + BotKern.selbst().getName() + "(" + BotKern.selbst().getRace() + ")");

    	Set<UnitType> vListeVonGebaeudenTypen = new HashSet<UnitType>();
    	Set<UnitType> vListeVonEinheitenTypen = new HashSet<UnitType>();
    	for( Unit vEigeneEinheit : BotKern.selbst().getUnits() ){
    		if( vEigeneEinheit.getType().isBuilding() ){
    			vListeVonGebaeudenTypen.add(vEigeneEinheit.getType());
    		} else {
    			vListeVonEinheitenTypen.add(vEigeneEinheit.getType());
    		}
    	}
    	
    	int vPositionsZaehler = 2;
    	BotKern.spiel().drawTextScreen(10, 20, "Gebaeude: ");
    	for( UnitType vGebaeudeTyp : vListeVonGebaeudenTypen ){
    		BotKern.spiel().drawTextScreen(10, ++vPositionsZaehler*10, " " + String.format("% 3d", BotKern.selbst().allUnitCount(vGebaeudeTyp)));
    		BotKern.spiel().drawTextScreen(30, vPositionsZaehler*10, "x " + vGebaeudeTyp.toString().replaceAll("Zerg_|Terran_|Protoss_", ""));
    	}
    	BotKern.spiel().drawTextScreen(10, ++vPositionsZaehler*10, "Einheiten: ");
    	for( UnitType vEinheitenTyp : vListeVonEinheitenTypen ){
    		BotKern.spiel().drawTextScreen(10, ++vPositionsZaehler*10, " " + String.format("% 3d", BotKern.selbst().allUnitCount(vEinheitenTyp)));
    		BotKern.spiel().drawTextScreen(30, vPositionsZaehler*10, "x " + vEinheitenTyp.toString().replaceAll("Zerg_|Terran_|Protoss_", ""));
    	}
    	
	}
	
	private static void drawLineWhenSeen( Position aBegin, Position aEnd, Color aColor){
		if( aBegin.getX() > BotKern.spiel().getScreenPosition().getX() && aBegin.getX() < BotKern.spiel().getScreenPosition().getX() + 640 &&
			aBegin.getY() > BotKern.spiel().getScreenPosition().getY() && aBegin.getY() < BotKern.spiel().getScreenPosition().getY() + 370 ||
			aEnd.getX() > BotKern.spiel().getScreenPosition().getX() && aEnd.getX() < BotKern.spiel().getScreenPosition().getX() + 640 &&
			aEnd.getY() > BotKern.spiel().getScreenPosition().getY() && aEnd.getY() < BotKern.spiel().getScreenPosition().getY() + 370){
			BotKern.spiel().drawLineMap(aBegin, aEnd, aColor);
		}
	}
	
	private static Map<Region, Pair<Position, Position>> sMinMaxOfRegion = new HashMap<Region, Pair<Position,Position>>();
	
	private static void calculateBorders() {
		for( Region vRegion : BWTA.getRegions() ){
			int vMinX = Integer.MAX_VALUE, vMinY = Integer.MAX_VALUE, vMaxX = Integer.MIN_VALUE, vMaxY = Integer.MIN_VALUE;
			for( Position vPunkt : vRegion.getPolygon().getPoints() ){
				if( vMinX > vPunkt.getX() ){
					vMinX = vPunkt.getX();
				}
				if( vMinY > vPunkt.getY() ){
					vMinY = vPunkt.getY();
				}
				if( vMaxX < vPunkt.getX() ){
					vMaxX = vPunkt.getX();
				}
				if( vMaxY < vPunkt.getY() ){
					vMaxY = vPunkt.getY();
				}
			}
			sMinMaxOfRegion.put(vRegion, new Pair<Position, Position>(new Position(vMinX, vMinY), new Position(vMaxX, vMaxY)));
		}
	}

	private static void gelaendeAnzeigen() {
		if( sMinMaxOfRegion.isEmpty() ){
			calculateBorders();
		}
		for( Region vRegion : BWTA.getRegions() ){
			if( sMinMaxOfRegion.containsKey(vRegion) &&
			  !(sMinMaxOfRegion.get(vRegion).first.getX() > BotKern.spiel().getScreenPosition().getX() && sMinMaxOfRegion.get(vRegion).first.getX() < BotKern.spiel().getScreenPosition().getX() + 640 &&
				sMinMaxOfRegion.get(vRegion).first.getY() > BotKern.spiel().getScreenPosition().getY() && sMinMaxOfRegion.get(vRegion).first.getY() < BotKern.spiel().getScreenPosition().getY() + 370 ||
				
			    sMinMaxOfRegion.get(vRegion).first.getX() > BotKern.spiel().getScreenPosition().getX() && sMinMaxOfRegion.get(vRegion).first.getX() < BotKern.spiel().getScreenPosition().getX() + 640 &&
				sMinMaxOfRegion.get(vRegion).second.getY() > BotKern.spiel().getScreenPosition().getY() && sMinMaxOfRegion.get(vRegion).second.getY() < BotKern.spiel().getScreenPosition().getY() + 370 ||
				
			    sMinMaxOfRegion.get(vRegion).second.getX() > BotKern.spiel().getScreenPosition().getX() && sMinMaxOfRegion.get(vRegion).second.getX() < BotKern.spiel().getScreenPosition().getX() + 640 &&
				sMinMaxOfRegion.get(vRegion).first.getY() > BotKern.spiel().getScreenPosition().getY() && sMinMaxOfRegion.get(vRegion).first.getY() < BotKern.spiel().getScreenPosition().getY() + 370 ||
				
			    sMinMaxOfRegion.get(vRegion).second.getX() > BotKern.spiel().getScreenPosition().getX() && sMinMaxOfRegion.get(vRegion).second.getX() < BotKern.spiel().getScreenPosition().getX() + 640 &&
				sMinMaxOfRegion.get(vRegion).second.getY() > BotKern.spiel().getScreenPosition().getY() && sMinMaxOfRegion.get(vRegion).second.getY() < BotKern.spiel().getScreenPosition().getY() + 370 ||
				
				sMinMaxOfRegion.get(vRegion).second.getX() > BotKern.spiel().getScreenPosition().getX() && sMinMaxOfRegion.get(vRegion).first.getX() < BotKern.spiel().getScreenPosition().getX() + 640 &&
				sMinMaxOfRegion.get(vRegion).second.getY() > BotKern.spiel().getScreenPosition().getY() && sMinMaxOfRegion.get(vRegion).first.getY() < BotKern.spiel().getScreenPosition().getY() + 370)){
				continue;
			}
			Position vVorher = vRegion.getPolygon().getPoints().get(vRegion.getPolygon().getPoints().size()-1);
			for( Position vPunkt : vRegion.getPolygon().getPoints() ){
				drawLineWhenSeen(vVorher, vPunkt, Color.Yellow);
				vVorher = vPunkt;
			}
			
		}
		
		for( BaseLocation vBaseLocation : BWTA.getBaseLocations() ){
			if( !( vBaseLocation.getX() > BotKern.spiel().getScreenPosition().getX() && vBaseLocation.getX() < BotKern.spiel().getScreenPosition().getX() + 640 &&
				   vBaseLocation.getY() > BotKern.spiel().getScreenPosition().getY() && vBaseLocation.getY() < BotKern.spiel().getScreenPosition().getY() + 370)){
				continue;
			}
			TilePosition vBasisKachel = vBaseLocation.getTilePosition();
			
			if( vBaseLocation.isStartLocation() ){
				BotKern.spiel().drawBoxMap(
					vBasisKachel.toPosition().getX(), vBasisKachel.toPosition().getY(), vBasisKachel.toPosition().getX() + TilePosition.SIZE_IN_PIXELS*4, vBasisKachel.toPosition().getY() + TilePosition.SIZE_IN_PIXELS*3, Color.Orange);
			} else {
				BotKern.spiel().drawBoxMap(
						vBasisKachel.toPosition().getX(), vBasisKachel.toPosition().getY(), vBasisKachel.toPosition().getX() + TilePosition.SIZE_IN_PIXELS*4, vBasisKachel.toPosition().getY() + TilePosition.SIZE_IN_PIXELS*3, Color.Purple);
			}
		}
	}

	private static void sammelpunktAnzeigen(){
		if( Gebaeude.holeSammelpunkt() != null && Gebaeude.holeSammelpunkt().isValid() ){
			BotKern.spiel().drawCircleMap(Gebaeude.holeSammelpunkt(), 10, Color.White);
			BotKern.spiel().drawCircleMap(Gebaeude.holeSammelpunkt(), 20, Color.White);
		}
	}

	private static List<UnitType> sAllUnits = new ArrayList<UnitType>();
	private static List<UpgradeType> sAllUpgrades = new ArrayList<UpgradeType>();
	private static List<TechType> sAllTechs = new ArrayList<TechType>();
	
	private static void buildLists(){
    	try {
			for (Field vMember : UnitType.class.getDeclaredFields()) {
			    if( Modifier.isStatic(vMember.getModifiers()) && 
			    	vMember.getName().startsWith("Zerg_") && BotKern.selbst().getRace() == Race.Zerg || 
			    	vMember.getName().startsWith("Terran_") && BotKern.selbst().getRace() == Race.Terran || 
			    	vMember.getName().startsWith("Protoss_") && BotKern.selbst().getRace() == Race.Protoss
			    	) {
						
			    	sAllUnits.add((UnitType) vMember.get(UnitType.AllUnits));
			    } 
			}
			for (Field vMember : UpgradeType.class.getDeclaredFields()) {
		    	if( Modifier.isStatic(vMember.getModifiers()) &&
			    	Modifier.isPublic(vMember.getModifiers()) &&
			    	((UpgradeType) vMember.get(UpgradeType.None)).getRace() == BotKern.selbst().getRace()){
			    	
		    		sAllUpgrades.add((UpgradeType) vMember.get(UpgradeType.None));
			    } 
			}
	    	
	    	for (Field vMember : TechType.class.getDeclaredFields()) {
	    		if( Modifier.isStatic(vMember.getModifiers()) &&
				    Modifier.isPublic(vMember.getModifiers()) &&
			    	((TechType) vMember.get(TechType.None)).getRace() == BotKern.selbst().getRace()){

	    			sAllTechs.add((TechType) vMember.get(TechType.None));
			    } 
			}
		} catch( Exception vException ){
			vException.printStackTrace();
		}
	}
	
	private static void möglicheEinheitenGebäudeUndForschung(){
		if( sAllUnits.isEmpty() ){
			buildLists();
		}
		
		BotKern.spiel().setTextSize(bwapi.Text.Size.Enum.Small);

		try {
	    	int vPositionsZaehler = 1;

	    	BotKern.spiel().drawTextScreen(535, ++vPositionsZaehler * 10, "Moegliche" );
	    	BotKern.spiel().drawTextScreen(540, ++vPositionsZaehler * 10, "Gebaeude/Einheiten:" );
			for (UnitType vUnitType : sAllUnits) {
				if( BotKern.selbst().allUnitCount(vUnitType.whatBuilds().first) == 0){
					continue;
				}
		    	for( Unit vBuilder : BotKern.selbst().getUnits() ){
					if( vBuilder.getType() == vUnitType.whatBuilds().first ){
						if( vBuilder.canBuild( vUnitType ) ||
							vBuilder.canTrain( vUnitType ) ||
							vBuilder.canMorph( vUnitType ) ){
							
					    	BotKern.spiel().drawTextScreen(550, ++vPositionsZaehler * 10, vUnitType.toString().replaceAll("Zerg_|Terran_|Protoss_", "").toLowerCase() );
					    	break;
						}
					}
		    	}
			}
			
			
			++vPositionsZaehler;
	    	BotKern.spiel().drawTextScreen(540, ++vPositionsZaehler * 10, "Forschung:" );
			for (UpgradeType vUpgrade : sAllUpgrades) {
				if( BotKern.selbst().allUnitCount(vUpgrade.whatUpgrades()) == 0){
					continue;
				}
		    	for( Unit vBuilder : BotKern.selbst().getUnits() ){
					if( vBuilder.getType() == vUpgrade.whatUpgrades() ){
						if( vBuilder.canUpgrade( vUpgrade ) ) {
							BotKern.spiel().drawTextScreen(550, ++vPositionsZaehler * 10, vUpgrade.toString().replaceAll("Zerg_|Terran_|Protoss_", "").toLowerCase() );
							break;
						}
					}
		    	}	
			}
	    	
	    	for (TechType vTech : sAllTechs) {
				if( BotKern.selbst().hasResearched(vTech) ||
					BotKern.selbst().allUnitCount(vTech.whatResearches()) == 0){
					continue;
				}
		    	for( Unit vBuilder : BotKern.selbst().getUnits() ){
					if( vBuilder.getType() == vTech.whatResearches() ){
						if( vBuilder.canResearch(vTech) ) {
							BotKern.spiel().drawTextScreen(550, ++vPositionsZaehler * 10, vTech.toString().replaceAll("Zerg_|Terran_|Protoss_", "").toLowerCase() );
							break;
						}
					}
		    	}	
			}
		} catch ( Exception vException ) {
			vException.printStackTrace();
		}
		
	}
	
}
