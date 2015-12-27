package scripts.AutoWoodcutter;

import org.tribot.api.DynamicClicking;
import org.tribot.api.types.generic.Condition;
import org.tribot.api.util.ABCUtil;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Game;
import org.tribot.api2007.GroundItems;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Objects;
import org.tribot.api2007.Options;
import org.tribot.api2007.Player;
import org.tribot.api2007.Walking;
import org.tribot.api2007.WebWalking;
import org.tribot.api2007.types.RSArea;
import org.tribot.api2007.types.RSGroundItem;
import org.tribot.api2007.types.RSObject;
import org.tribot.api2007.types.RSTile;
import org.tribot.api2007.util.ThreadSettings;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.api2007.Skills;

import java.util.HashSet;
import java.awt.*;

import org.tribot.api.Timing;
import org.tribot.script.interfaces.Painting;
import org.tribot.api.General;

@ScriptManifest(authors = "gbaelement7", category = "Woodcutting", name = "Auto Woodcutter" )
public class AutoWoodcutter extends Script implements Painting {
	ABCUtil abcUtil = new ABCUtil();
	private String ourTree;
	private RSTile last_tree_tile = null;
	private RSTile startingPosition = Player.getPosition();
	private boolean waitTime = false;
	private long lastChopTime = 0L;
	private RSArea startingArea = new RSArea(startingPosition, 20); //may cause banking problem unless changed for some trees and locations
	private HashSet<RSTile> treeCollection = new HashSet<RSTile>();
	private final String[] axe_names = { "Bronze axe", "Iron axe", "Black axe",
			"Steel axe", "Mithril axe", "Adamant axe", "Rune axe",
			"Dragon axe" };
	
	private boolean isAtTrees() {
		RSObject[] trees = getTrees();
		if (trees == null) {
			return false;
		} else if (trees.length > 0) {
			return trees[0].isOnScreen();
		}
		return startingArea.contains(Player.getPosition()) ;
	}
	
	private boolean isInBank() {
		return Banking.isInBank();
	}
	
	private RSObject[] getTrees() {
		return Objects.findNearest(20, ourTree);
	}
	
