package hu.example.cma;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Az aliasok tárolásáért (aliases.yml) és a CommandMap-es
 * regisztrálásáért / törléséért felelős osztály.
 */
public class AliasManager {

    private final CmaPlugin plugin;
    private final CommandMapUtil commandMapUtil;
    private final File file;
    private final FileConfiguration config;

    // alias (kisbetűs) -> DynamicAliasCommand
    private final Map<String, DynamicAliasCommand> registeredCommands = new LinkedHashMap<>();

    public AliasManager(CmaPlugin plugin, CommandMapUtil commandMapUtil) {
        this.plugin = plugin;
        this.commandMapUtil = commandMapUtil;

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        this.file = new File(plugin.getDataFolder(), "aliases.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Nem sikerult letrehozni az aliases.yml fajlt: " + e.getMessage());
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    /** Szerverindításkor az összes elmentett alias visszaregisztrálása. */
    public void loadAndRegisterAll() {
        if (config.getConfigurationSection("aliases") == null) return;
        for (String alias : config.getConfigurationSection("aliases").getKeys(false)) {
            String targetCommand = config.getString("aliases." + alias);
            if (targetCommand != null) {
                registerAliasCommand(alias, targetCommand);
            }
        }
    }

    public boolean createAlias(String targetCommand, String alias) {
        String key = alias.toLowerCase();
        if (registeredCommands.containsKey(key)) {
            return false; // mar letezik
        }
        registerAliasCommand(key, targetCommand);
        config.set("aliases." + key, targetCommand);
        save();
        return true;
    }

    public boolean removeAlias(String alias) {
        String key = alias.toLowerCase();
        DynamicAliasCommand cmd = registeredCommands.remove(key);
        if (cmd == null) return false;
        commandMapUtil.unregisterCommand(cmd);
        config.set("aliases." + key, null);
        save();
        return true;
    }

    public boolean editAlias(String oldAlias, String newAlias) {
        String oldKey = oldAlias.toLowerCase();
        String newKey = newAlias.toLowerCase();

        DynamicAliasCommand oldCmd = registeredCommands.get(oldKey);
        if (oldCmd == null) return false;
        if (registeredCommands.containsKey(newKey)) return false;

        String targetCommand = oldCmd.getTargetCommand();
        removeAlias(oldKey);
        registerAliasCommand(newKey, targetCommand);
        config.set("aliases." + newKey, targetCommand);
        save();
        return true;
    }

    private void registerAliasCommand(String alias, String targetCommand) {
        DynamicAliasCommand cmd = new DynamicAliasCommand(alias, targetCommand);
        boolean success = commandMapUtil.registerCommand(cmd);
        if (!success) {
            plugin.getLogger().warning("Az '" + alias + "' parancs regisztralasa utkozesbe kerult egy mar letezo paranccsal!");
        }
        registeredCommands.put(alias, cmd);
    }

    public void unregisterAll() {
        for (DynamicAliasCommand cmd : registeredCommands.values()) {
            commandMapUtil.unregisterCommand(cmd);
        }
        registeredCommands.clear();
    }

    public Map<String, DynamicAliasCommand> getRegisteredCommands() {
        return registeredCommands;
    }

    public int getAliasCount() {
        return registeredCommands.size();
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Nem sikerult elmenteni az aliases.yml fajlt: " + e.getMessage());
        }
    }
}
