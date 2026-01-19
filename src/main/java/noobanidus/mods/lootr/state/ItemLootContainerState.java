package noobanidus.mods.lootr.state;

import com.hypixel.hytale.builtin.adventure.stash.StashPlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.windows.WindowManager;
import com.hypixel.hytale.server.core.inventory.container.EmptyItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.state.TickableBlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import it.unimi.dsi.fastutil.objects.ObjectList;
import noobanidus.mods.lootr.LootrPlugin;
import org.bson.BsonDocument;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

@SuppressWarnings({"removal", "deprecation"})
public class ItemLootContainerState extends ItemContainerState {
  public static final Codec<ItemLootContainerState> CODEC = BuilderCodec.builder(
          ItemLootContainerState.class, ItemLootContainerState::new, ItemContainerState.BASE_CODEC
      )
      .addField(new KeyedCodec<>("Capacity", Codec.SHORT), (state, o) -> state.capacity = o, (state) -> state.capacity)
      .addField(new KeyedCodec<>("Custom", Codec.BOOLEAN), (state, o) -> state.custom = o, state -> state.custom)
      .addField(new KeyedCodec<>("AllowViewing", Codec.BOOLEAN), (state, o) -> state.allowViewing = o, state -> state.allowViewing)
      .addField(new KeyedCodec<>("Droplist", Codec.STRING), (state, o) -> state.droplist = o, state -> state.droplist)
      .addField(new KeyedCodec<>("OriginalBlock", Codec.STRING), (state, o) -> state.originalBlock = o, state -> state.originalBlock)
      .addField(
          new KeyedCodec<>("Marker", WorldMapManager.MarkerReference.CODEC),
          (state, o) -> state.marker = o,
          state -> state.marker
      )
      .addField(
          new KeyedCodec<>("PlayerContainers",
              new MapCodec<>(ItemContainer.CODEC, ConcurrentHashMap::new)),
          (state, o) -> {
            // TODO: I'm just defaulting to UUID/String conversion because Minecraft generally doesn't support non-String keys when serializing maps.
            ConcurrentHashMap<UUID, ItemContainer> newMap = new ConcurrentHashMap<>();
            for (Map.Entry<String, ItemContainer> entry : o.entrySet()) {
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
          state -> {
            HashMap<String, ItemContainer> temp = new HashMap<>();
            for (Map.Entry<UUID, ItemContainer> entry : state.playerContainers.entrySet()) {
              temp.put(entry.getKey().toString(), entry.getValue());
            }
            return temp;
          }
      )
      .build();
  protected Map<UUID, ItemContainer> playerContainers = new ConcurrentHashMap<>();
  protected short capacity = -1;
  // This is serialized in case we want to de-convert at some point
  protected String originalBlock;

  public void setOriginalBlock (String originalBlock) {
    this.originalBlock = originalBlock;
  }

  @Override
  public boolean initialize(@Nonnull BlockType blockType) {
    var oldCustom = this.custom;
    this.custom = true;
    var result = super.initialize(blockType);
    this.custom = oldCustom;
    if (!result) {
      return false;
    }

    if (this.capacity == -1) {
      this.capacity = 54;
    }

    if (originalBlock != null) {
      BlockType originalBlockType = BlockType.getAssetMap().getAsset(originalBlock);
      if (originalBlockType != null && originalBlockType.getState() instanceof ItemContainerStateData data) {
        this.capacity = data.getCapacity();
      }
    }

    return true;
  }

  @Override
  public void onDestroy() {
    WindowManager.closeAndRemoveAll(this.getWindows());
    // We don't drop any contents as that would be confusing

    if (this.marker != null) {
      this.marker.remove();
    }
  }

  @Override
  public void setItemContainer(SimpleItemContainer itemContainer) {
    // NO-OP
  }

  @Override
  public void setDroplist(@Nullable String droplist) {
    // Wiping the droplist means we can never generate loot again, so we don't do it
    if (droplist == null) {
      return;
    }
    this.droplist = droplist;
    this.markNeedsSave();
  }

  @Override
  public ItemContainer getItemContainer() {
    return EmptyItemContainer.INSTANCE;
  }

  public ItemContainer getItemContainer(Player playerComponent, UUID player) {
    ItemContainer newContainer = new SimpleItemContainer(this.capacity);
    if ("".equals(droplist) || droplist == null || droplist.isEmpty()) {
      playerComponent.sendMessage(
          Message.translation("general.Noobanidus_Lootr.NoDropList").bold(true).color(Color.red)
      );
    }
    if (playerContainers.putIfAbsent(player, newContainer) == null) {
      newContainer.registerChangeEvent(EventPriority.LAST, this::onItemChange);
      TemporaryContainerState temp = new TemporaryContainerState(newContainer);
      StashPlugin.stash(temp, false);
      return newContainer;
    } else {
      return playerContainers.get(player);
    }
  }

/*  @Override
  public void tick(float tick, int index, ArchetypeChunk<ChunkStore> archetype, Store<ChunkStore> store, CommandBuffer<ChunkStore> commandBuffer) {
    Vector3d vector3d = this.getCenteredBlockPosition();
    var entityStore = commandBuffer.getExternalData().getWorld().getEntityStore().getStore();
    var spatialresource = entityStore
        .getResource(
        EntityModule.get().getPlayerSpatialResourceType()
    );
    ObjectList<Ref<EntityStore>> objectlist = SpatialResource.getThreadLocalReferenceList();
    spatialresource.getSpatialStructure().collect(vector3d, 75.0, objectlist);
    ParticleUtil.spawnParticleEffect("Dust_Sparkles_Fine", vector3d, objectlist, entityStore);
  }*/

  // This monstrosity allows us to reuse `StashPlugin::stash` without cloning it
  // TODO: If at any point StashPlugin is adjusted and tries to access other methods of ItemContainerState, this will most likely break as they'll end up being null.
  private class TemporaryContainerState extends ItemContainerState {
    private final ItemContainer container;

    public TemporaryContainerState(ItemContainer container) {
      this.container = container;
    }

    @Override
    public ItemContainer getItemContainer() {
      return container;
    }

    @NullableDecl
    @Override
    public String getDroplist() {
      return ItemLootContainerState.this.getDroplist();
    }

    @Override
    public void setDroplist(@NullableDecl String droplist) {
      ItemLootContainerState.this.setDroplist(droplist);
    }

    @NonNullDecl
    @Override
    public Vector3i getPosition() {
      return ItemLootContainerState.this.getPosition();
    }

    @Override
    public int getBlockX() {
      return ItemLootContainerState.this.getBlockX();
    }

    @Override
    public int getBlockY() {
      return ItemLootContainerState.this.getBlockY();
    }

    @Override
    public int getBlockZ() {
      return ItemLootContainerState.this.getBlockZ();
    }

    @NonNullDecl
    @Override
    public Vector3i getBlockPosition() {
      return ItemLootContainerState.this.getBlockPosition();
    }

    @NonNullDecl
    @Override
    public Vector3d getCenteredBlockPosition() {
      return ItemLootContainerState.this.getCenteredBlockPosition();
    }

    @NullableDecl
    @Override
    public WorldChunk getChunk() {
      return ItemLootContainerState.this.getChunk();
    }

    @NullableDecl
    @Override
    public BlockType getBlockType() {
      return ItemLootContainerState.this.getBlockType();
    }

    @Override
    public int getRotationIndex() {
      return ItemLootContainerState.this.getRotationIndex();
    }
  }

  public static ItemLootContainerState fromContainerState(String originalBlockName, ItemContainerState state) {
    if (state instanceof ItemLootContainerState lootContainerState) {
      return lootContainerState;
    }

    var newState = CODEC.decode(new BsonDocument());
    if (newState == null) {
      throw new RuntimeException();
    }
    // TODO: This initialize will override the capacity of `state`
    newState.setOriginalBlock(originalBlockName);
    newState.initialize(LootrPlugin.getLootrChestBlockType());
    newState.droplist = state.getDroplist();
    return newState;
  }
}
