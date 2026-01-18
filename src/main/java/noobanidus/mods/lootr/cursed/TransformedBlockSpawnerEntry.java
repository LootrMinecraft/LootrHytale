package noobanidus.mods.lootr.cursed;

import com.hypixel.hytale.builtin.blockspawner.BlockSpawnerEntry;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

public class TransformedBlockSpawnerEntry extends BlockSpawnerEntry {
  private final String newBlockName;
  private final Holder<ChunkStore> transformedComponents;
  private final RotationMode rotationMode;
  private final double weight;

  public TransformedBlockSpawnerEntry(BlockSpawnerEntry entry, String newBlockName, Holder<ChunkStore> transformedComponents) {
    this.newBlockName = newBlockName;
    this.transformedComponents = transformedComponents;
    this.rotationMode = entry.getRotationMode();
    this.weight = entry.getWeight();
  }

  @Override
  public String getBlockName() {
    return newBlockName;
  }

  @Override
  public Holder<ChunkStore> getBlockComponents() {
    return transformedComponents;
  }

  @Override
  public RotationMode getRotationMode() {
    return rotationMode;
  }

  @Override
  public double getWeight() {
    return weight;
  }
}
