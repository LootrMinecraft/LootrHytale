package noobanidus.mods.lootr.plugin;

import com.hypixel.hytale.builtin.blockspawner.BlockSpawnerEntry;
import com.hypixel.hytale.builtin.blockspawner.BlockSpawnerTable;
import com.hypixel.hytale.common.map.IWeightedMap;

public class BlockSpawnerTableWrapped extends BlockSpawnerTable {
  private final BlockSpawnerTable wrapped;

  public BlockSpawnerTableWrapped(BlockSpawnerTable wrapped) {
    this.wrapped = wrapped;
  }
}
