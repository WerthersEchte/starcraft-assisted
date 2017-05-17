package de.fh_kiel.robotics.starcraft.assist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Color;
import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;

public class Fun {
	
	private static Map<TilePosition, UnitType> sGeb�ude = new HashMap<>();
	public static void bauen(){

		for( TilePosition vPosition : new ArrayList<>(sGeb�ude.keySet()) ){
			Kern.spiel().drawBoxMap(
					vPosition.getX()*32, 
					vPosition.getY()*32, 
					vPosition.getX()*32 + sGeb�ude.get(vPosition).tileWidth()*32, 
					vPosition.getY()*32 + sGeb�ude.get(vPosition).tileHeight()*32, 
					Color.White);
			if( Kern.spiel().getUnitsInRadius(new Position(vPosition.toPosition().getX()+32, vPosition.toPosition().getY()+32), 20).stream().filter(unit->unit.getType().isBuilding()).findAny().isPresent() ){
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
			} else if( !Kern.spiel().isExplored(vPosition) ){
				for( Unit vUnit : Kern.selbst().getUnits() ){
					if( vUnit.canMove() && vUnit.move(new Position(vPosition.toPosition().getX(), vPosition.toPosition().getY())) ){
						break;
					}
				}
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
	
	public static void schreiben( String aText ){
		sGeb�ude.clear();
		
		UnitType vGeb�udeTyp = UnitType.None;
		if( Kern.selbst().getRace() == Race.Protoss ){
			vGeb�udeTyp = UnitType.Protoss_Pylon;
		} else if( Kern.selbst().getRace() == Race.Zerg ){
			vGeb�udeTyp = UnitType.Zerg_Creep_Colony;
			sGeb�ude.put(new TilePosition( -3 + Kern.spiel().getScreenPosition().toTilePosition().getX() + Kern.spiel().getMousePosition().toTilePosition().getX(), +1 + Kern.spiel().getScreenPosition().toTilePosition().getY() + Kern.spiel().getMousePosition().toTilePosition().getY()),
					UnitType.Zerg_Hatchery);
			Gebaeude.braucheEinheiten(UnitType.Zerg_Drone, 17);
		} else if( Kern.selbst().getRace() == Race.Terran ){
			vGeb�udeTyp = UnitType.Terran_Missile_Turret;
			if( Kern.selbst().allUnitCount(UnitType.Terran_Engineering_Bay) <= 0){
				Gebaeude.braucheEinheiten(UnitType.Terran_Engineering_Bay, 1);
			}
		}
		
		int vSize=0;
		for( char vBuchstabe : aText.toCharArray() ){
			for( double[] vPosition : Alphabet.valueOf("" + vBuchstabe).getImage() ){
				sGeb�ude.put(new TilePosition((int)(vPosition[0]*2) + vSize + Kern.spiel().getScreenPosition().toTilePosition().getX() + Kern.spiel().getMousePosition().toTilePosition().getX(), (int)(vPosition[1]*2) + Kern.spiel().getScreenPosition().toTilePosition().getY() + Kern.spiel().getMousePosition().toTilePosition().getY()),
						vGeb�udeTyp);
			}
			vSize+=Alphabet.valueOf("" + vBuchstabe).getWidth()*2;
		}
	}
	
	enum Alphabet{
		A( new double[][]{{0.5,0},{1.5,0},{0,1},{2,1},{0,2},{1,2},{2,2},{0,3},{2,3},{0,4},{2,4}}, 4 ),
		L( new double[][]{{0,0},{0,1},{0,2},{0,3},{0,4},{1,4},{2,4}}, 4 ),
		o( new double[][]{{0.5,2},{1.5,2},{0,3},{2,3},{0.5,4},{1.5,4}}, 4 ),
		u( new double[][]{{0,2},{2,2},{0,3},{2,3},{0.5,4},{1.5,4}}, 4 ),
		i( new double[][]{{0,0},{0,2},{0,3},{0,4}}, 2 ),
		s( new double[][]{{0.5,0},{1.5,0},{0,1},{0.5,2},{1.5,2},{2,3},{0.5,4},{1.5,4}}, 4 );
		
		double[][] mPositions;
		double mWidth;
		private Alphabet( double[][] aPositions, double aWidth ) {
			mPositions = aPositions;
			mWidth = aWidth;
		}
		
		double[][] getImage(){
			return mPositions;
		}
		
		double getWidth(){
			return mWidth;
		}
		
	}

}
