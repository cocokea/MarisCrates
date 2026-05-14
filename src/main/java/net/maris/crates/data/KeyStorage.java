package net.maris.crates.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.maris.crates.MarisCratesPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class KeyStorage {
    private final MarisCratesPlugin plugin;
    private final Map<String, Long> cache = new ConcurrentHashMap<>();
    private final ExecutorService writer = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "MarisCrates-DB");
        thread.setDaemon(true);
        return thread;
    });
    private HikariDataSource ds;
    private boolean mysql;

    public KeyStorage(MarisCratesPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() throws SQLException {
        HikariConfig cfg = new HikariConfig();
        String type = plugin.getConfig().getString("storage.type", "sqlite");
        mysql = type.equalsIgnoreCase("mysql");
        if (mysql) {
            String host = plugin.getConfig().getString("storage.mysql.host");
            int port = plugin.getConfig().getInt("storage.mysql.port");
            String db = plugin.getConfig().getString("storage.mysql.database");
            cfg.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
            cfg.setUsername(plugin.getConfig().getString("storage.mysql.username"));
            cfg.setPassword(plugin.getConfig().getString("storage.mysql.password"));
            cfg.setMaximumPoolSize(plugin.getConfig().getInt("storage.mysql.pool-size", 10));
            cfg.setMinimumIdle(1);
        } else {
            File file = new File(plugin.getDataFolder(), "keys.db");
            cfg.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath() + "?journal_mode=WAL&busy_timeout=5000");
            cfg.setMaximumPoolSize(1);
        }
        ds = new HikariDataSource(cfg);
        try (Connection c = ds.getConnection(); Statement st = c.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS mariscrates_keys(uuid VARCHAR(36) NOT NULL, crate VARCHAR(64) NOT NULL, amount BIGINT NOT NULL DEFAULT 0, PRIMARY KEY(uuid, crate))");
        }
        loadCache();
    }

    public long get(UUID uuid, String crate) {
        return cache.getOrDefault(key(uuid, crate), 0L);
    }

    public long total(String crate) {
        String suffix = ":" + normalizeCrate(crate);
        long total = 0L;
        for (Map.Entry<String, Long> entry : cache.entrySet()) {
            if (entry.getKey().endsWith(suffix)) {
                total += Math.max(0L, entry.getValue());
            }
        }
        return total;
    }

    public void add(UUID uuid, String crate, long delta) {
        if (delta == 0L) {
            return;
        }
        String key = key(uuid, crate);
        cache.compute(key, (ignored, current) -> Math.max(0L, (current == null ? 0L : current) + delta));
        writeAsync(() -> addDatabase(uuid, crate, delta));
    }

    public boolean tryTake(UUID uuid, String crate, long amount) {
        if (amount <= 0L) {
            return true;
        }
        AtomicBoolean taken = new AtomicBoolean(false);
        String key = key(uuid, crate);
        cache.compute(key, (ignored, current) -> {
            long value = current == null ? 0L : current;
            if (value < amount) {
                return value;
            }
            taken.set(true);
            return value - amount;
        });
        if (taken.get()) {
            writeAsync(() -> takeDatabase(uuid, crate, amount));
        }
        return taken.get();
    }

    public void set(UUID uuid, String crate, long amount) {
        long clamped = Math.max(0L, amount);
        cache.put(key(uuid, crate), clamped);
        writeAsync(() -> setDatabase(uuid, crate, clamped));
    }

    public void close() {
        writer.shutdown();
        try {
            if (!writer.awaitTermination(5L, TimeUnit.SECONDS)) {
                writer.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            writer.shutdownNow();
        }
        cache.clear();
        if (ds != null) {
            ds.close();
        }
    }

    private void loadCache() throws SQLException {
        cache.clear();
        try (Connection c = ds.getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT uuid, crate, amount FROM mariscrates_keys")) {
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String crate = rs.getString("crate");
                long amount = Math.max(0L, rs.getLong("amount"));
                cache.put(key(uuid, crate), amount);
            }
        }
    }

    private void writeAsync(Runnable runnable) {
        writer.execute(() -> {
            try {
                runnable.run();
            } catch (RuntimeException exception) {
                plugin.getLogger().warning("Cannot persist crate keys: " + exception.getMessage());
            }
        });
    }

    private void addDatabase(UUID uuid, String crate, long delta) {
        String sql = mysql
                ? "INSERT INTO mariscrates_keys(uuid, crate, amount) VALUES(?,?,GREATEST(0,?)) ON DUPLICATE KEY UPDATE amount=GREATEST(0, amount + ?)"
                : "INSERT INTO mariscrates_keys(uuid, crate, amount) VALUES(?,?,MAX(0,?)) ON CONFLICT(uuid, crate) DO UPDATE SET amount=MAX(0, amount + ?)";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, normalizeCrate(crate));
            ps.setLong(3, delta);
            ps.setLong(4, delta);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void takeDatabase(UUID uuid, String crate, long amount) {
        String sql = mysql ? "UPDATE mariscrates_keys SET amount=GREATEST(0, amount-?) WHERE uuid=? AND crate=?" : "UPDATE mariscrates_keys SET amount=MAX(0, amount-?) WHERE uuid=? AND crate=?";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, amount);
            ps.setString(2, uuid.toString());
            ps.setString(3, normalizeCrate(crate));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setDatabase(UUID uuid, String crate, long amount) {
        String sql = mysql
                ? "INSERT INTO mariscrates_keys(uuid, crate, amount) VALUES(?,?,?) ON DUPLICATE KEY UPDATE amount=?"
                : "INSERT INTO mariscrates_keys(uuid, crate, amount) VALUES(?,?,?) ON CONFLICT(uuid, crate) DO UPDATE SET amount=excluded.amount";
        try (Connection c = ds.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, normalizeCrate(crate));
            ps.setLong(3, amount);
            if (mysql) {
                ps.setLong(4, amount);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String key(UUID uuid, String crate) {
        return uuid + ":" + normalizeCrate(crate);
    }

    private String normalizeCrate(String crate) {
        return crate == null ? "" : crate.toLowerCase(Locale.ROOT);
    }
}

