package de.joekawum.punishCore;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import de.joekawum.punishCore.commands.BanCommand;
import de.joekawum.punishCore.commands.BanHistoryCommand;
import de.joekawum.punishCore.commands.PardonCommand;
import de.joekawum.punishCore.listener.ConnectionListener;
import de.joekawum.punishCore.manager.BanManager;
import org.slf4j.Logger;

@Plugin(id = "velocity-punishcore", name = "PunishCore", version = "1.0-SNAPSHOT", dependencies = {
        @Dependency(id = "velocitymysqlfix"),
        @Dependency(id = "plugincore-velocity")
})
public class PunishCore {

    private final ProxyServer proxyServer;
    private final Logger logger;
    private final BanManager banManager;

    @Inject
    public PunishCore(ProxyServer proxyServer, Logger logger) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.banManager = new BanManager(proxyServer);

        logger.info("booting up...");
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        CommandManager commandManager = proxyServer.getCommandManager();

        CommandMeta banMeta = commandManager.metaBuilder("ban")
                .aliases("punish").plugin(this).build();
        commandManager.register(banMeta, new BanCommand(this.banManager));

        CommandMeta banHistoryMeta = commandManager.metaBuilder("banhistory")
                .aliases("banlog", "history").plugin(this).build();
        commandManager.register(banHistoryMeta, new BanHistoryCommand(this.banManager));

        CommandMeta pardonMeta = commandManager.metaBuilder("pardon")
                .aliases("unban").plugin(this).build();
        commandManager.register(pardonMeta, new PardonCommand());

        proxyServer.getEventManager().register(this, new ConnectionListener());
    }
}
