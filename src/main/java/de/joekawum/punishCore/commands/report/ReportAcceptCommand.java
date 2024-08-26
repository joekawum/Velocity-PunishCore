package de.joekawum.punishCore.commands.report;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.joekawum.punishCore.manager.report.Report;
import de.joekawum.punishCore.manager.report.ReportManager;
import net.kyori.adventure.text.Component;

import java.util.Optional;
import java.util.UUID;

public class ReportAcceptCommand implements SimpleCommand {

    private final ProxyServer proxyServer;

    public ReportAcceptCommand(ProxyServer proxyServer) {
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
                        player.sendMessage(Component.text("§aReport angenommen! §7§oBitte vergewissere dich, den Spieler zu bestrafen, da das vom System NICHT übernommen wird!"));

                        ReportManager.reportCache.get(uuid).forEach(r -> {
                            Optional<Player> optionalPlayer = this.proxyServer.getPlayer(r.getSender());
                            if(optionalPlayer.isPresent() && !optionalPlayer.isEmpty()) {
                                Player sender = optionalPlayer.get();
                                if (sender != null && sender.isActive())
                                    sender.sendMessage(Component.text("§aDein Report wurde angenommen und der Spieler bestraft §7(§e" + r.getId() + "§7)§a. Vielen Dank für deine Meldung!"));
                            }
                        });

                        ReportManager.reportCache.remove(uuid);

                        // TODO: 14.08.24 improve and finish
                        return;
                    }
                }
            }
            player.sendMessage(Component.text("§cInvalid report-id!"));
        } else
            player.sendMessage(Component.text("§cBitte benutze: §7/reportaccept <id>"));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("blockheaven.punish.manage");
    }
}
