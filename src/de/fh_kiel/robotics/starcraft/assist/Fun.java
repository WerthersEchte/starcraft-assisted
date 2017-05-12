package de.fh_kiel.robotics.starcraft.assist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Position;
import bwapi.Race;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;

public class Fun {
	
	
	private static Map<TilePosition, UnitType> sGebäude = new HashMap<>();
	public static void bauen(){

		for( TilePosition vPosition : new ArrayList<>(sGebäude.keySet()) ){
			if( Kern.spiel().getUnitsInRadius(new Position(vPosition.toPosition().getX()+32, vPosition.toPosition().getX()+32), 20).stream().filter(unit->unit.getType().isBuilding()).findAny().isPresent() ){
				sGebäude.remove(vPosition);
			}
		}
		
		for( TilePosition vPosition : sGebäude.keySet() ){
			Unit vBauer = null;
			
			List<Unit> vUnits = new ArrayList<>(Kern.selbst().getUnits());
			Collections.shuffle(vUnits);
			
			for( Unit vPotenzellerBauer : vUnits ){
				if( vPotenzellerBauer.getType() == sGebäude.get(vPosition).whatBuilds().first && (vPotenzellerBauer.isIdle() || vPotenzellerBauer.isGatheringMinerals()) ){
					vBauer = vPotenzellerBauer;
					break;
				}
			}
			
			if( vBauer == null ){
				return;
			}
			
			if( Gebaeude.istPositionFrei(vPosition) && vBauer.build(sGebäude.get(vPosition), vPosition)){
				Gebaeude.zukünftigesGebäudeAn(sGebäude.get(vPosition), vPosition);
			}
		}
	}
	
	public static void herz(){
		
		UnitType vGebäudeTyp = UnitType.None;
		if( Kern.selbst().getRace() == Race.Protoss ){
			vGebäudeTyp = UnitType.Protoss_Pylon;
		} else if( Kern.selbst().getRace() == Race.Zerg ){
			vGebäudeTyp = UnitType.Zerg_Creep_Colony;
			sGebäude.put(new TilePosition(-1 + Kern.spiel().getScreenPosition().toTilePosition().getX() + Kern.spiel().getMousePosition().toTilePosition().getX(), +3 + Kern.spiel().getScreenPosition().toTilePosition().getY() + Kern.spiel().getMousePosition().toTilePosition().getY()),
					UnitType.Zerg_Hatchery);
			Gebaeude.braucheEinheiten(UnitType.Zerg_Drone, 17);
		} else if( Kern.selbst().getRace() == Race.Terran ){
			vGebäudeTyp = UnitType.Terran_Missile_Turret;
			if( Kern.selbst().allUnitCount(UnitType.Terran_Engineering_Bay) <= 0){
				Gebaeude.braucheEinheiten(UnitType.Terran_Engineering_Bay, 1);
			}
		}
		
		int[][] vPositions = new int[][]{{0,0},{-1,-1},{-2,-1},{-3,0},{-3,1},{-3,2},{-2,3},{-1,4},{0,5},{1,-1},{2,-1},{3,0},{3,1},{3,2},{2,3},{1,4}};
		
		for( int[] vPosition : vPositions){
			sGebäude.put(new TilePosition(vPosition[0]*2 + Kern.spiel().getScreenPosition().toTilePosition().getX() + Kern.spiel().getMousePosition().toTilePosition().getX(), vPosition[1]*2 + Kern.spiel().getScreenPosition().toTilePosition().getY() + Kern.spiel().getMousePosition().toTilePosition().getY()),
					vGebäudeTyp);
		}
		
	}

}
