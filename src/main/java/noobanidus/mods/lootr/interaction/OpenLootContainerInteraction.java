package noobanidus.mods.lootr.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockOperations;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import noobanidus.mods.lootr.block.ItemLootContainerBlock;
import org.joml.Vector3d;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

// Basically a duplicate of `OpenContainerInteraction` except it expects an `ItemLootContainerBlock` and it uses the `getItemContainer(UUID)` method to get a player-specific container.
@SuppressWarnings({"DataFlowIssue"})
public class OpenLootContainerInteraction extends SimpleBlockInteraction {
  public static final BuilderCodec<OpenLootContainerInteraction> CODEC = BuilderCodec.builder(
          OpenLootContainerInteraction.class, OpenLootContainerInteraction::new, SimpleBlockInteraction.CODEC
      )
      .documentation("Opens the instanced container keyed to the player currently interacting with the block.")
      .build();

  @SuppressWarnings("removal")
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
    PlayerRef playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
    ChunkStore chunkstore = world.getChunkStore();
    Ref<ChunkStore> ref1 = chunkstore.getChunkSectionReferenceAtBlock(pos.x, pos.y, pos.z);
    if (ref1 == null || !ref1.isValid()) {
      return;
    }
    Store<ChunkStore> store1 = chunkstore.getStore();
    Ref<ChunkStore> ref2 = BlockModule.getBlockEntity(store1, ref1, pos.x, pos.y, pos.z);
    if (ref2 == null) {
      return;
    }
    ItemLootContainerBlock itemcontainerblock = store1.getComponent(ref2, ItemLootContainerBlock.getLootComponentType());
    if (itemcontainerblock == null) {
      playerRef.sendMessage(
          Message.translation("server.interactions.invalidBlockState")
              .param("interaction", this.getClass().getSimpleName())
              .param("blockState", chunkstore.getStore().getArchetype(ref2).toString())
      );
    } else {
      BlockSection blocksection = store1.getComponent(ref1, BlockSection.getComponentType());
      if (blocksection == null) {
        return;
      }

      int i = blocksection.get(pos.x, pos.y, pos.z);
      BlockType blocktype = BlockType.getAssetMap().getAsset(i);
      if (blocktype == null) {
        return;
      }

      int j = blocksection.getRotationIndex(pos.x, pos.y, pos.z);
      UUIDComponent uuidcomponent = commandBuffer.getComponent(ref, UUIDComponent.getComponentType());

      assert uuidcomponent != null;

      UUID uuid = uuidcomponent.getUuid();
      WorldChunk worldchunk = world.getChunk(ChunkUtil.indexChunkFromBlock(pos.x, pos.z));
      ContainerBlockWindow containerblockwindow = new ContainerBlockWindow(
          pos.x, pos.y, pos.z, worldchunk.getRotationIndex(pos.x, pos.y, pos.z), blocktype, itemcontainerblock.getItemContainer(ref2, chunkstore.getStore(), player, playerRef, uuid)
      );
      Map<UUID, ContainerBlockWindow> map = itemcontainerblock.getWindows();
      if (map.putIfAbsent(uuid, containerblockwindow) == null) {
        if (player.getPageManager().setPageWithWindows(ref, store, Page.Bench, true, containerblockwindow)) {
          containerblockwindow.registerCloseEvent(event -> onWindowClose(world, ref, uuid, pos, blocktype, containerblockwindow, map, commandBuffer));
          if (map.size() == 1) {
            BlockOperations.setBlockInteractionState(chunkstore, ref1, pos.x, pos.y, pos.z, blocktype, "OpenWindow", false);
          }

          BlockType blocktype1 = blocktype.getBlockForState("OpenWindow");
          if (blocktype1 == null) {
            return;
          }

          int k = blocktype1.getInteractionSoundEventIndex();
          if (k == 0) {
            return;
          }

          Vector3d vector3d = new Vector3d();
          blocktype.getBlockCenter(j, vector3d);
          vector3d.add(pos.x, pos.y, pos.z);
          SoundUtil.playSoundEvent3d(ref, i, vector3d, commandBuffer);
        } else {
          map.remove(uuid, containerblockwindow);
        }
      }
    }
  }


  private static void onWindowClose(
      @Nonnull World world,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull UUID uuid,
      @Nonnull Vector3i pos,
      @Nonnull BlockType blockType,
      @Nonnull ContainerBlockWindow window,
      @Nonnull Map<UUID, ContainerBlockWindow> windows,
      @Nonnull CommandBuffer<EntityStore> commandBuffer
  ) {
    windows.remove(uuid, window);
    ChunkStore chunkstore = world.getChunkStore();
    Ref<ChunkStore> refx = chunkstore.getChunkSectionReferenceAtBlock(pos.x, pos.y, pos.z);
    if (refx != null && refx.isValid()) {
      Store<ChunkStore> store = chunkstore.getStore();
      BlockSection blocksection = store.getComponent(refx, BlockSection.getComponentType());
      if (blocksection != null) {
        BlockType blocktype = BlockType.getAssetMap().getAsset(blocksection.get(pos.x, pos.y, pos.z));
        if (blocktype != null) {
          if (windows.isEmpty()) {
            String s = Objects.requireNonNullElse(blockType.getDefaultStateKey(), blockType.getId());
            String s1 = Objects.requireNonNullElse(blocktype.getDefaultStateKey(), blocktype.getId());
            if (Objects.equals(s1, s)) {
              BlockOperations.setBlockInteractionState(chunkstore, refx, pos.x, pos.y, pos.z, blocktype, "CloseWindow", false);
            }
          }

          BlockType blocktype1 = blocktype.getBlockForState("CloseWindow");
          if (blocktype1 != null) {
            int j = blocktype1.getInteractionSoundEventIndex();
            if (j != 0) {
              int i = blocksection.getRotationIndex(pos.x, pos.y, pos.z);
              Vector3d vector3d = new Vector3d();
              blockType.getBlockCenter(i, vector3d);
              vector3d.add(pos.x, pos.y, pos.z);
              SoundUtil.playSoundEvent3d(ref, j, vector3d, commandBuffer);
            }
          }
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
