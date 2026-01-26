package noobanidus.mods.lootr.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.command.commands.player.inventory.ItemStateCommand;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;

public class LootrConfig {
  public static final BuilderCodec<LootrConfig> CODEC = BuilderCodec.builder(LootrConfig.class, LootrConfig::new)
      .append(
          new KeyedCodec<>("Enable_Break", BuilderCodec.BOOLEAN),
          (config, o) -> config.enableBreak = o,
          (config) -> config.enableBreak
      )
      .documentation("If true, Loot Chests can be broken by anyone. If false, Loot Chests can only be broken while crouching. [default: false]")
      .add()
      .append(
          new KeyedCodec<>("Disable_Break", BuilderCodec.BOOLEAN),
          (config, o) -> config.disableBreak = o,
          (config) -> config.disableBreak
      )
      .documentation("If true, Loot Chests can only be broken in Creative Mode while crouching. [default: false]")
      .add()
      .append(
          new KeyedCodec<>("Minimum_Capacity", BuilderCodec.INTEGER),
          (config, o) -> config.minimumCapacity = o,
          (config) -> config.minimumCapacity
      )
      .documentation("The minimum capacity an ItemContainerState must have to be converted. [default: 2]")
      .add()
      .build();

  private boolean enableBreak = false;
  private boolean disableBreak = false;
  private int minimumCapacity = 2;

  public boolean isBreakEnabled () {
    return enableBreak;
  }

  public boolean isBreakDisabled () {
    return disableBreak;
  }

  public int getMinimumCapacity() {
    return minimumCapacity;
  }

  public boolean canBeConverted (ItemContainerState state) {
    return canBeConverted(state.getItemContainer());
  }

  public boolean canBeConverted (ItemContainer container) {
    return container.getCapacity() >= this.minimumCapacity;
  }
}
