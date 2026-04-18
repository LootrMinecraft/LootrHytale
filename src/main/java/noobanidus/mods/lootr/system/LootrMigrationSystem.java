package noobanidus.mods.lootr.system;

import com.hypixel.hytale.builtin.blockspawner.state.BlockSpawner;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.data.unknown.UnknownComponents;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import noobanidus.mods.lootr.LootrPlugin;
import noobanidus.mods.lootr.block.ItemLootContainerBlock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Deprecated(forRemoval = true)
public class LootrMigrationSystem extends BlockModule.MigrationSystem {
  @Override
  public void onEntityAdd(@Nonnull Holder<ChunkStore> holder, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store) {
    UnknownComponents<ChunkStore> unknowncomponents = holder.getComponent(ChunkStore.REGISTRY.getUnknownComponentType());

    assert unknowncomponents != null;

    ItemLootContainerBlock containerBlock = unknowncomponents.removeComponent(LootrPlugin.LOOT_CHEST_ID, ItemLootContainerBlock.CODEC);
    if (containerBlock != null) {
      holder.putComponent(LootrPlugin.get().getLootContainerType(), containerBlock);
    }
  }

  @Override
  public void onEntityRemoved(@Nonnull Holder<ChunkStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<ChunkStore> store) {
  }

  @Nullable
  @Override
  public Query<ChunkStore> getQuery() {
    return ChunkStore.REGISTRY.getUnknownComponentType();
  }
}
