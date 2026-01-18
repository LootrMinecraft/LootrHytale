package noobanidus.mods.lootr.state;

import com.hypixel.hytale.server.core.inventory.container.ItemContainer;

import java.util.UUID;

// TODO: Does this even need to exist?
public interface ItemLootContainerBlockState {
  ItemContainer getItemContainer (UUID player);
}
