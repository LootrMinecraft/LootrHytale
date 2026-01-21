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
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import noobanidus.mods.lootr.LootrPlugin;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.awt.*;

public class BlockBreakEventSystem extends EntityEventSystem<EntityStore, BreakBlockEvent> {
  public BlockBreakEventSystem() {
    super(BreakBlockEvent.class);
  }

  @Override
  public void handle(int var1, @NonNullDecl ArchetypeChunk<EntityStore> var2, @NonNullDecl Store<EntityStore> var3, @NonNullDecl CommandBuffer<EntityStore> var4, @NonNullDecl BreakBlockEvent var5) {
    if (var5.isCancelled()) {
      return;
    }
    if (var5.getBlockType().equals(LootrPlugin.get().getLootrChestBlockType())) {
      Ref<EntityStore> ref = var2.getReferenceTo(var1);
      Player player = var2.getComponent(var1, Player.getComponentType());
      if (player != null) {
        var movement = var4.getComponent(ref, MovementStatesComponent.getComponentType());
        if (movement != null && !movement.getMovementStates().crouching) {
          // TODO: Messages send multiple times which is annoying
          if (player.getGameMode() == GameMode.Creative) {
            player.sendMessage(
                Message.translation("general.Noobanidus_Lootr.CrouchToBreakCreative").bold(true).color(Color.red)
            );
            var5.setCancelled(true);
          } else {
            player.sendMessage(
                Message.translation("general.Noobanidus_Lootr.CrouchToBreak").bold(true).color(Color.red)
            );
            var5.setCancelled(true);
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
