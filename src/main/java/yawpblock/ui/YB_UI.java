package yawpblock.ui;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.ui.ContainerInterface;
import net.fexcraft.mod.uni.ui.UIButton;
import net.fexcraft.mod.uni.ui.UserInterface;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class YB_UI extends UserInterface {

	private static final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
	private YB_Con con;
	private int timer = 20;

	public YB_UI(JsonMap map, ContainerInterface container) throws Exception {
		super(map, container);
		con = (YB_Con)container;
	}

	@Override
	public void init(){
		askSync();
	}

	private void askSync(){
		TagCW com = TagCW.create();
		com.set("act", "sync");
		container.SEND_TO_SERVER.accept(com);
	}

	@Override
	public void predraw(float ticks, int mx, int my){
		texts.get("expiry").value("tcyawp.ui.expiry");
		long rem = ((YB_Con)container).expiry - Time.getDate();
		if(rem > 0){
			long h = rem / Time.HOUR_MS;
			long m = rem % Time.HOUR_MS / Time.MIN_MS;
			long s = rem % Time.MIN_MS / Time.SEC_MS;
			texts.get("expiry").translate(h + ":" + (m < 10 ? "0" + m : m) + ":" + (s < 10 ? "0" + s : s));
		}
		else{
			texts.get("expiry").translate("00:00:00");
		}
		timer--;
		if(timer < 0){
			timer = 20;
			askSync();
		}
	}

	@Override
	public void getTooltip(int mx, int my, List<String> list){
		if(buttons.get("info").hovered()){
			for(Map.Entry<String, Integer> entry : con.ni.entrySet()){
				list.add(con.ns.get(entry.getKey()).getHoverName().getString() + " x" + entry.getValue());
			}
		}
	}

	@Override
	public boolean onAction(UIButton button, String id, int x, int y, int b){
		if(id.equals("add")){
			TagCW com = TagCW.create();
			com.set("act", "add");
			container.SEND_TO_SERVER.accept(com);
		}
		if(id.equals("rem")){
			TagCW com = TagCW.create();
			com.set("act", "rem");
			container.SEND_TO_SERVER.accept(com);
		}
		return false;
	}

}
