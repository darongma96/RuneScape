package scripts.AutoSmelter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import org.tribot.api.Clicking;
import org.tribot.api.DynamicClicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.input.Keyboard;
import org.tribot.api.input.Mouse;
import org.tribot.api.types.generic.Condition;
import org.tribot.api.util.ABCUtil;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Camera;
import org.tribot.api2007.Equipment;
import org.tribot.api2007.Game;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Login;
import org.tribot.api2007.Objects;
import org.tribot.api2007.Options;
import org.tribot.api2007.Player;
import org.tribot.api2007.Skills;
import org.tribot.api2007.WebWalking;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.MessageListening07;
import org.tribot.script.interfaces.Painting;

@ScriptManifest(authors = "gbaelement7", category = "Smithing", name = "Auto Smelter" )
public class AutoSmelter extends Script implements Painting, MessageListening07 {
	ABCUtil abcUtil = new ABCUtil();
	private String barType;
	private String location;
	private String ore_name;
	private String second_ore;
	private String bar_name;
	private int timeToSmelt;
	private RSArea furnaceArea;
	private int barsMade;
	private boolean useRof;
	private boolean run_script;
	
	private boolean isAtFurnace() {
		final RSObject[] furnace = Objects.find(10, "Furnace");
		return furnace.length > 0 && furnace[0] != null && furnace[0].isClickable(); //change to is on screen?
	}
	
	private boolean isAtBank() {
		return Banking.isInBank();
	}
	
	private boolean walkToFurnace() {
		if (Game.getRunEnergy() >= abcUtil.INT_TRACKER.NEXT_RUN_AT.next()) {
			Options.setRunOn(true);
			abcUtil.INT_TRACKER.NEXT_RUN_AT.reset();
		}

		WebWalking.walkTo(furnaceArea.getRandomTile()); // doesnt open door sometimes, make area smaller and/or make tiles have furnace on screen
		
		RSObject[] furnace = Objects.find(10, "Furnace");
		if (furnace.length > 0 && furnace[0] != null && !furnace[0].isClickable()) {
			Camera.turnToTile(furnace[0]);
			sleep(500,1000);
		}
		
		return Timing.waitCondition(new Condition() {
			@Override
			public boolean active() {
				sleep(200);
				return isAtFurnace();
			}
		}, General.random(7000, 10000));
	}
	
	private boolean walkToBank() {
		if (Game.getRunEnergy() >= abcUtil.INT_TRACKER.NEXT_RUN_AT.next()) {
			Options.setRunOn(true);
			abcUtil.INT_TRACKER.NEXT_RUN_AT.reset();
		}
		
		WebWalking.walkToBank();
		
		return Timing.waitCondition(new Condition() {
			@Override
			public boolean active() {
				sleep(200);
				return isAtBank();
			}
		}, General.random(8000, 10000));	
	}
	
	private boolean depositBars() {
		if (!Banking.isBankScreenOpen()) {
			if (!Banking.openBank()) {
				return false;
			}
		}
		Timing.waitCondition(new Condition() {
			@Override
			public boolean active() {
				sleep(100);
				return Banking.isBankScreenOpen();
			}
		}, General.random(5000, 8000));
		return (Banking.depositAllExcept("Ammo mould") > 0);

	}
	
