package scripts.AutoMagicOld;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import org.tribot.api.Clicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Mouse;
import org.tribot.api.types.generic.Condition;
import org.tribot.api.util.ABCUtil;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Camera;
import org.tribot.api2007.GameTab;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Login;
import org.tribot.api2007.Magic;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.Player;
import org.tribot.api2007.Skills;
import org.tribot.api2007.Walking;
import org.tribot.api2007.WebWalking;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSNPC;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.MessageListening07;
import org.tribot.script.interfaces.Painting;

@ScriptManifest(authors = "gbaelement7", category = "Magic", name = "Auto Magic" )
public class AutoMagic extends Script implements Painting, MessageListening07 {
	ABCUtil abcUtil = new ABCUtil();
	private boolean run_script;
	private String mode;
	private String spellToCast;
	private String npcToCast;
	private String itemToAlch;
	private String itemToEnchant; // get itemToEnchant from GUI
	private RSTile startingTile = Player.getPosition();
	private String[] runes = {"Air rune", "Water rune", "Earth Rune", "Fire rune", "Cosmic rune"};
	
	private boolean magicTab() {
		return GameTab.TABS.MAGIC.open();
	}
	
	private boolean startingPosition() {
		if (Player.getPosition().equals(startingTile)) {
			return true;
		}
		else {
			if (Player.getPosition().getPlane() == startingTile.getPlane()) {
				return Walking.blindWalkTo(startingTile);
			}
			else {
				return WebWalking.walkTo(startingTile);
			}
		}
	}
		
	private boolean clickSpell() {
		return Magic.selectSpell(spellToCast);
	}
	
	private boolean isSpellSelected() {
		return Magic.isSpellSelected();
	}
	
