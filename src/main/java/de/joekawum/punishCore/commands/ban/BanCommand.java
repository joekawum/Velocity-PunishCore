package de.joekawum.punishCore.commands.ban;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.joekawum.pluginCore.PluginCore;
import de.joekawum.punishCore.data.Data;
import de.joekawum.punishCore.manager.ban.BanManager;
import net.kyori.adventure.text.Component;

import java.sql.SQLException;
import java.util.UUID;

public class BanCommand implements SimpleCommand {

    private final BanManager banManager;

    public BanCommand(BanManager banManager) {
        this.banManager = banManager;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();
        if (args.length == 2) {
            try {
                UUID uuid = PluginCore.instance().uuidFetcher().getUniqueId(args[0]);
                if (uuid == null) {
                    sender.sendMessage(Data.text("§cDer Spieler wurde nicht gefunden!"));
                    return;
                }
                int id = Integer.parseInt(args[1]);
                BanManager.BanReason banReason = BanManager.BanReason.byId(id);
                if (banReason == null) {
                    sender.sendMessage(Data.text("§cBitte gebe eine richtige Reason-ID an!"));
                    return;
                }
                this.banManager.banPlayer(uuid, banReason, (sender instanceof Player ? ((Player)sender).getUsername() : "CONSOLE"));
                sender.sendMessage(Data.text("§aSpieler erfolgreich gebannt!"));
            } catch (SQLException throwables) {
                sender.sendMessage(Data.text("§cFehlerbeim schreiben in die Datenbank! LOG auf Fehler überprüfen!"));
                        throwables.printStackTrace();
            } catch (NumberFormatException exception) {
                sender.sendMessage(Data.text("§cBitte gebe eine richtige Reason-ID an!"));
                return;
            }
        } else if (args.length >= 3) {
            try {
                long timeValue;
                UUID uuid = PluginCore.instance().uuidFetcher().getUniqueId(args[0]);
                if (uuid == null) {
                    sender.sendMessage(Data.text("§cDer Spieler wurde nicht gefunden!"));
                    return;
                }
                String time = args[1];
                if (args[1].equalsIgnoreCase("perma") || args[1].equalsIgnoreCase("permanent")) {
                    String str = args[2];
                    if (args.length > 3)
                        for (int i = 3; i < args.length; i++)
                            str = str + " " + args[i];
                    if (str.length() > 36) {
                        sender.sendMessage(Data.text("§cBanngrund zu lang! Max. 36 Zeichen erlaubt."));
                        return;
                    }
                    this.banManager.banPlayer(uuid, (sender instanceof Player ? ((Player)sender).getUsername() : "CONSOLE"), str, -1L, "");
                    sender.sendMessage(Data.text("§aSpieler erfolgreich gebannt!"));
                    return;
                }
                if (time.toLowerCase().endsWith("min")) {
                    int i = Integer.parseInt(time.toLowerCase().replace("min", ""));
                    timeValue = i * 60000L;
                    time = i + " Minute" + ((i != 1) ? "n" : "");
                } else if (time.toLowerCase().endsWith("h")) {
                    int i = Integer.parseInt(time.toLowerCase().replace("h", ""));
                    timeValue = i * 3600000L;
                    time = i + " Stunde" + ((i != 1) ? "n" : "");
                } else if (time.toLowerCase().endsWith("d")) {
                    int i = Integer.parseInt(time.toLowerCase().replace("d", ""));
                    timeValue = i * 86400000L;
                    time = i + " Tag" + ((i != 1) ? "e" : "");
                } else if (time.toLowerCase().endsWith("m")) {
                    int i = Integer.parseInt(time.toLowerCase().replace("m", ""));
                    timeValue = i * 2592000000L;
                    time = i + " Monat" + ((i != 1) ? "e" : "");
                } else {
                    sender.sendMessage(Data.text("§cBitte gebe eine richtige Zeit an!"));
                    return;
                }
                String reason = args[2];
                if (args.length > 3)
                    for (int i = 3; i < args.length; i++)
                        reason = reason + " " + args[i];
                if (reason.length() > 36) {
                    sender.sendMessage(Data.text("§cBanngrund zu lang! Max. 36 Zeichen erlaubt."));
                    return;
                }
                this.banManager.banPlayer(uuid, (sender instanceof Player ? ((Player)sender).getUsername() : "CONSOLE"), reason, timeValue, time);
                sender.sendMessage(Data.text("§aSpieler erfolgreich gebannt!"));
            } catch (SQLException throwables) {
                sender.sendMessage(Data.text("§cFehler beim schreiben in die Datenbank! LOG auf Fehler überprüfen!"));
                        throwables.printStackTrace();
            } catch (NumberFormatException exception) {
                sender.sendMessage(Data.text("§cBitte gebe eine richtige Zeit an!"));
                return;
            }
        } else {
            sender.sendMessage(Component.text(" "));
            for (BanManager.BanReason value : BanManager.BanReason.values()) {
                sender.sendMessage(Data.text("§b" + value.getId() + " §7| §c" + value.getText() + " §7| §e" + value.getPoints() + " Punkte"));
            }
            sender.sendMessage(Component.text(" "));
            sender.sendMessage(Data.text("§7Verwende §c/ban <Spieler> <id>"));
            sender.sendMessage(Data.text("§7oder §c/ban <Spieler> <[Zeit]min,H,d,M/perma> [Grund]"));
        }
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("blockheaven.punish.ban");
    }
}
