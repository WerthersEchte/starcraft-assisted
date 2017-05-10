package de.fh_kiel.robotics.starcraft.assist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bwapi.Position;
import bwapi.TechType;
import bwapi.TilePosition;
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
	
	private static int mLetzterFrameMitGebauterEinheit = 0;
	public static void produziereEinheiten(){
		if( mLetzterFrameMitGebauterEinheit + 3 >= BotKern.spiel().getFrameCount() ){
			return;
		}
		for(UnitType benoetigteEinheit : benoetigteEinheiten.keySet()){
			if( benoetigteEinheiten.getOrDefault(benoetigteEinheit, 0) > 0){
				if(produziereEinheit(benoetigteEinheit)){
					benoetigteEinheiten.put(benoetigteEinheit, benoetigteEinheiten.getOrDefault(benoetigteEinheit, 0) - 1);
					mLetzterFrameMitGebauterEinheit = BotKern.spiel().getFrameCount();
					return;
				}
				if(baueGebäude(benoetigteEinheit)){
					benoetigteEinheiten.put(benoetigteEinheit, benoetigteEinheiten.getOrDefault(benoetigteEinheit, 0) - 1);
					mLetzterFrameMitGebauterEinheit = BotKern.spiel().getFrameCount();
					return;
				}
			}
		}
	}
	
	public static boolean produziereEinheit(UnitType aEinheitenTyp){
		for(Unit vEinheit : BotKern.selbst().getUnits()){
			if( vEinheit.isIdle() &&
				vEinheit.canTrain(aEinheitenTyp) && 
				aEinheitenTyp.gasPrice() <= BotKern.selbst().gas() &&  
				aEinheitenTyp.mineralPrice() <= BotKern.selbst().minerals()){
				return vEinheit.train(aEinheitenTyp);
			}
		}
		return false;
	}
	
	public static boolean baueGebäude( UnitType aGebäudeTyp ){
		if( !aGebäudeTyp.isBuilding() || aGebäudeTyp.mineralPrice() > BotKern.selbst().minerals() || aGebäudeTyp.gasPrice() > BotKern.selbst().gas() ){
			return false;
		}
		
		Unit vBauer = null;
		for( Unit vPotenzellerBauer : BotKern.selbst().getUnits() ){
			if( vPotenzellerBauer.getType() == aGebäudeTyp.whatBuilds().first && (vPotenzellerBauer.isIdle() || vPotenzellerBauer.isGatheringMinerals()) ){
				vBauer = vPotenzellerBauer;
				break;
			}
		}
		
		if( vBauer == null ){
			return false;
		}
		
		if( aGebäudeTyp.isRefinery() ){
			for( Unit vGeysir : BotKern.spiel().neutral().getUnits()  ){
				if( vGeysir.getType() == UnitType.Resource_Vespene_Geyser ){
					if( vBauer.build(aGebäudeTyp, vGeysir.getTilePosition()) ){
						return true;
					}
				}
			}
		}
		
		for( Unit vAusgangsPunkt : BotKern.selbst().getUnits() ){
			if( vAusgangsPunkt.getType().isBuilding() ){
				
				TilePosition vAusgangsPosition = vAusgangsPunkt.getTilePosition();
				if( vBauer.build(aGebäudeTyp, new TilePosition(vAusgangsPosition.getX(), vAusgangsPosition.getY()-1-aGebäudeTyp.tileHeight())) ){
					return true;
				}
				if( vBauer.build(aGebäudeTyp, new TilePosition(vAusgangsPosition.getX(), vAusgangsPosition.getY()+1+1)) ){
					return true;
				}
				if( vBauer.build(aGebäudeTyp, new TilePosition(vAusgangsPosition.getX()-1-aGebäudeTyp.tileWidth(), vAusgangsPosition.getY())) ){
					return true;
				}
				if( vBauer.build(aGebäudeTyp, new TilePosition(vAusgangsPosition.getX()+1+1, vAusgangsPosition.getY())) ){
					return true;
				}
			}
		}
		
		return false;
	}
	
	private static Position mSammelpunkt = Position.Unknown; 
	public static void setzeSammelpunkt( Position aSammelpunkt ){
		mSammelpunkt = aSammelpunkt;
		if( mSammelpunkt != null && mSammelpunkt.isValid() ){
			for(Unit vEinheit : BotKern.selbst().getUnits()){
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
		
		if( !mExpansionswunsch || BotKern.selbst().getRace().getCenter().mineralPrice() >= BotKern.selbst().minerals() || mBaumeister != null && mBaumeister.exists() && !mBaumeister.isIdle() && !mBaumeister.isGatheringMinerals() ){
			return;
		}
		
		Unit vBasis = null;
		for( Unit vPotenzielleBasis : BotKern.selbst().getUnits() ){
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
			for(Unit vEinheitAufPositionFürBasis : BotKern.spiel().getUnitsInRadius(vPotenzielleBasis.getPoint(), 20)){
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
		for(Unit vArbeiter : BotKern.selbst().getUnits() ){
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
		if( BotKern.spiel().isExplored(vNaechsteMöglicheExpansion.getTilePosition()) ){
			if(vNaechsterArbeiter.build(BotKern.selbst().getRace().getCenter(), vNaechsteMöglicheExpansion.getTilePosition())){
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
		for(Unit vEinheit : BotKern.selbst().getUnits()){
			if( vEinheit.isIdle() &&
				vEinheit.canResearch(aTech) && 
				aTech.gasPrice() <= BotKern.selbst().gas() &&  
				aTech.mineralPrice() <= BotKern.selbst().minerals()){
				return vEinheit.research(aTech);
			}
		}
		return false;
	}
	
	public static boolean erforsche(UpgradeType aUpgrade){
		for(Unit vEinheit : BotKern.selbst().getUnits()){
			if( vEinheit.isIdle() &&
				vEinheit.canUpgrade(aUpgrade) && 
				aUpgrade.gasPrice() <= BotKern.selbst().gas() &&  
				aUpgrade.mineralPrice() <= BotKern.selbst().minerals()){
				return vEinheit.upgrade(aUpgrade);
			}
		}
		return false;
	}
}
