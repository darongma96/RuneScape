package scripts.AutoMagic;

import java.awt.Graphics;
import java.awt.Graphics2D;

import org.tribot.api.General;
import org.tribot.api.util.ABCUtil;
import org.tribot.api2007.GameTab;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Login;
import org.tribot.api2007.Skills;
import org.tribot.script.Script;
import org.tribot.script.ScriptManifest;
import org.tribot.script.interfaces.MessageListening07;
import org.tribot.script.interfaces.Painting;

import scripts.AutoMagic.Constants.Spells;
import scripts.api.graphics.Paint;

@ScriptManifest(authors = "gbaelement7", name = "AutoMagicPro",category = "Magic", version = 2.00)
public class Main extends Script implements Painting, MessageListening07 {
	ABCUtil abcUtil = new ABCUtil();
	private ScriptManifest manifest = getClass().getAnnotation(ScriptManifest.class);
	private boolean run_script = true;
	private Paint Paint;
	
	@Override
	public void run() {
		GUI gui = new GUI();
		General.useAntiBanCompliance(true);
		run_script = true;
		
		while (!gui.isGuiComplete()) {
			sleep(100);
		}
		
		Spells.mode = gui.getMode();
		Spells.spellToCast = gui.getSpell();
		Spells.npcToCast = gui.getNPC();
		Spells.itemToAlch = gui.getItem();
		
		BotManager botManager = new BotManager();
		Paint = new Paint(manifest.name(), manifest.version(), Skills.SKILLS.MAGIC);
		
		while (run_script) {
			sleep(100,200);
			abcUtil.performTimedActions(Skills.SKILLS.MAGIC);
			
			GameTab.TABS.MAGIC.open();

			botManager.bot();
		}
		Login.logout();
	}
	
    @Override
	public void onPaint(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics;

        if (Paint != null){
            Paint.draw(graphics);
        }
	}
    
	@Override
	public void serverMessageReceived(String arg0) {
		if (arg0.toLowerCase().contains("you do not have enough")) {
			if (Spells.mode == "curse" || Spells.mode == "stunalch") {
				if (Spells.spellToCast == "Confuse" || Spells.spellToCast == "Weaken" || Spells.spellToCast == "Curse"){
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
			if (Spells.mode == "alch" || Spells.mode == "stunalch") {
				if (Inventory.getCount("Nature rune") < 1) {
					println("out of runes");
					run_script = false;
				}
			}
			if (Spells.mode == "teleport") {
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
