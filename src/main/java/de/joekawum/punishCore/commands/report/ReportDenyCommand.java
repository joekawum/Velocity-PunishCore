package de.joekawum.punishCore.commands.report;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.joekawum.punishCore.data.Data;
import de.joekawum.punishCore.manager.report.Report;
import de.joekawum.punishCore.manager.report.ReportManager;

import java.util.Optional;
import java.util.UUID;

public class ReportDenyCommand implements SimpleCommand {

    private final ProxyServer proxyServer;

    public ReportDenyCommand(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
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
            String id = args[0];
            for (UUID uuid : ReportManager.reportCache.keySet()) {
                for (Report report : ReportManager.reportCache.get(uuid)) {
                    if(report.getId().equals(id)) {
                        player.sendMessage(Data.text("§cReport abgelehnt §7(§e" + report.getId() + "§7)§c!"));

                        ReportManager.reportCache.get(uuid).forEach(r -> {
                            Optional<Player> optionalPlayer = this.proxyServer.getPlayer(r.getSender());
                            if(optionalPlayer.isPresent() && !optionalPlayer.isEmpty()) {
                                Player sender = optionalPlayer.get();
                                if (sender != null && sender.isActive())
                                    sender.sendMessage(Data.text("§cDein Report wurde abgelehnt und der Spieler NICHT bestraft §7(§e" + r.getId() + "§7)§a."));
                            }
                        });

                        ReportManager.reportCache.remove(uuid);

                        // TODO: 14.08.24 improve and finish
                        return;
                    }
                }
            }
            player.sendMessage(Data.text("§cBitte gebe eine richtige Report-ID an!"));
        } else
            player.sendMessage(Data.text("§7Verwende §c/reportdeny <id>"));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("blockheaven.punish.manage");
    }
}