	private boolean withdrawOres() {
		if (!Banking.isBankScreenOpen()) {
			if (!Banking.openBank()) {
				return false;
			}
		}
		Timing.waitCondition(new Condition() {
			@Override
			public boolean active() {
				sleep(100);
				return Banking.isBankScreenOpen();
			}
		}, General.random(6000, 9000));
		
		switch (barType) {
		case "Bronze": {
			Banking.withdraw(14, "Tin ore");
			Timing.waitCondition(new Condition() {
				@Override
				public boolean active() {
					sleep(500);
					return Inventory.getCount(ore_name) >= 14;
				}
			}, 5000);
			if (Inventory.getCount("Tin ore") > 14) {
				Banking.depositAll();
				Banking.withdraw(14, "Tin ore");
				sleep(2000,3000);
			}
			Banking.withdraw(14, "Copper ore");
				if (!Timing.waitCondition(new Condition() {
					@Override
					public boolean active() {
						sleep(500);
						return Inventory.getCount("Copper ore") > 0;
					}
				}, General.random(6000, 9000))) {
					println("out of materials");
					Login.logout();
					run_script = false;
				}
				return true;
		}
		case "Iron": {
			if (useRof && !Equipment.isEquipped("Ring of forging") && Banking.withdraw(1, "Ring of forging")) {
				sleep(750,1500);
				Banking.close();
				Clicking.click(Inventory.find("Ring of forging"));
				Banking.openBank();
				Timing.waitCondition(new Condition() {
					@Override
					public boolean active() {
						sleep(100);
						return Banking.isBankScreenOpen();
					}
				}, General.random(5000, 8000));
			}
			Banking.withdraw(0, "Iron ore");
				if (!Timing.waitCondition(new Condition() {
					@Override
					public boolean active() {
						sleep(500);
						return Inventory.getCount("Iron ore") > 0;
					}
				}, General.random(6000, 9000))) {
					println("out of materials");
					Login.logout();
					run_script = false;
				}
				return true;
		}
		case "Silver": {
			Banking.withdraw(0, "Silver ore");
				if (!Timing.waitCondition(new Condition() {
					@Override
					public boolean active() {
						sleep(500);
						return Inventory.getCount("Silver ore") > 0;
					}
				}, General.random(6000, 9000))) {
					println("out of materials");
					Login.logout();
					run_script = false;
				}
				return true;
		}
		case "Steel": {
			Banking.withdraw(9, "Iron ore");
			Timing.waitCondition(new Condition() {
				@Override
				public boolean active() {
					sleep(500);
					return Inventory.getCount("Iron ore") >= 9;
				}
			}, 5000);
			if (Inventory.getCount("Iron ore") > 9) {
				Banking.depositAll();
				Banking.withdraw(9, "Iron ore");
				sleep(2000,3000);
			}
			Banking.withdraw(18, "Coal");
				if (!Timing.waitCondition(new Condition() {
					@Override
					public boolean active() {
						sleep(500);
						return Inventory.getCount("Coal") > 1;
					}
				}, General.random(6000, 9000))) {
					println("out of materials");
					Login.logout();
					run_script = false;
				}
				return true;
		}
		case "Cannonball": {
			Banking.withdraw(0, "Steel bar");
				if (!Timing.waitCondition(new Condition() {
					@Override
					public boolean active() {
						sleep(500);
						return Inventory.getCount("Steel bar") > 0;
					}
				}, General.random(6000, 9000))) {
					println("out of materials");
					Login.logout();
					run_script = false;
				}
				return true;
		}
		case "Gold": {
			Banking.withdraw(0, "Gold ore");
				if (!Timing.waitCondition(new Condition() {
					@Override
					public boolean active() {
						sleep(500);
						return Inventory.getCount("Gold ore") > 0;
					}
				}, General.random(6000, 9000))) {
					println("out of materials");
					Login.logout();
					run_script = false;
				}
				return true;
		}
		case "Mithril": {
			Banking.withdraw(5, "Mithril ore");
			Timing.waitCondition(new Condition() {
				@Override
				public boolean active() {
					sleep(500);
					return Inventory.getCount("Mithril ore") >= 5;
				}
			}, 5000);
			if (Inventory.getCount("Mithril ore") > 5) {
				Banking.depositAll();
				Banking.withdraw(5, "Mithril ore");
				sleep(2000,3000);
			}
			Banking.withdraw(20, "Coal");
				if (!Timing.waitCondition(new Condition() {
					@Override
					public boolean active() {
						sleep(200);
						return Inventory.getCount("Coal") > 3;
					}
				}, General.random(6000, 9000))) {
					println("out of materials");
					Login.logout();
					run_script = false;
				}
				return true;
		}
		case "Adamantite": {
			Banking.withdraw(4, "Adamantite ore");
			Timing.waitCondition(new Condition() {
				@Override
				public boolean active() {
					sleep(500);
					return Inventory.getCount("Adamantite ore") >= 4;
				}
			}, 5000);
			if (Inventory.getCount("Adamantite ore") > 4) {
				Banking.depositAll();
				Banking.withdraw(4, "Adamantite ore");
				sleep(2000,3000);
			}
			Banking.withdraw(0, "Coal");
				if (!Timing.waitCondition(new Condition() {
					@Override
					public boolean active() {
						sleep(200);
						return Inventory.getCount("Coal") > 5;
					}
				}, General.random(6000, 9000))) {
					println("out of materials");
					Login.logout();
					run_script = false;
				}
				return true;
		}
		case "Runite": {
			Banking.withdraw(3, "Runite ore");
			Timing.waitCondition(new Condition() {
				@Override
				public boolean active() {
					sleep(500);
					return Inventory.getCount("Runite ore") >= 3;
				}
			}, 5000);
			if (Inventory.getCount("Runite ore") > 3) {
				Banking.depositAll();
				Banking.withdraw(3, "Runite ore");
				sleep(2000,3000);
			}
			Banking.withdraw(24, "Coal");
				if (!Timing.waitCondition(new Condition() {
					@Override
					public boolean active() {
						sleep(200);
						return Inventory.getCount("Coal") > 7;
					}
				}, General.random(6000, 9000))) {
					println("out of materials");
					Login.logout();
					run_script = false;
				}
				return true;
		}
		}
		return false;
	}
	
