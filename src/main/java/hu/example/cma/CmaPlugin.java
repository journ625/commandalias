package hu.example.cma;

import org.bukkit.plugin.java.JavaPlugin;

public final class CmaPlugin extends JavaPlugin {

    private AliasManager aliasManager;
    private CommandMapUtil commandMapUtil;

    @Override
    public void onEnable() {
        this.commandMapUtil = new CommandMapUtil(this);
        this.aliasManager = new AliasManager(this, commandMapUtil);
        this.aliasManager.loadAndRegisterAll();

        CmaCommand cmaCommand = new CmaCommand(this, aliasManager);
        if (getCommand("cma") != null) {
            getCommand("cma").setExecutor(cmaCommand);
            getCommand("cma").setTabCompleter(cmaCommand);
        }

        getLogger().info("CMA plugin bekapcsolva! Betöltött aliasok: " + aliasManager.getAliasCount());
    }

    @Override
    public void onDisable() {
        if (aliasManager != null) {
            aliasManager.unregisterAll();
        }
        getLogger().info("CMA plugin kikapcsolva.");
    }

    public AliasManager getAliasManager() {
        return aliasManager;
    }
}
