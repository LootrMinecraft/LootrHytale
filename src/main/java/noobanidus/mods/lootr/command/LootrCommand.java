package noobanidus.mods.lootr.command;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.commands.block.SimpleBlockCommand;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import noobanidus.mods.lootr.LootrPlugin;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class LootrCommand extends AbstractCommandCollection {
  public LootrCommand() {
    super("lootr", "commands.lootr.description");
    this.setPermissionGroup(GameMode.Creative);
    this.addSubCommand(new LootrCustomCommand());
  }

  @SuppressWarnings("removal")
  public static class LootrCustomCommand extends SimpleBlockCommand {

    public LootrCustomCommand() {
      super("custom", "commands.lootr.custom.description");
    }

    @Override
    protected void executeWithBlock(@NonNullDecl CommandContext context, @NonNullDecl WorldChunk chunk, int x, int y, int z) {
      CommandSender commandsender = context.sender();
      BlockType type = chunk.getBlockType(x, y, z);
      if (type == null || type.getId().equals(LootrPlugin.LOOT_CHEST_ID)) {
        return;
      }

      Store<ChunkStore> store = chunk.getWorld().getChunkStore().getStore();
      BlockComponentChunk blockComponentChunk = store.getComponent(chunk.getReference(), BlockComponentChunk.getComponentType());
      if (blockComponentChunk == null) {
        commandsender.sendMessage(
            Message.translation("commands.lootr.custom.failure_invalid_state")
                .param("x", x).param("y", y).param("z", z).param("type", type.getId())
        );
        return;
      }

      int slotIndex = ChunkUtil.indexBlockInColumn(x, y, z);
      ItemContainerBlock itemcontainerblock = null;
      Ref<ChunkStore> blockEntityRef = blockComponentChunk.getEntityReference(slotIndex);
      if (blockEntityRef != null) {
        itemcontainerblock = blockEntityRef.getStore()
            .getComponent(blockEntityRef, ItemContainerBlock.getComponentType());
      } else {
        Holder<ChunkStore> existingHolder = blockComponentChunk.getEntityHolder(slotIndex);
        if (existingHolder != null) {
          itemcontainerblock = existingHolder.getComponent(ItemContainerBlock.getComponentType());
        }
      }

      if (itemcontainerblock == null) {
        commandsender.sendMessage(
            Message.translation("commands.lootr.custom.failure_invalid_state")
                .param("x", x).param("y", y).param("z", z).param("type", type.getId())
        );
        return;
      }

      var template = itemcontainerblock.getItemContainer().clone();
      itemcontainerblock.getItemContainer().clear();

      RotationTuple rotation = chunk.getRotation(x, y, z);
      boolean placed = chunk.placeBlock(x, y, z, LootrPlugin.LOOT_CHEST_ID, rotation, 0, false);
      if (!placed) {
        commandsender.sendMessage(
            Message.translation("commands.lootr.custom.failure_to_replace")
                .param("x", x).param("y", y).param("z", z)
        );
        return;
      }

      Ref<ChunkStore> newEntityRef = chunk.getBlockComponentEntity(x, y, z);
      if (newEntityRef == null) {
        commandsender.sendMessage(
            Message.translation("commands.lootr.custom.failure_to_replace")
                .param("x", x).param("y", y).param("z", z)
        );
        return;
      }

      var newBlock = store.getComponent(newEntityRef, LootrPlugin.get().getLootContainerType());
      if (newBlock == null) {
        commandsender.sendMessage(
            Message.translation("commands.lootr.custom.failure_to_replace")
                .param("x", x).param("y", y).param("z", z)
        );
        return;
      }

      newBlock.setDroplist(null);
      newBlock.setOriginalBlock(type.getId());
      newBlock.setTemplate(template);
      newBlock.setCapacity((short) Math.max(18, template.getCapacity()));

      commandsender.sendMessage(
          Message.translation("commands.lootr.custom.success")
              .param("x", x).param("y", y).param("z", z)
      );
    }
  }
}