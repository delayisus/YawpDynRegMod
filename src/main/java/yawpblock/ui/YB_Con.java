package yawpblock.ui;

import net.fexcraft.app.json.JsonMap;
import net.fexcraft.lib.common.math.V3I;
import yawpblock.Config;
import yawpblock.DecayTimer;
import yawpblock.DynRegion;
import yawpblock.YAWPBlock;
import net.fexcraft.mod.uni.EnvInfo;
import net.fexcraft.mod.uni.UniEntity;
import net.fexcraft.mod.uni.item.StackWrapper;
import net.fexcraft.mod.uni.tag.TagCW;
import net.fexcraft.mod.uni.ui.InventoryInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class YB_Con extends InventoryInterface {

	private DynRegion region;
	private SimpleContainer stacks;
	protected HashMap<String, Integer> ni = new LinkedHashMap<>();
	protected HashMap<String, ItemStack> ns = new LinkedHashMap<>();
	protected long expiry;

	public YB_Con(JsonMap map, UniEntity ply, V3I pos){
		super(map, ply, pos);
		region = DynRegion.exists(new BlockPos(pos.x, pos.y, pos.z));
		stacks = region == null || ply.entity.isOnClient() ? new SimpleContainer(18) : region.stacks;
	}

	@Override
	public Object getInventory(){
		return stacks;
	}

	@Override
	public void setInventoryContent(int index, TagCW com){
		stacks.setItem(index, ItemStack.of(com.local()));
	}

	@Override
	public StackWrapper getInventoryContent(int index){
		return StackWrapper.wrap(stacks.getItem(index));
	}

	@Override
	public boolean isInventoryEmpty(int at){
		return stacks.getItem(at).isEmpty();
	}

	@Override
	public void packet(TagCW com, boolean client){
		if(client){
			if(EnvInfo.DEV) YAWPBlock.log("received " + com);
			expiry = com.getLong("expiry");
			ni.clear();
			ns.clear();
			CompoundTag tag = com.getCompound("mats").local();
			for(String key : tag.getAllKeys()){
				ni.put(key, tag.getCompound(key).getInt("mat:am"));
				ns.put(key, ItemStack.of(tag.getCompound(key)));
			}
			return;
		}
		String act = com.getString("act");
		if(act.equals("add")){
			try{
				MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
				server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
					"/wp local minecraft:overworld %s add player members %s".formatted(region.id, player.entity.getName())
				);
				region.members().add(player.entity.getUUID());
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		else if(act.equals("rem")){
			for(UUID uuid : region.members()){
				try{
					MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
					server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
						"/wp local minecraft:overworld %s remove player members by-uuid %s".formatted(region.id, uuid.toString())
					);
				}
				catch(Exception e){
					e.printStackTrace();
				}
			}
			region.members().clear();
		}
		else if(act.equals("sync")){
			com.set("expiry", region.last + DecayTimer.minten);
			HashMap<String, Integer> map = new LinkedHashMap<>();
			for(Map.Entry<String, HashMap<Long, Block>> entry : region.blocks.entrySet()){
				if(entry.getValue().isEmpty()) continue;
				int am  = 0;
				for(Block sub : entry.getValue().values()){
					am += Config.block_vals.get(sub)[0];
				}
				map.put(entry.getKey(), am);
			}
			TagCW tag = TagCW.create();
			for(Map.Entry<String, Integer> entry : map.entrySet()){
				CompoundTag stack = Config.materials.get(entry.getKey()).save(new CompoundTag());
				stack.putInt("mat:am", entry.getValue());
				tag.set(entry.getKey(), TagCW.wrap(stack));
			}
			com.set("mats", tag);
			if(EnvInfo.DEV) YAWPBlock.log("sending " + com);
			SEND_TO_CLIENT.accept(com, player);
		}
	}

	@Override
	public void onClosed(){
		//
	}

}
