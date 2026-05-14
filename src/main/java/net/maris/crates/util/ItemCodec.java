package net.maris.crates.util;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import java.io.*;
import java.util.Base64;

public final class ItemCodec {
    private ItemCodec() {}
    public static String encode(ItemStack item) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); BukkitObjectOutputStream out = new BukkitObjectOutputStream(bos)) {
            out.writeObject(item);
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        } catch (IOException e) { throw new IllegalStateException(e); }
    }
    public static ItemStack decode(String data) {
        byte[] bytes = Base64.getDecoder().decode(data);
        if (isJavaObjectStream(bytes)) {
            try (BukkitObjectInputStream in = new BukkitObjectInputStream(new ByteArrayInputStream(bytes))) {
                return (ItemStack) in.readObject();
            } catch (IOException | ClassNotFoundException e) { throw new IllegalStateException(e); }
        }
        try (InputStream in = new ByteArrayInputStream(bytes)) {
            return NBT.itemStackFromNBT(NBT.readNBT(in));
        } catch (IOException e) { throw new IllegalStateException(e); }
    }

    private static boolean isJavaObjectStream(byte[] bytes) {
        return bytes.length >= 2 && bytes[0] == (byte) 0xAC && bytes[1] == (byte) 0xED;
    }
}
