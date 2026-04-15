package noobanidus.mods.lootr.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import noobanidus.mods.lootr.LootrPlugin;
import noobanidus.mods.lootr.state.ItemLootContainerBlock;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.awt.*;

public class BlockBreakEventSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {
  public BlockBreakEventSystem() {
    super(BreakBlockEvent.class);
  }

  @Override
  public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> archetype, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl BreakBlockEvent event) {
    if (event.isCancelled() || LootrPlugin.get().getConfig().isBreakEnabled()) {
      return;
    }

    Vector3i pos = event.getTargetBlock();
    World world = commandBuffer.getExternalData().getWorld();
    long i = ChunkUtil.indexChunkFromBlock(pos.x, pos.z);
    WorldChunk worldchunk = world.getChunkIfLoaded(i);
    if (worldchunk == null) {
      return;
    }

    ChunkStore chunkstore = world.getChunkStore();
    Ref<ChunkStore> ref1 = chunkstore.getChunkReference(i);
    if (ref1 == null) {
      return;
    }
    BlockComponentChunk blockComponentChunk = chunkstore.getStore()
        .getComponent(ref1, BlockComponentChunk.getComponentType());
    if (blockComponentChunk == null) {
      return;
    }
    Ref<ChunkStore> ref2 = blockComponentChunk.getEntityReference(ChunkUtil.indexBlockInColumn(pos.x, pos.y, pos.z));
    if (ref2 == null) {
      return;
    }
    ItemLootContainerBlock itemcontainerblock = chunkstore.getStore()
        .getComponent(ref2, ItemLootContainerBlock.getLootComponentType());
    if (itemcontainerblock == null) {
      return;
    }

    Ref<EntityStore> ref = archetype.getReferenceTo(index);
    Player player = archetype.getComponent(index, Player.getComponentType());
    var movement = commandBuffer.getComponent(ref, MovementStatesComponent.getComponentType());
    assert player != null;
    assert movement != null;
    if (!movement.getMovementStates().crouching) {
      // TODO: Messages send multiple times which is annoying
      if (player.getGameMode() == GameMode.Creative) {
        // Not crouching in creative mode
        player.sendMessage(
            Message.translation("general.Noobanidus_Lootr.CrouchToBreakCreative").bold(true).color(Color.red)
        );
        event.setCancelled(true);
      } else {
        if (LootrPlugin.get().getConfig().isBreakDisabled()) {
          player.sendMessage(
              Message.translation("general.Noobanidus_Lootr.CannotBreak").bold(true).color(Color.red)
          );
        } else {
          player.sendMessage(
              Message.translation("general.Noobanidus_Lootr.CrouchToBreak").bold(true).color(Color.red)
          );
        }
        event.setCancelled(true);
      }
    } else {
      if (player.getGameMode() != GameMode.Creative) {
        if (LootrPlugin.get().getConfig().isBreakDisabled()) {
          player.sendMessage(
              Message.translation("general.Noobanidus_Lootr.CannotBreak").bold(true).color(Color.red)
          );
          event.setCancelled(true);
        }
      }
    }
  }

  @NullableDecl
  @Override
  public Query<EntityStore> getQuery() {
    return Query.and(Player.getComponentType(), MovementStatesComponent.getComponentType());
  }
}
