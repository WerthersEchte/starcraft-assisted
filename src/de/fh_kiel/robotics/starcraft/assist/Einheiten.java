package de.fh_kiel.robotics.starcraft.assist;

import bwapi.Unit;
import bwta.BWTA;

public class Einheiten {
	
	public static void mineralienSammeln(){
		
		for( Unit vEinheit : Kern.selbst().getUnits() ){
			
			if( vEinheit.getType().isWorker() &&
				vEinheit.isIdle() &&
				!vEinheit.isSelected() ){
				
				Unit vHauptGebaude = null;
				for( Unit vPotenzialesHauptGebaude : Kern.selbst().getUnits()){
					if( vPotenzialesHauptGebaude.getType().isResourceDepot() ){
						if( vHauptGebaude == null || 
							vHauptGebaude.getDistance(vEinheit) > vPotenzialesHauptGebaude.getDistance(vEinheit) ){
							vHauptGebaude = vPotenzialesHauptGebaude;
						}
					}
				}
				
				Unit vMineralienKristall = null;
				if( vHauptGebaude != null ){
					for( Unit vPotenzialerMineralienKristall : BWTA.getNearestBaseLocation(vHauptGebaude.getPosition()).getMinerals() ){
						if( vMineralienKristall == null || 
								vMineralienKristall.getDistance(vEinheit) > vPotenzialerMineralienKristall.getDistance(vEinheit) ){
							vMineralienKristall = vPotenzialerMineralienKristall;
						}
					}
					
				}
				if( vMineralienKristall == null ){
					for( Unit vPotenzialerMineralienKristall : Kern.spiel().neutral().getUnits() ){
						if( vPotenzialerMineralienKristall.getType().isMineralField() ){
							if( vMineralienKristall == null || 
									vMineralienKristall.getDistance(vEinheit) > vPotenzialerMineralienKristall.getDistance(vEinheit) ){
								vMineralienKristall = vPotenzialerMineralienKristall;
							}
						}
					}
				}
				
				if( vMineralienKristall != null ){
					vEinheit.gather(vMineralienKristall);
				}
				
			}
			
		}
		
	}

}
