package de.fh_kiel.robotics.starcraft.assist;

import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;

public class Bot {

	Bot(){
		defineRace();
	}
	
	private void defineRace() {
		
		if( Kern.selbst().getRace() == Race.Zerg ){
			mSupply = UnitType.Zerg_Overlord;
			mProduction = UnitType.Zerg_Spawning_Pool;
			mNumberOfProduction = 1;
			mAttackUnit = UnitType.Zerg_Zergling;
		}
		if( Kern.selbst().getRace() == Race.Terran ){
			mSupply = UnitType.Terran_Supply_Depot;
			mProduction = UnitType.Terran_Barracks;
			mAttackUnit = UnitType.Terran_Marine;
		}
		if( Kern.selbst().getRace() == Race.Protoss ){
			mSupply = UnitType.Protoss_Pylon;
			mProduction = UnitType.Protoss_Gateway;
			mAttackUnit = UnitType.Protoss_Zealot;
		}
		
	}

    private UnitType mSupply = null;
    private UnitType mProduction = null;
    private int mNumberOfProduction = 4;
    private UnitType mAttackUnit = null;
    
    public void agiere(){

		if( !Gebaeude.istInProduction( Kern.selbst().getRace().getWorker() ) &&
			Kern.selbst().allUnitCount(Kern.selbst().getRace().getWorker()) < 70 ){
			
			Gebaeude.braucheEinheiten(Kern.selbst().getRace().getWorker(), 1);
		}
		
		if( Kern.selbst().supplyTotal() - Kern.selbst().supplyUsed() < 8 ){
			if( !Gebaeude.istInProduction(mSupply ) ){
				Gebaeude.braucheEinheiten( mSupply, 1);
			}
		}

		if( !Gebaeude.istInProduction( mProduction ) &&
			Kern.selbst().allUnitCount( mProduction ) < mNumberOfProduction){
			Gebaeude.braucheEinheiten( mProduction, 1);
		}
		
		if( !Gebaeude.istInProduction( mAttackUnit )){
			Gebaeude.braucheEinheiten( mAttackUnit, 10);
		}
		
		if( Kern.selbst().allUnitCount(mAttackUnit) >= 10 || !Kern.spiel().enemy().getUnits().isEmpty() ){
			angreifen();
		}
		
    }

    private int mLastAttack = 0;
    private Position vGegnerPosition = null;
    
	private void angreifen() {
		if( mLastAttack + 50 > Kern.spiel().getFrameCount()){
			return;
		}
		mLastAttack = Kern.spiel().getFrameCount();
		
		if( vGegnerPosition == null || Kern.spiel().isVisible(vGegnerPosition.toTilePosition()) && Kern.spiel().enemy().getUnits().isEmpty()){
			if( !Kern.spiel().enemy().getUnits().isEmpty() ){
				vGegnerPosition = Kern.spiel().enemy().getUnits().get(0).getPosition();		
			} else {
				
				for( BaseLocation vBase : BWTA.getStartLocations() ){
					
					if( !Kern.spiel().isExplored(vBase.getTilePosition()) ){
						vGegnerPosition = vBase.getPosition();
						break;
					}
					
				}
				
				if( vGegnerPosition == null ){
					for( BaseLocation vBase : BWTA.getBaseLocations() ){
						if( !Kern.spiel().isVisible(vBase.getTilePosition()) ){
							vGegnerPosition = vBase.getPosition();
							break;
						}
						
					}
				}			
				
			}
		}
		
		for(Unit vEinheit : Kern.selbst().getUnits()){
			if( vEinheit.canMove() && !vEinheit.getType().isWorker() ){
				vEinheit.attack( vGegnerPosition );
			}
		}
		
	}
}
