package de.joekawum.punishCore.manager.ban;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.joekawum.pluginCore.PluginCore;
import net.kyori.adventure.text.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class BanManager {

    public static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    public static final List<Player> banNotify = new ArrayList<>();

    private final ProxyServer proxyServer;

    public BanManager(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
        PluginCore.instance().mysql().createTable("Bans", "uuid VARCHAR(36), points INT, operator VARCHAR(16), reason VARCHAR(36), banDate LONG, expireDate LONG, id VARCHAR(10)");
        PluginCore.instance().mysql().createTable("BanLog", "uuid VARCHAR(36), points INT, operator VARCHAR(16), reason VARCHAR(36), banDate LONG, expireDate LONG, id VARCHAR(10)");
    }

    public void banPlayer(UUID uuid, String operator, String reason, long duration, String banScreen) throws SQLException {
        int banId = calculateBanLog() + 1;

        StringBuilder current = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            int random = ThreadLocalRandom.current().nextInt(2);
            char c = ALPHABET.charAt(ThreadLocalRandom.current().nextInt(ALPHABET.length()));
            String s = String.valueOf(c);
            if(random > 0)
                current.append(s);
            else
                current.append(s.toUpperCase());
        }

        String banIdString = "#" + current + "-" + (banId < 10 ? "000" + banId : (banId < 100 ? "00" + banId : (banId < 1000 ? "0" + banId : banId)));

        for (Player player : banNotify) {
            player.sendMessage(Component.text("§4§lBAN §8>> §e" + PluginCore.instance().uuidFetcher().getUsername(uuid) + " §7wurde von §c" + operator + " §7gebannt! Grund: §e" + reason));
        }
        Object points = PluginCore.instance().mysql().getValue("Bans", "uuid", uuid.toString(), "points");
        if (points == null || points.equals("null") || points.equals("NULL"))
            points = "0";
        if (PluginCore.instance().mysql().valueExists("Bans", "uuid", uuid.toString())) {
            PluginCore.instance().mysql().setValue("Bans", "uuid", uuid.toString(), "expireDate", Long.valueOf((duration < 0L) ? -1L : (System.currentTimeMillis() + duration)));
            PluginCore.instance().mysql().setValue("Bans", "uuid", uuid.toString(), "operator", operator);
            PluginCore.instance().mysql().setValue("Bans", "uuid", uuid.toString(), "banDate", Long.valueOf(System.currentTimeMillis()));
            PluginCore.instance().mysql().setValue("Bans", "uuid", uuid.toString(), "id", banIdString);
            PluginCore.instance().mysql().setValue("Bans", "uuid", uuid.toString(), "reason", reason);
        } else {
            PluginCore.instance().mysql().insertValue("Bans", "uuid, points, operator, reason, banDate, expireDate, id", new Object[] { uuid
                    .toString(), points, operator, reason,

                    Long.valueOf(System.currentTimeMillis()),
                    Long.valueOf((duration < 0L) ? -1L : (System.currentTimeMillis() + duration)),
                    banIdString });
        }
        PluginCore.instance().mysql().insertValue("BanLog", "uuid, points, operator, reason, banDate, expireDate, id", new Object[] { uuid
                .toString(), points, operator, reason,

                Long.valueOf(System.currentTimeMillis()),
                Long.valueOf((duration < 0L) ? -1L : (System.currentTimeMillis() + duration)),
                banIdString });

        Optional<Player> optionalPlayer = proxyServer.getPlayer(uuid);
        if(optionalPlayer.isEmpty() || !optionalPlayer.isPresent()) return;
        Player player = optionalPlayer.get();
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

        StringBuilder current = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            int random = ThreadLocalRandom.current().nextInt(2);
            char c = ALPHABET.charAt(ThreadLocalRandom.current().nextInt(ALPHABET.length()));
            String s = String.valueOf(c);
            if(random > 0)
                current.append(s);
            else
                current.append(s.toUpperCase());
        }

        String banIdString = "#" + current + "-" + (banId < 10 ? "000" + banId : (banId < 100 ? "00" + banId : (banId < 1000 ? "0" + banId : banId)));

        for (Player player : banNotify) {
            player.sendMessage(Component.text("§4§lBAN §8>> §e" + PluginCore.instance().uuidFetcher().getUsername(uuid) + " §7wurde von §c" + operator + " §7gebannt! Grund: §e" + banReason.getText()));
        }
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
            PluginCore.instance().mysql().setValue("Bans", "uuid", uuid.toString(), "id", banIdString);
            PluginCore.instance().mysql().setValue("Bans", "uuid", uuid.toString(), "reason", banReason.getText());
            PluginCore.instance().mysql().insertValue("BanLog", "uuid, points, operator, reason, banDate, expireDate, id", new Object[] { uuid
                    .toString(),
                    Integer.valueOf(points), operator, banReason

                    .getText(),
                    Long.valueOf(System.currentTimeMillis()),
                    Long.valueOf((banTime < 0L) ? -1L : (System.currentTimeMillis() + banTime)),
                    banIdString });
        } else {
            long banTime = calculateBan(points);
            if (banTime < 0L) {
                PluginCore.instance().mysql().insertValue("Bans", "uuid, points, operator, reason, banDate, expireDate, id", new Object[] { uuid
                        .toString(),
                        Integer.valueOf(points), operator, banReason

                        .getText(),
                        Long.valueOf(System.currentTimeMillis()),
                        Integer.valueOf(-1),
                        banIdString });
            } else {
                banTime *= 1000L;
                PluginCore.instance().mysql().insertValue("Bans", "uuid, points, operator, reason, banDate, expireDate, id", new Object[] { uuid
                        .toString(),
                        Integer.valueOf(points), operator, banReason

                        .getText(),
                        Long.valueOf(System.currentTimeMillis()),
                        Long.valueOf(System.currentTimeMillis() + banTime),
                        banIdString });
            }
            PluginCore.instance().mysql().insertValue("BanLog", "uuid, points, operator, reason, banDate, expireDate, id", new Object[] { uuid
                    .toString(),
                    Integer.valueOf(points), operator, banReason

                    .getText(),
                    Long.valueOf(System.currentTimeMillis()),
                    Long.valueOf((banTime < 0L) ? -1L : (System.currentTimeMillis() + banTime)),
                    banIdString});
        }
        Optional<Player> optionalPlayer = proxyServer.getPlayer(uuid);
        if(optionalPlayer.isEmpty() || !optionalPlayer.isPresent()) return;
        Player player = optionalPlayer.get();
        if (player != null && player.isActive())
            player.disconnect(Component.text("§7Du wurdest " + banScreen + " §7vom Netzwerk gebannt.\n§7Grund: §c" + banReason.getText() + "\n§c§oDu kannst KEINEN Entbannungsantrag stellen."));
    }

    public boolean isBanned(UUID uuid) throws SQLException {
        if (!PluginCore.instance().mysql().valueExists("Bans", "uuid", uuid.toString()))
            return false;
        long expireDate = Long.parseLong((String)PluginCore.instance().mysql().getValue("Bans", "uuid", uuid.toString(), "expireDate"));
        return (System.currentTimeMillis() < expireDate);
    }

    public boolean isBanned(String id) throws SQLException {
        if (!PluginCore.instance().mysql().valueExists("Bans", "id", id))
            return false;
        long expireDate = Long.parseLong((String)PluginCore.instance().mysql().getValue("Bans", "id", id, "expireDate"));
        if(expireDate < 0) return true;
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
        TROLLING(13, "Trolling/Griefing", 7),
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
