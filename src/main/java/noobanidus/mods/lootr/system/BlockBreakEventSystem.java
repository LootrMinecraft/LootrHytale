package noobanidus.mods.lootr.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import noobanidus.mods.lootr.LootrPlugin;
import noobanidus.mods.lootr.block.ItemLootContainerBlock;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.joml.Vector3i;

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

    ChunkStore chunkStore = world.getChunkStore();
    Ref<ChunkStore> ref1 = chunkStore.getChunkSectionReferenceAtBlock(pos.x, pos.y, pos.z);
    if (ref1 == null || !ref1.isValid()) {
      return;
    }

    Store<ChunkStore> store1 = chunkStore.getStore();
    Ref<ChunkStore> ref2 = BlockModule.getBlockEntity(store1, ref1, pos.x, pos.y, pos.z);
    if (ref2 == null) {
      return;
    }
    ItemLootContainerBlock itemcontainerblock = store1.getComponent(ref2, ItemLootContainerBlock.getLootComponentType());
    if (itemcontainerblock == null) {
      return;
    }

    Ref<EntityStore> ref = archetype.getReferenceTo(index);
    Player player = archetype.getComponent(index, Player.getComponentType());
    var movement = commandBuffer.getComponent(ref, MovementStatesComponent.getComponentType());
    assert player != null;
    assert movement != null;

    PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
    assert playerRef != null;

    if (!movement.getMovementStates().crouching) {
      // TODO: Messages send multiple times which is annoying
      if (player.getGameMode() == GameMode.Creative) {
        // Not crouching in creative mode
        playerRef.sendMessage(
            Message.translation("general.Noobanidus_Lootr.CrouchToBreakCreative").bold(true).color(Color.red)
        );
        event.setCancelled(true);
      } else {
        if (LootrPlugin.get().getConfig().isBreakDisabled()) {
          playerRef.sendMessage(
              Message.translation("general.Noobanidus_Lootr.CannotBreak").bold(true).color(Color.red)
          );
        } else {
          playerRef.sendMessage(
              Message.translation("general.Noobanidus_Lootr.CrouchToBreak").bold(true).color(Color.red)
          );
        }
        event.setCancelled(true);
      }
    } else {
      if (player.getGameMode() != GameMode.Creative) {
        if (LootrPlugin.get().getConfig().isBreakDisabled()) {
          playerRef.sendMessage(
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
