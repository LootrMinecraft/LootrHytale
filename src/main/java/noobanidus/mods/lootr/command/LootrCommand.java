package noobanidus.mods.lootr.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.commands.block.SimpleBlockCommand;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import noobanidus.mods.lootr.LootrPlugin;
import noobanidus.mods.lootr.block.ItemLootContainerBlock;
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
      Ref<ChunkStore> entityRef = chunk.getBlockComponentEntity(x, y, z);
      if (entityRef == null) {
        commandsender.sendMessage(
            Message.translation("commands.lootr.custom.failure_invalid_state").param("x", x).param("y", y).param("z", z)
                .param("type", type.getId())
        );
        return; // ???
      }

      // TODO: Is this correct?
      var store = chunk.getWorld().getChunkStore();

      ItemContainerBlock itemcontainerblock = store.getStore()
          .getComponent(entityRef, ItemContainerBlock.getComponentType());
      if (itemcontainerblock == null) {
        commandsender.sendMessage(
            Message.translation("commands.lootr.custom.failure_invalid_state").param("x", x).param("y", y).param("z", z)
                .param("type", type.getId())
        );
        return; // TODO: I had some message for this
      }
      // This creates the template
      var template = itemcontainerblock.getItemContainer().clone();
      // This clears the original chest
      itemcontainerblock.getItemContainer().clear();
      var rotation = chunk.getRotationIndex(x, y, z);
      chunk.setBlock(x, y, z, BlockType.getAssetMap().getIndex(LootrPlugin.LOOT_CHEST_ID), LootrPlugin.get()
          .getLootrChestBlockType(), rotation, 0, 0);

      var newBlock = store.getStore().getComponent(entityRef, ItemLootContainerBlock.getLootComponentType());
      if (newBlock == null) {
        commandsender.sendMessage(
            Message.translation("commands.lootr.custom.failure_to_replace").param("x", x).param("y", y).param("z", z)
        );
        return; // TODO: Something something message here
      }
      newBlock.setDroplist(null);
      newBlock.setOriginalBlock(type.getId());
      newBlock.setTemplate(template);
      newBlock.setCapacity((short) Math.max(18, template.getCapacity()));
      commandsender.sendMessage(
          Message.translation("commands.lootr.custom.success").param("x", x).param("y", y).param("z", z)
      );
    }
  }
}