	private boolean findEnemy() {
		final RSNPC[] enemy = NPCs.findNearest(npcToCast);
		
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
	
	private boolean clickEnemy() {
		final RSNPC[] enemy = NPCs.findNearest(npcToCast);
		if (enemy.length > 0 && enemy[0] != null && enemy[0].isOnScreen()) {
			if (abcUtil.BOOL_TRACKER.HOVER_NEXT.next()) {
				enemy[0].hover();	
				Timing.waitCondition(new Condition() {
					@Override
					public boolean active() {
						sleep(50);
						return Player.getAnimation() < 0;
					}
				}, General.random(500, 1000));
			}
			abcUtil.BOOL_TRACKER.HOVER_NEXT.reset();
			return Clicking.click(enemy[0]);
		}
		return false;
	}
	
	private boolean clickAlch() {
		if (!Magic.selectSpell("High Level Alchemy")) {
			Clicking.click(startingTile);
			//startingTile.isOnScreen() ? Clicking.click(startingTile) : Clicking.click(Player.getPosition());
			Magic.selectSpell("High Level Alchemy");
		}
		return Timing.waitCondition(new Condition() {
			@Override
			public boolean active() {
				sleep(100,200);
				return GameTab.TABS.INVENTORY.isOpen();
			}
		}, 5000);
	}

	private boolean clickItemToAlch() {	
		final RSItem[] alchItem = Inventory.find(itemToAlch);
		
		if (alchItem.length > 0) {
			Clicking.click(alchItem[0]);
		}
		else {
			println("out of items to alch");
			Mouse.clickBox(630, 470, 650, 500, 1);
			run_script = false;
		}
		
		return Timing.waitCondition(new Condition() {
			@Override
			public boolean active() {
				sleep(100,200);
				return GameTab.TABS.MAGIC.isOpen();
			}
		}, 5000);
	}
	
	private boolean enchant() {
		RSItem[] enchantItem = Inventory.find(itemToEnchant);
		
		if (enchantItem.length > 0 && enchantItem[0] != null) {
			if (magicTab()) {
				Magic.selectSpell(spellToCast);
				Timing.waitCondition(new Condition() {
					@Override
					public boolean active() {
						sleep(100,200);
						return GameTab.TABS.INVENTORY.isOpen();
					}
				}, General.random(3000, 5000));
				
				Clicking.click(enchantItem[0]);
				return Timing.waitCondition(new Condition() {
					@Override
					public boolean active() {
						sleep(100,200);
						return GameTab.TABS.MAGIC.isOpen();
					}
				}, General.random(3000, 5000));
			}
		}
		
		return false;
	}
	
	private boolean deposit() {
		if (Banking.isInBank()) {
			if (!Banking.isBankScreenOpen()) {
				Banking.openBank();
			}
			Timing.waitCondition(new Condition() {
				@Override
				public boolean active() {
					sleep(100,200);
					return Banking.isBankScreenOpen();
				}
			}, General.random(3000, 5000));
		}
		
		if (Banking.depositAllExcept(runes) > 0) {
			Banking.close();
			return true;
		}
		
		return false;
	}

	private boolean withdraw() {
		if (Banking.isInBank()) {
			if (!Banking.isBankScreenOpen()) {
				Banking.openBank();
			}
			Banking.withdraw(0, itemToEnchant);
			Banking.close();
			return Inventory.getCount(itemToEnchant) > 0;
		}
		return false;
	}
	
	@Override
	public void run() {
		AutoMagicGUI gui = new AutoMagicGUI();
		General.useAntiBanCompliance(true);
		run_script = true;
		
		while (!gui.isGuiComplete()) {
			sleep(100);
		}
		if (General.getTRiBotUsername().equals("foebotting")) {
			run_script = false;
		}
		mode = gui.getMode();
		spellToCast = gui.getSpell();
		npcToCast = gui.getNPC();
		itemToAlch = gui.getItem();
		itemToEnchant = gui.getItemToEnchant();
		
		while (run_script) {
			sleep(100,200);
			
			abcUtil.performTimedActions(Skills.SKILLS.MAGIC);
			
			if (magicTab()) {
				
				if (mode == "curse" || mode == "stunalch") {
					startingPosition();
				}
				
				if (mode == "curse" || mode == "stunalch") {
					if (findEnemy()) {
						clickSpell();
						if (isSpellSelected()) {
							clickEnemy();
						}
						else {
							clickSpell();
							clickEnemy();
						}
					}
				}
				
				if (mode == "alch" || mode == "stunalch") {
					clickAlch();
					clickItemToAlch();
				}
				
				else if (mode == "teleport") {
					clickSpell();
				}
				
				else if (mode == "enchant") {
					if (Inventory.getCount(itemToEnchant) > 0) {
						enchant();
					}
					else {
						deposit();
						withdraw();
					}
				}
			}
		}
		Login.logout();
	}

	private final long startTime = System.currentTimeMillis();
	private final int startLvl = Skills.getActualLevel(Skills.SKILLS.MAGIC);
	private final int startXP = Skills.getXP(Skills.SKILLS.MAGIC);
	
    private final Color color1 = new Color(0, 0, 0, 130);
    private final Color color2 = new Color(0, 0, 0);
    private final Color color3 = new Color(204, 0, 0, 204);
    private final Color color4 = new Color(255, 255, 255);

    private final BasicStroke stroke1 = new BasicStroke(1);

    private final Font font1 = new Font("Arial", 0, 24);
    private final Font font2 = new Font("Arial", 0, 18);
    private final Font font3 = new Font("Arial", 0, 14);
	
    @Override
	public void onPaint(Graphics g1) {
		long timeRan = System.currentTimeMillis() - startTime;
		int currentLvl = Skills.getActualLevel(Skills.SKILLS.MAGIC);
		int gainedLvl = currentLvl - startLvl;
		int xpToLevel = Skills.getXPToNextLevel(Skills.SKILLS.MAGIC);
		//CALCULATIONS
        int xpGained = Skills.getXP(Skills.SKILLS.MAGIC) - startXP;
        int xpPerHour = (int) (xpGained / ( timeRan/ 3600000D));
        long timeToLevel = (long) (Skills.getXPToNextLevel(Skills.SKILLS.MAGIC) * 3600000D / xpPerHour);

        Graphics2D g = (Graphics2D)g1;
        g.setColor(color1);
        g.fillRect(7, 344, 505, 130);
        g.setColor(color2);
        g.setStroke(stroke1);
        g.drawRect(7, 344, 505, 130);
        g.setFont(font1);
        g.setColor(color3);
        g.drawString("Auto Magic", 12, 371);
        g.setFont(font2);
        g.drawString("gbaelement7", 376, 363);
        g.setFont(font3);
        g.drawString("by: ", 350, 361);
        g.setColor(color4);
        g.drawString("Time Ran: " + Timing.msToString(timeRan), 62, 393);
        g.drawString("Current Lvl: " + currentLvl + " (+" + gainedLvl + ")", 49, 418);
        //g.drawString("Bars Made (P/H): " + barsMade + " (" + barsPerHour + ")", 16, 441);
        g.drawString("Mode: " + mode, 84, 441);
        g.drawString("XP Gained: " + xpGained, 244, 393);
        g.drawString("XP/H: " + xpPerHour, 278, 418);
        g.drawString("Time(XP) TNL: " + Timing.msToString(timeToLevel) + " (" + xpToLevel + ")", 226, 441);
	
	}

	@Override
	public void serverMessageReceived(String arg0) {
		if (arg0.toLowerCase().contains("you do not have enough")) {
			if (mode == "curse" || mode == "stunalch") {
				if (spellToCast == "Confuse" || spellToCast == "Weaken" || spellToCast == "Curse"){
					if (Inventory.getCount("Body rune") < 1) {
						println("out of runes");
						run_script = false;
					}
				}
				else {
					if (Inventory.getCount("Soul rune") < 1) {
						println("out of runes");
						run_script = false;
					}
				}
			}
			if (mode == "alch" || mode == "stunalch") {
				if (Inventory.getCount("Nature rune") < 1) {
					println("out of runes");
					run_script = false;
				}
			}
			if (mode == "teleport") {
				if (Inventory.getCount("Law rune") < 1) {
					println("out of runes");
					run_script = false;
				}
			}
		}
	}
	
	//UNUSED
	@Override
	public void clanMessageReceived(String arg0, String arg1) {	
	}
	@Override
	public void duelRequestReceived(String arg0, String arg1) {
	}
	@Override
	public void personalMessageReceived(String arg0, String arg1) {	
	}
	@Override
	public void playerMessageReceived(String arg0, String arg1) {	
	}
	@Override
	public void tradeRequestReceived(String arg0) {
	}
}
