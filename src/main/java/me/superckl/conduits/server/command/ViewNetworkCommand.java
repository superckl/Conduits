package me.superckl.conduits.server.command;

import java.util.Collection;

import com.mojang.brigadier.context.CommandContext;

import me.superckl.conduits.common.block.ConduitBlockEntity;
import me.superckl.conduits.conduit.ConduitType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ForgeMod;

public class ViewNetworkCommand {

	public static int execute(final CommandContext<CommandSourceStack> sender) {
		if(sender.getSource().getEntity() instanceof final Player player) {
			final BlockHitResult hit = player.level.clip(new ClipContext(player.getEyePosition(),
					player.getEyePosition().add(player.getLookAngle().scale(player.getAttributeValue(ForgeMod.REACH_DISTANCE.get()))),
					ClipContext.Block.VISUAL, ClipContext.Fluid.NONE,  player));
			if(hit != null && player.level.getBlockEntity(hit.getBlockPos()) instanceof final ConduitBlockEntity conduit) {
				sender.getSource().sendSuccess(new TranslatableComponent("conduits.command.network.conduit", conduit.getBlockPos()), true);
				final Collection<ConduitType<?>> types = conduit.getConnections().getTypes();
				if(types.isEmpty())
					sender.getSource().sendFailure(new TranslatableComponent("conduits.command.network.type.none"));
				else
					types.forEach(type -> conduit.getNetwork(type).ifPresentOrElse(network -> {
						sender.getSource().sendSuccess(new TranslatableComponent("conduits.command.network.type",
								type, network), true);
					}, () -> sender.getSource().sendFailure(new TranslatableComponent("conduits.command.network.none", type))));
				return 1;
			}
		}
		return 0;
	}

}
