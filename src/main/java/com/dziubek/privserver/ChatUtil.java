package com.dziubek.privserver;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

/**
 * Buduje komendy w czacie, które po najechaniu pokazują opis/użycie (tooltip), a po kliknięciu
 * wstawiają tę komendę do wpisania w czacie gracza - zamiast zwykłego, martwego tekstu.
 */
final class ChatUtil {

    private ChatUtil() {
    }

    /** Sama "hoverowalna" komenda (np. do wklejenia w środek dłuższej linijki tekstu). */
    static Component command(String command, String description) {
        return Component.text(command, NamedTextColor.GOLD)
                .hoverEvent(HoverEvent.showText(Component.text(description, NamedTextColor.GRAY)))
                .clickEvent(ClickEvent.suggestCommand(command));
    }

    /** Cała linijka pomocy: hoverowalna komenda + szary opis obok - do /help i innych list komend. */
    static Component helpLine(String command, String description) {
        return command(command, description).append(Component.text(" - " + description, NamedTextColor.GRAY));
    }

    static void sendHelpLine(CommandSender sender, String command, String description) {
        sender.sendMessage(helpLine(command, description));
    }

    /** "Użycie: <hoverowalna komenda>" na czerwono - do komunikatów o złym wywołaniu komendy. */
    static void sendUsage(CommandSender sender, String command, String description) {
        sender.sendMessage(Component.text("Użycie: ", NamedTextColor.RED).append(command(command, description)));
    }

    /** Zdanie z hoverowalną komendą wklejoną w środku, np. "Zaznacz kolejny albo /cmd." */
    static Component sentenceWithCommand(String prefix, String command, String description, String suffix,
                                          NamedTextColor textColor) {
        return Component.text(prefix, textColor).append(command(command, description)).append(Component.text(suffix, textColor));
    }
}
