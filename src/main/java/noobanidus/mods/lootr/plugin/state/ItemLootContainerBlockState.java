package noobanidus.mods.lootr.plugin.state;

import com.hypixel.hytale.server.core.inventory.container.ItemContainer;

import java.util.UUID;

public interface ItemLootContainerBlockState {
  ItemContainer getItemContainer (UUID player);
}
