package de.fh_kiel.robotics.starcraft.assist;

import bwapi.DefaultBWListener;
import bwapi.Game;
import bwapi.Mirror;
import bwapi.Player;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;

public class BotKern extends DefaultBWListener {

	private Mirror mirror = new Mirror();

    private static Game mSpiel;

    public static Game spiel(){
    	return mSpiel;
    }
    private static Player mSelbst;

    public static Player selbst(){
    	return mSelbst;
    }
    
    @Override
    public void onUnitComplete(Unit unit) {
    	Gebaeude.setzeSammelpunkt(Gebaeude.holeSammelpunkt());
    }
    
    @Override
    public void onSendText(String aBefehl) {

    	aBefehl = EingabeVerarbeitung.einheitenBauenLassen(aBefehl);
    	aBefehl = EingabeVerarbeitung.upgradesErforschenLassen(aBefehl);
    	aBefehl = EingabeVerarbeitung.sammelpunktSetzen(aBefehl);
    	aBefehl = EingabeVerarbeitung.sammeln(aBefehl);
    	aBefehl = EingabeVerarbeitung.angreifen(aBefehl);
    	aBefehl = EingabeVerarbeitung.expandieren(aBefehl);

    	aBefehl = EingabeVerarbeitung.toggleAnzeigen(aBefehl);
    	aBefehl = EingabeVerarbeitung.resetAll(aBefehl);
    	
    	if( !aBefehl.isEmpty() ){
    		spiel().sendText(aBefehl);
    	}
    }

    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }
    
    @Override
    public void onStart() {
		Anzeige.reset();
		Gebaeude.reset();
    	
        mSpiel = mirror.getGame();
        mSelbst = mSpiel.self();

        System.out.println("Karte: " + spiel().mapFileName());
        System.out.println("Analysiere Karte...");
        BWTA.readMap();
        BWTA.analyze();
        System.out.println("Kartendaten bereit!");
        
    	spiel().enableFlag(1);
    	
    	defineRace();
    }

	@Override
    public void onFrame() {
    	try{
    		
    		long vStartZeitFrame = System.nanoTime();
    		
    		realBot();
    		
    		Anzeige.anzeigen();
    	
    		Einheiten.mineralienSammeln();

    		Gebaeude.produziereEinheiten();
    		Gebaeude.erforsche();
    		Gebaeude.expandiere();
    		
    		long vEndZeitFrame = System.nanoTime();
    		
    		if( vEndZeitFrame-vStartZeitFrame > 5*1e6 ){
    			System.out.println("Frame " + spiel().getFrameCount() + ": " +(vEndZeitFrame-vStartZeitFrame)/1e6 + " ms");
    		}
    		if( spiel().getFrameCount() > 25 && vEndZeitFrame-vStartZeitFrame > 40*1e6 ){
    			if( Anzeige.sBildschirmAusgabe ){
    				Anzeige.sBildschirmAusgabe = false;
    			} else if( Anzeige.sGeländeAusgabe ) {
    				Anzeige.sGeländeAusgabe = false;
    			}
    		}

    	}catch( Exception vException ){
    		vException.printStackTrace();
    	}
    }

    private void defineRace() {
		
		if( selbst().getRace() == Race.Zerg ){
			mSupply = UnitType.Zerg_Overlord;
			mProduction = UnitType.Zerg_Spawning_Pool;
			mNumberOfProduction = 1;
			mAttackUnit = UnitType.Zerg_Zergling;
		}
		if( selbst().getRace() == Race.Terran ){
			mSupply = UnitType.Terran_Supply_Depot;
			mProduction = UnitType.Terran_Barracks;
			mAttackUnit = UnitType.Terran_Marine;
		}
		if( selbst().getRace() == Race.Protoss ){
			mSupply = UnitType.Protoss_Pylon;
			mProduction = UnitType.Protoss_Gateway;
			mAttackUnit = UnitType.Protoss_Zealot;
		}
		
	}

    private UnitType mSupply = null;
    private UnitType mProduction = null;
    private int mNumberOfProduction = 4;
    private UnitType mAttackUnit = null;
    
    private void realBot(){

		if( !Gebaeude.istInProduction( selbst().getRace().getWorker() ) &&
			 selbst().allUnitCount(selbst().getRace().getWorker()) < 70 ){ //TODO: Magic Number
			Gebaeude.braucheEinheiten(selbst().getRace().getWorker(), 1);
		}
		
		if( selbst().supplyTotal() - selbst().supplyUsed() < 8 ){
			if( !Gebaeude.istInProduction(mSupply ) ){
				Gebaeude.braucheEinheiten( mSupply, 1);
			}
		}

		if( !Gebaeude.istInProduction( mProduction ) &&
			selbst().allUnitCount( mProduction ) < mNumberOfProduction){
			Gebaeude.braucheEinheiten( mProduction, 1);
		}
		
		if( !Gebaeude.istInProduction( mAttackUnit )){
			Gebaeude.braucheEinheiten( mAttackUnit, 10);
		}
		
		
		
    }

	public static void main(String[] args) {
        new BotKern().run();
    }
}
