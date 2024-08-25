package de.joekawum.punishCore.commands.report;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.joekawum.punishCore.manager.report.Report;
import de.joekawum.punishCore.manager.report.ReportManager;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class ReportDenyCommand implements SimpleCommand {
    @Override
    public void execute(Invocation invocation) {
        if(!(invocation.source() instanceof Player)) {
            System.out.println("You must be a player!");
            return;
        }
        Player player = (Player) invocation.source();
        String[] args = invocation.arguments();
        if(args.length == 1) {
            String id = args[0];
            for (UUID uuid : ReportManager.reportCache.keySet()) {
                for (Report report : ReportManager.reportCache.get(uuid)) {
                    if(report.getId().equals(id)) {
                        player.sendMessage(Component.text("§cReport abgelehnt!"));

                        ReportManager.reportCache.get(uuid).forEach(r -> {
                            Player sender = r.getSender();
                            if(sender != null && sender.isActive())
                                sender.sendMessage(Component.text("§cDein Report wurde abgelehnt und der Spieler NICHT bestraft §7(§e" + r.getId() + "§7)§a."));
                        });

                        ReportManager.reportCache.remove(uuid);

                        // TODO: 14.08.24 improve and finish
                        return;
                    }
                }
            }
            player.sendMessage(Component.text("§cInvalid report-id!"));
        } else
            player.sendMessage(Component.text("§cBitte benutze: §7/reportdeny <id>"));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("blockheaven.punish.manage");
    }
}
