package de.joekawum.punishCore.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.joekawum.pluginCore.PluginCore;
import de.joekawum.punishCore.manager.ban.BanManager;
import de.joekawum.punishCore.manager.report.ReportManager;
import net.kyori.adventure.text.Component;

public class NotifyCommand implements SimpleCommand {

    public NotifyCommand() {
        PluginCore.instance().mysql().createTable("Notify", "uuid VARCHAR(36), report BOOLEAN, ban BOOLEAN, mute BOOLEAN");
    }

    @Override
    public void execute(Invocation invocation) {
        if(!(invocation.source() instanceof Player)) {
            System.out.println("You must be a player!");
            return;
        }
        Player player = (Player) invocation.source();
        String[] args = invocation.arguments();
        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("report")) {
                if (ReportManager.reportNotify.contains(player)) {
                    ReportManager.reportNotify.remove(player);
                    player.sendMessage(Component.text("§7Du bekommst nun §cKEINE §7reports mehr gemeldet!"));
                } else {
                    ReportManager.reportNotify.add(player);
                    player.sendMessage(Component.text("§7Du bekommst nun §awieder §7reports gemeldet!"));
                }
            }
            else if(args[0].equalsIgnoreCase("ban")) {
                if(BanManager.banNotify.contains(player)) {
                    BanManager.banNotify.remove(player);
                    player.sendMessage(Component.text("§7Du bekommst nun §cKEINE §7bans mehr gemeldet!"));
                } else {
                    BanManager.banNotify.add(player);
                    player.sendMessage(Component.text("§7Du bekommst nun §awieder §7bans gemeldet!"));
                }
            }
            else
                player.sendMessage(Component.text("§cBitte benutze: §7/notify <report/ban§7§o/mute§7>"));
        } else
            player.sendMessage(Component.text("§cBitte benutze: §7/notify <report/ban§7§o/mute§7>"));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("blockheaven.punish.notify");
    }
}
