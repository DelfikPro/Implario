package net.minecraft.command.server;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

public class CommandSaveOff extends CommandBase {

	/**
	 * Gets the name of the command
	 */
	public String getCommandName() {
		return "save-off";
	}

	/**
	 * Gets the usage string for the command.
	 */
	public String getCommandUsage(ICommandSender sender) {
		return "commands.save-off.usage";
	}

	/**
	 * Callback when the command is invoked
	 */
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
		MinecraftServer minecraftserver = MinecraftServer.getServer();
		boolean flag = false;
		for (WorldServer world : minecraftserver.getWorlds()) {
			if (world == null) continue;

			if (!world.disableLevelSaving) {
				world.disableLevelSaving = true;
				flag = true;
			}
		}

		if (flag) {
			notifyOperators(sender, this, "commands.save.disabled");
		} else {
			throw new CommandException("commands.save-off.alreadyOff");
		}
	}

}
