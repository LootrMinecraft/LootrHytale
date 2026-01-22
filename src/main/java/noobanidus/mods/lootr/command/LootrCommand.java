package noobanidus.mods.lootr.command;

import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.commands.block.SimpleBlockCommand;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import noobanidus.mods.lootr.LootrPlugin;
import noobanidus.mods.lootr.state.ItemLootContainerState;
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
      // TODO: Handle combined chests?
      // TODO: Handle rotation?
      BlockState state = chunk.getState(x, y, z);
      if (state instanceof ItemContainerState itemContainerState) {
        var copy = itemContainerState.getItemContainer().clone();
        itemContainerState.getItemContainer().clear();
        chunk.setBlock(x, y, z, LootrPlugin.get().getLootrChestBlockType());
        if (chunk.getState(x, y, z) instanceof ItemLootContainerState lootContainerState) {
          lootContainerState.setDroplist(null);
          lootContainerState.setOriginalBlock(type.getId());
          lootContainerState.setTemplate(copy);
          if (type.getState() instanceof ItemContainerState.ItemContainerStateData stateData) {
            lootContainerState.setCapacity(stateData.getCapacity());
          } else {
            lootContainerState.setCapacity((short) Math.max(18, copy.getCapacity()));
          }
          commandsender.sendMessage(
              Message.translation("commands.lootr.custom.success").param("x", x).param("y", y).param("z", z)
          );
        } else {
          commandsender.sendMessage(
              Message.translation("commands.lootr.custom.failure_to_replace").param("x", x).param("y", y).param("z", z)
          );
        }
      } else {
        commandsender.sendMessage(
            Message.translation("commands.lootr.custom.failure_invalid_state").param("x", x).param("y", y).param("z", z).param("type", type.getId())
        );
      }
    }
  }
}
