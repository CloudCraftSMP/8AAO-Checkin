package fi.dandabs.checkin;

import fi.dandabs.checkin.Listeners.NPCRightClickEventListener;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main extends JavaPlugin {
    private static Main instance;
    FileConfiguration config = this.getConfig();
    public static Connection connection;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Enabling!");
        config.addDefault("database.username", "user");
        config.addDefault("database.password", "pass123");
        config.addDefault("database.database", "box");
        config.addDefault("database.host", "localhost");
        config.addDefault("database.port", "3306");
        config.options().copyDefaults(true);
        saveConfig();

        getServer().getPluginManager().registerEvents(new NPCRightClickEventListener(), this);

        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + config.getString("database.host") + ":" +
                    config.getString("database.port") + "/" + config.getString("database.database"),
                    config.getString("database.username"), config.getString("database.password"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onDisable() {
        getLogger().info("Disabling!");
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static Main getInstance() {
        return instance;
    }
}
