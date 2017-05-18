package de.fh_kiel.robotics.starcraft.assist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Color;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;

public class Fun {
	
	private static Map<TilePosition, UnitType> sGebäude = new HashMap<>();
	public static void bauen(){

		for( TilePosition vPosition : new ArrayList<>(sGebäude.keySet()) ){
			Kern.spiel().drawBoxMap(
					vPosition.getX()*32, 
					vPosition.getY()*32, 
					vPosition.getX()*32 + sGebäude.get(vPosition).tileWidth()*32, 
					vPosition.getY()*32 + sGebäude.get(vPosition).tileHeight()*32, 
					Color.White);
			if( Kern.spiel().getUnitsInRadius(new Position(vPosition.toPosition().getX()+32, vPosition.toPosition().getY()+32), 20).stream().filter(unit->unit.getType().isBuilding()).findAny().isPresent() ){
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
		sGebäude.clear();
		
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
	
	public static void schreiben( String aText ){
		sGebäude.clear();
		
		UnitType vGebäudeTyp = UnitType.None;
		if( Kern.selbst().getRace() == Race.Protoss ){
			vGebäudeTyp = UnitType.Protoss_Pylon;
		} else if( Kern.selbst().getRace() == Race.Zerg ){
			vGebäudeTyp = UnitType.Zerg_Creep_Colony;
			sGebäude.put(new TilePosition( -3 + Kern.spiel().getScreenPosition().toTilePosition().getX() + Kern.spiel().getMousePosition().toTilePosition().getX(), +1 + Kern.spiel().getScreenPosition().toTilePosition().getY() + Kern.spiel().getMousePosition().toTilePosition().getY()),
					UnitType.Zerg_Hatchery);
			Gebaeude.braucheEinheiten(UnitType.Zerg_Drone, 17);
		} else if( Kern.selbst().getRace() == Race.Terran ){
			vGebäudeTyp = UnitType.Terran_Missile_Turret;
			if( Kern.selbst().allUnitCount(UnitType.Terran_Engineering_Bay) <= 0){
				Gebaeude.braucheEinheiten(UnitType.Terran_Engineering_Bay, 1);
			}
		}
		
		int vSize=0;
		for( char vBuchstabe : aText.toCharArray() ){
			for( double[] vPosition : Alphabet.valueOf("" + vBuchstabe).getImage() ){
				sGebäude.put(new TilePosition((int)(vPosition[0]*2) + vSize + Kern.spiel().getScreenPosition().toTilePosition().getX() + Kern.spiel().getMousePosition().toTilePosition().getX(), (int)(vPosition[1]*2) + Kern.spiel().getScreenPosition().toTilePosition().getY() + Kern.spiel().getMousePosition().toTilePosition().getY()),
						vGebäudeTyp);
			}
			vSize+=Alphabet.valueOf("" + vBuchstabe).getWidth()*2;
		}
	}
	
	private static Map<Position, UnitType> sEinheiten = new HashMap<>();
	public static void darstellen( String aText ){
		sEinheiten.clear();
		sEinheitenAnPosition.clear();
		
		UnitType vUnitTyp = UnitType.None;
		if( Kern.selbst().getRace() == Race.Protoss ){
			vUnitTyp = UnitType.Protoss_Probe;
		} else if( Kern.selbst().getRace() == Race.Zerg ){
			vUnitTyp = UnitType.Zerg_Zergling;
			if( Kern.selbst().allUnitCount(UnitType.Zerg_Spawning_Pool) <= 0){
				Gebaeude.braucheEinheiten(UnitType.Zerg_Spawning_Pool, 1);
			}
		} else if( Kern.selbst().getRace() == Race.Terran ){
			vUnitTyp = UnitType.Terran_Marine;
			if( Kern.selbst().allUnitCount(UnitType.Terran_Engineering_Bay) <= 4){
				Gebaeude.braucheEinheiten(UnitType.Terran_Barracks, 4);
			}
		}
		
		int vSize=0;
		for( char vBuchstabe : aText.toCharArray() ){
			for( double[] vPosition : Alphabet.valueOf("" + vBuchstabe).getImage() ){
				sEinheiten.put(new Position((int)(vPosition[0]*(vUnitTyp.width()+1)) + vSize + Kern.spiel().getScreenPosition().getX() + Kern.spiel().getMousePosition().getX(), (int)(vPosition[1]*(vUnitTyp.height()+1)) + Kern.spiel().getScreenPosition().getY() + Kern.spiel().getMousePosition().getY()),
						vUnitTyp);
			}
			vSize+=Alphabet.valueOf("" + vBuchstabe).getWidth()*(vUnitTyp.width()*1.5);
		}
	}
	
	private static Map<Position, Unit> sEinheitenAnPosition = new HashMap<>();
	public static void darstellen(){
		try{
			if( sEinheiten.size() + sEinheitenAnPosition.size() <= 0 ){
				return;
			}
	
			UnitType vEinheitenTyp = UnitType.None;
			for( Position vPosition : new ArrayList<>(sEinheiten.keySet()) ){
				vEinheitenTyp = sEinheiten.get(vPosition);
				Kern.spiel().drawBoxMap(
						vPosition.getX(), 
						vPosition.getY(), 
						vPosition.getX() + sEinheiten.get(vPosition).width(), 
						vPosition.getY() + sEinheiten.get(vPosition).height(), 
						Color.White);
			}
			for( Position vPosition : new ArrayList<>(sEinheitenAnPosition.keySet()) ){
				Kern.spiel().drawBoxMap(
						vPosition.getX(), 
						vPosition.getY(), 
						vPosition.getX() + sEinheitenAnPosition.get(vPosition).getType().width(), 
						vPosition.getY() + sEinheitenAnPosition.get(vPosition).getType().height(), 
						Color.Grey);
			}
			
			if( Kern.selbst().allUnitCount(vEinheitenTyp) < sEinheiten.size() + sEinheitenAnPosition.size() && !Gebaeude.istInProduction(vEinheitenTyp) ){
				Gebaeude.braucheEinheiten(vEinheitenTyp,  sEinheiten.size() + sEinheitenAnPosition.size() - Kern.selbst().allUnitCount(vEinheitenTyp));
			}

			if( Kern.spiel().getFrameCount()%20 == 0 ){
				for( Position vPosition : new ArrayList<>(sEinheitenAnPosition.keySet()) ){
					if( sEinheitenAnPosition.get(vPosition).exists()){
						sEinheitenAnPosition.get(vPosition).move(vPosition);
					} else {
						sEinheiten.put(vPosition, sEinheitenAnPosition.get(vPosition).getType());
						sEinheitenAnPosition.remove(vPosition);
					}
				}
				for( Unit vUnit : Kern.selbst().getUnits() ){
					if( vUnit.getType() == vEinheitenTyp && !sEinheitenAnPosition.values().contains(vUnit) ){
						vUnit.move(new Position(0, 0));
					}
				}
			}
			
			for( Position vPosition : new ArrayList<>(sEinheiten.keySet()) ){
				for( Unit vUnit : Kern.selbst().getUnits() ){
					if( vUnit.getType() == vEinheitenTyp && !sEinheitenAnPosition.values().contains(vUnit) && vUnit.move(vPosition)){
						sEinheitenAnPosition.put(vPosition, vUnit);
						sEinheiten.remove(vPosition);
					}
				}
			}
			
		} catch( Exception vException ){
			vException.printStackTrace();
		}
	}
	
	public static boolean sDance = false;
	
	private static Position[] sPositions = new Position[]{};
	private static int sPosition = 0;
	public static boolean dance(){
		
		if( !sDance || sPositions.length == 0 ){
			return false;
		}
		
		List<Unit> vDancers = new ArrayList<>();
		vDancers.addAll(Kern.selbst().getUnits());
		
		int vPosition = sPosition++;
		
		System.out.println(sPositions[vPosition%sPositions.length]);
		
		for( Unit vDancer : vDancers ){
			if( Kern.spiel().getFrameCount()%13 == 0 ){
				vDancer.move(sPositions[vPosition%sPositions.length].add(vDancer.getPosition()));
			}
		}
		
		return true;
		
	}
	
	public static void setDance( String aDancePattern ){
		
		List<Position> vDance = new ArrayList<>();
		
		for( String vDirection : aDancePattern.split(" ") ){
			try{
				vDance.add(Directionen.valueOf(vDirection).getDirection());
			} catch( Exception vException){
				vException.printStackTrace();
			}
		}
		System.out.println("L: " + vDance);
		sPositions = vDance.toArray(sPositions);
		System.out.println("A: " + Arrays.toString(sPositions));
		
	}
	
	enum Directionen{

		n( new Position(0, -1) ),
		ne( new Position(1, -1) ),
		e( new Position(1, 0) ),
		se( new Position(1, 1) ),
		s( new Position(0, 1) ),
		sw( new Position(-1, 1) ),
		w( new Position(-1, 0) ),
		nw( new Position(-1, -1) ),
		h( new Position(0, 0) );
		
		Position mDirection;
		private Directionen( Position aDirection ) {
			mDirection = aDirection;
		}
		
		Position getDirection(){
			return mDirection;
		}
		
	}
	
	enum Alphabet{
		A( new double[][]{{0.5,0},{1.5,0},{0,1},{2,1},{0,2},{1,2},{2,2},{0,3},{2,3},{0,4},{2,4}}, 4 ),
		a( new double[][]{{1,2},{2,2},{0,2.5},{0,3.5},{2,3},{1,4},{2,4}}, 4 ),
		E( new double[][]{{0,0},{1,0},{2,0},{0,1},{0,2},{1,2},{2,2},{0,3},{0,4},{1,4},{2,4}}, 4 ),
		e( new double[][]{{0,0},{1,0},{2,0},{0,1},{0,2},{1,2},{2,2},{0,3},{0,4},{1,4},{2,4}}, 4 ),
		H( new double[][]{{0,0},{2,0},{0,1},{2,1},{0,2},{1,2},{2,2},{0,3},{2,3},{0,4},{2,4}}, 4 ),
		i( new double[][]{{0,0},{0,2},{0,3},{0,4}}, 2 ),
		L( new double[][]{{0,0},{0,1},{0,2},{0,3},{0,4},{1,4},{2,4}}, 4 ),
		n( new double[][]{{0.5,2},{1.5,2},{0,3},{2,3},{0,4},{2,4}}, 4 ),
		o( new double[][]{{0.5,2},{1.5,2},{0,3},{2,3},{0.5,4},{1.5,4}}, 4 ),
		S( new double[][]{{0.5,0},{1.5,0},{0,1},{0.5,2},{1.5,2},{2,3},{0.5,4},{1.5,4}}, 4 ),
		s( new double[][]{{0.5,0},{1.5,0},{0,1},{0.5,2},{1.5,2},{2,3},{0.5,4},{1.5,4}}, 4 ),
		u( new double[][]{{0,2},{2,2},{0,3},{2,3},{0.5,4},{1.5,4}}, 4 );
		
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
