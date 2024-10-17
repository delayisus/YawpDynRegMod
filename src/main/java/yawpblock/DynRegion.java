package yawpblock;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.uni.EnvInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
public class DynRegion {

	public static ConcurrentHashMap<String, DynRegion> REGIONS = new ConcurrentHashMap<>();
	public static final String[] FLAGS = new String[]{
		"break-blocks", "place-blocks", "place-fluids", "tools-secondary",
		"shovel-path", "fire-tick", "spawning-monster", "spawning-animal",
		"use-entities", "use-items", "use-blocks", "explosion-blocks",
		"other-explosion-blocks", "mob-griefing", "enderman-griefing"
	};
	private static File rootfolder;
	private static File mainfile;
	//
	public SimpleContainer stacks = new SimpleContainer(18);
	private ArrayList<UUID> members = new ArrayList<>();
	public ConcurrentHashMap<String, HashMap<Long, Block>> blocks = new ConcurrentHashMap<>();
	public final String id;
	private long created;
	public long last;
	private BlockPos center;
	private BlockPos min;
	private BlockPos max;

	public DynRegion(String uuid){
		id = uuid;
	}

	public static void register(Level level, BlockPos pos){
		BlockPos min = pos.offset(-Config.cubrad, -Config.cubrad, -Config.cubrad);
		BlockPos max = pos.offset(Config.cubrad, Config.cubrad, Config.cubrad);
		UUID uuid = new UUID(min.asLong(), max.asLong());
		DynRegion region = new DynRegion("trg" + uuid.toString().replace("-", ""));
		region.created = region.last = Time.getDate();
		region.center = pos;
		region.min = min;
		region.max = max;
		REGIONS.put(region.id, region);
		BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
		for(int x = -Config.cubrad; x < Config.cubrad; x++){
			for(int y = -Config.cubrad; y < Config.cubrad; y++){
				for(int z = -Config.cubrad; z < Config.cubrad; z++){
					mpos.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
					region.addIfValid(level.getBlockState(mpos).getBlock(), mpos);
				}
			}
		}
		try{
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
				"/wp dim minecraft:overworld create local %s Cuboid %s %s %s %s %s %s"
					.formatted(region.id, min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ())
			);
			for(String flag : FLAGS){
				server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
					"/wp local minecraft:overworld " + region.id + " add flags " + flag);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void deregister(BlockPos pos){
		DynRegion region = exists(pos);
		if(region != null) region.deregister();
	}

	public void deregister(){
		REGIONS.remove(id);
		try{
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
				"/wp dim minecraft:overworld delete %s -y".formatted(id)
			);
			Level level = server.overworld();
			for(int i = 0; i < stacks.getContainerSize(); i++){
				if(!stacks.getItem(i).isEmpty()){
					level.addFreshEntity(new ItemEntity(level, center.getX() + 0.5, center.getY() + 0.5, + center.getZ() + 0.5, stacks.getItem(i)));
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static DynRegion isInRegion(BlockPos pos){
		for(DynRegion region : REGIONS.values()){
			if(pos.getX() >= region.min.getX() && pos.getY() >= region.min.getY() && pos.getZ() >= region.min.getZ()
				&& pos.getX() <= region.max.getX() && pos.getY() <= region.max.getY() && pos.getZ() <= region.max.getZ())
				return region;
		}
		return null;
	}

	public static DynRegion exists(BlockPos pos){
		for(DynRegion reg : REGIONS.values()){
			if(reg.center.equals(pos)) return reg;
		}
		return null;
	}

	public static void load(File servdir){
		YAWPBlock.log("Loading " + YAWPBlock.MODID + " region data...");
		rootfolder = new File(servdir, "/" + YAWPBlock.MODID);
		rootfolder.mkdirs();
		mainfile = new File(rootfolder, YAWPBlock.MODID + ".nbt");
		if(!mainfile.exists()) return;
		try{
			CompoundTag com = NbtIo.read(mainfile);
			for(String key : com.getAllKeys()){
				REGIONS.put(key, new DynRegion(key)._load(com.getCompound(key)));
			}
		}
		catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	public static void save(){
		YAWPBlock.log("Saving " + YAWPBlock.MODID + " region data...");
		CompoundTag com = new CompoundTag();
		for(DynRegion region : REGIONS.values()){
			com.put(region.id, region._save());
		}
		try{
			NbtIo.write(com, mainfile);
		}
		catch(IOException e){
			YAWPBlock.log("DATA FAILED TO SAVE");
			YAWPBlock.log(com.toString());
			e.printStackTrace();
		}
	}

	private CompoundTag _save(){
		CompoundTag com = new CompoundTag();
		com.putLong("c", created);
		com.putLong("l", last);
		com.putLong("p", center.asLong());
		com.putLong("mi", min.asLong());
		com.putLong("ma", max.asLong());
		if(members.size() > 0){
			ListTag list = new ListTag();
			for(UUID mem : members){
				list.add(new LongArrayTag(new long[]{
					mem.getMostSignificantBits(), mem.getLeastSignificantBits()
				}));
			}
			com.put("m", list);
		}
		CompoundTag inv = new CompoundTag();
		for(int i = 0 ; i < stacks.getContainerSize(); i++){
			ItemStack stack = stacks.getItem(i);
			if(!stack.isEmpty()) inv.put("i" + i, stack.save(new CompoundTag()));
		}
		com.put("i", inv);
		CompoundTag blk = new CompoundTag();
		for(Map.Entry<String, HashMap<Long, Block>> entry : blocks.entrySet()){
			ListTag list = new ListTag();
			for(Map.Entry<Long, Block> sub : entry.getValue().entrySet()){
				CompoundTag tag = new CompoundTag();
				tag.putLong("p", sub.getKey());
				tag.putString("i", BuiltInRegistries.BLOCK.getKey(sub.getValue()).toString());
				list.add(tag);
			}
			blk.put(entry.getKey(), list);
		}
		if(!blk.isEmpty()) com.put("b", blk);
		return com;
	}

	private DynRegion _load(CompoundTag com){
		created = com.getLong("c");
		last = com.getLong("l");
		center = BlockPos.of(com.getLong("p"));
		min = BlockPos.of(com.getLong("mi"));
		max = BlockPos.of(com.getLong("ma"));
		if(com.contains("m")){
			ListTag list = (ListTag)com.get("m");
			for(int i = 0; i < list.size(); i++){
				long[] arr = list.getLongArray(i);
				members.add(new UUID(arr[0], arr[1]));
			}
		}
		if(com.contains("i")){
			CompoundTag inv = com.getCompound("i");
			for(int i = 0; i < stacks.getContainerSize(); i++){
				if(inv.contains("i" + i)){
					stacks.setItem(i, ItemStack.of(inv.getCompound("i" + i)));
				}
			}
		}
		if(com.contains("b")){
			try{
				CompoundTag blk = com.getCompound("b");
				for(String str : blk.getAllKeys()){
					HashMap<Long, Block> map = new HashMap<>();
					ListTag list = (ListTag)blk.get(str);
					for(int i = 0; i < list.size(); i++){
						CompoundTag tag = list.getCompound(i);
						Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(tag.getString("i")));
						if(block == null) continue;
						map.put(tag.getLong("p"), block);
					}
					blocks.put(str, map);
				}
			}
			catch(Exception e){
				YAWPBlock.log(com.get("b").toString());
				e.printStackTrace();
			}
		}
		return this;
	}

	public ArrayList<UUID> members(){
		return members;
	}

	public void addIfValid(Block snap, BlockPos pos){
		if(snap == null || snap instanceof AirBlock) return;
		String type = null;
		boolean valid = false;
		for(Map.Entry<String, ArrayList<Block>> entry : Config.blocks.entrySet()){
			if(valid) break;
			for(Block block : entry.getValue()){
				if(block == snap){
					valid = true;
					type = entry.getKey();
					break;
				}
			}
		}
		if(valid){
			if(!blocks.containsKey(type)) blocks.put(type, new HashMap<>());
			blocks.get(type).put(pos.asLong(), snap);
			if(EnvInfo.DEV) YAWPBlock.log("added " + pos + " / " + snap + " / " + type);
		}
	}

	public void remBlock(BlockPos pos){
		boolean rem = false;
		for(HashMap<Long, Block> value : blocks.values()){
			if(value.remove(pos.asLong()) != null) rem = true;
		}
		blocks.values().removeIf(val -> val.isEmpty());
		if(rem && EnvInfo.DEV) YAWPBlock.log("removed " + pos);
	}

}
