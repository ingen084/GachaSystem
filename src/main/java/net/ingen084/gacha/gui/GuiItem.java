package net.ingen084.gacha.gui;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType.PrimitivePersistentDataType;

import net.ingen084.gacha.Plugin;

public class GuiItem {
    private static Random random = new Random();

    private ItemStack item;
    public ItemStack getItemStack() {
        return item;
    }
    private Consumer<InventoryClickEvent> clickedEvent;
    public void onClicked(InventoryClickEvent pl) {
        if (clickedEvent != null)
            clickedEvent.accept(pl);
    }

    public GuiItem(String name, Material m) {
        this(name, null, m, 1, null);
    }
    public GuiItem(String name, List<String> lore, Material m, int amount, Consumer<InventoryClickEvent> clicked) {
        item = new ItemStack(m, amount);
        var meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
        meta.setUnbreakable(true);
        meta.setDisplayName(ChatColor.RESET + name);
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(new NamespacedKey(Plugin.Instance, "guid"), PrimitivePersistentDataType.INTEGER, (Integer)random.nextInt());
        item.setItemMeta(meta);
        clickedEvent = clicked;
    }
    public GuiItem(ItemStack is, Consumer<InventoryClickEvent> clicked) {
        item = is.clone();
        var meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(Plugin.Instance, "guid"), PrimitivePersistentDataType.INTEGER, (Integer)random.nextInt());
        item.setItemMeta(meta);

        clickedEvent = clicked;
    }

    public void setName(String name) {
        item.getItemMeta().setDisplayName(ChatColor.RESET + name);
    }
    public void setLore(List<String> lore) {
        item.getItemMeta().setLore(lore);
    }
}