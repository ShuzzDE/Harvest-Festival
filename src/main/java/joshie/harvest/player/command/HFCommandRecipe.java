package joshie.harvest.player.command;

import joshie.harvest.api.cooking.Recipe;
import joshie.harvest.core.HFTrackers;
import joshie.harvest.core.commands.AbstractHFCommand;
import joshie.harvest.core.commands.HFCommand;
import joshie.harvest.player.PlayerTrackerServer;
import joshie.harvest.player.tracking.TrackingServer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;

@HFCommand
@SuppressWarnings("unused")
public class HFCommandRecipe extends AbstractHFCommand {
    @Override
    public String getCommandName() {
        return "recipe";
    }

    @Override
    public String getUsage() {
        return "/hf recipe [player] <value|all|clear>";
    }

    @Override
    public boolean execute(MinecraftServer server, ICommandSender sender, String[] parameters) {
        if (parameters != null && (parameters.length == 1 || parameters.length == 2)) {
            try {
                EntityPlayerMP player = parameters.length == 1? CommandBase.getCommandSenderAsPlayer(sender) : CommandBase.getPlayer(server, sender, parameters[0]);
                TrackingServer tracking = HFTrackers.<PlayerTrackerServer>getPlayerTrackerFromPlayer(player).getTracking();
                String recipe = parameters[parameters.length - 1];
                switch (recipe) {
                    case "all":
                        Recipe.REGISTRY.values().forEach(tracking::learnRecipe);
                        tracking.sync(player);
                        return true;
                    case "clear":
                        tracking.learnRecipe(null);
                        tracking.sync(player);
                        return true;
                    default:
                        if (!recipe.contains(":")) recipe = "harvestfestival:" + recipe;
                        tracking.learnRecipe(Recipe.REGISTRY.get(new ResourceLocation(recipe)));
                        tracking.sync(player);
                        return true;
                }
            } catch (NumberFormatException | PlayerNotFoundException ignored) {}
        }

        return false;
    }
}
