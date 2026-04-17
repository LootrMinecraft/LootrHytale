package noobanidus.mods.lootr.system;

import com.hypixel.hytale.builtin.blockspawner.BlockSpawnerTable;
import com.hypixel.hytale.builtin.blockspawner.state.BlockSpawner;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import noobanidus.mods.lootr.LootrPlugin;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nonnull;
import java.util.Set;

public class BlockSpawnerPreSystem extends RefSystem<ChunkStore> {
  private Set<Dependency<ChunkStore>> dependencies = null;

  public BlockSpawnerPreSystem() {
  }

  @Override
  public void onEntityAdded(@Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
    if (LootrPlugin.get().getConfig().isConversionDisabled()) {
      return;
    }
    WorldConfig worldConfig = store.getExternalData().getWorld().getWorldConfig();
    if (worldConfig.getGameMode() != GameMode.Creative) {
      BlockSpawner block = commandBuffer.getComponent(ref, BlockSpawner.getComponentType());
      BlockModule.BlockStateInfo info = commandBuffer.getComponent(ref, BlockModule.BlockStateInfo.getComponentType());

      if (block != null && info != null) {
        String blockSpawnerId = block.getBlockSpawnerId();
        if (blockSpawnerId != null) {
          BlockSpawnerTable table = BlockSpawnerTable.getAssetMap().getAsset(blockSpawnerId);
          if (table != null) {
            LootrPlugin.get().wrapTable(blockSpawnerId, table);
          }
        }
      }
    }
  }

  @Override
  public void onEntityRemove(@NonNullDecl Ref<ChunkStore> var1, @NonNullDecl RemoveReason var2, @NonNullDecl Store<ChunkStore> var3, @NonNullDecl CommandBuffer<ChunkStore> var4) {
    // NO-OP
  }

  @NullableDecl
  @Override
  public Query<ChunkStore> getQuery() {
    return Query.and(BlockSpawner.getComponentType(), BlockModule.BlockStateInfo.getComponentType());
  }

  @SuppressWarnings("unchecked")
  @NonNullDecl
  @Override
  public Set<Dependency<ChunkStore>> getDependencies() {
    if (this.dependencies == null) {
      Class<RefSystem<ChunkStore>> cls;
      try {
        cls = (Class<RefSystem<ChunkStore>>) Class.forName("com.hypixel.hytale.builtin.blockspawner.BlockSpawnerPlugin$BlockSpawnerSystem");
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
      this.dependencies = Set.of(new SystemDependency<>(Order.BEFORE, cls));
    }
    return dependencies;
  }
}
