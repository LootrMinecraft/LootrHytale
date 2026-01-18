package noobanidus.mods.lootr;

import com.hypixel.hytale.builtin.blockspawner.BlockSpawnerEntry;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateModule;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import noobanidus.mods.lootr.state.ItemLootContainerState;

import javax.annotation.Nonnull;

public class LootrPlugin extends JavaPlugin {
  public static ComponentType<ChunkStore, ItemContainerState> ITEM_CONTAINER_COMPONENT_TYPE = null;

  public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  public LootrPlugin(@Nonnull JavaPluginInit init) {
    super(init);
  }

  @SuppressWarnings("removal")
  @Override
  protected void setup() {
    super.setup();
    ITEM_CONTAINER_COMPONENT_TYPE = BlockStateModule.get().getComponentType(ItemContainerState.class);
    this.getBlockStateRegistry()
        .registerBlockState(ItemLootContainerState.class, "Noobanidus_Lootr_LootChest", ItemLootContainerState.CODEC, ItemContainerState.ItemContainerStateData.class, ItemContainerState.ItemContainerStateData.CODEC);
    this.getChunkStoreRegistry().registerSystem(new BlockSpawnerPrePlugin());
  }

  public static boolean canWrap(BlockSpawnerEntry entry) {
    var comp = entry.getBlockComponents().getComponent(LootrPlugin.ITEM_CONTAINER_COMPONENT_TYPE);
    if (comp == null) {
      return false;
    }
    return comp.getDroplist() != null;
  }
}