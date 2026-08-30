package noobanidus.mods.lootr.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.world.TickSuppressionRegistry;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockComponentSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.ChunkSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import noobanidus.mods.lootr.block.ItemLootContainerBlock;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class LootContainerBlockTickSystem extends EntityTickingSystem<ChunkStore> {
  private final ComponentType<ChunkStore, BlockSection> blockSectionComponentType;
  private final ComponentType<ChunkStore, ChunkSection> chunkSectionComponentType;
  private final ComponentType<ChunkStore, ItemLootContainerBlock> lootContainerComponentType;
  private final Query<ChunkStore> query;

  public LootContainerBlockTickSystem(ComponentType<ChunkStore, BlockSection> blockSectionComponentType, ComponentType<ChunkStore, ChunkSection> chunkSectionComponentType, ComponentType<ChunkStore, ItemLootContainerBlock> lootContainerComponentType) {
    this.blockSectionComponentType = blockSectionComponentType;
    this.chunkSectionComponentType = chunkSectionComponentType;
    this.lootContainerComponentType = lootContainerComponentType;
    this.query = Query.and(blockSectionComponentType, chunkSectionComponentType);
  }

  @Override
  public void tick(float tick, int index, @NonNullDecl ArchetypeChunk<ChunkStore> archetype, @NonNullDecl Store<ChunkStore> store, @NonNullDecl CommandBuffer<ChunkStore> commandBuffer) {
    BlockSection blockSection = archetype.getComponent(index, this.blockSectionComponentType);
    assert blockSection != null;

    if (blockSection.getTickingBlocksCountCopy() == 0) {
      return;
    }

    ChunkSection chunkSection = archetype.getComponent(index, this.chunkSectionComponentType);

    assert chunkSection != null;

    Ref<ChunkStore> ref = chunkSection.getChunkColumnReference();
    if (ref == null || !ref.isValid()) {
      return;
    }

    BlockComponentSection blockcomponentsection = archetype.getComponent(index, BlockComponentSection.getComponentType());
    assert blockcomponentsection != null;

    Ref<ChunkStore> ref1 = archetype.getReferenceTo(index);
    TickSuppressionRegistry tickSuppressionRegistry = store.getExternalData().getWorld().getEntityStore().getStore()
        .getResource(TickSuppressionRegistry.RESOURCE);
    TickSuppressionRegistry tickSuppressionRegistry1 = tickSuppressionRegistry != null && tickSuppressionRegistry.hasKind(1) ? tickSuppressionRegistry : null;

    int i = chunkSection.getX();
    int j = chunkSection.getZ();
    int k = chunkSection.getY();

    blockSection.forEachTicking(blockcomponentsection, commandBuffer, chunkSection.getY(),
        (blockComponentSection1, commandbuffer1, localX, localY, localZ, blockId) -> {
          Ref<ChunkStore> ref2 = blockComponentSection1.getBlockReference(ChunkUtil.indexBlock(localX, localY, localZ));
          if (ref2 == null) {
            return BlockTickStrategy.IGNORED;
          }

          BlockModule.BlockStateInfo blockmodule$blockstateinfo = commandbuffer1.getComponent(ref2, BlockModule.BlockStateInfo.getComponentType());

          ItemLootContainerBlock block = commandbuffer1.getComponent(ref2, this.lootContainerComponentType);
          if (block == null) {
            return BlockTickStrategy.IGNORED;
          }

          int worldX = ChunkUtil.worldCoordFromLocalCoord(i, localX);
          int worldY = ChunkUtil.worldCoordFromLocalCoord(k, localY);
          int worldZ = ChunkUtil.worldCoordFromLocalCoord(j, localZ);


          if (tickSuppressionRegistry1 != null && tickSuppressionRegistry1.isBlockSuppressed(worldX, worldY, worldZ, 1)) {
            return BlockTickStrategy.IGNORED;
          }

          block.tick(commandbuffer1, worldX, worldY, worldZ);
          return BlockTickStrategy.CONTINUE;
        });
  }

  @NullableDecl
  @Override
  public Query<ChunkStore> getQuery() {
    return query;
  }
}
