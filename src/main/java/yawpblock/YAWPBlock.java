package yawpblock;

import com.mojang.logging.LogUtils;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import yawpblock.ui.YB_Con;
import yawpblock.ui.YB_UI;
import net.fexcraft.mod.uni.EnvInfo;
import net.fexcraft.mod.uni.UniReg;
import net.fexcraft.mod.uni.ui.UIKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.io.File;

import static net.minecraft.commands.Commands.literal;

@Mod(YAWPBlock.MODID)
public class YAWPBlock {

	public static final String MODID = "tcyawp";
	protected static final Logger LOGGER = LogUtils.getLogger();
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
	public static final UIKey UIKEY = new UIKey(1, MODID + ":main");

	public static final RegistryObject<Block> REGION_BLOCK = BLOCKS.register("rg_block", () -> new RegionBlock());
	public static final RegistryObject<Item> REGION_BLOCKITEM = ITEMS.register("rg_block", () -> new BlockItem(REGION_BLOCK.get(), new Item.Properties()));

	public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("tab", () -> CreativeModeTab.builder()
		.withTabsBefore(CreativeModeTabs.COMBAT)
		.icon(() -> REGION_BLOCKITEM.get().getDefaultInstance())
		.displayItems((parameters, output) -> {
			output.accept(REGION_BLOCKITEM.get());
		}).build());

	public static Config CONFIG;

	public YAWPBlock(){
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::commonSetup);
		BLOCKS.register(bus);
		ITEMS.register(bus);
		CREATIVE_MODE_TABS.register(bus);
		MinecraftForge.EVENT_BUS.register(this);
		bus.addListener(this::addCreative);
		//
		UniReg.registerMod(MODID, this);
		UniReg.registerUI(UIKEY, YB_UI.class);
		UniReg.registerMenu(UIKEY, "assets/" + MODID + "/uis/main", YB_Con.class);
	}

	public static void log(String s){
		LOGGER.info(s);
	}

	private void commonSetup(final FMLCommonSetupEvent event){
		CONFIG = new Config(new File(FMLPaths.CONFIGDIR.get().toFile(), "yawp-tc.json"));
	}

	private void addCreative(BuildCreativeModeTabContentsEvent event){
		if(event.getTabKey() == CreativeModeTabs.OP_BLOCKS) event.accept(REGION_BLOCKITEM);
	}

	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event){
		//
	}

	@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientModEvents {

		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event){
			//
		}

	}

	@SubscribeEvent
	public void onCmdReg(RegisterCommandsEvent event){
		event.getDispatcher().register(literal("yawp-tc").requires(cmd -> cmd.getServer().isSingleplayer() || cmd.getServer().getPlayerList().isOp(cmd.getPlayer().getGameProfile()))
			.then(literal("reload").executes(cmd -> {
				cmd.getSource().sendSystemMessage(Component.literal("Reloading config..."));
				CONFIG.reload();
				cmd.getSource().sendSystemMessage(Component.literal("Restarting timer..."));
				DecayTimer.start();
				return 0;
			}))
			.then(literal("uuid").executes(cmd -> {
				cmd.getSource().sendSystemMessage(Component.literal(cmd.getSource().getPlayerOrException().getGameProfile().getId().toString()));
				return 0;
			}))
			.then(literal("dev").executes(cmd -> {
				cmd.getSource().sendSystemMessage(Component.literal("dev: " + (EnvInfo.DEV = !EnvInfo.DEV)));
				return 0;
			}))
		);
	}

}
