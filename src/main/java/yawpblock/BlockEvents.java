package yawpblock;

import net.minecraft.network.chat.Component;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import static yawpblock.YAWPBlock.MODID;

/**
 * @author Ferdinand Calo' (FEX___96)
 */
@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BlockEvents {

	protected static DynRegion region;

	@SubscribeEvent
	public static void placeBlock(BlockEvent.EntityPlaceEvent event){
		if(event.getLevel().isClientSide()) return;
		if(event.getLevel() != ServerLifecycleHooks.getCurrentServer().overworld()) return;
		if((region = DynRegion.isInRegion(event.getPos())) != null){
			if(event.getState().getBlock() instanceof RegionBlock){
				event.getEntity().sendSystemMessage(Component.translatable(MODID + ".too_close"));
				event.setCanceled(true);
				return;
			}
			//region.addIfValid(event.getBlockSnapshot().getCurrentBlock().getBlock(), event.getPos());
		}
	}

	@SubscribeEvent
	public static void breakBlock(BlockEvent.BreakEvent event){
		if(event.getLevel().isClientSide()) return;
		if(event.getLevel() != ServerLifecycleHooks.getCurrentServer().overworld()) return;
		if((region = DynRegion.isInRegion(event.getPos())) != null){
			region.remBlock(event.getPos());
		}
		if(event.getState().getBlock() instanceof RegionBlock){
			DynRegion.deregister(event.getPos());
		}
	}

}
