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
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import noobanidus.mods.lootr.LootrPlugin;
import noobanidus.mods.lootr.state.ItemLootContainerState;
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

    Vector3i vector3i = event.getTargetBlock();
    World world = commandBuffer.getExternalData().getWorld();
    long i = ChunkUtil.indexChunkFromBlock(vector3i.x, vector3i.z);
    WorldChunk worldchunk = world.getChunkIfLoaded(i);
    if (worldchunk != null && !(worldchunk.getState(vector3i.x, vector3i.y, vector3i.z) instanceof ItemLootContainerState)) {
      return;
    }

    Ref<EntityStore> ref = archetype.getReferenceTo(index);
    Player player = archetype.getComponent(index, Player.getComponentType());
    if (player != null) {
      var movement = commandBuffer.getComponent(ref, MovementStatesComponent.getComponentType());
      if (movement != null) {
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
    }
  }

  @NullableDecl
  @Override
  public Query<EntityStore> getQuery() {
    return PlayerRef.getComponentType();
  }
}
