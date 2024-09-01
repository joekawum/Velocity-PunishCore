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

import java.util.LinkedList;
import java.util.Optional;

public class ReportCommand implements SimpleCommand {

    private final ProxyServer proxyServer;

    public ReportCommand(ProxyServer proxyServer) {
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
        if(args.length == 2) {
            Optional<Player> optionalPlayer = proxyServer.getPlayer(args[0]);
            if(optionalPlayer.isEmpty() || !optionalPlayer.isPresent()) {
                player.sendMessage(Data.text("§cDer Spieler wurde nicht gefunden!"));
                return;
            }
            Player suspect = optionalPlayer.get();
            if(suspect.getUsername().equals(player.getUsername())) {
                player.sendMessage(Data.text("§cDu kannst dich nicht selbst melden!"));
                return;
            }

            if(ReportManager.reportCache.containsKey(suspect.getUniqueId())) {
                LinkedList<Report> reports = ReportManager.reportCache.get(suspect.getUniqueId());
                for (Report report : reports) {
                    Optional<Player> op2 = this.proxyServer.getPlayer(report.getSender());
                    if(op2.isPresent() && !op2.isEmpty()) {
                        Player sender = op2.get();
                        if (sender.getUsername().equals(player.getUsername())) {
                            player.sendMessage(Data.text("§7Du hast den Spieler bereits gemeldet!"));
                            return;
                        }
                    }
                }
            }

            for (ReportManager.Reasons value : ReportManager.Reasons.values()) {
                if(args[1].equalsIgnoreCase(value.getName()) | args[1].equalsIgnoreCase("" + value.getId())) {
                    Optional<ServerConnection> currentServer = player.getCurrentServer();
                    String serverString = "PROXY";
                    if(!currentServer.isEmpty() && currentServer.isPresent())
                        serverString = currentServer.get().getServerInfo().getName();
                    Report report = new Report(player.getUniqueId(), suspect.getUniqueId(), value, serverString, System.currentTimeMillis());
                    player.sendMessage(Data.text("§aDein Report wurde erstellt §7(§e" + report.getId() + "§7)§a. Es wird sich in Kürze darum gekümmert!"));

                    for (Player proxiedPlayer : ReportManager.reportNotify) {
                        proxiedPlayer.sendMessage(Component.text("§4§lREPORT §8>> §e" + report.getReason().getName() + " §8>> §e" + report.getServer() + " §8>> " + (suspect.isActive() ? "§a" : "§c") + suspect.getUsername() + " §8[§7" + report.getId() + "§8]"));

                        proxiedPlayer.sendMessage(Component.text("§4§lREPORT §8>> ")
                                .append(Component.text("§7[§bDETAILS§7]")
                                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/reportinfo " + report.getId()))
                                        .hoverEvent(HoverEvent.showText(Component.text("§7§oclick for more details"))))
                                .append(Component.text(" "))
                                .append(Component.text("§7[§aTELEPORT§7]")
                                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/reportteleport " + report.getId()))
                                        .hoverEvent(HoverEvent.showText(Component.text("§7§oclick to teleport"))))
                        );
                    }
                    return;
                }
            }
            player.sendMessage(Data.text("§cBitte gebe eine richtige Report-ID an!"));
        } else {
            player.sendMessage(Data.text("§cReport Gründe:"));
            for (ReportManager.Reasons value : ReportManager.Reasons.values()) {
                player.sendMessage(Data.text("§7- §e" + value.getId() + " §7| §e" + value.getName()));
            }
            player.sendMessage(Data.text("§7Verwende §c/report <Spieler> <Id>"));
        }
    }
}
