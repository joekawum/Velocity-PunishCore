package de.joekawum.punishCore.commands.report;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.joekawum.punishCore.manager.report.Report;
import de.joekawum.punishCore.manager.report.ReportManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

import java.util.LinkedList;
import java.util.Optional;
import java.util.UUID;

public class ReportListCommand implements SimpleCommand {

    private final ProxyServer proxyServer;

    public ReportListCommand(ProxyServer proxyServer) {
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
        if(ReportManager.reportCache.isEmpty()) {
            player.sendMessage(Component.text("§aEs sind momentan keine Reports offen!"));
            return;
        }

        for (UUID uuid : ReportManager.reportCache.keySet()) {
            LinkedList<Report> reports = ReportManager.reportCache.get(uuid);
            Report first = reports.getFirst();
            Optional<Player> optionalPlayer = proxyServer.getPlayer(uuid);
            if(optionalPlayer.isEmpty() || !optionalPlayer.isPresent()) return;
            Player suspect = optionalPlayer.get();
            player.sendMessage(Component.text(" "));
            player.sendMessage(Component.text("§4§lREPORT §8>> §e" + first.getReason().getName() + " §8>> §e" + first.getServer() + " §8>> " + (suspect.isActive() ? "§a" : "§c") + suspect.getUsername() + " §8[§7" + first.getId() + "§8]"));

            player.sendMessage(Component.text("§4§lREPORT §8>> ")
                    .append(Component.text("§7[§bDETAILS§7]")
                            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/reportinfo " + first.getId()))
                            .hoverEvent(HoverEvent.showText(Component.text("§7§oclick for more details"))))
                    .append(Component.text(" "))
                    .append(Component.text("§7[§aTELEPORT§7]")
                            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/reportteleport " + first.getId()))
                            .hoverEvent(HoverEvent.showText(Component.text("§7§oclick to teleport"))))
            );
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("blockheaven.punish.manage");
    }
}
