package de.fh_kiel.robotics.starcraft.assist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Position;
import bwapi.Race;
import bwapi.TechType;
import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import bwta.BWTA;
import bwta.BaseLocation;

public class Gebaeude {
	
	public static void reset(){
		
		benoetigteEinheiten.clear();
		benoetigteTechs.clear();
		benoetigteUpgrades.clear();
		mLetzterFrameMitGebauterEinheit = 0;
		
		mBaumeister = null;
		mExpansionswunsch = false;
		
		
	}
	
	private static Map<UnitType, Integer> benoetigteEinheiten = new HashMap<>(); 
	
	public static boolean istInProduction( UnitType aEinheitentyp ){
		return benoetigteEinheiten.containsKey(aEinheitentyp) && benoetigteEinheiten.get(aEinheitentyp) > 0;
	}
	
	public static void braucheEinheiten(UnitType aEinheit, int aAnzahl){
		benoetigteEinheiten.put(aEinheit, benoetigteEinheiten.getOrDefault( aEinheit, 0) + aAnzahl);
	}
	
	public static void einheitErstellt( UnitType vEinheit ){
		if( istInProduction( vEinheit ) ){
			benoetigteEinheiten.put(vEinheit, benoetigteEinheiten.getOrDefault(vEinheit, 0) - 1);
			sGebäudeImBau.remove(vEinheit);
		}
	}
	
	private static int mLetzterFrameMitGebauterEinheit = 0;
	public static void produziereEinheiten(){
		if( mLetzterFrameMitGebauterEinheit + 3 >= Kern.spiel().getFrameCount() ){
			return;
		}
	
		List<UnitType> vAlleGewüschtenEinheiten = new ArrayList<UnitType>(benoetigteEinheiten.keySet());
		Collections.shuffle(vAlleGewüschtenEinheiten);
		
		for(UnitType benoetigteEinheit : vAlleGewüschtenEinheiten){
			if( benoetigteEinheiten.getOrDefault(benoetigteEinheit, 0) > 0){
				if(produziereEinheit(benoetigteEinheit)){
					mLetzterFrameMitGebauterEinheit = Kern.spiel().getFrameCount();
					return;
				}
				if(baueGebäude(benoetigteEinheit)){
					mLetzterFrameMitGebauterEinheit = Kern.spiel().getFrameCount();
					sGebäudeImBau.put(benoetigteEinheit, Kern.spiel().getFrameCount());
					return;
				}
			}
		}
	}
	
	public static boolean produziereEinheit(UnitType aEinheitenTyp){
		for(Unit vEinheit : Kern.selbst().getUnits()){
			if( vEinheit.isIdle() &&
				vEinheit.canTrain(aEinheitenTyp) && 
				aEinheitenTyp.gasPrice() <= Kern.selbst().gas() &&  
				aEinheitenTyp.mineralPrice() <= Kern.selbst().minerals()){
				return vEinheit.train(aEinheitenTyp);
			}
		}
		return false;
	}
	
