package noobanidus.mods.lootr;

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
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nonnull;
import java.lang.invoke.VarHandle;
import java.util.Set;

public class BlockSpawnerPrePlugin extends RefSystem<ChunkStore> {
  private final Set<Dependency<ChunkStore>> dependencies;

  public BlockSpawnerPrePlugin() {
    Class<RefSystem<ChunkStore>> cls;
    try {
      //noinspection unchecked
      cls = (Class<RefSystem<ChunkStore>>) Class.forName("com.hypixel.hytale.builtin.blockspawner.BlockSpawnerPlugin$BlockSpawnerSystem");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    this.dependencies = Set.of(new SystemDependency<>(Order.BEFORE, cls));
  }

  @Override
  public void onEntityAdded(@Nonnull Ref<ChunkStore> ref, @Nonnull AddReason reason, @Nonnull Store<ChunkStore> store, @Nonnull CommandBuffer<ChunkStore> commandBuffer) {
    WorldConfig worldConfig = store.getExternalData().getWorld().getWorldConfig();
    if (worldConfig.getGameMode() != GameMode.Creative) {
      BlockSpawner state = commandBuffer.getComponent(ref, BlockSpawner.getComponentType());
      BlockModule.BlockStateInfo info = commandBuffer.getComponent(ref, BlockModule.BlockStateInfo.getComponentType());

      if (state != null && info != null) {
        String blockSpawnerId = state.getBlockSpawnerId();
        if (blockSpawnerId != null) {
          BlockSpawnerTable table = BlockSpawnerTable.getAssetMap().getAsset(blockSpawnerId);
          if (table != null) {
          }
        }
      }
    }
  }

  @Override
  public void onEntityRemove(@NonNullDecl Ref<ChunkStore> var1, @NonNullDecl RemoveReason var2, @NonNullDecl Store<ChunkStore> var3, @NonNullDecl CommandBuffer<ChunkStore> var4) {

  }

  @NullableDecl
  @Override
  public Query<ChunkStore> getQuery() {
    return Query.and(BlockSpawner.getComponentType(), BlockModule.BlockStateInfo.getComponentType());
  }

  @NonNullDecl
  @Override
  public Set<Dependency<ChunkStore>> getDependencies() {
    return dependencies;
  }
}
