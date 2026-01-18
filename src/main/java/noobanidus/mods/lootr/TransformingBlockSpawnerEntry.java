package noobanidus.mods.lootr;

import com.hypixel.hytale.builtin.blockspawner.BlockSpawnerEntry;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public class TransformingBlockSpawnerEntry extends BlockSpawnerEntry {
  private final BlockSpawnerEntry wrapped;
  private final Holder<ChunkStore> modified;

  public TransformingBlockSpawnerEntry(BlockSpawnerEntry wrapped) {
    this.wrapped = wrapped;
    this.modified = wrapped.getBlockComponents().clone();
    var original = this.modified.getComponent(LootrPlugin.ITEM_CONTAINER_COMPONENT_TYPE);
  }

  @Override
  public String getBlockName() {
    return wrapped.getBlockName();
  }

  @Override
  public Holder<ChunkStore> getBlockComponents() {
    return super.getBlockComponents();
  }

  @Override
  public RotationMode getRotationMode() {
    return wrapped.getRotationMode();
  }

  @Override
  public double getWeight() {
    return wrapped.getWeight();
  }
}
