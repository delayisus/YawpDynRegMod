package yawpblock;

import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = YAWPBlock.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WorldEvents {
	
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onWorldLoad(LevelEvent.Load event){
		if(event.getLevel().isClientSide()) return;
		if(event.getLevel() != ServerLifecycleHooks.getCurrentServer().overworld()) return;
		DynRegion.load(ServerLifecycleHooks.getCurrentServer().getServerDirectory());
		YAWPBlock.CONFIG.reload();
		DecayTimer.start();
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onWorldUnload(LevelEvent.Unload event){
		if(event.getLevel().isClientSide()) return;
		if(event.getLevel() != ServerLifecycleHooks.getCurrentServer().overworld()) return;
		DynRegion.save();
		DynRegion.REGIONS.clear();
		DecayTimer.stop();
	}

	/*@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onGameEvent(VanillaGameEvent event){
		if(event.getLevel().isClientSide()) return;
		if(event.getLevel() != ServerLifecycleHooks.getCurrentServer().overworld()) return;
		if(event.getVanillaEvent() == GameEvent.BLOCK_DESTROY){
			BlockPos pos = new BlockPos((int)event.getEventPosition().x, (int)event.getEventPosition().y, (int)event.getEventPosition().z);
			BlockState state = event.getContext().affectedState();
			if(EnvInfo.DEV) YAWPBlock.log(event.getVanillaEvent().getName() + " " + (state == null ? null : state.getBlock()));
			if(state.getBlock() instanceof RegionBlock){
				DynRegion.deregister(pos);
			}
			if((region = TempRegion.isInRegion(pos)) != null){
				region.remBlock(pos);
			}
		}
		if(event.getVanillaEvent() == GameEvent.BLOCK_PLACE){
			BlockPos pos = new BlockPos((int)event.getEventPosition().x, (int)event.getEventPosition().y, (int)event.getEventPosition().z);
			BlockState state = event.getContext().affectedState();
			if(EnvInfo.DEV) YAWPBlock.log(event.getVanillaEvent().getName() + " " + (state == null ? null : state.getBlock()));
			if((region = DynRegion.isInRegion(pos)) != null){
				region.addIfValid(state.getBlock(), pos);
			}
		}
	}*/
	
}
