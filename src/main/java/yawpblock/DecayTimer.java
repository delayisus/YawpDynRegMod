package yawpblock;

import com.tacz.guns.world.DamageBlockSaveData;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.uni.EnvInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class DecayTimer extends TimerTask {

	public static long minten;

	public static Timer TIMER = null;

	public static void start(){
		stop();
		minten = Time.SEC_MS * Config.timer;
		try{
			TIMER = new Timer("DecayTimer");
			YAWPBlock.log("Scheduling a new timer...");
			TIMER.schedule(new DecayTimer(), new Date(Time.getDate()), Time.SEC_MS * Config.timer);
		}
		catch(Throwable e){
			e.printStackTrace();
		}
	}

	public static void stop(){
		if(TIMER == null) return;
		try{
			YAWPBlock.log("Cancelling existing timer...");
			TIMER.cancel();
		}
		catch(Throwable e){
			e.printStackTrace();
		}
	}

	@Override
	public void run(){
		try{
			Level level = ServerLifecycleHooks.getCurrentServer().overworld();
			DamageBlockSaveData data = DamageBlockSaveData.get(level);
			BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
			if(EnvInfo.DEV){
				YAWPBlock.log("Starting Decay Check");
				YAWPBlock.log("Materials: ");
				for(Map.Entry<String, ItemStack> entry : Config.materials.entrySet()){
					YAWPBlock.log(entry.getKey() + " = " + entry.getValue());
				}
			}
			for(DynRegion region : DynRegion.REGIONS.values()){
				if(region.last + minten > Time.getDate()) continue;
				region.last = Time.getDate();
				int[] brr;
				for(Map.Entry<String, HashMap<Long, Block>> entry : region.blocks.entrySet()){
					ItemStack stack = Config.materials.get(entry.getKey());
					if(stack.isEmpty()){
						if(EnvInfo.DEV) YAWPBlock.log("skipping '" + entry.getKey() + "' because item is empty");
						continue;
					}
					for(Map.Entry<Long, Block> val : entry.getValue().entrySet()){
						brr = Config.block_vals.get(val.getValue());
						if(consume(region, stack, brr[0])){
							if(EnvInfo.DEV) YAWPBlock.log("no decay on " + mpos.set(val.getKey()) + ", consumed " + brr[0] + " of " + stack.getDisplayName().getString());
							continue;
						}
						data.damageBlock(level, mpos.set(val.getKey()), brr[1]);
						if(EnvInfo.DEV) YAWPBlock.log("damaging " + mpos + " of type '" + entry.getKey() + "' by " + brr[1]);
					}
				}
			}
		}
		catch(Throwable e){
			e.printStackTrace();
		}
	}

	private boolean consume(DynRegion region, ItemStack stack, int am){
		int cons = 0;
		for(int a = 0; a < am; a++){
			for(int s = 0; s < region.stacks.getContainerSize(); s++){
				if(equals(region.stacks.getItem(s), stack)){
					region.stacks.getItem(s).shrink(1);
					cons++;
					break;
				}
			}
		}
		return cons >= am;
	}

	private static boolean equals(ItemStack inv, ItemStack mat){
		if(inv.isEmpty()) return false;
		return mat.getItem() == inv.getItem();
	}

}
