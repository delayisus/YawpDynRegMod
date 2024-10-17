package com.tacz.guns.world;

import yawpblock.YAWPBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class DamageBlockSaveData extends SavedData {

    public HashMap<Long,Integer> storage = new HashMap<>();


    public Set<BlockPos> getAllBlocks() {
        Set<BlockPos> positions = new HashSet<>();
        for (Long longPos : storage.keySet()) {
            positions.add(BlockPos.of(longPos));
        }
        return positions;
    }

    public int damageBlock(Level world, BlockPos pos, int block_damage){
        int curDamage = storage.computeIfAbsent(pos.asLong(), k->getDefaultResistance(world,pos));
        int newDamage = Math.max(0,curDamage-block_damage);
        storage.put(pos.asLong(), newDamage);
        YAWPBlock.log(pos + ": " + curDamage + " -> " + newDamage);
        setDirty();
        return newDamage;
    }

    public void removeBlock(BlockPos pos){
        storage.remove(pos.asLong());
        setDirty();
    }

    public boolean hasBlock(BlockPos pos){
        return storage.containsKey(pos.asLong());
    }

    public void setBlockHP(BlockPos pos, int hp){
        storage.put(pos.asLong(), hp);
    }

    public int getBlockHP(BlockPos pos){
        return storage.get(pos.asLong());
    }

    public int getDefaultResistance(LevelAccessor world, BlockPos pos){
        double resistance = world.getBlockState(pos).getBlock().getExplosionResistance();
        return (int) (resistance*100d);
    }

    @Nonnull
    public static DamageBlockSaveData get(LevelAccessor level) {
        if (level.isClientSide()) {
            throw new RuntimeException("Don't access this client-side!");
        }
        DimensionDataStorage storage = ((ServerLevel)level).getDataStorage();
        return storage.computeIfAbsent(DamageBlockSaveData::new, DamageBlockSaveData::new, "damagemanager");
    }

    public DamageBlockSaveData(){

    }

    public DamageBlockSaveData(CompoundTag tag){
        ListTag list = tag.getList("blockDamages", Tag.TAG_COMPOUND);
        for (Tag t : list) {
            CompoundTag damageTag = (CompoundTag) t;
            int currentHp = damageTag.getInt("currentHp");
            Long pos = damageTag.getLong("pos");
            storage.put(pos, currentHp);
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        storage.forEach((longPos, currentHp) -> {
            CompoundTag manaTag = new CompoundTag();
            manaTag.putLong("pos", longPos);
            manaTag.putInt("currentHp", currentHp);
            list.add(manaTag);
        });
        tag.put("blockDamages", list);
        return tag;
    }
}