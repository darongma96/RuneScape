package scripts.AutoMagic.Handlers;

import org.tribot.api.Clicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api.util.ABCUtil;
import org.tribot.api2007.Camera;
import org.tribot.api2007.Magic;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSNPC;

import scripts.AutoMagic.Constants.Spells;

public class CurseHandler {
	private static ABCUtil abcUtil = new ABCUtil();
	
	public static boolean clickSpell() {
		return Magic.selectSpell(Spells.spellToCast);
	}
	
	public static boolean findEnemy() {
		final RSNPC[] enemy = NPCs.findNearest(Spells.npcToCast);
		
		if (enemy.length > 0 && enemy[0] != null) {
			if (enemy[0].isOnScreen()) {
				return true;
			}
			else {
				Camera.turnToTile(enemy[0]);
				return enemy[0].isOnScreen();
			}
		}
		return false;
	}
	
	public static boolean clickEnemy() {
		final RSNPC[] enemy = NPCs.findNearest(Spells.npcToCast);
		if (enemy.length > 0 && enemy[0] != null && enemy[0].isOnScreen()) {
			if (abcUtil.BOOL_TRACKER.HOVER_NEXT.next()) {
				enemy[0].hover();	
				Timing.waitCondition(new Condition() {
					@Override
					public boolean active() {
						General.sleep(50);
						return Player.getAnimation() < 0;
					}
				}, General.random(500, 1000));
			}
			abcUtil.BOOL_TRACKER.HOVER_NEXT.reset();
			return Clicking.click(enemy[0]);
		}
		return false;
	}
}
