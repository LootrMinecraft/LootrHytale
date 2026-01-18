package noobanidus.mods.lootr.state;

import com.hypixel.hytale.builtin.adventure.stash.StashPlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.player.windows.WindowManager;
import com.hypixel.hytale.server.core.inventory.container.EmptyItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import noobanidus.mods.lootr.LootrPlugin;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

@SuppressWarnings({"removal", "deprecation"})
public class ItemLootContainerState extends ItemContainerState implements ItemLootContainerBlockState {
  public static final Codec<ItemLootContainerState> CODEC = BuilderCodec.builder(
          ItemLootContainerState.class, ItemLootContainerState::new, ItemContainerState.BASE_CODEC
      )
      .addField(new KeyedCodec<>("Capacity", Codec.SHORT), (state, o) -> state.capacity = o, (state) -> state.capacity)
      .addField(new KeyedCodec<>("Custom", Codec.BOOLEAN), (state, o) -> state.custom = o, state -> state.custom)
      .addField(new KeyedCodec<>("AllowViewing", Codec.BOOLEAN), (state, o) -> state.allowViewing = o, state -> state.allowViewing)
      .addField(new KeyedCodec<>("Droplist", Codec.STRING), (state, o) -> state.droplist = o, state -> state.droplist)
      .addField(
          new KeyedCodec<>("Marker", WorldMapManager.MarkerReference.CODEC),
          (state, o) -> state.marker = o,
          state -> state.marker
      )
      .addField(
          new KeyedCodec<>("PlayerContainers",
              new MapCodec<>(ItemContainer.CODEC, ConcurrentHashMap::new)),
          (state, o) -> {
            // Transform from String to UUID
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
            // Transform from UUID to String
            HashMap<String, ItemContainer> temp = new HashMap<>();
            for (Map.Entry<UUID, ItemContainer> entry : state.playerContainers.entrySet()) {
              temp.put(entry.getKey().toString(), entry.getValue());
            }
            return temp;
          }
      )
      .build();
  protected Map<UUID, ItemContainer> playerContainers = new ConcurrentHashMap<>();
  protected short capacity;

  @Override
  public boolean initialize(@Nonnull BlockType blockType) {
    var oldCustom = this.custom;
    this.custom = true;
    var result = super.initialize(blockType);
    this.custom = oldCustom;
    if (!result) {
      return false;
    }

    this.capacity = 20;
    if (blockType.getState() instanceof ItemContainerState.ItemContainerStateData itemContainerStateData) {
      this.capacity = itemContainerStateData.getCapacity();
    }

    return true;
  }

  public boolean canOpen
      (@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
    return true;
  }

  public void onOpen(@Nonnull Ref<EntityStore> ref, @Nonnull World world, @Nonnull Store<EntityStore> store) {
  }

  @Override
  public void onDestroy() {
    WindowManager.closeAndRemoveAll(this.getWindows());
    /*    WorldChunk chunk = this.getChunk();*/
    /*    World world = chunk.getWorld();*/
    /*    Store<EntityStore> store = world.getEntityStore().getStore();*/
/*    List<ItemStack> allItemStacks = this.itemContainer.dropAllItemStacks();
    Vector3d dropPosition = this.getBlockPosition().toVector3d().add(0.5, 0.0, 0.5);
    Holder<EntityStore>[] itemEntityHolders = ItemComponent.generateItemDrops(store, allItemStacks, dropPosition, Vector3f.ZERO);
    if (itemEntityHolders.length > 0) {
      world.execute(() -> store.addEntities(itemEntityHolders, AddReason.SPAWN));
    }*/

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
    // Don't wipe the drop list under any circumstances
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

  public void onItemChange(ItemContainer.ItemContainerChangeEvent event) {
    this.markNeedsSave();
  }

  @Override
  public ItemContainer getItemContainer(UUID player) {
    ItemContainer newContainer = new SimpleItemContainer(this.capacity);
    if (playerContainers.putIfAbsent(player, newContainer) == null) {
      newContainer.registerChangeEvent(EventPriority.LAST, this::onItemChange);
      TemporaryContainerState temp = new TemporaryContainerState(newContainer);
      StashPlugin.stash(temp, false);
      return newContainer;
    } else {
      return playerContainers.get(player);
    }
  }

  // This allows us to just reuse `StashPlugin` without duplicating too much code.
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
  }
}
