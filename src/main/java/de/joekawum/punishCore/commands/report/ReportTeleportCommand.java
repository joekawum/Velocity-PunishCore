package de.joekawum.punishCore.commands.report;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import de.joekawum.punishCore.data.Data;
import de.joekawum.punishCore.manager.report.Report;
import de.joekawum.punishCore.manager.report.ReportManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.Optional;
import java.util.UUID;

public class ReportTeleportCommand implements SimpleCommand {

    private final ProxyServer proxyServer;
    private final ReportManager reportManager;

    public ReportTeleportCommand(ProxyServer proxyServer, ReportManager reportManager) {
        this.proxyServer = proxyServer;
        this.reportManager = reportManager;
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
                        Optional<Player> optionalPlayer = proxyServer.getPlayer(report.getSuspect());
                        if(optionalPlayer.isEmpty() || !optionalPlayer.isPresent()) {
                            player.sendMessage(Data.text("§cDer gemeldete Spieler ist nicht mehr online!"));
                            player.sendMessage(Data.text("§cKlicke ")
                                    .append(Component.text("§aHIER")
                                            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/reportdeny " + id))
                                            .hoverEvent(HoverEvent.showText(Component.text("§7§odelete")))
                                    )
                                    .append(Component.text(" §cum den Report zu löschen!")));
                            return;
                        }
                        Player suspect = optionalPlayer.get();
                        String playerServer = "", suspectServer = "";
                        Optional<ServerConnection> playerCurrentServer = player.getCurrentServer();
                        Optional<ServerConnection> suspectCurrentServer = suspect.getCurrentServer();
                        if(playerCurrentServer.isPresent())
                            playerServer = playerCurrentServer.get().getServer().getServerInfo().getName();
                        if(suspectCurrentServer.isPresent())
                            suspectServer = suspectCurrentServer.get().getServer().getServerInfo().getName();
                        if(!playerServer.equals(suspectServer))
                            player.createConnectionRequest(proxyServer.getServer(suspectServer).get()).connect();

                        // TODO: 13.08.24 teleport mechanic paperserver via pl messaging
                        reportManager.sendTeleportData(player, suspect.getUniqueId());

                        player.sendMessage(Data.text("§7Du bist nun auf §e" + suspectServer));

                        player.sendMessage(Data.text("")
                                .append(Component.text("§7[§aACCEPT§7]")
                                                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/reportaccept " + report.getId()))
                                                        .hoverEvent(HoverEvent.showText(Component.text("§a§oclick to accept report"))))
                                .append(Component.text(" "))
                                .append(Component.text("§7[§cDENY§7]")
                                                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/reportdeny " + report.getId()))
                                                .hoverEvent(HoverEvent.showText(Component.text("§c§oclick to deny report")))));
                        player.sendMessage(Data.text("§7§othe player will not be automatically banned if the report has been accepted"));
                        return;
                    }
                }
            }
            player.sendMessage(Data.text("§cBitte gebe eine richtige Report-ID an!"));
        } else
            player.sendMessage(Data.text("§7Verwende §c/reportteleport <id>"));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("blockheaven.punish.manage");
    }
}
