package de.joekawum.punishCore.commands.report;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.joekawum.pluginCore.PluginCore;
import de.joekawum.punishCore.manager.report.Report;
import de.joekawum.punishCore.manager.report.ReportManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.Optional;
import java.util.UUID;

public class ReportInfoCommand implements SimpleCommand {

    private final ProxyServer proxyServer;

    public ReportInfoCommand(ProxyServer proxyServer) {
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
                LinkedList<Report> reports = ReportManager.reportCache.get(uuid);
                for (Report report : reports) {
                    if(report.getId().equals(id)) {
                        sendReportInfo(player, uuid);
                        return;
                    }
                }
            }
            player.sendMessage(Component.text("§cInvalid report-id!"));
        } else
            player.sendMessage(Component.text("§cBitte benutze: §7/reportinfo <id>"));
    }

    private void sendReportInfo(Player player, UUID uuid) {
        LinkedList<Report> reports = ReportManager.reportCache.get(uuid);
        Optional<Player> optionalPlayer = proxyServer.getPlayer(uuid);
        if(optionalPlayer.isEmpty() || !optionalPlayer.isPresent()) {
            try {
                String username = PluginCore.instance().uuidFetcher().getUsername(uuid);
                player.sendMessage(Component.text("§7Gemeldeter Spieler: §c" + username));
            } catch (SQLException e) {
                player.sendMessage(Component.text("§cFehler beim suchen der Spielerdaten! '" + uuid.toString() + "'"));
                throw new RuntimeException(e);
            }
        }
        Player suspect = optionalPlayer.get();
        if(!suspect.isActive())
            player.sendMessage(Component.text("§7Gemeldeter Spieler: §c" + suspect.getUsername()));
        else
            player.sendMessage(Component.text("§7Gemeldeter Spieler: §a" + suspect.getUsername()));

        String reason = reports.getFirst().getReason().getName();
        for (Report report : reports) {
            if(!reason.contains(report.getReason().getName()))
                reason += "," + report.getReason().getName();
        }
        if(reason.split(",").length > 1)
            player.sendMessage(Component.text("§7Gründe: §e" + String.join(", ", reason.split(","))));
        else
            player.sendMessage(Component.text("§7Grund: §e" + reason));

        Player sender = reports.getFirst().getSender();
        if(sender != null) {
            if (!sender.isActive())
                player.sendMessage(Component.text("§7Gemeldet von: §c" + sender.getUsername() + (reports.size() > 1 ? " §7§o(" + reports.size() + ")" : "")));
            else
                player.sendMessage(Component.text("§7Gemeldet von: §a" + sender.getUsername() + (reports.size() > 1 ? " §7§o(" + reports.size() + ")" : "")));
        }

        player.sendMessage(Component.text("§7Gemeldet am: §e" + new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(reports.getFirst().getTimestamp()) + " Uhr"));

        player.sendMessage(Component.text("§7ID: §c" + reports.getFirst().getId()));

        player.sendMessage(Component.text("§4§lREPORT §8>> ")
                .append(Component.text("§7[§aTELEPORT§7]")
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/reportteleport " + reports.getFirst().getId())).hoverEvent(HoverEvent.showText(Component.text("§a§oclick to teleport")))).append(Component.text(" "))
                .append(Component.text("§7[§cDELETE§7]")
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/reportdeny "+ reports.getFirst().getId()))
                        .hoverEvent(HoverEvent.showText(Component.text("§c§oclick to delete")))));
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("blockheaven.punish.manage");
    }
}
