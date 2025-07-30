package wang.stan.flip;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;

public class Flip implements ModInitializer {
	public static final String MOD_ID = "flip";
	private SecureRandom rng;

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public int show(CommandContext<ServerCommandSource> commandContext) {
		ServerPlayerEntity plr = commandContext.getSource().getPlayer();

		if (plr == null) {
			return 1;
		}

		ItemStack item = plr.getInventory().getSelectedStack();
		if (item.isEmpty()) {
			commandContext.getSource().sendError(Text.literal("You must be holding an item to display to the server."));
			return 1;
		}


		MutableText res = plr.getName().copy()
				.append(" is holding " + item.getCount() + "x ")
				.append(item.toHoverableText());

		commandContext.getSource().getServer().getPlayerManager().getPlayerList().forEach(
				(player) -> {
					player.sendMessage(res);
				}
		);

		return 1;
	}

	public int coinFlip(CommandContext<ServerCommandSource> commandContext) {
		ServerPlayerEntity plr = commandContext.getSource().getPlayer();

		if (plr == null) {
			return 1;
		}

		ItemStack item = plr.getInventory().getSelectedStack();
		if (item.isEmpty()) {
			commandContext.getSource().sendError(Text.literal("You must be holding an item to coin flip."));
			return 1;
		}

		boolean success = false;
		int original_amount = item.getCount();

		if (rng.nextBoolean()) {
			plr.giveOrDropStack(item.copy());
			success = true;
		} else {
			plr.getInventory().removeOne(item);
		}

		MutableText res = plr.getName().copy()
						.append(Text.literal(success ? " succeeded" : " failed").formatted(success ? Formatting.GREEN : Formatting.RED, Formatting.BOLD))
						.append(" a coin flip for " + original_amount + "x ")
						.append(item.toHoverableText());

		commandContext.getSource().getServer().getPlayerManager().getPlayerList().forEach(
				(player) -> {
					player.sendMessage(res);
				}
		);

		return 1;
	}

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		LOGGER.info("Hello from flip!");
		this.rng = new SecureRandom();

		CommandRegistrationCallback.EVENT.register(
			(dispatcher, registryAccess, environment) -> {
				dispatcher.register(CommandManager.literal("flip").executes(this::coinFlip));
				dispatcher.register(CommandManager.literal("display").executes(this::show));
			});

		LOGGER.info("flip initialized!");
	}
}