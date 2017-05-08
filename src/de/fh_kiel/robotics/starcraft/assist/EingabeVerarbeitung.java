package de.fh_kiel.robotics.starcraft.assist;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import bwapi.Position;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;

public class EingabeVerarbeitung {
	
	public static String einheitenBauenLassen( String aBefehl ){
		try{
	    	
	    	int vAnzahl = 1;
	    	String vEinheit = aBefehl;
	    	
	    	if(aBefehl.split(" ").length == 2){
	    		vAnzahl = Integer.parseInt(aBefehl.split(" ")[0]);
	    		vEinheit = aBefehl.split(" ")[1];
	    	}
	    	
	    	for (Field vMember : UnitType.class.getDeclaredFields()) {
			    if( Modifier.isStatic(vMember.getModifiers()) && 
			    	vMember.getName().startsWith("Zerg_") || vMember.getName().startsWith("Terran_") || vMember.getName().startsWith("Protoss_")) {
			        if( vMember.getName().replaceAll("Zerg_|Terran_|Protoss_", "").equalsIgnoreCase(vEinheit)){
			        	Gebaeude.braucheEinheiten((UnitType) vMember.get(UnitType.AllUnits), vAnzahl);
			        	return "";
			        }
			    } 
			}
	    	
    	}catch( Exception vException ){
    		return aBefehl;
    	}
		return aBefehl;
	}
	
	public static String upgradesErforschenLassen( String aBefehl ){
		try{
	    	
	    	int vAnzahl = 1;
	    	String vEinheit = aBefehl;
	    	
	    	if(aBefehl.split(" ").length == 2){
	    		vAnzahl = Integer.parseInt(aBefehl.split(" ")[0]);
	    		vEinheit = aBefehl.split(" ")[1];
	    	}
	    	
	    	for (Field vMember : UpgradeType.class.getDeclaredFields()) {
			    if( Modifier.isStatic(vMember.getModifiers())) {
			        if( vMember.getName().replaceAll("Zerg_|Terran_|Protoss_", "").equalsIgnoreCase(vEinheit)){
			        	Gebaeude.braucheUpgrades((UpgradeType) vMember.get(UpgradeType.None), vAnzahl);
			        	return "";
			        }
			    } 
			}
	    	
	    	for (Field vMember : TechType.class.getDeclaredFields()) {
			    if( Modifier.isStatic(vMember.getModifiers())) {
			        if( vMember.getName().replaceAll("Zerg_|Terran_|Protoss_", "").equalsIgnoreCase(vEinheit)){
			        	Gebaeude.braucheTechs((TechType) vMember.get(TechType.None), vAnzahl);
			        	return "";
			        }
			    } 
			}
	    	
    	}catch( Exception vException ){
    		return aBefehl;
    	}
		return aBefehl;
	}
	
	public static String sammelpunktSetzen( String aBefehl ){
		if( aBefehl.equalsIgnoreCase("sammelpunkt") ){
			Position vBildschirm = BotKern.spiel().getScreenPosition();
			Position vMauszeiger = BotKern.spiel().getMousePosition();
			Gebaeude.setzeSammelpunkt(new Position(vBildschirm.getX() + vMauszeiger.getX(), vBildschirm.getY() + vMauszeiger.getY()));
			return "";
		}
		return aBefehl;
	}
	
	public static String sammeln( String aBefehl ){
		if( aBefehl.equalsIgnoreCase("sammeln") ){
			Position vBildschirm = BotKern.spiel().getScreenPosition();
			Position vMauszeiger = BotKern.spiel().getMousePosition();
			Position vSammelpunkt = new Position(vBildschirm.getX() + vMauszeiger.getX(), vBildschirm.getY() + vMauszeiger.getY());
			for(Unit vEinheit : BotKern.selbst().getUnits()){
				if( vEinheit.canMove() && !vEinheit.getType().isWorker() ){
					vEinheit.move( vSammelpunkt );
				}
			}
			return "";
		}
		return aBefehl;
	}
	
	public static String angreifen( String aBefehl ){
		if( aBefehl.equalsIgnoreCase("angreifen") ){
			if( !BotKern.spiel().getSelectedUnits().isEmpty() &&
				 BotKern.spiel().getSelectedUnits().get(0).getPlayer().isEnemy(BotKern.selbst()) ){
				
				Unit vGegner = BotKern.spiel().getSelectedUnits().get(0);
				for(Unit vEinheit : BotKern.selbst().getUnits()){
					if( vEinheit.canAttack( vGegner ) && !vEinheit.getType().isWorker() ){
						vEinheit.attack( vGegner );
					}
				}
			} else {
				Position vBildschirm = BotKern.spiel().getScreenPosition();
				Position vMauszeiger = BotKern.spiel().getMousePosition();
				Position vAnzugreifenderPunkt = new Position(vBildschirm.getX() + vMauszeiger.getX(), vBildschirm.getY() + vMauszeiger.getY());
				for(Unit vEinheit : BotKern.selbst().getUnits()){
					if( vEinheit.canMove() && !vEinheit.getType().isWorker() ){
						vEinheit.attack( vAnzugreifenderPunkt );
					}
				}
			}
			return "";
		}
		return aBefehl;
	}
	
	public static String expandieren( String aBefehl ){
		if( aBefehl.equalsIgnoreCase("expandieren") ){
			Gebaeude.expansionswunsch();
			return "";
		}
		return aBefehl;
	}

}
