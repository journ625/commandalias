package hu.example.cma;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Reflection segítségével hozzáférünk a szerver belső CommandMap-jéhez,
 * hogy futásidőben (plugin.yml nélkül) tudjunk parancsokat regisztrálni / törölni.
 */
public class CommandMapUtil {

    private final JavaPlugin plugin;
    private final CommandMap commandMap;

    public CommandMapUtil(JavaPlugin plugin) {
        this.plugin = plugin;
        this.commandMap = fetchCommandMap();
    }

    private CommandMap fetchCommandMap() {
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            return (CommandMap) field.get(Bukkit.getServer());
        } catch (Exception e) {
            plugin.getLogger().severe("Nem sikerult elerni a CommandMap-et: " + e.getMessage());
            return null;
        }
    }

    public CommandMap getCommandMap() {
        return commandMap;
    }

    /**
     * Regisztrál egy új parancsot. A fallbackPrefix a plugin neve lesz,
     * így ütközés esetén a "cmaplugin:parancs" formában is elérhető marad.
     */
    public boolean registerCommand(Command command) {
        if (commandMap == null) return false;
        return commandMap.register(plugin.getName().toLowerCase(), command);
    }

    /**
     * Teljesen eltávolítja a parancsot a CommandMap-ből (a knownCommands map-ból is),
     * hogy a neve azonnal újra felhasználható legyen (pl. edit/remove után).
     */
    @SuppressWarnings("unchecked")
    public boolean unregisterCommand(Command command) {
        if (commandMap == null) return false;
        boolean result = command.unregister(commandMap);
        try {
            Field knownCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);
            knownCommands.remove(command.getName());
            for (String alias : command.getAliases()) {
                knownCommands.remove(alias);
            }
            // a plugin-prefixes verziót is töröljük, ha lett ilyen ütközés miatt
            knownCommands.remove(plugin.getName().toLowerCase() + ":" + command.getName());
        } catch (Exception e) {
            plugin.getLogger().warning("Nem sikerult teljesen eltavolitani a parancsot a knownCommands map-bol: " + e.getMessage());
        }
        return result;
    }
}
