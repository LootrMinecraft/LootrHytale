package noobanidus.mods.lootr;

import com.hypixel.hytale.builtin.blockspawner.BlockSpawnerTable;

public class BlockSpawnerTableWrapped extends BlockSpawnerTable {
  private final BlockSpawnerTable wrapped;

  public BlockSpawnerTableWrapped(BlockSpawnerTable wrapped) {
    this.wrapped = wrapped;
  }
}
