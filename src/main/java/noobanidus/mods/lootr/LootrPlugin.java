package noobanidus.mods.lootr;

import com.hypixel.hytale.builtin.blockspawner.BlockSpawnerEntry;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateModule;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import javax.annotation.Nonnull;

/**
 * This class serves as the entrypoint for your plugin. Use the setup method to register into game registries or add
 * event listeners.
 */
public class LootrPlugin extends JavaPlugin {
  public static ComponentType<ChunkStore, ItemContainerState> ITEM_CONTAINER_COMPONENT_TYPE = null;

  private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

  public LootrPlugin(@Nonnull JavaPluginInit init) {
    super(init);
    LOGGER.atInfo().log("Hello from " + this.getName() + " version " + this.getManifest().getVersion().toString());
  }

  @SuppressWarnings("removal")
  @Override
  protected void setup() {
    ITEM_CONTAINER_COMPONENT_TYPE = BlockStateModule.get().getComponentType(ItemContainerState.class);
    this.getChunkStoreRegistry().registerSystem(new BlockSpawnerPrePlugin());
    LOGGER.atInfo().log("Setting up plugin " + this.getName());
    /*        this.getCommandRegistry().registerCommand(new ExampleCommand(this.getName(), this.getManifest().getVersion().toString()));*/
  }

  public static boolean canWrap (BlockSpawnerEntry entry) {
    var comp = entry.getBlockComponents().getComponent(LootrPlugin.ITEM_CONTAINER_COMPONENT_TYPE);
    if (comp == null) {
      return false;
    }
    return comp.getDroplist() != null;
  }
}