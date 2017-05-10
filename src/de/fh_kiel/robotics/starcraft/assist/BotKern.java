package de.fh_kiel.robotics.starcraft.assist;

import bwapi.DefaultBWListener;
import bwapi.Game;
import bwapi.Mirror;
import bwapi.Player;
import bwapi.Unit;
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
    }

    @Override
    public void onFrame() {
    	try{
    		
    		long vStartZeitFrame = System.nanoTime();
    		
    		Anzeige.anzeigen();
    	
    		Einheiten.mineralienSammeln();

    		Gebaeude.produziereEinheiten();
    		Gebaeude.erforsche();
    		Gebaeude.expandiere();
    		
    		long vEndZeitFrame = System.nanoTime();
    		
    		if( vEndZeitFrame-vStartZeitFrame > 5*1e6 ){
    			System.out.println("Frame " + spiel().getFrameCount() + ": " +(vEndZeitFrame-vStartZeitFrame)/1e6 + " ms");
    		}
    		if( vEndZeitFrame-vStartZeitFrame > 40*1e6 ){
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

	public static void main(String[] args) {
        new BotKern().run();
    }
}
