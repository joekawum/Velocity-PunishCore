package de.joekawum.punishCore.manager;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.joekawum.pluginCore.PluginCore;
import net.kyori.adventure.text.Component;

import java.sql.SQLException;
import java.util.UUID;

public class BanManager {

    private final ProxyServer proxyServer;

    public BanManager(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
        PluginCore.instance().mysql().createTable("Bans", "uuid VARCHAR(36), points INT, operator VARCHAR(16), reason VARCHAR(36), banDate LONG, expireDate LONG, id INT");
        PluginCore.instance().mysql().createTable("BanLog", "uuid VARCHAR(36), points INT, operator VARCHAR(16), reason VARCHAR(36), banDate LONG, expireDate LONG, id INT");
    }

    public void banPlayer(UUID uuid, String operator, String reason, long duration, String banScreen) throws SQLException {
        int banId = calculateBanLog() + 1;
        Object points = PluginCore.instance().mysql().getValue("Bans", "uuid", uuid.toString(), "points");
        if (points == null || points.equals("null") || points.equals("NULL"))
            points = "0";
        if (PluginCore.instance().mysql().valueExists("Bans", "uuid", uuid.toString())) {
            PluginCore.instance().mysql().setValue("Bans", "uuid", uuid.toString(), "expireDate", Long.valueOf((duration < 0L) ? -1L : (System.currentTimeMillis() + duration)));
            PluginCore.instance().mysql().setValue("Bans", "uuid", uuid.toString(), "operator", operator);
            PluginCore.instance().mysql().setValue("Bans", "uuid", uuid.toString(), "banDate", Long.valueOf(System.currentTimeMillis()));
            PluginCore.instance().mysql().setValue("Bans", "uuid", uuid.toString(), "id", Integer.valueOf(banId));
            PluginCore.instance().mysql().setValue("Bans", "uuid", uuid.toString(), "reason", reason);
        } else {
            PluginCore.instance().mysql().insertValue("Bans", "uuid, points, operator, reason, banDate, expireDate, id", new Object[] { uuid
                    .toString(), points, operator, reason,

                    Long.valueOf(System.currentTimeMillis()),
                    Long.valueOf((duration < 0L) ? -1L : (System.currentTimeMillis() + duration)),
                    Integer.valueOf(banId) });
        }
        PluginCore.instance().mysql().insertValue("BanLog", "uuid, points, operator, reason, banDate, expireDate, id", new Object[] { uuid
                .toString(), points, operator, reason,

                Long.valueOf(System.currentTimeMillis()),
                Long.valueOf((duration < 0L) ? -1L : (System.currentTimeMillis() + duration)),
                Integer.valueOf(banId) });
        Player player = proxyServer.getPlayer(uuid).get();
        if (player != null && player.isActive())
            if (duration < 0L) {
                player.disconnect(Component.text("§7Du wurdest §cPERMANENT §7vom Netzwerk gebannt.\n§7Grund: §c" + reason + "\n§c§oDu kannst KEINEN Entbannungsantrag stellen."));
            } else {
                player.disconnect(Component.text("§7Du wurdest für §c" + banScreen + " §7vom Netzwerk gebannt.\n§7Grund: §c" + reason + "\n§c§oDu kannst KEINEN Entbannungsantrag stellen."));
            }
    }

