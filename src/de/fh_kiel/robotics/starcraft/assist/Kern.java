package de.fh_kiel.robotics.starcraft.assist;

import bwapi.DefaultBWListener;
import bwapi.Game;
import bwapi.Mirror;
import bwapi.Player;
import bwapi.Position;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;
import bwta.BaseLocation;

public class Kern extends DefaultBWListener {

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
    public void onUnitCreate(Unit unit) {
    	Gebaeude.einheitErstellt(unit.getType());
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

    	aBefehl = EingabeVerarbeitung.toggleBot(aBefehl, this);

    	aBefehl = EingabeVerarbeitung.fun(aBefehl);
    	
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
    	
    	mExampleBot = new Bot();
    }
    
    private Bot mExampleBot;
    public boolean mBotActive = false;

	@Override
    public void onFrame() {
    	try{
    		
    		long vStartZeitFrame = System.nanoTime();
    		
    		if(Fun.dance()){
    			return;
    		}
    		
    		if( mExampleBot != null & mBotActive ){
    			mExampleBot.agiere();
    		}
    		Fun.bauen();
    		
    		Anzeige.anzeigen();
    	
    		Einheiten.mineralienSammeln();

    		Gebaeude.produziereEinheiten();
    		Gebaeude.erforsche();
    		Gebaeude.expandiere();
    		
    		Fun.darstellen();
    		
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

	public static void main(String[] args) {
        new Kern().run();
    }
}


































