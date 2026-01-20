package noobanidus.mods.lootr.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.UUIDUtil;
import noobanidus.mods.lootr.LootrPlugin;

import javax.annotation.Nonnull;
import java.util.UUID;

public final class UUIDComponent implements Component<ChunkStore> {
  public static final BuilderCodec<UUIDComponent> CODEC = BuilderCodec.builder(UUIDComponent.class, UUIDComponent::new)
      .append(new KeyedCodec<>("UUID", Codec.UUID_BINARY), (o, i) -> o.uuid = i, o -> o.uuid)
      .addValidator(Validators.nonNull())
      .add()
      .afterDecode(v -> {
        if (v.uuid == null) {
          v.uuid = UUIDUtil.generateVersion3UUID();
        }
      })
      .build();
  private UUID uuid;

  @Nonnull
  public static ComponentType<ChunkStore, UUIDComponent> getComponentType() {
    return LootrPlugin.getUuidComponentType();
  }

  public UUIDComponent(@Nonnull UUID uuid) {
    this.uuid = uuid;
  }

  private UUIDComponent() {
  }

  @Nonnull
  public UUID getUuid() {
    return this.uuid;
  }

  @Nonnull
  @Override
  public Component<ChunkStore> clone() {
    return this;
  }

  @Nonnull
  public static UUIDComponent generateVersion3UUID() {
    return new UUIDComponent(UUIDUtil.generateVersion3UUID());
  }

  @Nonnull
  public static UUIDComponent randomUUID() {
    return new UUIDComponent(UUID.randomUUID());
  }
}

