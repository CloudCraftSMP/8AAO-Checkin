package fi.dandabs.checkin.Listeners;

import com.iridium.iridiumcolorapi.IridiumColorAPI;
import fi.dandabs.checkin.Main;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NPCRightClickEventListener implements Listener {
    @EventHandler
    public void onRightClick(NPCRightClickEvent event) {
        NPC npc = event.getNPC();
        if (npc.getName().contains("aaoci-")) { // if the npc is a checkin npc
            String eventName = npc.getName().split("aaoci-")[1];
            Player player = event.getClicker();

            int size = 0;
            try {
                PreparedStatement stmt = Main.getInstance().connection.prepareStatement("SELECT * FROM `CHECKINS` WHERE event=? AND minecraft=?",
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE);
                stmt.setString(1, eventName);
                stmt.setString(2, player.getUniqueId().toString());
                ResultSet results = stmt.executeQuery();
                 results.last(); size = results.getRow();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (size != 0) {
                player.sendMessage("You have already checked in for this event.");
                return;
            }

            size = 0;
            String door = "";
            String seat = "";
            try {
                PreparedStatement stmt = Main.getInstance().connection.prepareStatement("SELECT TICKETS.*,SEATS.door,USERS.minecraft FROM `TICKETS` INNER JOIN `SEATS` ON TICKETS.seat=SEATS.id INNER JOIN `USERS` ON TICKETS.discord=USERS.id WHERE event=? AND minecraft=?",
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE);
                stmt.setString(1, eventName);
                stmt.setString(2, player.getUniqueId().toString());
                ResultSet results = stmt.executeQuery();
                results.first(); door = results.getString("customdoor"); if (results.wasNull()) door = results.getString("door");
                                 seat = results.getString("seat");
                results.last(); size = results.getRow();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (size == 0) {
                player.sendMessage("You have not purchased a ticket for this event.");
                return;
            }

            try {
                PreparedStatement stmt = Main.getInstance().connection.prepareStatement("INSERT INTO `CHECKINS` VALUES (?,?,?)",
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE);
                stmt.setString(1, eventName);
                stmt.setString(2, player.getUniqueId().toString());
                stmt.setLong(3, System.currentTimeMillis() / 1000L);
                stmt.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            player.sendMessage("You have been successfully checked in for " + eventName.toUpperCase() + ".");
            player.sendMessage("When doors open, please enter the arena through Door " + door + ". Your seat is " + seat + ".");
            player.sendMessage("Enjoy the show!");
            return;
        }
    }
}
