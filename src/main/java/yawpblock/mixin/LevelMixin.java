package yawpblock.mixin;

import net.fexcraft.mod.uni.EnvInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yawpblock.DynRegion;
import yawpblock.YAWPBlock;

import java.util.List;
import java.util.concurrent.Executor;

@Mixin(ServerLevel.class)
public abstract class LevelMixin extends ServerLevel {


	public LevelMixin(MinecraftServer p_214999_, Executor p_215000_, LevelStorageSource.LevelStorageAccess p_215001_, ServerLevelData p_215002_, ResourceKey<Level> p_215003_, LevelStem p_215004_, ChunkProgressListener p_215005_, boolean p_215006_, long p_215007_, List<CustomSpawner> p_215008_, boolean p_215009_, @Nullable RandomSequences p_288977_){
		super(p_214999_, p_215000_, p_215001_, p_215002_, p_215003_, p_215004_, p_215005_, p_215006_, p_215007_, p_215008_, p_215009_, p_288977_);
	}

	@Inject(at = @At("HEAD"), method = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z")
	public void setBlockHead(BlockPos pos, BlockState state, int i, int j, CallbackInfoReturnable<Boolean> info){
		if((Level)this != getServer().overworld()) return;
		DynRegion region = DynRegion.isInRegion(pos);
		if(region == null) return;
		if(EnvInfo.DEV) YAWPBlock.log("set_head " + pos);
		region.remBlock(pos);
	}

	@Inject(at = @At("TAIL"), method = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z")
	public void setBlockTail(BlockPos pos, BlockState state, int i, int j, CallbackInfoReturnable info){
		if((Level)this != getServer().overworld()) return;
		DynRegion region = DynRegion.isInRegion(pos);
		if(region == null) return;
		if(EnvInfo.DEV) YAWPBlock.log("set_tail " + pos);
		region.addIfValid(state.getBlock(), pos);
	}

}
