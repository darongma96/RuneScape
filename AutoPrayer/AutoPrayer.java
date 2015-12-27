package scripts.AutoPrayer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import org.tribot.api.Clicking;
import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api.types.generic.Condition;
import org.tribot.api.util.ABCUtil;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Login;
import org.tribot.api2007.Player;
import org.tribot.api2007.Skills;
import org.tribot.api2007.types.RSItem;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.Painting;

@ScriptManifest(authors = "gbaelement7", category = "Prayer", name = "Auto Prayer")
public class AutoPrayer extends Script implements Painting {
	ABCUtil abcUtil = new ABCUtil();
	private boolean run_script;
	private String boneType;
	private int bonesBuried;
	
	private boolean bury() {
		RSItem[] bones = Inventory.find(boneType);
		for (int i = 0; i < bones.length; i++) {
			if (bones[i] != null) {
				Clicking.click(bones[i]);
			}
			General.sleep(abcUtil.DELAY_TRACKER.ITEM_INTERACTION.next());
			if (abcUtil.BOOL_TRACKER.HOVER_NEXT.next() && bones.length - 1 > i && bones[i+1] != null) {
				bones[i+1].hover();
			}
			abcUtil.BOOL_TRACKER.HOVER_NEXT.reset();
			Timing.waitCondition(new Condition() {
				@Override
				public boolean active() {
					sleep(100);
					return isBurying();
				}
			}, 3000);
			bonesBuried++;
			Timing.waitCondition(new Condition() {
				@Override
				public boolean active() {
					sleep(100);
					return !isBurying();
				}
			}, 3000);
		}
		abcUtil.performTimedActions(Skills.SKILLS.PRAYER);
		RSItem[] leftoverBones = Inventory.find(boneType);
		while (leftoverBones != null && leftoverBones.length > 0) {
			Clicking.click(leftoverBones);
			Timing.waitCondition(new Condition() {
				@Override
				public boolean active() {
					sleep(100);
					return isBurying();
				}
			}, 3000);
			Timing.waitCondition(new Condition() {
				@Override
				public boolean active() {
					sleep(100);
					return !isBurying();
				}
			}, 3000);
			leftoverBones = Inventory.find(boneType);
		}
		return leftoverBones.length == 0;
	}
	private boolean isBurying() {
		return Player.getAnimation() > 0;
	}
	private boolean withdraw() {
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
		}, 5000);
		
		if (Banking.withdraw(0, boneType)) {
			return Banking.close();
		} else {
			return run_script = false; 
		}
	}
	
	@Override
	public void run() {
		AutoPrayerGUI gui = new AutoPrayerGUI();
		General.useAntiBanCompliance(true);
		run_script = true;
		
		while (!gui.isGuiComplete()) {
			sleep(100);
		}
		boneType = gui.getBoneType();
		
		while (run_script) {
			sleep(100);
			
			if (Inventory.getCount(boneType) > 0) {
				bury();
			} else {
				withdraw();
			}
		}
		println("out of bones");
		Login.logout();
	}
	
	private final long startTime = System.currentTimeMillis();
	private final int startLvl = Skills.getActualLevel(Skills.SKILLS.PRAYER);
	private final int startXP = Skills.getXP(Skills.SKILLS.PRAYER);
	
    private final Color color1 = new Color(0, 0, 0, 123);
    private final Color color2 = new Color(0, 0, 0);
    private final Color color3 = new Color(254, 53, 38);
    private final Color color4 = new Color(255, 255, 255);

    private final BasicStroke stroke1 = new BasicStroke(1);

    private final Font font1 = new Font("Verdana", 0, 26);
    private final Font font2 = new Font("Verdana", 0, 18);
    private final Font font3 = new Font("Verdana", 0, 12);
    
	@Override
	public void onPaint(Graphics g1) {
		long timeRan = System.currentTimeMillis() - startTime;
		int currentLvl = Skills.getActualLevel(Skills.SKILLS.PRAYER);
		int gainedLvl = currentLvl - startLvl;
		int xpToLevel = Skills.getXPToNextLevel(Skills.SKILLS.PRAYER);
		//CALCULATIONS
	    int xpGained = Skills.getXP(Skills.SKILLS.PRAYER) - startXP;
	    int xpPerHour = (int) (xpGained / ( timeRan/ 3600000D));
	    long timeToLevel = (long) (Skills.getXPToNextLevel(Skills.SKILLS.PRAYER) * 3600000D / xpPerHour);
	    int bonesPerHour = (int) (bonesBuried / (timeRan / 3600000D));
	    
        Graphics2D g = (Graphics2D)g1;
        g.setColor(color1);
        g.fillRect(7, 344, 505, 130);
        g.setColor(color2);
        g.setStroke(stroke1);
        g.drawRect(7, 344, 505, 130);
        g.setFont(font1);
        g.setColor(color3);
        g.drawString("Auto Prayer", 12, 371);
        g.setFont(font2);
        g.drawString("gbaelement7", 376, 363);
        g.setFont(font3);
        g.drawString("by: ", 350, 361);
        g.setColor(color4);
        g.drawString("Time Ran: " + Timing.msToString(timeRan), 62, 393);
        g.drawString("Current Lvl: " + currentLvl + " (+" + gainedLvl + ")", 49, 418);
        g.drawString("Bones Buried (P/H): " + bonesBuried + " (" + bonesPerHour + ")", 16, 441);
        g.drawString("XP Gained: " + xpGained, 244, 393);
        g.drawString("XP/H: " + xpPerHour, 278, 418);
        g.drawString("Time(XP) TNL: " + Timing.msToString(timeToLevel) + " (" + xpToLevel + ")", 226, 441);
	}

}
