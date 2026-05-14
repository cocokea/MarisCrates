package net.maris.crates.util;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {
    private static final Pattern HEX = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private ColorUtil() {
    }

    public static String color(String text) {
        if (text == null) return "";
        Matcher matcher = HEX.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, Matcher.quoteReplacement(ChatColor.of("#" + matcher.group(1)).toString()));
        }
        matcher.appendTail(sb);
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

    public static String stripRaw(String text) {
        return text == null ? "" : text;
    }

    public static String smallCaps(String text) {
        if (text == null) return "";
        StringBuilder out = new StringBuilder();
        for (char ch : text.toLowerCase().toCharArray()) {
            out.append(switch (ch) {
                case 'a' -> '\u1d00';
                case 'b' -> '\u0299';
                case 'c' -> '\u1d04';
                case 'd' -> '\u1d05';
                case 'e' -> '\u1d07';
                case 'f' -> '\ua730';
                case 'g' -> '\u0262';
                case 'h' -> '\u029c';
                case 'i' -> '\u026a';
                case 'j' -> '\u1d0a';
                case 'k' -> '\u1d0b';
                case 'l' -> '\u029f';
                case 'm' -> '\u1d0d';
                case 'n' -> '\u0274';
                case 'o' -> '\u1d0f';
                case 'p' -> '\u1d18';
                case 'q' -> '\u01eb';
                case 'r' -> '\u0280';
                case 's' -> 's';
                case 't' -> '\u1d1b';
                case 'u' -> '\u1d1c';
                case 'v' -> '\u1d20';
                case 'w' -> '\u1d21';
                case 'x' -> 'x';
                case 'y' -> '\u028f';
                case 'z' -> '\u1d22';
                default -> ch;
            });
        }
        return out.toString();
    }
}
