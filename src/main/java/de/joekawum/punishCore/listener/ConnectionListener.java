package de.joekawum.punishCore.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import de.joekawum.pluginCore.PluginCore;
import de.joekawum.punishCore.data.Data;
import de.joekawum.punishCore.manager.ban.BanManager;
import de.joekawum.punishCore.manager.report.ReportManager;
import net.kyori.adventure.text.Component;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConnectionListener {

    @Subscribe
    public void handlePostLogin(PostLoginEvent event) throws SQLException {
        Player player = event.getPlayer();
        PluginCore.instance().uuidFetcher().fetch(player.getUniqueId(), player.getUsername(), player.getRemoteAddress().getHostName());
        if (PluginCore.instance().mysql().valueExists("Bans", "uuid", player.getUniqueId().toString())) {
            long expireDate = Long.parseLong((String)PluginCore.instance().mysql().getValue("Bans", "uuid", player.getUniqueId().toString(), "expireDate"));
            String id = (String) PluginCore.instance().mysql().getValue("Bans", "uuid", player.getUniqueId().toString(), "id");
            Object reason = PluginCore.instance().mysql().getValue("Bans", "uuid", player.getUniqueId().toString(), "reason");
            if (expireDate < 0L) {
                player.disconnect(Component.text("§cDu wurdest vom Netzwerk gebannt!\n§7Grund: §c" + reason + " §7(" + id + ")\n§cGebannt auf Lebenszeit"));
                return;
            }
            if (System.currentTimeMillis() < expireDate) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                player.disconnect(Component.text("§cDu wurdest vom Netzwerk gebannt!\n§7Grund: §c "+ reason + " §7(" + id + ")\n§7Gebanntbis zum: §c" + simpleDateFormat.format(new Date(expireDate)) + " Uhr"));
            }
        }

        if(player.hasPermission("blockheaven.punish.notify")) {
            if (PluginCore.instance().mysql().valueExists("Notify", "uuid", player.getUniqueId().toString())) {
                boolean report = (boolean) PluginCore.instance().mysql().getValue("Notify", "uuid", player.getUniqueId().toString(), "report");
                boolean ban = (boolean) PluginCore.instance().mysql().getValue("Notify", "uuid", player.getUniqueId().toString(), "ban");
                boolean mute = (boolean) PluginCore.instance().mysql().getValue("Notify", "uuid", player.getUniqueId().toString(), "mute");

                if (report) {
                    ReportManager.reportNotify.add(player);
                    player.sendMessage(Data.text("§7Du bekommst §aaktuell §7reports gemeldet!"));
                    int size = ReportManager.reportCache.size();
                    player.sendMessage(Data.text("§7Es " + (size != 1 ? "sind" : "ist") + " momentan §e" + size + " Report" + (size != 1 ? "s" : "") + " §7offen."));
                }

                if (ban) {
                    BanManager.banNotify.add(player);
                    player.sendMessage(Data.text("§7Du wirst §aaktuell §7bei bans benachrichtigt!"));
                }

                if (mute)
                    player.sendMessage(Data.text("add mute"));
            }
        }
    }

    @Subscribe
    public void handleDisconnect(DisconnectEvent event) throws SQLException {
        Player player = event.getPlayer();

        if(player.hasPermission("blockheaven.punish.notify")) {
            boolean report = ReportManager.reportNotify.contains(player);
            boolean ban = BanManager.banNotify.contains(player);
            boolean mute = false;
            String uuid = player.getUniqueId().toString();
            if(PluginCore.instance().mysql().valueExists("Notify", "uuid", uuid)) {
                PluginCore.instance().mysql().setValue("Notify", "uuid", uuid, "report", report);
                PluginCore.instance().mysql().setValue("Notify", "uuid", uuid, "ban", ban);
                PluginCore.instance().mysql().setValue("Notify", "uuid", uuid, "mute", mute);
            } else {
                PluginCore.instance().mysql().insertValue("Notify", "uuid, report, ban, mute", new Object[]{
                        uuid,
                        report,
                        ban,
                        mute
                });
            }
        }
    }
}