	private boolean cut() {
		final long timeout = System.currentTimeMillis() + General.random(60000,90000);

		while (isCutting() && System.currentTimeMillis() < timeout) {
			sleep(100,150);
			abcUtil.performTimedActions(Skills.SKILLS.WOODCUTTING);
			this.lastChopTime = Timing.currentTimeMillis();
			
			if (last_tree_tile != null) {
				if (!Objects.isAt(last_tree_tile, ourTree)) {
					break;
				}
			}
		}
		
		RSObject[] trees = getTrees();
		
		if (trees != null && trees.length < 1 && treeCollection != null && treeCollection.size() > 0) {
			goToAnticipated(treeCollection.iterator().next(), 2);
			Timing.waitCondition(new Condition() {
				@Override
				public boolean active() {
					sleep(100,500);
					RSObject[] findTree = getTrees();
					return findTree.length > 0;
				}
			}, General.random(45000, 60000));
			trees = getTrees();
		}
		
		if (trees != null) {
			if (!abcUtil.BOOL_TRACKER.USE_CLOSEST.next() && trees.length > 1 && trees[1].getPosition().distanceToDouble(trees[0]) < 3.0) {
				if (cutTree(trees[1])) {
					hoverNext();
					return true;
				}
			}
			else {
				if (cutTree(trees[0])) {
					hoverNext();
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean cutTree(RSObject tree) {
		if (tree == null) {
			return false;
		}
		
		delayObject();
		
		if (!tree.isOnScreen()) {
			Walking.blindWalkTo(tree);
			
			if (!Timing.waitCondition(new Condition() {
				@Override
				public boolean active() {
					sleep(100);
					return tree.isOnScreen();
				}
			}, General.random(7500, 10000)))
				return false;
		}
		
		if (DynamicClicking.clickRSObject(tree, "Chop down")) {	
			Timing.waitCondition(new Condition() {
				@Override
				public boolean active() {
					sleep(100);
					return isCutting() || Inventory.isFull();
				}
			}, General.random(6000, 10000));
			
			last_tree_tile = tree.getPosition();
			abcUtil.BOOL_TRACKER.USE_CLOSEST.reset();
			treeCollection.add(tree.getPosition());
			return true;
		}
		return false;
	}
		
	private boolean delayObject() {
		if (!this.waitTime) {
			if (Timing.timeFromMark(this.lastChopTime) < General.random(7500, 10000)) {
				sleep(this.abcUtil.DELAY_TRACKER.SWITCH_OBJECT.next());
				this.abcUtil.DELAY_TRACKER.SWITCH_OBJECT.reset();
			}
			else {
				sleep(this.abcUtil.DELAY_TRACKER.NEW_OBJECT.next());
				this.abcUtil.DELAY_TRACKER.NEW_OBJECT.reset();
			}
			this.waitTime = true;
			return true;
		}
		return false;
	}
	
	private boolean hoverNext() {
		final RSObject[] trees = getTrees();
		if (trees.length > 1) {
			if (abcUtil.BOOL_TRACKER.HOVER_NEXT.next() && trees[1].isOnScreen()) {
				trees[1].hover();
				abcUtil.BOOL_TRACKER.HOVER_NEXT.reset();
				sleep(100);
				return true;
			}
		}
		abcUtil.BOOL_TRACKER.HOVER_NEXT.reset();
		return false;
	}
	
	private boolean goToAnticipated(RSTile anticipated, int random_offset) {
		if (anticipated != null && abcUtil.BOOL_TRACKER.GO_TO_ANTICIPATED.next()) {	
			if (Walking.blindWalkTo(new RSArea(anticipated, random_offset <= 0 ? 1 : random_offset).getRandomTile())) {
				abcUtil.BOOL_TRACKER.GO_TO_ANTICIPATED.reset();
				return true;
			}
		}
		abcUtil.BOOL_TRACKER.GO_TO_ANTICIPATED.reset();
		return false;
	}
	
	private boolean isCutting() {
		return Player.getAnimation() > 0;
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
				return isInBank();
			}
		}, General.random(8000, 10000));	
	}
	
	private boolean walkToTrees() {		
		if (Game.getRunEnergy() >= abcUtil.INT_TRACKER.NEXT_RUN_AT.next()) {
			Options.setRunOn(true);
			abcUtil.INT_TRACKER.NEXT_RUN_AT.reset();
		}
        return WebWalking.walkTo(new RSArea((startingPosition), 3).getRandomTile());
	}
	
	private boolean bank() {
		if (Banking.isBankScreenOpen() || Banking.openBank() && Timing.waitCondition(new Condition() {
			@Override
			public boolean active() {
				sleep(100, 300);
				return Banking.isBankScreenOpen();
			}
		}, General.random(6000, 9000))) {
			if (Banking.depositAllExcept(axe_names) < 1) {
				return false;
			}
			sleep(1000,2000);
			return !Inventory.isFull();
		}
		return false;
	}

	private boolean powerChop(){ //todo: add powerchopping support
		return Inventory.dropAllExcept(axe_names) > 0;
	}
	
	private boolean birdNest() {
		if (GroundItems.find("Bird nest").length < 1) {
			return false;
		}
		else {
			final RSGroundItem[] birdNest = GroundItems.find("Bird nest");
			if (birdNest.length >= 1 && !birdNest[0].isOnScreen()) {
				Walking.blindWalkTo(birdNest[0]);
			}
			return DynamicClicking.clickRSGroundItem(birdNest[0], "Take");
		}
	}
	
	@Override
	public void run() {
		AutoWoodcutterGUI gui = new AutoWoodcutterGUI();
		General.useAntiBanCompliance(true);
		ThreadSettings.get().setClickingAPIUseDynamic(true);
		
		while (!gui.isGuiComplete()) {
			sleep(100);
		}
		
		ourTree = gui.getChosenTree();

		if (ourTree == "Willow"){
			startingArea = new RSArea(startingPosition, 7);
		}
		
		while (true) {
			sleep(100,200);
			if (isAtTrees()) {
				if (Inventory.isFull()) {
					walkToBank();
				} else {
					cut();
					birdNest();	
				}
			} else if (isInBank()) {
				if (Inventory.isFull()) {
					bank();
				} else {
					walkToTrees();
				}	
			} else {
				if (Inventory.isFull()) {
					walkToBank();
				} else {
					walkToTrees();
				}
			}
		}
	}
    
	private final long startTime = System.currentTimeMillis();
	private final int startLvl = Skills.getActualLevel(Skills.SKILLS.WOODCUTTING);
	private final int startXP = Skills.getXP(Skills.SKILLS.WOODCUTTING);
	
    private final Color color1 = new Color(0, 0, 0, 130);
    private final Color color2 = new Color(0, 0, 0);
    private final Color color3 = new Color(204, 0, 0, 204);
    private final Color color4 = new Color(255, 255, 255);

    private final BasicStroke stroke1 = new BasicStroke(1);

    private final Font font1 = new Font("Arial", 0, 24);
    private final Font font2 = new Font("Arial", 0, 18);
    private final Font font3 = new Font("Arial", 0, 14);
	
	public void onPaint(Graphics g1) {
		long timeRan = System.currentTimeMillis() - startTime;
		int currentLvl = Skills.getActualLevel(Skills.SKILLS.WOODCUTTING);
		int gainedLvl = currentLvl - startLvl;
		int xpToLevel = Skills.getXPToNextLevel(Skills.SKILLS.WOODCUTTING);
		//CALCULATIONS
        int xpGained = Skills.getXP(Skills.SKILLS.WOODCUTTING) - startXP;
        int xpPerHour = (int) (xpGained / ( timeRan/ 3600000D));
        long timeToLevel = (long) (Skills.getXPToNextLevel(Skills.SKILLS.WOODCUTTING) * 3600000D / xpPerHour);

        Graphics2D g = (Graphics2D)g1;
        g.setColor(color1);
        g.fillRect(8, 345, 504, 129);
        g.setColor(color2);
        g.setStroke(stroke1);
        g.drawRect(8, 345, 504, 129);
        g.setFont(font1);
        g.setColor(color3);
        g.drawString("Auto Woodcutter", 17, 371);
        g.setFont(font2);
        g.drawString("gbaelement7", 390, 365);
        g.setFont(font3);
        g.setColor(color4);
        g.drawString("Time Ran: " + Timing.msToString(timeRan), 108, 390);
        g.drawString("Current Lvl: " + currentLvl + " (+" + gainedLvl + ")", 99, 414);
        g.drawString("Time to Lvl: " + Timing.msToString(timeToLevel), 100, 438);
        g.drawString("XP Gained: " + xpGained, 290, 390);
        g.drawString("XP/H: " + xpPerHour, 327, 414);
        g.drawString("XP TNL: " + xpToLevel, 310, 438);
	}
}