	private boolean smelt() {
		/*while (!isSmelting() && Interfaces.isInterfaceValid(174)) {
			Clicking.click("Continue", Interfaces.get(174, 2));
			if (Timing.waitCondition(new Condition() {
				@Override
				public boolean active() {
					sleep(200);
					return Interfaces.isInterfaceValid(131);
				}
			}, General.random(1000, 2000))) {
				Clicking.click("Continue", Interfaces.get(131,3));
			}
			Timing.waitCondition(new Condition() {
				@Override
				public boolean active() {
					sleep (100);
					return isSmelting();
				}
			}, General.random(2000,4000));
		}*/

		RSObject[] furnace = Objects.find(10, "Furnace");
		if (furnace != null && furnace.length > 0) {
			if (!furnace[0].isClickable()) {
				WebWalking.walkTo(furnaceArea.getRandomTile());
			}
			else {
				if (barType == "Cannonball") {
					RSItem[] steelbars = Inventory.find("Steel bar");
					if (steelbars != null && steelbars.length > 0) {
						Clicking.click(steelbars);
						Clicking.click(furnace);
						sleep(3000,4000);
					}
				} else {
					DynamicClicking.clickRSObject(furnace[0], "Smelt");
					Timing.waitCondition(new Condition() {
						@Override
						public boolean active() {
							sleep(200);
							return Interfaces.isInterfaceValid(311);
						}
					}, General.random(6000, 10000));
				}
			}
				/*if (DynamicClicking.clickRSObject(furnace[0], "Smelt")) {
					if (!(Timing.waitCondition(new Condition() {
						@Override
						public boolean active() {
							sleep(200);
							return Interfaces.isInterfaceValid(311);
						}
					}, General.random(6000, 10000)))) {
						DynamicClicking.clickRSObject(furnace[0], "Smelt");
					}
				}
			}*/
		}
		
			switch (barType) {
			case "Bronze": {
				Clicking.click("Smelt X", Interfaces.get(311, 4));
				timeToSmelt = General.random(50000, 60000);
				break;
			}
			case "Iron": {
				Clicking.click("Smelt X", Interfaces.get(311, 6));
				timeToSmelt = General.random(100000, 120000);
				break;
			}
			case "Steel": {
				Clicking.click("Smelt X", Interfaces.get(311, 8));
				timeToSmelt = General.random(35000,45000);
				break;
			}
			case "Cannonball": {
				Mouse.clickBox(220, 390, 270, 440, 3);
				Timing.waitChooseOption("Make All", 3000);
				timeToSmelt = General.random(180000,200000);
				break;
			}
			case "Gold": {
				Clicking.click("Smelt X", Interfaces.get(311, 9));
				timeToSmelt = General.random(100000, 120000);
				break;
			}
			case "Mithril": {
				Clicking.click("Smelt X", Interfaces.get(311, 10));
				timeToSmelt = General.random(20000,30000);
				break;
			}
			case "Adamantite": {
				Clicking.click("Smelt X", Interfaces.get(311, 11));
				timeToSmelt = General.random(16000,20000);
				break;
			}
			case "Runite": {
				Clicking.click("Smelt X", Interfaces.get(311, 12));
				timeToSmelt = General.random(15000,20000);
				break;
			}
			}
			
			if (barType != "Cannonball") {
				sleep(1000,2000);
				Keyboard.typeString(General.random(1, 5) + "");
				Keyboard.typeString(General.random(1, 5) + "");
				Keyboard.typeString(General.random(1, 5) + "");
				Keyboard.pressEnter();
			}

			if (Timing.waitCondition(new Condition() {
				@Override
				public boolean active() {
					sleep(750);
					abcUtil.performTimedActions(Skills.SKILLS.SMITHING);
					return Inventory.getCount(ore_name) == 0 || !isSmelting();
				}
			}, timeToSmelt)) {
				return false;
			}
			return true;
	}
	
