package yawpblock;

import net.fexcraft.lib.common.math.V3I;
import net.fexcraft.mod.uni.UniEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.server.ServerLifecycleHooks;

public class RegionBlock extends Block {

	public RegionBlock(){
		super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit){
		if(level.isClientSide || hand == InteractionHand.OFF_HAND) return InteractionResult.PASS;
		DynRegion region = DynRegion.exists(pos);
		if(region != null){
			UniEntity.getEntity(player).openUI(YAWPBlock.UIKEY, new V3I(pos.getX(), pos.getY(), pos.getZ()));
		}
		SnowLayerBlock e;
		return InteractionResult.PASS;
	}

	@Deprecated
	public void onPlace(BlockState state0, Level level, BlockPos pos, BlockState state1, boolean bool){
		if(level.isClientSide || level != ServerLifecycleHooks.getCurrentServer().overworld()) return;
		DynRegion.register(level, pos);
	}

	@Deprecated
	public void onRemove(BlockState state0, Level level, BlockPos pos, BlockState state1, boolean bool){
		if(level != ServerLifecycleHooks.getCurrentServer().overworld()) return;
		DynRegion.deregister(pos);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader reader, BlockPos pos){
		return Block.isFaceFull(reader.getBlockState(pos.below()).getCollisionShape(reader, pos.below()), Direction.UP);
	}

	@Override
	public BlockState updateShape(BlockState state0, Direction dir, BlockState state1, LevelAccessor level, BlockPos pos0, BlockPos pos1){
		return !state0.canSurvive(level, pos0) ? Blocks.AIR.defaultBlockState() : state0;
	}

}
