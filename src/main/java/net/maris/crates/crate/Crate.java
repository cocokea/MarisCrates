package net.maris.crates.crate;

import org.bukkit.Location;
import java.util.*;

public final class Crate {
    private final String name;
    private String color;
    private Location location;
    private final List<CrateReward> rewards = new ArrayList<>();
    public Crate(String name, String color, Location location) { this.name = name.toLowerCase(Locale.ROOT); this.color = color; this.location = location; }
    public String name() { return name; }
    public String color() { return color; }
    public void color(String color) { this.color = color; }
    public Location location() { return location; }
    public void location(Location location) { this.location = location; }
    public List<CrateReward> rewards() { return rewards; }
}
