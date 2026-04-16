package noobanidus.mods.lootr.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import noobanidus.mods.lootr.block.ItemLootContainerBlock;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.time.Instant;

public class LootContainerBlockAddedSystem extends RefSystem<ChunkStore> {
  private final ComponentType<ChunkStore, BlockModule.BlockStateInfo> blockStateInfoComponentType;
  private final ComponentType<ChunkStore, ItemLootContainerBlock> lootContainerComponentType;
  private final Query<ChunkStore> query;

  public LootContainerBlockAddedSystem(ComponentType<ChunkStore, BlockModule.BlockStateInfo> blockStateInfoComponentType, ComponentType<ChunkStore, ItemLootContainerBlock> lootContainerComponentType) {
    this.blockStateInfoComponentType = blockStateInfoComponentType;
    this.lootContainerComponentType = lootContainerComponentType;
    this.query = Query.and(blockStateInfoComponentType, lootContainerComponentType);
  }

  @Override
  public void onEntityAdded(@NonNullDecl Ref<ChunkStore> ref, @NonNullDecl AddReason reason, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
    ItemLootContainerBlock block = commandBuffer.getComponent(ref, this.lootContainerComponentType);
    assert block != null;

    BlockModule.BlockStateInfo blockStateInfo = commandBuffer.getComponent(ref, this.blockStateInfoComponentType);
    assert blockStateInfo != null;

    Ref<ChunkStore> refx = blockStateInfo.getChunkRef();
    if (!refx.isValid()) {
      return;
    }

    int index = blockStateInfo.getIndex();
    int j = ChunkUtil.xFromBlockInColumn(index);
    int k = ChunkUtil.yFromBlockInColumn(index);
    int l = ChunkUtil.zFromBlockInColumn(index);

    BlockChunk chunk = commandBuffer.getComponent(refx, BlockChunk.getComponentType());

    assert chunk != null;

    // Y?
    BlockSection section = chunk.getSectionAtBlockY(k);

    World world = commandBuffer.getExternalData().getWorld();
    Store<EntityStore> worldStore = world.getEntityStore().getStore();
    WorldTimeResource resource = worldStore.getResource(WorldTimeResource.getResourceType());
    Instant instance = resource.getGameTime();

    section.scheduleTick(ChunkUtil.indexBlock(j, k, l), instance.plusMillis(600));
  }

  @Override
  public void onEntityRemove(@NonNullDecl Ref<ChunkStore> var1, @NonNullDecl RemoveReason var2, @NonNullDecl Store<ChunkStore> var3, @NonNullDecl CommandBuffer<ChunkStore> var4) {

  }

  @NullableDecl
  @Override
  public Query<ChunkStore> getQuery() {
    return query;
  }
}
