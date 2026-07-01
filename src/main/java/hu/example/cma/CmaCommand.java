package hu.example.cma;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * /cma create "parancs" "alias"
 * /cma list
 * /cma edit "alias" "uj alias"
 * /cma remove "alias"
 *
 * Az idezojeles argumentumok kezelese sajat regex-szel tortenik,
 * mert Bukkit alapertelmezetten sima szokoz menten vagja szet az argumentumokat.
 */
public class CmaCommand implements CommandExecutor, TabCompleter {

    private static final Pattern QUOTED_PATTERN = Pattern.compile("\"([^\"]*)\"");

    private final CmaPlugin plugin;
    private final AliasManager aliasManager;

    public CmaCommand(CmaPlugin plugin, AliasManager aliasManager) {
        this.plugin = plugin;
        this.aliasManager = aliasManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        String rest = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        List<String> quoted = extractQuoted(rest);

        switch (sub) {
            case "create": {
                if (quoted.size() < 2) {
                    sender.sendMessage(ChatColor.RED + "Hasznalat: /cma create \"parancs\" \"alias\"");
                    return true;
                }
                String targetCommand = stripLeadingSlash(quoted.get(0));
                String alias = stripLeadingSlash(quoted.get(1));

                boolean success = aliasManager.createAlias(targetCommand, alias);
                if (success) {
                    sender.sendMessage(ChatColor.GREEN + "Alias letrehozva: " + ChatColor.AQUA + "/" + alias
                            + ChatColor.GREEN + " -> " + ChatColor.WHITE + "/" + targetCommand);
                } else {
                    sender.sendMessage(ChatColor.RED + "Ez az alias mar letezik: " + alias);
                }
                return true;
            }
            case "list": {
                Map<String, DynamicAliasCommand> all = aliasManager.getRegisteredCommands();
                if (all.isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "Nincs regisztralt alias.");
                    return true;
                }
                sender.sendMessage(ChatColor.GOLD + "--- CMA Aliasok (" + all.size() + ") ---");
                for (Map.Entry<String, DynamicAliasCommand> entry : all.entrySet()) {
                    sender.sendMessage(ChatColor.AQUA + "/" + entry.getKey() + ChatColor.GRAY + " -> "
                            + ChatColor.WHITE + "/" + entry.getValue().getTargetCommand());
                }
                return true;
            }
            case "edit": {
                if (quoted.size() < 2) {
                    sender.sendMessage(ChatColor.RED + "Hasznalat: /cma edit \"alias\" \"uj alias\"");
                    return true;
                }
                String oldAlias = stripLeadingSlash(quoted.get(0));
                String newAlias = stripLeadingSlash(quoted.get(1));

                boolean success = aliasManager.editAlias(oldAlias, newAlias);
                if (success) {
                    sender.sendMessage(ChatColor.GREEN + "Alias atnevezve: " + ChatColor.AQUA + "/" + oldAlias
                            + ChatColor.GREEN + " -> " + ChatColor.AQUA + "/" + newAlias);
                } else {
                    sender.sendMessage(ChatColor.RED + "Nem sikerult az atnevezes (nem letezo vagy mar foglalt alias).");
                }
                return true;
            }
            case "remove": {
                if (quoted.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + "Hasznalat: /cma remove \"alias\"");
                    return true;
                }
                String alias = stripLeadingSlash(quoted.get(0));
                boolean success = aliasManager.removeAlias(alias);
                if (success) {
                    sender.sendMessage(ChatColor.GREEN + "Alias eltavolitva: /" + alias);
                } else {
                    sender.sendMessage(ChatColor.RED + "Nem talalhato ilyen alias: " + alias);
                }
                return true;
            }
            default:
                sendHelp(sender);
                return true;
        }
    }

    private String stripLeadingSlash(String s) {
        return s.startsWith("/") ? s.substring(1) : s;
    }

    private List<String> extractQuoted(String input) {
        List<String> result = new ArrayList<>();
        Matcher matcher = QUOTED_PATTERN.matcher(input);
        while (matcher.find()) {
            result.add(matcher.group(1));
        }
        return result;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "--- CMA parancsok ---");
        sender.sendMessage(ChatColor.AQUA + "/cma create \"parancs\" \"alias\"" + ChatColor.GRAY + " - Uj alias letrehozasa");
        sender.sendMessage(ChatColor.AQUA + "/cma list" + ChatColor.GRAY + " - Aliasok listazasa");
        sender.sendMessage(ChatColor.AQUA + "/cma edit \"alias\" \"uj alias\"" + ChatColor.GRAY + " - Alias atnevezese");
        sender.sendMessage(ChatColor.AQUA + "/cma remove \"alias\"" + ChatColor.GRAY + " - Alias eltavolitasa");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "list", "edit", "remove").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
