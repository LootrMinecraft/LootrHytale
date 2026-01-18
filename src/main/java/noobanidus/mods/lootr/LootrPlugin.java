package noobanidus.mods.lootr;

import com.hypixel.hytale.builtin.blockspawner.BlockSpawnerEntry;
import com.hypixel.hytale.builtin.blockspawner.BlockSpawnerTable;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateModule;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import noobanidus.mods.lootr.cursed.TransformedBlockSpawnerEntry;
import noobanidus.mods.lootr.state.ItemLootContainerState;

import javax.annotation.Nonnull;

@SuppressWarnings("removal")
public class LootrPlugin extends JavaPlugin {
  public static final String LOOT_CHEST_ID = "Noobanidus_Lootr_LootChest";
  private static ComponentType<ChunkStore, ItemContainerState> ITEM_CONTAINER_COMPONENT_TYPE = null;
  private static ComponentType<ChunkStore, ItemLootContainerState> ITEM_LOOT_CONTAINER_COMPONENT_TYPE = null;
  private static BlockType LOOTR_CHEST_BLOCK_TYPE = null;

  public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  public LootrPlugin(@Nonnull JavaPluginInit init) {
    super(init);
  }

  @SuppressWarnings("removal")
  @Override
  protected void setup() {
    super.setup();
    this.getBlockStateRegistry()
        .registerBlockState(ItemLootContainerState.class, LOOT_CHEST_ID, ItemLootContainerState.CODEC, ItemContainerState.ItemContainerStateData.class, ItemContainerState.ItemContainerStateData.CODEC);
    this.getChunkStoreRegistry().registerSystem(new BlockSpawnerPrePlugin());
  }

  public static ComponentType<ChunkStore, ItemContainerState> getContainerType() {
    if (ITEM_CONTAINER_COMPONENT_TYPE == null) {
      ITEM_CONTAINER_COMPONENT_TYPE = BlockStateModule.get().getComponentType(ItemContainerState.class);
    }
    return ITEM_CONTAINER_COMPONENT_TYPE;
  }

  public static ComponentType<ChunkStore, ItemLootContainerState> getLootContainerType() {
    if (ITEM_LOOT_CONTAINER_COMPONENT_TYPE == null) {
      ITEM_LOOT_CONTAINER_COMPONENT_TYPE = BlockStateModule.get().getComponentType(ItemLootContainerState.class);
    }
    return ITEM_LOOT_CONTAINER_COMPONENT_TYPE;
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
    // TODO: Block set checks
    var comp = entry.getBlockComponents().getComponent(getContainerType());
    if (comp == null || comp instanceof ItemLootContainerState) {
      return false;
    }
    return comp.getDroplist() != null;
  }

  public static boolean canWrap(BlockSpawnerTable table) {
    for (BlockSpawnerEntry entry : table.getEntries().internalKeys()) {
      if (!canWrap(entry)) {
        return false;
      }
    }

    return true;
  }


}