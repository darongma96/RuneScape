package scripts.AutoMagic;

import org.tribot.api.General;
import org.tribot.api2007.Magic;

import scripts.AutoMagic.Constants.Spells;
import scripts.AutoMagic.Handlers.AlchHandler;
import scripts.AutoMagic.Handlers.CurseHandler;

public class BotManager {
	
	private State getState() {
		if (Spells.mode == "curse")
			return State.CURSE;
		if (Spells.mode == "alch") 
			return State.ALCH;
		if (Spells.mode == "stunalch") 
			return State.STUNALCH;
		if (Spells.mode == "teleport")
			return State.TELEPORT;
		return null;
	}
	
	public void bot() {
		State state = getState();
		
		switch (state) {
		
		case STUNALCH:
		case CURSE:
			CurseHandler.clickSpell();
			CurseHandler.findEnemy();
			CurseHandler.clickEnemy();
			if (state == State.CURSE)
				break;
			
		case ALCH:
			AlchHandler.clickAlch();
			AlchHandler.clickItemToAlch();
			break;
			
		case TELEPORT:
			General.sleep(500,1500);
			Magic.selectSpell(Spells.spellToCast);
			break;
		}
	}
}
