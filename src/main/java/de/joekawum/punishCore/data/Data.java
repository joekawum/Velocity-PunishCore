package de.joekawum.punishCore.data;

import net.kyori.adventure.text.Component;

public class Data {

    public static Component prefix() {
        return Component.text("§9§lBlockHeaven §8• §7");
    }

    public static String getPrefix() {
        return prefix().toString();
    }

    public static Component text(String text) {
        return prefix().append(Component.text(text));
    }

}
