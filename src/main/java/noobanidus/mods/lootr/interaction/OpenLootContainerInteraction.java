package noobanidus.mods.lootr.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import noobanidus.mods.lootr.block.ItemLootContainerBlock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

// Basically a duplicate of `OpenContainerInteraction` except it expects an `ItemLootContainerBlock` and it uses the `getItemContainer(UUID)` method to get a player-specific container.
@SuppressWarnings({"DataFlowIssue"})
public class OpenLootContainerInteraction extends SimpleBlockInteraction {
  public static final BuilderCodec<OpenLootContainerInteraction> CODEC = BuilderCodec.builder(
          OpenLootContainerInteraction.class, OpenLootContainerInteraction::new, SimpleBlockInteraction.CODEC
      )
      .documentation("Opens the instanced container keyed to the player currently interacting with the block.")
      .build();

  @Override
  protected void interactWithBlock(
      @Nonnull World world,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull InteractionType type,
      @Nonnull InteractionContext context,
      @Nullable ItemStack itemInHand,
      @Nonnull Vector3i pos,
      @Nonnull CooldownHandler cooldownHandler
  ) {
    Ref<EntityStore> ref = context.getEntity();
    Store<EntityStore> store = ref.getStore();
    Player player = commandBuffer.getComponent(ref, Player.getComponentType());
    if (player == null) {
      return;
    }
    ChunkStore chunkstore = world.getChunkStore();
    Ref<ChunkStore> ref1 = chunkstore.getChunkReference(ChunkUtil.indexChunkFromBlock(pos.x, pos.z));
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
      player.sendMessage(
          Message.translation("server.interactions.invalidBlockState")
              .param("interaction", this.getClass().getSimpleName())
              .param("blockState", chunkstore.getStore().getArchetype(ref2).toString())
      );
    } else {
      BlockType blocktype = world.getBlockType(pos.x, pos.y, pos.z);
      UUIDComponent uuidcomponent = commandBuffer.getComponent(ref, UUIDComponent.getComponentType());

      assert uuidcomponent != null;

      UUID uuid = uuidcomponent.getUuid();
      WorldChunk worldchunk = world.getChunk(ChunkUtil.indexChunkFromBlock(pos.x, pos.z));
      ContainerBlockWindow containerblockwindow = new ContainerBlockWindow(
          pos.x, pos.y, pos.z, worldchunk.getRotationIndex(pos.x, pos.y, pos.z), blocktype, itemcontainerblock.getItemContainer(ref2, chunkstore.getStore(), player, uuid)
      );
      Map<UUID, ContainerBlockWindow> map = itemcontainerblock.getWindows();
      if (map.putIfAbsent(uuid, containerblockwindow) == null) {
        if (player.getPageManager().setPageWithWindows(ref, store, Page.Bench, true, containerblockwindow)) {
          containerblockwindow.registerCloseEvent(event -> {
            map.remove(uuid, containerblockwindow);
            BlockType blocktype2 = world.getBlockType(pos);
            if (map.isEmpty()) {
              world.setBlockInteractionState(pos, blocktype2, "CloseWindow");
            }

            BlockType blocktype3 = blocktype2.getBlockForState("CloseWindow");
            if (blocktype3 != null) {
              int k = blocktype3.getInteractionSoundEventIndex();
              if (k != 0) {
                int l = worldchunk.getRotationIndex(pos.x, pos.y, pos.z);
                Vector3d vector3d1 = new Vector3d();
                blocktype.getBlockCenter(l, vector3d1);
                vector3d1.add(pos);
                SoundUtil.playSoundEvent3d(ref, k, vector3d1, commandBuffer);
              }
            }
          });
          if (map.size() == 1) {
            world.setBlockInteractionState(pos, blocktype, "OpenWindow");
          }

          BlockType blocktype1 = blocktype.getBlockForState("OpenWindow");
          if (blocktype1 == null) {
            return;
          }

          int i = blocktype1.getInteractionSoundEventIndex();
          if (i == 0) {
            return;
          }

          int j = worldchunk.getRotationIndex(pos.x, pos.y, pos.z);
          Vector3d vector3d = new Vector3d();
          blocktype.getBlockCenter(j, vector3d);
          vector3d.add(pos);
          SoundUtil.playSoundEvent3d(ref, i, vector3d, commandBuffer);
        } else {
          map.remove(uuid, containerblockwindow);
        }
      }
    }
  }


  @Override
  protected void simulateInteractWithBlock(
      @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack
          itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock
  ) {
  }
}
