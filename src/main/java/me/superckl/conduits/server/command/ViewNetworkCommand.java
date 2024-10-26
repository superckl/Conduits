package me.superckl.conduits.server.command;

import java.util.Collection;

import com.mojang.brigadier.context.CommandContext;

import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.conduit.ConduitType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.NeoForgeMod;

public class ViewNetworkCommand {

	public static int execute(final CommandContext<CommandSourceStack> sender) {
		if(sender.getSource().getEntity() instanceof final Player player) {
			final BlockHitResult hit = player.level().clip(new ClipContext(player.getEyePosition(),
					player.getEyePosition().add(player.getLookAngle().normalize().scale(player.blockInteractionRange())),
					ClipContext.Block.VISUAL, ClipContext.Fluid.NONE,  player));
			if(hit.getType() == HitResult.Type.BLOCK && player.level().getBlockEntity(hit.getBlockPos()) instanceof final ConduitBlockEntity conduit) {
				sender.getSource().sendSuccess(() -> Component.translatable("conduits.command.network.conduit", conduit.getBlockPos()), true);
				final Collection<ConduitType<?>> types = conduit.getConnections().getTypes();
				if(types.isEmpty())
					sender.getSource().sendFailure(Component.translatable("conduits.command.network.type.none"));
				else
					types.forEach(type -> conduit.getNetwork(type).ifPresentOrElse(network -> {
						sender.getSource().sendSuccess(() -> Component.translatable("conduits.command.network.type",
								type, network), true);
					}, () -> sender.getSource().sendFailure(Component.translatable("conduits.command.network.none", type))));
				return 1;
			}
		}
		return 0;
	}

}
