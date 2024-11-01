package dev.jaqobb.message_editor.command;

import dev.jaqobb.message_editor.message.MessagePlace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class MessageEditorCommandTabCompleter implements TabCompleter {
    
    private static final Collection<String> POSSIBLE_ARGUMENTS = Arrays.asList("reload", "edit", "activate", "deactivate", "deactivate-all", "deactivateall", "migrate");
    
    @Override
    public List<String> onTabComplete(CommandSender sender, @NotNull Command command, @NotNull String label, String[] arguments) {
        if (!sender.hasPermission("messageeditor.use")) {
            return null;
        }
        List<String> completions = new LinkedList<>();
        if (arguments.length == 0) {
            return completions;
        }
        if (arguments.length == 1) {
            String argument = arguments[0].toLowerCase();
            for (String possibleArgument : POSSIBLE_ARGUMENTS) {
                if (!possibleArgument.startsWith(argument.toLowerCase())) {
                    continue;
                }
                completions.add(possibleArgument);
            }
            return completions;
        }
        if (!arguments[0].equalsIgnoreCase("activate") && !arguments[0].equalsIgnoreCase("deactivate")) {
            return completions;
        }
        for (int index = 1; index < arguments.length; index += 1) {
            for (MessagePlace place : MessagePlace.VALUES) {
                if (!place.name().startsWith(arguments[index].toUpperCase())) {
                    continue;
                }
                if (!place.isSupported()) {
                    continue;
                }
                boolean correctState = arguments[0].equalsIgnoreCase("activate") != place.isAnalyzing();
                if (!correctState) {
                    continue;
                }
                completions.add(place.name());
            }
        }
        return completions;
    }
}