	private boolean isSmelting() {

		if (Player.getAnimation() > 0) {
			abcUtil.performTimedActions(Skills.SKILLS.SMITHING);
			
			Timing.waitCondition(new Condition() {
				@Override
				public boolean active() {
					sleep(100);
					return Player.getAnimation() == -1;
				}
			}, General.random(3000, 4000));
			
			if (Timing.waitCondition(new Condition() {
				@Override
				public boolean active() {
					sleep(100);
					return Player.getAnimation() > 0;
				}
			}, General.random(3000, 4000))) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void run() {
		AutoSmelterGUI gui = new AutoSmelterGUI();
		General.useAntiBanCompliance(true);
		run_script = true;
		
		while (!gui.isGuiComplete()) {
			sleep(100);
		}
		
		barType = gui.getChosenBar();
		location = gui.getChosenLocation();
		
		if (barType == "Iron" && Equipment.isEquipped("Ring of forging")) {
			useRof = true;
		}
		
		switch (barType) {
		case "Bronze": {
			ore_name = "Tin ore";
			second_ore = "Copper ore";
			bar_name = "Bronze bar";
			break;
		}
		case "Iron": {
			ore_name = "Iron ore";
			bar_name = "Iron bar";
			break;
		}
		case "Silver": {
			ore_name = "Silver ore";
			bar_name = "Silver bar";
			break;
		}
		case "Steel": {
			ore_name = "Iron ore";
			second_ore = "Coal";
			bar_name = "Steel bar";
			break;
		}
		case "Cannonball": {
			ore_name = "Steel bar";
			bar_name = "Cannonball";
			break;
		}
		case "Gold": {
			ore_name = "Gold ore";
			bar_name = "Gold bar";
			break;
		}
		case "Mithril": {
			ore_name = "Mithril ore";
			second_ore = "Coal";
			bar_name = "Mithril bar";
			break;
		}
		case "Adamantite": {
			ore_name = "Adamantite ore";
			second_ore = "Coal";
			bar_name = "Adamantite bar";
			break;
		}
		case "Runite": {
			ore_name = "Runite ore";
			second_ore = "Coal";
			bar_name = "Runite bar";
			break;
		}
		}
		
		switch (location) {
		case "Edgeville" : {
			furnaceArea = new RSArea(new RSTile(3105,3500), new RSTile(3109,3497));
			break;
		}
		case "Al Kharid": {
			furnaceArea = new RSArea(new RSTile(3275,3186), new RSTile(3278,3185));
			break;
		}
		case "Port Phasmatys": {
			RSObject[] furnace = Objects.find(20, "Furnace");
			furnaceArea = new RSArea(new RSTile(furnace[0].getPosition().getX() - 5, furnace[0].getPosition().getY() - 1), 2);
			break;
		}
		case "Neitiznot": {
			RSObject[] furnace = Objects.find(20, "Furnace");
			furnaceArea = new RSArea(new RSTile(furnace[0].getPosition().getX(), furnace[0].getPosition().getY() - 1), 1);
			break;
		}
		case "Falador": {
			furnaceArea = new RSArea(new RSTile(2973,3374),new RSTile(2974,3368));
			break;
		}
		case "Lumbridge": {
			furnaceArea = new RSArea(new RSTile(3225,3252), new RSTile(3228,3254));
			break;
		}
		}
		
		while (run_script) {
			sleep(100,200);
			
			if (isAtBank()) {
				if (Inventory.getCount(bar_name) > 0) {
					depositBars();
				} else if (Inventory.getCount(ore_name) < 1) {
					withdrawOres();
				} else {
					walkToFurnace();
				}
			} else if (isAtFurnace()) {
				if (Inventory.getCount(ore_name) > 0) {
					smelt();
				} else {
					walkToBank();
				}
			} else {
				if (Inventory.getCount(ore_name) > 0) {
					walkToFurnace();
				} else {
					walkToBank();
				}	
			}
		}
	}
		
		private final long startTime = System.currentTimeMillis();
		private final int startLvl = Skills.getActualLevel(Skills.SKILLS.SMITHING);
		private final int startXP = Skills.getXP(Skills.SKILLS.SMITHING);
		
	    private final Color color1 = new Color(0, 0, 0, 123);
	    private final Color color2 = new Color(0, 0, 0);
	    private final Color color3 = new Color(254, 53, 38);
	    private final Color color4 = new Color(255, 255, 255);

	    private final BasicStroke stroke1 = new BasicStroke(1);

	    private final Font font1 = new Font("Verdana", 0, 26);
	    private final Font font2 = new Font("Verdana", 0, 18);
	    private final Font font3 = new Font("Verdana", 0, 12);
		
	public void onPaint(Graphics g1) {
		long timeRan = System.currentTimeMillis() - startTime;
		int currentLvl = Skills.getActualLevel(Skills.SKILLS.SMITHING);
		int gainedLvl = currentLvl - startLvl;
		int xpToLevel = Skills.getXPToNextLevel(Skills.SKILLS.SMITHING);
		//CALCULATIONS
	    int xpGained = Skills.getXP(Skills.SKILLS.SMITHING) - startXP;
	    int xpPerHour = (int) (xpGained / ( timeRan/ 3600000D));
	    long timeToLevel = (long) (Skills.getXPToNextLevel(Skills.SKILLS.SMITHING) * 3600000D / xpPerHour);
	    int barsPerHour = (int) (barsMade / (timeRan / 3600000D));
	    
        Graphics2D g = (Graphics2D)g1;
        g.setColor(color1);
        g.fillRect(7, 344, 505, 130);
        g.setColor(color2);
        g.setStroke(stroke1);
        g.drawRect(7, 344, 505, 130);
        g.setFont(font1);
        g.setColor(color3);
        g.drawString("Auto Smelter", 12, 371);
        g.setFont(font2);
        g.drawString("gbaelement7", 376, 363);
        g.setFont(font3);
        g.drawString("by: ", 350, 361);
        g.setColor(color4);
        g.drawString("Time Ran: " + Timing.msToString(timeRan), 62, 393);
        g.drawString("Current Lvl: " + currentLvl + " (+" + gainedLvl + ")", 49, 418);
        g.drawString("Bars Made (P/H): " + barsMade + " (" + barsPerHour + ")", 16, 441);
        g.drawString("XP Gained: " + xpGained, 244, 393);
        g.drawString("XP/H: " + xpPerHour, 278, 418);
        g.drawString("Time(XP) TNL: " + Timing.msToString(timeToLevel) + " (" + xpToLevel + ")", 226, 441);
	
	}

	@Override
	public void serverMessageReceived(String arg0) {
		if (arg0.toLowerCase().contains("you retrieve")) {
			barsMade++;
		}
		else if (arg0.toLowerCase().contains("you remove the cannonballs")) {
			barsMade++;
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
