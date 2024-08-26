package de.joekawum.punishCore.commands.ban;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import de.joekawum.pluginCore.PluginCore;
import de.joekawum.punishCore.manager.ban.BanManager;
import net.kyori.adventure.text.Component;

import java.sql.SQLException;
import java.util.UUID;

public class PardonCommand implements SimpleCommand {

    private final BanManager banManager;

    public PardonCommand(BanManager banManager) {
        this.banManager = banManager;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();
        if (args.length == 1) {
            if(args[0].startsWith("#")) {
                String banId = args[0];
                if(!args[0].contains("-")) {
                    sender.sendMessage(Component.text("§cInvalid ban-id!"));
                    return;
                }
                try {
                    if(this.banManager.isBanned(banId)) {
                        PluginCore.instance().mysql().deleteValue("Bans", "id", banId);
                        sender.sendMessage(Component.text("§aSpieler entbannt."));
                        return;
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                sender.sendMessage(Component.text("§cInvalid ban-id!"));
                return;
            }
            try {
                UUID uniqueId = PluginCore.instance().uuidFetcher().getUniqueId(args[0]);
                if (uniqueId == null) {
                    sender.sendMessage(Component.text("§cDer Spieler wurde nicht gefunden!"));
                    return;
                }
                if (PluginCore.instance().mysql().valueExists("Bans", "uuid", uniqueId.toString())) {
                    long l = Long.parseLong((String)PluginCore.instance().mysql().getValue("Bans", "uuid", uniqueId.toString(), "expireDate"));
                    if (l > System.currentTimeMillis() || l < 0L) {
                        PluginCore.instance().mysql().deleteValue("Bans", "uuid", uniqueId.toString());
                        sender.sendMessage(Component.text("§aSpieler entbannt."));
                        return;
                    }
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            sender.sendMessage(Component.text("§cDer Spieler ist nicht gebannt!"));
        } else {
            sender.sendMessage(Component.text("§7Verwende §c/unban <Spieler>"));
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("blockheaven.punish.pardon");
    }
}
