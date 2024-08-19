package de.joekawum.punishCore.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import de.joekawum.pluginCore.PluginCore;
import de.joekawum.punishCore.manager.BanManager;
import net.kyori.adventure.text.Component;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class BanHistoryCommand implements SimpleCommand {

    private final BanManager banManager;

    public BanHistoryCommand(BanManager banManager) {
        this.banManager = banManager;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        if (args.length == 1) {
            try {
                UUID uuid = PluginCore.instance().uuidFetcher().getUniqueId(args[0]);
                if (uuid == null) {
                    sender.sendMessage(Component.text("§cDer Spieler wurde nicht gefunden!"));
                    return;
                }
                List<Object[]> filteredTable = PluginCore.instance().mysql().filterTable("BanLog", "uuid", uuid.toString(), new String[] { "points", "operator", "reason", "banDate", "expireDate", "id" });
                if (filteredTable.isEmpty()) {
                    sender.sendMessage(Component.text("§aDer Spieler wurde noch nie gebannt."));
                    return;
                }
                sender.sendMessage(Component.text(" "));
                sender.sendMessage(Component.text("§cBanHistory von §e" + PluginCore.instance().uuidFetcher().getUsername(uuid)));
                        sender.sendMessage(Component.text(" "));
                sender.sendMessage(Component.text("§7Gebannt: " + (this.banManager.isBanned(uuid) ? "§cJa" : "§aNein")));
                sender.sendMessage(Component.text(" "));
                sender.sendMessage(Component.text("§7Totale Bans: §e" + filteredTable.size()));
                        sender.sendMessage(Component.text(" "));
                for (Object[] objects : filteredTable) {
                    int id = ((Integer)objects[5]).intValue();
                    String idString = "" + id;
                    if (id < 10) {
                        idString = "000" + id;
                    } else if (id < 100) {
                        idString = "00" + id;
                    } else if (id < 1000) {
                        idString = "0" + id;
                    }
                    sender.sendMessage(Component.text("§7Grund: §e" + objects[2] + " §7(§c#" + idString + "§7)"));
                            sender.sendMessage(Component.text("§7Gebannt von: §e" + objects[1]));
                                    sender.sendMessage(Component.text("§7Gebannt am: §e" + simpleDateFormat.format(new Date(Long.parseLong((String)objects[3])))));
                    long date = Long.parseLong((String)objects[4]);
                    sender.sendMessage(Component.text("§7Gebannt bis: " + ((System.currentTimeMillis() > date) ? "§a": "§c") + simpleDateFormat.format(new Date(date))));
                    sender.sendMessage(Component.text("§7Punkte: §e" + objects[0]));
                            sender.sendMessage(Component.text(" "));
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } else {
            sender.sendMessage(Component.text("§7Verwende §c/banhistory <Spieler>"));
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("blockheaven.punish.banhistory");
    }
}
