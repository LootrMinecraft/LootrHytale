package noobanidus.mods.lootr.block;

import com.hypixel.hytale.builtin.adventure.stash.StashPlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectList;
import noobanidus.mods.lootr.LootrPlugin;
import noobanidus.mods.lootr.container.EmptySimpleItemContainer;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nullable;
import java.awt.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class ItemLootContainerBlock extends ItemContainerBlock {
  private static final ItemContainerBlock EMPTY;

  // TODO: This could break at any moment.
  // TODO: Beg the Gods of Hytale to please make the constructor Protected.
  static {
    try {
      MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(ItemContainerBlock.class, MethodHandles.lookup());
      MethodHandle ctor = lookup.findConstructor(ItemContainerBlock.class, MethodType.methodType(void.class));
      EMPTY = (ItemContainerBlock) ctor.invoke();
    } catch (Throwable e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  // TODO: Who knows when the `addField` deprecation will break things???
  @SuppressWarnings("deprecation")
  public static final BuilderCodec<ItemLootContainerBlock> CODEC = BuilderCodec.builder(
          ItemLootContainerBlock.class, ItemLootContainerBlock::new, ItemContainerBlock.CODEC
      )
      .addField(new KeyedCodec<>("Capacity", Codec.SHORT), (state, o) -> state.capacity = o, (state) -> state.capacity)
      .addField(new KeyedCodec<>("Droplist", Codec.STRING), (state, o) -> state.droplist = o, state -> state.droplist)
      .addField(new KeyedCodec<>("OriginalBlock", Codec.STRING), (state, o) -> state.originalBlock = o, state -> state.originalBlock)
      .addField(
          new KeyedCodec<>("Template", SimpleItemContainer.CODEC),
          (state, o) -> state.template = o,
          (state) -> state.template
      )
      .addField(
          new KeyedCodec<>("PlayerContainers",
              new MapCodec<>(SimpleItemContainer.CODEC, ConcurrentHashMap::new)),
          (state, o) ->

          {
            // TODO: I'm just defaulting to UUID/String conversion because Minecraft generally doesn't support non-String keys when serializing maps.
            ConcurrentHashMap<UUID, SimpleItemContainer> newMap = new ConcurrentHashMap<>();
            for (Map.Entry<String, SimpleItemContainer> entry : o.entrySet()) {
              try {
                UUID uuid = UUID.fromString(entry.getKey());
                newMap.put(uuid, entry.getValue());
              } catch (IllegalArgumentException e) {
                LootrPlugin.LOGGER.at(Level.WARNING).withCause(e)
                    .log("Invalid UUID string in PlayerContainers: %s", entry.getKey());
              }
            }
            state.playerContainers = newMap;
          },
          state ->
          {
            HashMap<String, SimpleItemContainer> temp = new HashMap<>();
            for (Map.Entry<UUID, SimpleItemContainer> entry : state.playerContainers.entrySet()) {
              temp.put(entry.getKey().toString(), entry.getValue());
            }
            return temp;
          }
      )
      .build();

  protected Map<UUID, SimpleItemContainer> playerContainers = new ConcurrentHashMap<>();
  protected short capacity = -1;
  // This is serialized in case we want to de-convert at some point
  protected String originalBlock;
  protected SimpleItemContainer template;

  private UUID uuid = null;

  public ItemLootContainerBlock() {
    super(EMPTY);
  }

  @SuppressWarnings({"unused", "CopyConstructorMissesField"})
  public ItemLootContainerBlock(ItemLootContainerBlock other) {
    super(other);
    this.originalBlock = other.originalBlock;
    this.template = other.template == null ? null : other.template.clone();
    this.uuid = other.uuid == null ? null : other.uuid;
    this.capacity = other.capacity;
  }

  public ItemLootContainerBlock(ItemContainerBlock block) {
    super(block);
    // TODO: When reverting to super capacity, remove this
    this.capacity = block.getCapacity();
  }

  public Set<UUID> getPlayerContainerKeys() {
    return playerContainers.keySet();
  }

  public void setOriginalBlock(String originalBlock) {
    this.originalBlock = originalBlock;
  }

  public void setTemplate(SimpleItemContainer template) {
    this.template = template;
    if (template.getCapacity() < this.capacity) {
      LootrPlugin.LOGGER.at(Level.WARNING)
          .log("Template container capacity (%d) is less than loot container capacity (%d). Items may be lost.",
              template.getCapacity(), this.capacity);
    }
  }

  @Override
  public void setItemContainer(SimpleItemContainer itemContainer) {
    // NO-OP
  }

  // TODO: Default capacity will be stored in encoded block entity (ItemLootContainerBlock) with 1. Ensure capacity is properly copied from template block.
  public void setCapacity(short capacity) {
    this.capacity = capacity;
  }

  @Override
  public void setDroplist(@Nullable String droplist) {
    if (droplist == null && (template == null || template == EmptySimpleItemContainer.INSTANCE)) {
      return;
    }
    this.droplist = droplist;
  }

  @Override
  public SimpleItemContainer getItemContainer() {
    return EmptySimpleItemContainer.INSTANCE;
  }

  public SimpleItemContainer getItemContainer(Ref<ChunkStore> ref, Store<ChunkStore> store, Player playerComponent, UUID player) {
    SimpleItemContainer newContainer = new SimpleItemContainer(this.capacity);
    if ("".equals(droplist) || droplist == null || droplist.isEmpty()) {
      if (template == null || template == EmptySimpleItemContainer.INSTANCE) {
        playerComponent.sendMessage(
            Message.translation("general.Noobanidus_Lootr.NoDropList").bold(true).color(Color.red)
        );
        return EmptySimpleItemContainer.INSTANCE;
      } else {
        ItemContainer.copy(template, newContainer, null);
      }
    }
    if (playerContainers.putIfAbsent(player, newContainer) == null) {
      BlockModule.BlockStateInfo blockmodule$blockstateinfo = store.getComponent(ref, BlockModule.BlockStateInfo.getComponentType());
      assert blockmodule$blockstateinfo != null;
      // Ensure container capacity
      ItemContainer.ensureContainerCapacity(newContainer, this.capacity, SimpleItemContainer::new, null /* This is null as we don't want to drop any items */);
      World world = store.getExternalData().getWorld();
      newContainer.registerChangeEvent(EventPriority.LAST, _ -> {
        if (world.isInThread()) {
          blockmodule$blockstateinfo.markNeedsSaving();
        } else {
          world.execute(blockmodule$blockstateinfo::markNeedsSaving);
        }
      });
      TemporaryItemContainerBlock temp = new TemporaryItemContainerBlock(newContainer);
      StashPlugin.stash(blockmodule$blockstateinfo, temp, false);
      return newContainer;
    } else {
      return playerContainers.get(player);
    }
  }

  public void tick (CommandBuffer<ChunkStore> commandBuffer, BlockChunk blockChunk, BlockSection blockSection, Ref<ChunkStore> sectionRef, Ref<ChunkStore> blockRef, int x, int y, int z, boolean initialTick) {
    ComponentType<EntityStore, PlayerRef> componenttype = PlayerRef.getComponentType();

    Vector3d vector3d = new Vector3d(x, y, z).add(0.5); // TODO:

    // TODO: Stuff
    /*
    if (uuid == null) {
      uuid = UUID.randomUUID();
      commandBuffer.run((chunkStore) -> {
        var worldchunk = this.getChunk();
        var blockEntity = worldchunk.getBlockComponentEntity(this.getBlockX(), this.getBlockY(), this.getBlockZ());
        if (blockEntity == null) {
          blockEntity = BlockModule.ensureBlockEntity(worldchunk, this.getBlockX(), this.getBlockY(), this.getBlockZ());
        }

        if (blockEntity != null) {
          var currentUuid = chunkStore.getComponent(blockEntity, UUIDComponent.getComponentType());
          if (currentUuid == null) {
            chunkStore.putComponent(blockEntity, UUIDComponent.getComponentType(), new UUIDComponent(uuid));
          } else {
            uuid = currentUuid.getUuid();
          }
        } else {
          // Log that we couldn't store the uuid
          LootrPlugin.LOGGER.at(Level.WARNING)
              .log("Could not store UUID for Lootr chest at %s.", this.getCenteredBlockPosition());
        }
      });
    } */

    var entityStore = commandBuffer.getExternalData().getWorld().getEntityStore().getStore();
    var spatialresource = entityStore
        .getResource(
            EntityModule.get().getPlayerSpatialResourceType()
        );
    List<Ref<EntityStore>> objectlist = SpatialResource.getThreadLocalReferenceList();
    spatialresource.getSpatialStructure().collect(vector3d, 30.0, objectlist);
    objectlist.removeIf(ref -> {
      if (!ref.isValid()) {
        return true;
      }

      if (playerContainers.isEmpty()) {
        return false;
      }

      PlayerRef playerref = entityStore.getComponent(ref, componenttype);
      // TODO: Migrate to the UUID component
      if (playerref == null) {
        return true;
      }

      return playerContainers.containsKey(playerref.getUuid());
    });
    ParticleUtil.spawnParticleEffect("Noobanidus_Lootr_UnopenedChestSparkles", vector3d.x, vector3d.y, vector3d.z, 0f, 0f, 0f, 1f, new com.hypixel.hytale.protocol.Color((byte) 240, (byte) 203, (byte) 86), null, objectlist, entityStore);
  }

  private class TemporaryItemContainerBlock extends ItemContainerBlock {
    private final SimpleItemContainer container;

    public TemporaryItemContainerBlock(SimpleItemContainer container) {
      super(EMPTY);
      this.container = container;
    }

    @Override
    public SimpleItemContainer getItemContainer() {
      return container;
    }

    @NullableDecl
    @Override
    public String getDroplist() {
      return ItemLootContainerBlock.this.getDroplist();
    }

    @Override
    public void setDroplist(@NullableDecl String droplist) {
      ItemLootContainerBlock.this.setDroplist(droplist);
    }
  }

  public static ItemLootContainerBlock fromItemContainerBlock(String originalBlockName, ItemContainerBlock block) {
    if (block instanceof ItemLootContainerBlock lootContainerState) {
      return lootContainerState;
    }

    // TODO: This might be the wrong way to create a new ItemLootContainerBlock
    var newBlock = new ItemLootContainerBlock(block);
    newBlock.setOriginalBlock(originalBlockName);
    return newBlock;
  }

  public static ComponentType<ChunkStore, ItemLootContainerBlock> getLootComponentType() {
    return LootrPlugin.get().getLootContainerType();
  }
}
