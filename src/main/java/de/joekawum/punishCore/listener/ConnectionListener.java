package de.joekawum.punishCore.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import de.joekawum.pluginCore.PluginCore;
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
            int id = ((Integer)PluginCore.instance().mysql().getValue("Bans", "uuid", player.getUniqueId().toString(), "id")).intValue();
            Object reason = PluginCore.instance().mysql().getValue("Bans", "uuid", player.getUniqueId().toString(), "reason");
            String idString = "" + id;
            if (id < 10) {
                idString = "000" + id;
            } else if (id < 100) {
                idString = "00" + id;
            } else if (id < 1000) {
                idString = "0" + id;
            }
            if (expireDate < 0L) {
                player.disconnect(Component.text("§cDu wurdest vom Netzwerk gebannt!\n§7Grund: §c" + reason + " §7(#" + idString + ")\n§cGebannt auf Lebenszeit"));
                return;
            }
            if (System.currentTimeMillis() < expireDate) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                player.disconnect(Component.text("§cDu wurdest vom Netzwerk gebannt!\n§7Grund: §c "+ reason + " §7(#" + idString + ")\n§7Gebanntbis zum: §c" + simpleDateFormat.format(new Date(expireDate)) + " Uhr"));
            }
        }
    }
}
