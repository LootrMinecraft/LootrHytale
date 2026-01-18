package noobanidus.mods.lootr.state;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerBlockWindow;
import com.hypixel.hytale.server.core.entity.entities.player.windows.WindowManager;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.EmptyItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

@SuppressWarnings({"removal", "deprecation"})
public class ItemLootContainerState extends ItemContainerState implements ItemLootContainerBlockState {
  public static final Codec<ItemLootContainerState> CODEC = BuilderCodec.builder(
          ItemLootContainerState.class, ItemLootContainerState::new, ItemContainerState.BASE_CODEC
      )
      .addField(new KeyedCodec<>("Custom", Codec.BOOLEAN), (state, o) -> state.custom = o, state -> state.custom)
      .addField(new KeyedCodec<>("AllowViewing", Codec.BOOLEAN), (state, o) -> state.allowViewing = o, state -> state.allowViewing)
      .addField(new KeyedCodec<>("Droplist", Codec.STRING), (state, o) -> state.droplist = o, state -> state.droplist)
      .addField(
          new KeyedCodec<>("Marker", WorldMapManager.MarkerReference.CODEC),
          (state, o) -> state.marker = o,
          state -> state.marker
      )
      .addField(
          new KeyedCodec<>("ItemContainer", SimpleItemContainer.CODEC), (state, o) -> state.itemContainer = o, state -> state.itemContainer
      )
      .build();
  private final Map<UUID, ContainerBlockWindow> windows = new ConcurrentHashMap<UUID, ContainerBlockWindow>();
  protected final Map<String, ItemContainer> playerContainers = new ConcurrentHashMap<>();
  protected boolean custom;
  protected boolean allowViewing = true;
  @Nullable
  protected String droplist;
  protected WorldMapManager.MarkerReference marker;

  @Override
  public boolean initialize(@Nonnull BlockType blockType) {
    if (!super.initialize(blockType)) {
      return false;
    } else if (this.custom) {
      return true;
    } else {
      short capacity = 20;
      if (blockType.getState() instanceof ItemContainerState.ItemContainerStateData itemContainerStateData) {
        capacity = itemContainerStateData.getCapacity();
      }

      List<ItemStack> remainder = new ObjectArrayList();
      this.itemContainer = ItemContainer.<SimpleItemContainer>ensureContainerCapacity(this.itemContainer, capacity, SimpleItemContainer::new, remainder);
      this.itemContainer.registerChangeEvent(EventPriority.LAST, this::onItemChange);
      if (!remainder.isEmpty()) {
        WorldChunk chunk = this.getChunk();
        World world = chunk.getWorld();
        Store<EntityStore> store = world.getEntityStore().getStore();
        ((HytaleLogger.Api) HytaleLogger.getLogger().at(Level.WARNING).withCause(new Throwable()))
            .log(
                "Dropping %d excess items from item container: %s at world: %s, chunk: %s, block: %s",
                remainder.size(),
                blockType.getId(),
                chunk.getWorld().getName(),
                chunk,
                this.getPosition()
            );
        Vector3i blockPosition = this.getBlockPosition();
        Holder<EntityStore>[] itemEntityHolders = ItemComponent.generateItemDrops(store, remainder, blockPosition.toVector3d(), Vector3f.ZERO);
        store.addEntities(itemEntityHolders, AddReason.SPAWN);
      }

      return true;
    }
  }

  public boolean canOpen(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
    return true;
  }

  public void onOpen(@Nonnull Ref<EntityStore> ref, @Nonnull World world, @Nonnull Store<EntityStore> store) {
  }

  @Override
  public void onDestroy() {
    WindowManager.closeAndRemoveAll(this.windows);
    WorldChunk chunk = this.getChunk();
    World world = chunk.getWorld();
    Store<EntityStore> store = world.getEntityStore().getStore();
    List<ItemStack> allItemStacks = this.itemContainer.dropAllItemStacks();
    Vector3d dropPosition = this.getBlockPosition().toVector3d().add(0.5, 0.0, 0.5);
    Holder<EntityStore>[] itemEntityHolders = ItemComponent.generateItemDrops(store, allItemStacks, dropPosition, Vector3f.ZERO);
    if (itemEntityHolders.length > 0) {
      world.execute(() -> store.addEntities(itemEntityHolders, AddReason.SPAWN));
    }

    if (this.marker != null) {
      this.marker.remove();
    }
  }

  public void setCustom(boolean custom) {
    this.custom = custom;
    this.markNeedsSave();
  }

  public void setAllowViewing(boolean allowViewing) {
    this.allowViewing = allowViewing;
    this.markNeedsSave();
  }

  public boolean isAllowViewing() {
    return this.allowViewing;
  }

  public void setItemContainer(SimpleItemContainer itemContainer) {
    this.itemContainer = itemContainer;
    this.markNeedsSave();
  }

  @Nullable
  public String getDroplist() {
    return this.droplist;
  }

  public void setDroplist(@Nullable String droplist) {
    this.droplist = droplist;
    this.markNeedsSave();
  }

  @Override
  public void setMarker(WorldMapManager.MarkerReference marker) {
    this.marker = marker;
    this.markNeedsSave();
  }

  @Nonnull
  public Map<UUID, ContainerBlockWindow> getWindows() {
    return this.windows;
  }

  // TODO: This might be problematic for things that actually need the container
  @Override
  public ItemContainer getItemContainer() {
    return EmptyItemContainer.INSTANCE;
  }

  public void onItemChange(ItemContainer.ItemContainerChangeEvent event) {
    this.markNeedsSave();
  }

  @Override
  public ItemContainer getItemContainer(UUID player) {
    return null;
  }
}