	private static Map<UnitType, Integer> sGebäudeImBau = new HashMap<>();
	public static boolean baueGebäude( UnitType aGebäudeTyp ){
		if( !aGebäudeTyp.isBuilding() || 
			sGebäudeImBau.containsKey(aGebäudeTyp) && sGebäudeImBau.get(aGebäudeTyp) + 500 > Kern.spiel().getFrameCount() || 
			aGebäudeTyp.mineralPrice() > Kern.selbst().minerals() || 
			aGebäudeTyp.gasPrice() > Kern.selbst().gas() ){
			return false;
		}
		
		Unit vBauer = null;
		for( Unit vPotenzellerBauer : Kern.selbst().getUnits() ){
			if( vPotenzellerBauer.getType() == aGebäudeTyp.whatBuilds().first && (vPotenzellerBauer.isIdle() || vPotenzellerBauer.isGatheringMinerals()) && vPotenzellerBauer.getAddon() == null ){
				vBauer = vPotenzellerBauer;
				break;
			}
		}

		if( vBauer == null ){
			return false;
		}
		
		if( vBauer.getType().isBuilding() && vBauer.getType().getRace() == Race.Terran && !vBauer.isFlying() ){
			if( vBauer.build(aGebäudeTyp, new TilePosition(4, 1).add(vBauer.getTilePosition())) ){
				vBauer.lift();
				return true;
				
			}
			vBauer.lift();
			return false;
		}
		
		if( aGebäudeTyp.isRefinery() ){
			for( Unit vGeysir : Kern.spiel().neutral().getUnits()  ){
				if( vGeysir.getType() == UnitType.Resource_Vespene_Geyser ){
					if( vBauer.build(aGebäudeTyp, vGeysir.getTilePosition()) ){
						return true;
					}
				}
			}
		}
		
		List<Unit> vAlleEigenenEinheiten = new ArrayList<Unit>(Kern.selbst().getUnits());
		Collections.shuffle(vAlleEigenenEinheiten);
		
		for( Unit vAusgangsPunkt : vAlleEigenenEinheiten ){
			if( vAusgangsPunkt.getType().isBuilding() ){
				
				bwapi.TilePosition vAusgangsPosition = vAusgangsPunkt.getTilePosition();
				TilePosition vZielPosition = new TilePosition(vAusgangsPosition.getX(), vAusgangsPosition.getY()-1-aGebäudeTyp.tileHeight());
				if( istPositionFrei(vZielPosition) && sindKeineResourcenInDerNähe(vZielPosition.toPosition()) && vBauer.build(aGebäudeTyp, vZielPosition) ){
					zukünftigesGebäudeAn( aGebäudeTyp, vZielPosition);
					return true;
				}
				vZielPosition = new TilePosition(vAusgangsPosition.getX(), vAusgangsPosition.getY()+vAusgangsPunkt.getType().tileHeight()+1);
				if( istPositionFrei(vZielPosition) && sindKeineResourcenInDerNähe(vZielPosition.toPosition()) && vBauer.build(aGebäudeTyp, vZielPosition) ){
					zukünftigesGebäudeAn( aGebäudeTyp, vZielPosition);
					return true;
				}
				vZielPosition = new TilePosition(vAusgangsPosition.getX()-1-aGebäudeTyp.tileWidth(), vAusgangsPosition.getY());
				if( istPositionFrei(vZielPosition) && sindKeineResourcenInDerNähe(vZielPosition.toPosition()) && vBauer.build(aGebäudeTyp, vZielPosition) ){
					zukünftigesGebäudeAn( aGebäudeTyp, vZielPosition);
					return true;
				}
				vZielPosition = new TilePosition(vAusgangsPosition.getX()+vAusgangsPunkt.getType().tileWidth()+1, vAusgangsPosition.getY());
				if( istPositionFrei(vZielPosition) && sindKeineResourcenInDerNähe(vZielPosition.toPosition()) && vBauer.build(aGebäudeTyp, vZielPosition) ){
					zukünftigesGebäudeAn( aGebäudeTyp, vZielPosition);
					return true;
				}
				
			}
		}
		
		return false;
	}
	
	private static boolean sindKeineResourcenInDerNähe( Position aPosition ){
		for( Unit vEinheit : Kern.spiel().getUnitsInRadius(aPosition, 3*32)){
			if( vEinheit.getType().isMineralField() || vEinheit.getType() == UnitType.Resource_Vespene_Geyser || vEinheit.getType() == Kern.selbst().getRace().getRefinery() ){
				return false;
			}
		}
		return true;
	}
	
	private static Map<TilePosition, Integer> sPositionMitZukünftigemGebäude = new HashMap<>();
	
	public static boolean istPositionFrei( TilePosition aPosition ){
		for( TilePosition vPosition : new ArrayList<>(sPositionMitZukünftigemGebäude.keySet()) ){
			if( sPositionMitZukünftigemGebäude.get(vPosition) + 500 < Kern.spiel().getFrameCount() ){
				sPositionMitZukünftigemGebäude.remove(vPosition);
			}
		}
		
		return !sPositionMitZukünftigemGebäude.keySet().stream().filter(position->Helfer.istGleich(position, aPosition)).findAny().isPresent();
	}
	
	public static void zukünftigesGebäudeAn( UnitType aGebäude, TilePosition aPosition ){
		if( !aGebäude.isBuilding() || !aPosition.isValid()){
			return;
		}
		
		for( int vX = 0; vX < aGebäude.tileWidth(); ++vX){
			for( int vY = 0; vY < aGebäude.tileHeight(); ++vY){
				sPositionMitZukünftigemGebäude.put(new TilePosition(vX + aPosition.getX(), vY + aPosition.getY()), Kern.spiel().getFrameCount());
			}
		}
	}
	
	private static Position mSammelpunkt = Position.Unknown; 
	public static void setzeSammelpunkt( Position aSammelpunkt ){
		mSammelpunkt = aSammelpunkt;
		if( mSammelpunkt != null && mSammelpunkt.isValid() ){
			for(Unit vEinheit : Kern.selbst().getUnits()){
				if(vEinheit.canSetRallyPoint()){
					vEinheit.setRallyPoint(mSammelpunkt);
				}
			}
		}
	}
	public static Position holeSammelpunkt(){
		return mSammelpunkt;
	}
	
