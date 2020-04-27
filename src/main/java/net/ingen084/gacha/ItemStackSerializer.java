package net.ingen084.gacha;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class ItemStackSerializer {
    public static String serialize(ItemStack src) {
        try {
            var outputStream = new ByteArrayOutputStream();
            var dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(src);
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("ItemStack serialize error.", e);
        }
    }
    public static ItemStack deserialize(String source) {
        try {
            var inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(source));
            var dataInput = new BukkitObjectInputStream(inputStream);

            var result = (ItemStack)dataInput.readObject();
            dataInput.close();
            return result;
        } catch (Exception e) {
            throw new IllegalStateException("ItemStack deserialize error.", e);
        }
    }
}