package de.joekawum.punishCore.manager.report;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import de.joekawum.pluginCore.PluginCore;
import de.joekawum.punishCore.PunishCore;

import java.util.*;

public class ReportManager {

    public static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz", CODECHAR = "!=@&#?";
    public static final HashMap<UUID, LinkedList<Report>> reportCache = new HashMap<>();
    public static final List<Player> reportNotify = new ArrayList<>();
    public static int totalReportAmount = 0;

    private final ProxyServer proxyServer;

    public ReportManager(ProxyServer proxyServer){
        // TODO: 13.08.24 init mysql
        this.proxyServer = proxyServer;

        PluginCore.instance().mysql().createTable("Report", "operator VARCHAR(36), suspect VARCHAR(36), reason INT, server VARCHAR(20), timestamp LONG, id VARCHAR(10)");
    }

    public void sendTeleportData(Player player, UUID suspect) {
        Collection<Player> players = proxyServer.getAllPlayers();
        if(players == null || players.isEmpty()) {
            System.out.println("[Report] tried sending data while network was empty [" + player + "," + suspect + "]");
            return;
        }

        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeUTF("teleportInformation");
        output.writeUTF(player.getUniqueId().toString());
        output.writeUTF(suspect.toString());

        sendPluginMessageToBackendUsingPlayer(player, PunishCore.IDENTIFIER, output.toByteArray());
    }

    public boolean sendPluginMessageToBackendUsingPlayer(Player player, ChannelIdentifier identifier, byte[] data) {
        Optional<ServerConnection> connection = player.getCurrentServer();
        if (connection.isPresent()) {
            // On success, returns true
            return connection.get().sendPluginMessage(identifier, data);
        }
        return false;
    }

    public enum Reasons {

        CHEATING(1, "Hacking"),
        BUGUSING(2, "Bugusing"),
        TROLLING(3, "Trolling"),
        GRIEFING(4, "Griefing"),
        INSULT(5, "Beleidigung"),
        OTHER(6, "Sonstiges");

        Reasons(int id, String name) {
            this.id = id;
            this.name = name;
        }


        private static final Reasons[] values = values();

        private final int id;
        private final String name;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public static Reasons byId(int id) {
            for (Reasons value : values) {
                if(value.id == id) return value;
            }
            return null;
        }
    }

}