	private static boolean mExpansionswunsch = false;
	private static Unit mBaumeister = null;
	public static void expansionswunsch(){
		mExpansionswunsch = true;
	}
	public static void expandiere(){
		
		if( !mExpansionswunsch || Kern.selbst().getRace().getCenter().mineralPrice() >= Kern.selbst().minerals() || mBaumeister != null && mBaumeister.exists() && !mBaumeister.isIdle() && !mBaumeister.isGatheringMinerals() ){
			return;
		}
		
		Unit vBasis = null;
		for( Unit vPotenzielleBasis : Kern.selbst().getUnits() ){
			if( vPotenzielleBasis.getType().isResourceDepot() ){
				vBasis = vPotenzielleBasis;
				break;
			}
		}
		
		if( vBasis == null){
			return;
		}
		
		BaseLocation vNaechsteMöglicheExpansion = null;
		for( BaseLocation vPotenzielleBasis : BWTA.getBaseLocations() ){
			boolean vFree = true;
			for(Unit vEinheitAufPositionFürBasis : Kern.spiel().getUnitsInRadius(vPotenzielleBasis.getPoint(), 20)){
				if( vEinheitAufPositionFürBasis.getType().isBuilding() ){
					vFree = false;
					break;
				}
			}
			if( vFree && 
			   (vNaechsteMöglicheExpansion == null ||
				BWTA.getGroundDistance(vPotenzielleBasis.getTilePosition(), vBasis.getTilePosition()) < BWTA.getGroundDistance(vNaechsteMöglicheExpansion.getTilePosition(), vBasis.getTilePosition())
				)){
				vNaechsteMöglicheExpansion = vPotenzielleBasis;
			}
		}
		
		if( vNaechsteMöglicheExpansion == null){
			return;
		}
		
		Unit vNaechsterArbeiter = null;
		for(Unit vArbeiter : Kern.selbst().getUnits() ){
			if( vArbeiter.getType().isWorker() &&
				(vArbeiter.isIdle() || vArbeiter.isGatheringMinerals()) &&
				(vNaechsterArbeiter == null ||
				BWTA.getGroundDistance(vNaechsterArbeiter.getTilePosition(), vNaechsteMöglicheExpansion.getTilePosition()) > BWTA.getGroundDistance(vArbeiter.getTilePosition(), vNaechsteMöglicheExpansion.getTilePosition()))){
				vNaechsterArbeiter = vArbeiter;
			}
		}

		if( vNaechsterArbeiter == null){
			return;
		}
		
		mBaumeister = vNaechsterArbeiter;
		if( Kern.spiel().isExplored(vNaechsteMöglicheExpansion.getTilePosition()) ){
			if(vNaechsterArbeiter.build(Kern.selbst().getRace().getCenter(), vNaechsteMöglicheExpansion.getTilePosition())){
				mExpansionswunsch = false;
				mBaumeister = null;
			}
		} else {
			vNaechsterArbeiter.move(vNaechsteMöglicheExpansion.getPosition());
		}
		
	}
	
	private static List<UpgradeType> benoetigteUpgrades = new ArrayList<>(); 
	
	public static void braucheUpgrades(UpgradeType aUpgrade, int aAnzahl){
		for(int i = 0 ; i<aAnzahl; i++){
			benoetigteUpgrades.add(aUpgrade);
		}
	}
	
	private static List<TechType> benoetigteTechs= new ArrayList<>(); 
	
	public static void braucheTechs(TechType aTech, int aAnzahl){
		for(int i = 0 ; i<aAnzahl; i++){
			benoetigteTechs.add(aTech);
		}
	}
	
	public static void erforsche(){
		for(TechType benoetigteTech : new ArrayList<>(benoetigteTechs)){
			if(erforsche(benoetigteTech)){
				benoetigteTechs.remove(benoetigteTech);
				return;
			}
		}
		for(UpgradeType benoetigtesUpgrade : new ArrayList<>(benoetigteUpgrades)){
			if(erforsche(benoetigtesUpgrade)){
				benoetigteUpgrades.remove(benoetigtesUpgrade);
				return;
			}
		}
	}
	
	public static boolean erforsche(TechType aTech){
		for(Unit vEinheit : Kern.selbst().getUnits()){
			if( vEinheit.isIdle() &&
				vEinheit.canResearch(aTech) && 
				aTech.gasPrice() <= Kern.selbst().gas() &&  
				aTech.mineralPrice() <= Kern.selbst().minerals()){
				return vEinheit.research(aTech);
			}
		}
		return false;
	}
	
	public static boolean erforsche(UpgradeType aUpgrade){
		for(Unit vEinheit : Kern.selbst().getUnits()){
			if( vEinheit.isIdle() &&
				vEinheit.canUpgrade(aUpgrade) && 
				aUpgrade.gasPrice() <= Kern.selbst().gas() &&  
				aUpgrade.mineralPrice() <= Kern.selbst().minerals()){
				return vEinheit.upgrade(aUpgrade);
			}
		}
		return false;
	}
}
