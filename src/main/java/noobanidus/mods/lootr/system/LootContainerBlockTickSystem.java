package noobanidus.mods.lootr.system;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
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

    BlockComponentChunk blockComponentChunk = commandBuffer.getComponent(ref, BlockComponentChunk.getComponentType());

    assert blockComponentChunk != null;

    Ref<ChunkStore> ref1 = archetype.getReferenceTo(index);
    BlockChunk blockChunk = commandBuffer.getComponent(ref, BlockChunk.getComponentType());

    assert blockChunk != null;

    blockSection.forEachTicking(blockComponentChunk, commandBuffer, chunkSection.getY(),
        (blockCompChunk1, commandbuffer1, localX, localY, localZ, blockId) -> {
          Ref<ChunkStore> ref2 = blockCompChunk1.getEntityReference(ChunkUtil.indexBlockInColumn(localX, localY, localZ));
          if (ref2 == null) {
            return BlockTickStrategy.IGNORED;
          }

          ItemLootContainerBlock block = commandbuffer1.getComponent(ref2, this.lootContainerComponentType);
          if (block == null) {
            return BlockTickStrategy.IGNORED;
          }

          block.tick(commandbuffer1, blockChunk, blockSection, ref1, ref2, localX, localY, localZ, false);
          return BlockTickStrategy.CONTINUE;
        });
  }

  @NullableDecl
  @Override
  public Query<ChunkStore> getQuery() {
    return query;
  }
}