    public void banPlayer(UUID uuid, BanReason banReason, String operator) throws SQLException {
        int points = banReason.getPoints();
        int banId = calculateBanLog() + 1;
        String banScreen = calculateBanText(points);
        if (PluginCore.instance().mysql().valueExists("Bans", "uuid", uuid.toString())) {
            points += ((Integer)PluginCore.instance().mysql().getValue("Bans", "uuid", uuid.toString(), "points")).intValue();
            banScreen = calculateBanText(points);
            long banTime = calculateBan(points);
            if (banTime < 0L) {
                PluginCore.instance().mysql().setValue("Bans", "uuid", uuid.toString(), "expireDate", Integer.valueOf(-1));
            } else {
                banTime *= 1000L;
                PluginCore.instance().mysql().setValue("Bans", "uuid", uuid.toString(), "expireDate", Long.valueOf(System.currentTimeMillis() + banTime));
            }
            PluginCore.instance().mysql().setValue("Bans", "uuid", uuid.toString(), "points", Integer.valueOf(points));
            PluginCore.instance().mysql().setValue("Bans", "uuid", uuid.toString(), "operator", operator);
            PluginCore.instance().mysql().setValue("Bans", "uuid", uuid.toString(), "banDate", Long.valueOf(System.currentTimeMillis()));
            PluginCore.instance().mysql().setValue("Bans", "uuid", uuid.toString(), "id", Integer.valueOf(banId));
            PluginCore.instance().mysql().setValue("Bans", "uuid", uuid.toString(), "reason", banReason.getText());
            PluginCore.instance().mysql().insertValue("BanLog", "uuid, points, operator, reason, banDate, expireDate, id", new Object[] { uuid
                    .toString(),
                    Integer.valueOf(points), operator, banReason

                    .getText(),
                    Long.valueOf(System.currentTimeMillis()),
                    Long.valueOf((banTime < 0L) ? -1L : (System.currentTimeMillis() + banTime)),
                    Integer.valueOf(banId) });
        } else {
            long banTime = calculateBan(points);
            if (banTime < 0L) {
                PluginCore.instance().mysql().insertValue("Bans", "uuid, points, operator, reason, banDate, expireDate, id", new Object[] { uuid
                        .toString(),
                        Integer.valueOf(points), operator, banReason

                        .getText(),
                        Long.valueOf(System.currentTimeMillis()),
                        Integer.valueOf(-1),
                        Integer.valueOf(banId) });
            } else {
                banTime *= 1000L;
                PluginCore.instance().mysql().insertValue("Bans", "uuid, points, operator, reason, banDate, expireDate, id", new Object[] { uuid
                        .toString(),
                        Integer.valueOf(points), operator, banReason

                        .getText(),
                        Long.valueOf(System.currentTimeMillis()),
                        Long.valueOf(System.currentTimeMillis() + banTime),
                        Integer.valueOf(banId) });
            }
            PluginCore.instance().mysql().insertValue("BanLog", "uuid, points, operator, reason, banDate, expireDate, id", new Object[] { uuid
                    .toString(),
                    Integer.valueOf(points), operator, banReason

                    .getText(),
                    Long.valueOf(System.currentTimeMillis()),
                    Long.valueOf((banTime < 0L) ? -1L : (System.currentTimeMillis() + banTime)),
                    Integer.valueOf(banId) });
        }
        Player player = proxyServer.getPlayer(uuid).get();
        if (player != null && player.isActive())
            player.disconnect(Component.text("§7Du wurdest " + banScreen + " §7vom Netzwerk gebannt.\n§7Grund: §c" + banReason.getText() + "\n§c§oDu kannst KEINEN Entbannungsantrag stellen."));
    }

    public boolean isBanned(UUID uuid) throws SQLException {
        if (!PluginCore.instance().mysql().valueExists("Bans", "uuid", uuid.toString()))
            return false;
        long expireDate = Long.parseLong((String)PluginCore.instance().mysql().getValue("Bans", "uuid", uuid.toString(), "expireDate"));
        return (System.currentTimeMillis() < expireDate);
    }

    public long calculateBan(int points) {
        if (points < 3)
            return 600L;
        if (points < 7)
            return 1800L;
        if (points < 14)
            return 7200L;
        if (points < 21)
            return 86400L;
        if (points < 25)
            return 604800L;
        if (points < 30)
            return 2592000L;
        return -1L;
    }

    public String calculateBanText(int points) {
        if (points < 3)
            return "für §c10 Minuten";
        if (points < 7)
            return "für §c30 Minuten";
        if (points < 14)
            return "für §c2 Stunden";
        if (points < 21)
            return "für §c1 Tag";
        if (points < 25)
            return "für §c7 Tage";
        if (points < 30)
            return "für §c30 Tage";
        return "§cPERMANENT";
    }

    public int calculateBanLog() throws SQLException {
        return PluginCore.instance().mysql().getTableSize("BanLog");
    }

    public enum BanReason {
        CHEATING(1, "Clientmodifikationen", 25),
        TEAMING(2, "Teaming", 7),
        EXTREMEINSULT(3, "Extreme Beleidigung", 4),
        SKIN(4, "Skin", 2),
        NAME(5, "Name", 3),
        RACISM(6, "Rassismus", 14),
        PROVOCATION(7, "Provokation", 4),
        AD(8, "Werbung", 4),
        BUGUSING(9, "Bugusing", 7),
        SUPPORTEXPLOITATION(10, "Supportausnutzung", 3),
        REPORTEXPLOITATION(11, "Reportausnutzung", 3),
        THREAT(12, "Drohung", 5),
        RANDOMKILLING(13, "Randomkilling (TTT)", 7),
        BANBYPASS(14, "Banumgehung", 30),
        WARNING(15, "Verwarnung", 1),
        TADEL(16, "Tadel", 2),
        HOUSEBAN(17, "Hausverbot", 30);

        private final int id;

        private final String text;

        private final int points;

        BanReason(int id, String text, int points) {
            this.id = id;
            this.text = text;
            this.points = points;
        }

        public int getId() {
            return this.id;
        }

        public String getText() {
            return this.text;
        }

        public int getPoints() {
            return this.points;
        }

        public static BanReason byId(int id) {
            for (BanReason value : values()) {
                if (value.getId() == id)
                    return value;
            }
            return null;
        }
    }
}
