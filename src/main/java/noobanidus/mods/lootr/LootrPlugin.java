package noobanidus.mods.lootr;

import com.hypixel.hytale.builtin.blockspawner.BlockSpawnerEntry;
import com.hypixel.hytale.builtin.blockspawner.BlockSpawnerTable;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.common.map.WeightedMap;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateModule;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import noobanidus.mods.lootr.component.UUIDComponent;
import noobanidus.mods.lootr.interaction.OpenLootContainerInteraction;
import noobanidus.mods.lootr.state.ItemLootContainerState;
import noobanidus.mods.lootr.system.BlockBreakEventSystem;
import noobanidus.mods.lootr.system.BlockSpawnerPreSystem;
import noobanidus.mods.lootr.util.ReflectionHelper;
import noobanidus.mods.lootr.util.TransformedBlockSpawnerEntry;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

@SuppressWarnings("removal")
public class LootrPlugin extends JavaPlugin {
  public static final String LOOT_UUID = "Noobanidus_Lootr_LootId";
  public static final String LOOT_CHEST_ID = "Noobanidus_Lootr_LootChest";
  public static final String LOOT_CONTAINER_INTERACTION = "Noobanidus_Lootr_OpenLootContainer";
  private static ComponentType<ChunkStore, ItemLootContainerState> ITEM_LOOT_CONTAINER_COMPONENT_TYPE = null;
  private static ComponentType<ChunkStore, UUIDComponent> UUID_COMPONENT_TYPE = null;
  private static BlockType LOOTR_CHEST_BLOCK_TYPE = null;
  private static final Set<String> WRAPPED_TABLES = ConcurrentHashMap.newKeySet();

  @SuppressWarnings("rawtypes")
  private static BiConsumer<BlockSpawnerTable, IWeightedMap> BLOCK_SPAWNER_ACCESSOR;

  public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  public LootrPlugin(@Nonnull JavaPluginInit init) {
    super(init);
    BLOCK_SPAWNER_ACCESSOR = ReflectionHelper.setter(
        BlockSpawnerTable.class,
        "entries",
        IWeightedMap.class
    );
  }

  public static boolean isWrapped(String blockSpawnerId) {
    return WRAPPED_TABLES.contains(blockSpawnerId);
  }

  @Override
  protected void setup() {
    super.setup();
    this.getBlockStateRegistry()
        .registerBlockState(ItemLootContainerState.class, LOOT_CHEST_ID, ItemLootContainerState.CODEC, ItemContainerState.ItemContainerStateData.class, ItemContainerState.ItemContainerStateData.CODEC);
    this.getChunkStoreRegistry().registerSystem(new BlockSpawnerPreSystem());
    this.getCodecRegistry(Interaction.CODEC)
        .register(LOOT_CONTAINER_INTERACTION, OpenLootContainerInteraction.class, OpenLootContainerInteraction.CODEC);
    this.getEntityStoreRegistry().registerSystem(new BlockBreakEventSystem());
    UUID_COMPONENT_TYPE = this.getChunkStoreRegistry().registerComponent(UUIDComponent.class, LOOT_UUID, UUIDComponent.CODEC);
  }

  private static ComponentType<ChunkStore, ItemContainerState> ITEM_CONTAINER_STATE_COMPONENT_TYPE = null;

  public static ComponentType<ChunkStore, ItemContainerState> getContainerType() {
    if (ITEM_CONTAINER_STATE_COMPONENT_TYPE == null) {
      ITEM_CONTAINER_STATE_COMPONENT_TYPE = BlockStateModule.get()
          .getComponentType(ItemContainerState.class);
    }
    return ITEM_CONTAINER_STATE_COMPONENT_TYPE;
  }

  public static ComponentType<ChunkStore, ItemLootContainerState> getLootContainerType() {
    if (ITEM_LOOT_CONTAINER_COMPONENT_TYPE == null) {
      ITEM_LOOT_CONTAINER_COMPONENT_TYPE = BlockStateModule.get().getComponentType(ItemLootContainerState.class);
    }
    return ITEM_LOOT_CONTAINER_COMPONENT_TYPE;
  }

  public static ComponentType<ChunkStore, UUIDComponent> getUuidComponentType() {
    return Objects.requireNonNull(UUID_COMPONENT_TYPE, "UUID Component Type has not been initialized yet");
  }

  public static BlockType getLootrChestBlockType() {
    if (LOOTR_CHEST_BLOCK_TYPE == null) {
      LOOTR_CHEST_BLOCK_TYPE = BlockType.getAssetMap().getAsset(LOOT_CHEST_ID);
    }
    return LOOTR_CHEST_BLOCK_TYPE;
  }

  public static boolean canWrap(BlockSpawnerEntry entry) {
    if (entry instanceof TransformedBlockSpawnerEntry) {
      return false;
    }
    if (entry.getBlockComponents() == null) {
      return false;
    }
    // It's already a loot container somehow
    if (entry.getBlockComponents().getComponent(getLootContainerType()) != null) {
      return false;
    }
    var comp = entry.getBlockComponents().getComponent(getContainerType());
    if (comp == null) {
      return false;
    }
    if (comp.getDroplist() == null) {
      return false;
    }
    return comp.getDroplist() != null;
  }

  public static void wrapTable(String blockStateId, BlockSpawnerTable table) {
    if (isWrapped(blockStateId)) {
      return;
    }
    List<BlockSpawnerEntry> entries = new ArrayList<>();
    for (BlockSpawnerEntry entry : table.getEntries().internalKeys()) {
      if (canWrap(entry)) {
        var comp = entry.getBlockComponents().clone();
        var state = comp.getComponent(getContainerType());
        comp.removeComponent(getContainerType());
        comp.addComponent(getLootContainerType(), ItemLootContainerState.fromContainerState(entry.getBlockName(), state));
        entries.add(new TransformedBlockSpawnerEntry(entry, LootrPlugin.LOOT_CHEST_ID, comp));
      } else {
        entries.add(entry);
      }
    }
    BLOCK_SPAWNER_ACCESSOR.accept(table, WeightedMap.builder(BlockSpawnerEntry.EMPTY_ARRAY)
        .putAll(entries.toArray(BlockSpawnerEntry[]::new), BlockSpawnerEntry::getWeight).build());
    WRAPPED_TABLES.add(blockStateId);
  }
}