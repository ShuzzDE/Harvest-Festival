package joshie.harvest.calendar.command;

import joshie.harvest.calendar.CalendarHelper;
import joshie.harvest.calendar.HFCalendar;
import joshie.harvest.core.commands.AbstractHFCommand;
import joshie.harvest.core.commands.HFCommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

@HFCommand
@SuppressWarnings("unused")
public class HFCommandTime extends AbstractHFCommand {
    @Override
    public String getCommandName() {
        return "time";
    }

    @Override
    public String getUsage() {
        return "/hf time <set|add> <value>";
    }

    @Override
    public boolean execute(MinecraftServer server, ICommandSender sender, String[] parameters) {
        if (parameters.length > 1) {
            if (parameters[0].equals("set")) {
                long time = CalendarHelper.getElapsedDays(server.worldServers[0].getWorldTime()) * HFCalendar.TICKS_PER_DAY;
                switch (parameters[1]) {
                    case "day":
                        time += 3000;
                        break;
                    case "night":
                        time += 18000;
                        break;
                    default:
                        time += (parseInt(parameters[1]) - 6000L);
                        break;
                }

                CalendarHelper.setWorldTime(server, time);
                return true;
            }

            if (parameters[0].equals("add")) {
                int l = parseInt(parameters[1]);
                CalendarHelper.setWorldTime(server, server.worldServers[0].getWorldTime() + l);
                return true;
            }
        }

        return false;
    }
}