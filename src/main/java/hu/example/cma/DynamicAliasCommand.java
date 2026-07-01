package hu.example.cma;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Egy futásidőben regisztrált parancs, ami meghívja a mögötte lévő "cél" parancsot.
 * Játékos esetén ideiglenesen op jogot ad neki a végrehajtás idejére (majd visszavonja),
 * így a cél parancs jogosultság nélkül is lefut. Konzol / parancsblokk esetén
 * (aminek már úgyis mindenhez joga van) egyszerűen továbbítjuk a parancsot.
 */
public class DynamicAliasCommand extends Command {

    private final String targetCommand;

    public DynamicAliasCommand(String name, String targetCommand) {
        super(name);
        this.targetCommand = targetCommand;
        this.setDescription("Automatikusan generalt alias parancs: /" + name + " -> /" + targetCommand);
        this.setUsage("/" + name);
        this.setPermission(null); // nincs jogosultsagi kovetelmeny
    }

    public String getTargetCommand() {
        return targetCommand;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        String fullCommand = targetCommand;

        if (sender instanceof Player) {
            Player player = (Player) sender;
            fullCommand = fullCommand.replace("%player%", player.getName());
            if (args.length > 0) {
                fullCommand = fullCommand + " " + String.join(" ", args);
            }

            boolean wasOp = player.isOp();
            try {
                if (!wasOp) {
                    player.setOp(true); // ideiglenes jogosultsag-bypass
                }
                Bukkit.dispatchCommand(player, fullCommand);
            } finally {
                if (!wasOp) {
                    player.setOp(false); // visszaallitas
                }
            }
        } else {
            // konzol vagy parancsblokk - ezeknek mar mindenre van joguk
            if (args.length > 0) {
                fullCommand = fullCommand + " " + String.join(" ", args);
            }
            Bukkit.dispatchCommand(sender, fullCommand);
        }
        return true;
    }
}
