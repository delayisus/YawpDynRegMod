package yawpblock;

import net.fexcraft.app.json.JsonArray;
import net.fexcraft.app.json.JsonMap;
import net.fexcraft.app.json.JsonValue;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.uni.ConfigBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.File;
import java.util.*;

public class Config extends ConfigBase {

	public static int cubrad;
	public static int timer;
	public static int check;
	public static HashMap<String, ItemStack> materials = new HashMap<>();
	public static HashMap<String, ArrayList<Block>> blocks = new HashMap<>();
	public static HashMap<Block, int[]> block_vals = new HashMap<>();

	public Config(File fl){
		super(fl);
	}

	private static boolean validateItemName(final Object obj){
		return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
	}

	@Override
	protected void fillInfo(JsonMap map){
		map.add("info", "YAWP Region Block Configuration File");
	}

	@Override
	protected void fillEntries(){
		entries.add(new ConfigEntry(this, "general", "region-radius", 20).rang(5, Integer.MAX_VALUE)
			.info("Cuboid Region Radius")
			.cons((con, map) -> {
				cubrad = con.getInteger(map);
			})
		);
		entries.add(new ConfigEntry(this, "general", "decay-timer", 10).rang(10, 12000)
			.info("Decay Timer, in seconds.")
			.cons((con, map) -> {
				timer = con.getInteger(map);
				if(timer < 60) timer = 10;
			})
		);
		entries.add(new ConfigEntry(this, "general", "decay-check", 600).rang(10, Time.DAY_MS / 1000)
			.info("Decay Check Cooldown, in seconds.")
			.cons((con, map) -> {
				check = con.getInteger(map);
				if(check < 60) check = 10;
			})
		);
		entries.add(new ConfigEntry(this, "general", "material_types", new JsonMap("example", "minecraft:iron_ingot"))
			.info("Item types to prevent decay, pattern: { 'type_key': 'modid:itemid' }")
			.cons((con, map) -> {
				materials.clear();
				JsonMap mep = con.getJson(map).asMap();
				for(Map.Entry<String, JsonValue<?>> entry : mep.entries()){
					try{
						materials.put(entry.getKey(), new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(entry.getValue().string_value()))));
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
			})
		);
		entries.add(new ConfigEntry(this, "general", "affected_blocks", new JsonMap("minecraft:iron_block", new JsonArray("example", 1, 10)))
			.info("Blocks affected by decay, sorted by type, pattern: { 'modid:blockid': [ 'type', <amount>, <dmg_val> ] }")
			.cons((con, map) -> {
				blocks.clear();
				block_vals.clear();
				JsonMap mep = con.getJson(map).asMap();
				for(Map.Entry<String, JsonValue<?>> entry : mep.entries()){
					try{
						Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(entry.getKey()));
						if(block == null || block instanceof AirBlock){
							YAWPBlock.log("Block with ID '" + entry.getKey() + "' not found!");
							ServerLifecycleHooks.getCurrentServer().stopServer();
							continue;
						}
						JsonArray jar = entry.getValue().asArray();
						String type = jar.get(0).string_value();
						if(!blocks.containsKey(type)) blocks.put(type, new ArrayList<>());
						blocks.get(type).add(block);
						block_vals.put(block, new int[]{ jar.get(1).integer_value(), jar.get(2).integer_value() });
					}
					catch(Exception e){
						e.printStackTrace();
					}
				}
			})
		);
	}

	@Override
	protected void onReload(JsonMap map){
		//
	}

}
