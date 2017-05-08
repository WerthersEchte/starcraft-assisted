package de.fh_kiel.robotics.starcraft.assist;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import bwapi.Color;
import bwapi.Position;
import bwapi.TechType;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Region;

public class Anzeige {
	
	public static void anzeigen() {
		
    	informationenAnzeigen();
		
    	gelaendeAnzeigen();
    	
    	sammelpunktAnzeigen();
		
    	möglicheEinheitenGebäudeUndForschung();
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
    	
		for(Unit vUnit : BotKern.spiel().getAllUnits()){
			try{
				BotKern.spiel().drawTextMap(vUnit.getPosition(), "" + vUnit.getID());
				
				if( vUnit.getTarget() != null ){
					BotKern.spiel().drawLineMap(vUnit.getPosition(), vUnit.getTarget().getPosition(), Color.Red);
				} else if(  vUnit.getOrderTarget() != null ){
					BotKern.spiel().drawLineMap(vUnit.getPosition(), vUnit.getOrderTarget().getPosition(), Color.Purple);
				} else if(  vUnit.getOrderTargetPosition() != null && vUnit.canMove() && !vUnit.isIdle() ){
					BotKern.spiel().drawLineMap(vUnit.getPosition(), vUnit.getOrderTargetPosition(), Color.Green);
				}
			} catch ( Exception vException ){
				vException.printStackTrace();
			}
			
		}
	}

	private static void gelaendeAnzeigen() {
		for( Region vRegion : BWTA.getRegions() ){
			Position vVorher = vRegion.getPolygon().getPoints().get(vRegion.getPolygon().getPoints().size()-1);
			for( Position vPunkt : vRegion.getPolygon().getPoints() ){
				BotKern.spiel().drawLineMap(vVorher, vPunkt, Color.Yellow);
				vVorher = vPunkt;
			}
			
		}
		
		for( BaseLocation vBaseLocation : BWTA.getBaseLocations() ){
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
	
	private static void möglicheEinheitenGebäudeUndForschung(){
		BotKern.spiel().setTextSize(bwapi.Text.Size.Enum.Small);

		try {
	    	int vPositionsZaehler = 1;

	    	BotKern.spiel().drawTextScreen(535, ++vPositionsZaehler * 10, "Moegliche" );
	    	BotKern.spiel().drawTextScreen(540, ++vPositionsZaehler * 10, "Gebaeude/Einheiten:" );
			for (Field vMember : UnitType.class.getDeclaredFields()) {
			    if( Modifier.isStatic(vMember.getModifiers()) && 
			    	vMember.getName().startsWith("Zerg_") || vMember.getName().startsWith("Terran_") || vMember.getName().startsWith("Protoss_")) {
			    	for( Unit vBuilder : BotKern.selbst().getUnits() ){
						if( vBuilder.getType() == ((UnitType) vMember.get(UnitType.AllUnits)).whatBuilds().first ){
							if( vBuilder.canBuild( (UnitType) vMember.get(UnitType.AllUnits) ) ||
								vBuilder.canTrain( (UnitType) vMember.get(UnitType.AllUnits) ) ||
								vBuilder.canMorph( (UnitType) vMember.get(UnitType.AllUnits) ) ){
								
						    	BotKern.spiel().drawTextScreen(550, ++vPositionsZaehler * 10, vMember.getName().replaceAll("Zerg_|Terran_|Protoss_", "").toLowerCase() );
						    	break;
							}
						}
			    	}
			    } 
			}
			
			
			++vPositionsZaehler;
	    	BotKern.spiel().drawTextScreen(540, ++vPositionsZaehler * 10, "Forschung:" );
			for (Field vMember : UpgradeType.class.getDeclaredFields()) {
			    if( Modifier.isStatic(vMember.getModifiers()) &&
			    	Modifier.isPublic(vMember.getModifiers())){
			    	for( Unit vBuilder : BotKern.selbst().getUnits() ){
						if( vBuilder.getType() == ((UpgradeType) vMember.get(UpgradeType.None)).whatUpgrades() ){
							if( vBuilder.canUpgrade((UpgradeType) vMember.get(UpgradeType.None))) {
								BotKern.spiel().drawTextScreen(550, ++vPositionsZaehler * 10, vMember.getName().replaceAll("Zerg_|Terran_|Protoss_", "").toLowerCase() );
								break;
							}
						}
			    	}	
			    } 
			}
	    	
	    	for (Field vMember : TechType.class.getDeclaredFields()) {
	    		if( Modifier.isStatic(vMember.getModifiers()) &&
				    Modifier.isPublic(vMember.getModifiers())){
			    	for( Unit vBuilder : BotKern.selbst().getUnits() ){
						if( vBuilder.getType() == ((TechType) vMember.get(TechType.None)).whatResearches() ){
							if( vBuilder.canResearch((TechType) vMember.get(TechType.None))) {
								BotKern.spiel().drawTextScreen(550, ++vPositionsZaehler * 10, vMember.getName().replaceAll("Zerg_|Terran_|Protoss_", "").toLowerCase() );
								break;
							}
						}
			    	}	
			    } 
			}
		} catch ( Exception vException ) {
			vException.printStackTrace();
		}
		
	}
	
}
