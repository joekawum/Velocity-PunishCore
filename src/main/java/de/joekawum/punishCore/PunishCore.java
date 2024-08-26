package de.joekawum.punishCore;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import de.joekawum.pluginCore.PluginCore;
import de.joekawum.punishCore.commands.NotifyCommand;
import de.joekawum.punishCore.commands.ban.BanCommand;
import de.joekawum.punishCore.commands.ban.BanHistoryCommand;
import de.joekawum.punishCore.commands.ban.PardonCommand;
import de.joekawum.punishCore.commands.report.*;
import de.joekawum.punishCore.listener.ConnectionListener;
import de.joekawum.punishCore.manager.ban.BanManager;
import de.joekawum.punishCore.manager.report.Report;
import de.joekawum.punishCore.manager.report.ReportManager;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Plugin(id = "velocity-punishcore", name = "PunishCore", version = "1.0-SNAPSHOT", dependencies = {
        @Dependency(id = "velocitymysqlfix"),
        @Dependency(id = "plugincore-velocity")
})
public class PunishCore {

    //TODO finish NotifyCommand
    //TODO add sql for report
    //TODO add banId to unban (#xxx-0000)
    //TODO add reportLog (each player)
    //TODO set permission for all commands
    //TODO add prefix to all messages
    //TODO translate all messages (to german)
    //TODO general message edit

    public static final MinecraftChannelIdentifier IDENTIFIER = MinecraftChannelIdentifier.from("report:teleport");

    private final ProxyServer proxyServer;
    private final Logger logger;
    private final BanManager banManager;
    private final ReportManager reportManager;

    @Inject
    public PunishCore(ProxyServer proxyServer, Logger logger) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.banManager = new BanManager(proxyServer);
        this.reportManager = new ReportManager(proxyServer);

        logger.info("booting up...");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Initializing plugin...");

        CommandManager commandManager = proxyServer.getCommandManager();

        this.registerBan(commandManager);

        this.registerReport(commandManager);

        CommandMeta notifyMeta = commandManager.metaBuilder("notify")
                        .plugin(this).build();
        commandManager.register(notifyMeta, new NotifyCommand());

        proxyServer.getEventManager().register(this, new ConnectionListener());

        proxyServer.getChannelRegistrar().register(IDENTIFIER);

        try {
            List<Object[]> report = PluginCore.instance().mysql().getTable("Report", new String[]{"operator", "suspect", "reason", "server", "timestamp", "id"});
            if(!report.isEmpty()) {
                for (Object[] obj : report) {
                    UUID operator = UUID.fromString((String) obj[0]);
                    UUID suspect = UUID.fromString((String) obj[1]);
                    ReportManager.Reasons reason = ReportManager.Reasons.byId((int)obj[2]);
                    String server = (String) obj[3];
                    long timestamp = Long.parseLong((String) obj[4]);
                    String id = (String) obj[5];
                    new Report(operator, suspect, reason, server, timestamp, id);
                }
                logger.info("loaded all reports (" + ReportManager.reportCache.size() + ")...");
                PluginCore.instance().mysql().deleteTable("Report");
            } else
                logger.info("report cache was empty...");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        logger.info("done.");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        logger.info("Shutdown process started...");
        logger.info("saving storage data...");

        for (UUID uuid : ReportManager.reportCache.keySet()) {
            for (Report report : ReportManager.reportCache.get(uuid)) {
                UUID operator = report.getSender();
                UUID suspect = report.getSuspect();
                ReportManager.Reasons reason = report.getReason();
                String server = report.getServer();
                long timestamp = report.getTimestamp();
                String id = report.getId();
                try {
                    PluginCore.instance().mysql().insertValue("Report", "operator, suspect, reason, server, timestamp, id", new Object[]{
                            operator.toString(),
                            suspect.toString(),
                            reason.getId(),
                            server,
                            timestamp,
                            id
                    });
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        logger.info("done.");

        //TODO sql saving for report
    }

    private void registerBan(CommandManager commandManager) {
        CommandMeta banMeta = commandManager.metaBuilder("ban")
                .aliases("punish").plugin(this).build();
        commandManager.register(banMeta, new BanCommand(this.banManager));

        CommandMeta banHistoryMeta = commandManager.metaBuilder("banhistory")
                .aliases("banlog", "history").plugin(this).build();
        commandManager.register(banHistoryMeta, new BanHistoryCommand(this.banManager));

        CommandMeta pardonMeta = commandManager.metaBuilder("pardon")
                .aliases("unban").plugin(this).build();
        commandManager.register(pardonMeta, new PardonCommand(this.banManager));

        logger.info("registered ban...");
    }

    private void registerReport(CommandManager commandManager) {
        CommandMeta reportAcceptMeta = commandManager.metaBuilder("reportaccept")
                .aliases("acceptreport").plugin(this).build();
        commandManager.register(reportAcceptMeta, new ReportAcceptCommand(this.proxyServer));

        CommandMeta reportMeta = commandManager.metaBuilder("report")
                .plugin(this).build();
        commandManager.register(reportMeta, new ReportCommand(this.proxyServer));

        CommandMeta reportDenyMeta = commandManager.metaBuilder("reportdeny")
                .aliases("denyreport").plugin(this).build();
        commandManager.register(reportDenyMeta, new ReportDenyCommand(this.proxyServer));

        CommandMeta reportInfoMeta = commandManager.metaBuilder("reportinfo")
                .plugin(this).build();
        commandManager.register(reportInfoMeta, new ReportInfoCommand(this.proxyServer));

        CommandMeta reportListMeta = commandManager.metaBuilder("reportlist")
                .aliases("reports").plugin(this).build();
        commandManager.register(reportListMeta, new ReportListCommand(this.proxyServer));

        CommandMeta reportTeleportMeta = commandManager.metaBuilder("reportteleport")
                .aliases("reporttp").plugin(this).build();
        commandManager.register(reportTeleportMeta, new ReportTeleportCommand(this.proxyServer, this.reportManager));

        logger.info("registered report...");
    }

}
