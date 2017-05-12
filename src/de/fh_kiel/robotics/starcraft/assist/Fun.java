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
	
	
	private static Map<TilePosition, UnitType> sGeb�ude = new HashMap<>();
	public static void bauen(){

		for( TilePosition vPosition : new ArrayList<>(sGeb�ude.keySet()) ){
			if( Kern.spiel().getUnitsInRadius(new Position(vPosition.toPosition().getX()+32, vPosition.toPosition().getX()+32), 20).stream().filter(unit->unit.getType().isBuilding()).findAny().isPresent() ){
				sGeb�ude.remove(vPosition);
			}
		}
		
		for( TilePosition vPosition : sGeb�ude.keySet() ){
			Unit vBauer = null;
			
			List<Unit> vUnits = new ArrayList<>(Kern.selbst().getUnits());
			Collections.shuffle(vUnits);
			
			for( Unit vPotenzellerBauer : vUnits ){
				if( vPotenzellerBauer.getType() == sGeb�ude.get(vPosition).whatBuilds().first && (vPotenzellerBauer.isIdle() || vPotenzellerBauer.isGatheringMinerals()) ){
					vBauer = vPotenzellerBauer;
					break;
				}
			}
			
			if( vBauer == null ){
				return;
			}
			
			if( Gebaeude.istPositionFrei(vPosition) && vBauer.build(sGeb�ude.get(vPosition), vPosition)){
				Gebaeude.zuk�nftigesGeb�udeAn(sGeb�ude.get(vPosition), vPosition);
			}
		}
	}
	
	public static void herz(){
		
		UnitType vGeb�udeTyp = UnitType.None;
		if( Kern.selbst().getRace() == Race.Protoss ){
			vGeb�udeTyp = UnitType.Protoss_Pylon;
		} else if( Kern.selbst().getRace() == Race.Zerg ){
			vGeb�udeTyp = UnitType.Zerg_Creep_Colony;
			sGeb�ude.put(new TilePosition(-1 + Kern.spiel().getScreenPosition().toTilePosition().getX() + Kern.spiel().getMousePosition().toTilePosition().getX(), +3 + Kern.spiel().getScreenPosition().toTilePosition().getY() + Kern.spiel().getMousePosition().toTilePosition().getY()),
					UnitType.Zerg_Hatchery);
			Gebaeude.braucheEinheiten(UnitType.Zerg_Drone, 17);
		} else if( Kern.selbst().getRace() == Race.Terran ){
			vGeb�udeTyp = UnitType.Terran_Missile_Turret;
			if( Kern.selbst().allUnitCount(UnitType.Terran_Engineering_Bay) <= 0){
				Gebaeude.braucheEinheiten(UnitType.Terran_Engineering_Bay, 1);
			}
		}
		
		int[][] vPositions = new int[][]{{0,0},{-1,-1},{-2,-1},{-3,0},{-3,1},{-3,2},{-2,3},{-1,4},{0,5},{1,-1},{2,-1},{3,0},{3,1},{3,2},{2,3},{1,4}};
		
		for( int[] vPosition : vPositions){
			sGeb�ude.put(new TilePosition(vPosition[0]*2 + Kern.spiel().getScreenPosition().toTilePosition().getX() + Kern.spiel().getMousePosition().toTilePosition().getX(), vPosition[1]*2 + Kern.spiel().getScreenPosition().toTilePosition().getY() + Kern.spiel().getMousePosition().toTilePosition().getY()),
					vGeb�udeTyp);
		}
		
	}

}
