package scripts.AutoLavaCrafter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import org.tribot.api.Clicking;
import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Mouse;
import org.tribot.api.types.generic.Condition;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Equipment;
import org.tribot.api2007.GameTab;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Login;
import org.tribot.api2007.Magic;
import org.tribot.api2007.Objects;
import org.tribot.api2007.Player;
import org.tribot.api2007.Skills;
import org.tribot.api2007.Walking;
import org.tribot.api2007.WebWalking;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Painting;

@ScriptManifest(authors = "gbaelement7", category = "Runecrafting", name = "Auto Lava Crafter")
public class AutoLavaCrafter extends Script implements Painting {
	private boolean run_script;
	
	private boolean openBank() {
		if (Banking.isInBank()) {
			if (!Banking.isBankScreenOpen()) {
					if (!Banking.openBank()) {
						return false;
					}
				}
				return Timing.waitCondition(new Condition() {
					@Override
					public boolean active() {
						sleep(100);
						return Banking.isBankScreenOpen();
					}
				}, General.random(5000, 8000));
			}
		return false;
		}
	
	private boolean withdrawNecklace() {
		if (!Equipment.isEquipped("Binding necklace")) {
			if (!Banking.withdraw(1, "Binding necklace")) {
				Login.logout();
				run_script = false;
			}
			Banking.close();
			Clicking.click(Inventory.find("Binding necklace"));
			Banking.openBank();
			return Timing.waitCondition(new Condition() {
				@Override
				public boolean active() {
					sleep(100);
					return Banking.isBankScreenOpen();
				}
			}, General.random(2000,3000));
		}
		return Equipment.isEquipped("Binding necklace");
	}
	
	private boolean withdrawRing() {
		if (Inventory.find(2552,2554,2556,2558,2560,2562,2564,2566).length < 1) {
			if (!Banking.withdraw(1, 2552)) {
				Login.logout();
				run_script = false;
			}
			sleep(1000,2000);
		}
		return Inventory.find(2552,2554,2556,2558,2560,2562,2564,2566).length > 0;
	}
	
	private boolean withdrawEss() {
		Banking.withdraw(0, "Pure essence");
		sleep(1000,2000);
		return Timing.waitCondition(new Condition() {
			@Override
			public boolean active() {
				sleep(100);
				return Inventory.getCount("Pure essence") > 0;
			}
		}, General.random(2000,3000));
	}

	private boolean walkToFire() {
		Clicking.click("Rub", Inventory.find(2552,2554,2556,2558,2560,2562,2564,2566));
		sleep(1000,1500);
		Mouse.clickBox(250,380,400,385, 1);
		
		Timing.waitCondition(new Condition() {
			@Override
			public boolean active() {
				sleep(100);
				return new RSArea(new RSTile(3307,3246), new RSTile(3325,3226)).contains(Player.getPosition());
			}
		}, General.random(5000,7000));
		
		final RSObject[] ruins = Objects.find(20, "Mysterious ruins");
		
		WebWalking.walkTo(ruins[0]);
		
		Timing.waitCondition(new Condition() {
			@Override
			public boolean active() {
				sleep(100);
				return ruins[0].isOnScreen();
			}
		}, General.random(5000,10000));
		
		return DynamicClicking.clickRSObject(ruins[0], "Enter");
	}
	
	private boolean magicImbue() {
		return Magic.selectSpell("Magic Imbue");
	}
	
	private boolean runecraft() {
		final RSObject[] fireAltar = Objects.find(10, "Altar");
		
		Walking.blindWalkTo(fireAltar[0]);
		
		Timing.waitCondition(new Condition() {
			@Override
			public boolean active() {
				sleep(100,200);
				return fireAltar[0].isOnScreen();
			}
		}, General.random(1500, 3000));
		
		Clicking.click(Inventory.find("Earth rune"));
		Clicking.click("Craft-rune", fireAltar[0]);
		return false;
	}
	
	private boolean exit() {
		Clicking.click("Rub", Inventory.find(2552,2554,2556,2558,2560,2562,2564,2566));
		Mouse.clickBox(230, 407, 400, 415, 1);
		sleep(2000,2500);
		return false;
	}
	
	private boolean walkToBank() {
		return WebWalking.walkToBank();
	}
	
	
	@Override
	public void run() {
		run_script = true;
		
		while (run_script) {
			sleep(100,200);
			
			if (Banking.isInBank()) {
				if (!Equipment.isEquipped("Binding necklace") || Inventory.getCount(2552,2554,2556,2558,2560,2562,2564,2566) < 1 || Inventory.getCount("Pure essence") < 1) {
					if (openBank()) {
						withdrawNecklace();
						withdrawRing();
						withdrawEss();
					}
				}
				walkToFire();
				sleep(1000);
			}

			else {
				if (Equipment.isEquipped("Binding necklace") && Inventory.getCount("Pure essence") > 0) {
					walkToFire();
					magicImbue();
					runecraft();
				}
				else {
					exit();
					walkToBank();
				}
			}
		}
	}

	private final long startTime = System.currentTimeMillis();
	private final int startLvl = Skills.getActualLevel(Skills.SKILLS.RUNECRAFTING);
	private final int startXP = Skills.getXP(Skills.SKILLS.RUNECRAFTING);
	
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
		int currentLvl = Skills.getActualLevel(Skills.SKILLS.RUNECRAFTING);
		int gainedLvl = currentLvl - startLvl;
		int xpToLevel = Skills.getXPToNextLevel(Skills.SKILLS.RUNECRAFTING);
		//CALCULATIONS
        int xpGained = Skills.getXP(Skills.SKILLS.RUNECRAFTING) - startXP;
        int xpPerHour = (int) (xpGained / ( timeRan/ 3600000D));
        long timeToLevel = (long) (Skills.getXPToNextLevel(Skills.SKILLS.RUNECRAFTING) * 3600000D / xpPerHour);

        Graphics2D g = (Graphics2D)g1;
        g.setColor(color1);
        g.fillRect(7, 344, 505, 130);
        g.setColor(color2);
        g.setStroke(stroke1);
        g.drawRect(7, 344, 505, 130);
        g.setFont(font1);
        g.setColor(color3);
        g.drawString("Auto Lava Crafter", 12, 371);
        g.setFont(font2);
        g.drawString("gbaelement7", 376, 363);
        g.setFont(font3);
        g.drawString("by: ", 350, 361);
        g.setColor(color4);
        g.drawString("Time Ran: " + Timing.msToString(timeRan), 62, 393);
        g.drawString("Current Lvl: " + currentLvl + " (+" + gainedLvl + ")", 49, 418);
        //g.drawString("Bars Made (P/H): " + barsMade + " (" + barsPerHour + ")", 16, 441);
        //g.drawString("Mode: " + mode, 84, 441);
        g.drawString("XP Gained: " + xpGained, 244, 393);
        g.drawString("XP/H: " + xpPerHour, 278, 418);
        g.drawString("Time(XP) TNL: " + Timing.msToString(timeToLevel) + " (" + xpToLevel + ")", 226, 441);
	}
}
