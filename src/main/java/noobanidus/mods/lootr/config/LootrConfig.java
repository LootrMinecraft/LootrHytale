package noobanidus.mods.lootr.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;

@SuppressWarnings("removal")
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
      .append(
          new KeyedCodec<>("Disable_Conversion", BuilderCodec.BOOLEAN),
          (config, o) -> config.disableConversion = o,
          (config) -> config.disableConversion
      )
      .documentation("If true, conversion will not take place. [default: false]")
      .add()
      .build();

  private boolean enableBreak = false;
  private boolean disableBreak = false;
  private boolean disableConversion = false;
  private int minimumCapacity = 2;

  public boolean isBreakEnabled() {
    return enableBreak;
  }

  public boolean isBreakDisabled() {
    return disableBreak;
  }

  public int getMinimumCapacity() {
    return minimumCapacity;
  }

  public boolean canBeConverted(BlockType blockType, ItemContainerBlock block) {
    if (isConversionDisabled()) {
      return false;
    }

    return block.getCapacity() >= this.minimumCapacity;
  }

  public boolean isConversionDisabled() {
    return disableConversion;
  }
}
