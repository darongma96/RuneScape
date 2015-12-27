package scripts.AutoMagic.Handlers;

import org.tribot.api.Clicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Mouse;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.GameTab;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Magic;
import org.tribot.api2007.types.RSItem;

import scripts.AutoMagic.Constants.Spells;

public class AlchHandler {
	public static boolean clickAlch() {
		if (!Magic.selectSpell("High Level Alchemy")) {
			Clicking.click(Spells.startingTile);
			//startingTile.isOnScreen() ? Clicking.click(startingTile) : Clicking.click(Player.getPosition());
			Magic.selectSpell("High Level Alchemy");
		}
		return Timing.waitCondition(new Condition() {
			@Override
			public boolean active() {
				General.sleep(100,200);
				return GameTab.TABS.INVENTORY.isOpen();
			}
		}, 5000);
	}

	public static boolean clickItemToAlch() {	
		final RSItem[] alchItem = Inventory.find(Spells.itemToAlch);
		
		if (alchItem.length > 0) {
			Clicking.click(alchItem[0]);
		} else {
			General.println("out of items to alch");
			Mouse.clickBox(630, 470, 650, 500, 1);
			Spells.run_script = false;
		}
		
		return Timing.waitCondition(new Condition() {
			@Override
			public boolean active() {
				General.sleep(100,200);
				return GameTab.TABS.MAGIC.isOpen();
			}
		}, 5000);
	}
}
