package noobanidus.mods.lootr.config;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

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
      .add()
      .documentation("If true, Loot Chests can only be broken in Creative Mode while crouching. [default: false]")
      .build();

  private boolean enableBreak = false;
  private boolean disableBreak = false;

  public boolean isBreakEnabled () {
    return enableBreak;
  }

  public boolean isBreakDisabled () {
    return disableBreak;
  }
}
