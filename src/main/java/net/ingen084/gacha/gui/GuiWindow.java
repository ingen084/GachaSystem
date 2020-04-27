package net.ingen084.gacha.gui;

import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class GuiWindow {
    private String title;
    public String getTitle() {
        return title;
    }
    private int rows;
    public int getRowSize() {
        return rows;
    }
    private GuiItem[] items;

    public GuiWindow(String t, int rowCount, Consumer<Player> closed) {
        title = t;
        rows = rowCount;
        items = new GuiItem[rowCount * 9];
        closedEvent = closed;
    }
    
    public GuiWindow setItem(int col, int row, GuiItem item) {
        return setItem(col + row * 9, item);
    }
    public GuiWindow setItem(int slot, GuiItem item) {
        items[slot] = item;
        if (inventory != null) {
            if (item != null)
                inventory.setItem(slot, item.getItemStack());
            else
                inventory.setItem(slot, null);
        }
        return this;
    }
    public GuiItem getItem(int col, int row) {
        return getItem(row * 9 + col);
    }
    public GuiItem getItem(int slot) {
        if (items.length < slot)
            return null;
        return items[slot];
    }

    Inventory inventory;
    public Inventory getInventory() {
        if (inventory == null) {
            inventory = Bukkit.createInventory(null, getRowSize() * 9, getTitle());
            for (int i = 0; i < getRowSize() * 9; i++)
                if (getItem(i) != null)
                    inventory.setItem(i, getItem(i).getItemStack());
        }
        return inventory;
    }

    private Consumer<Player> closedEvent;
    public void onClosed(Player pl) {
        if (closedEvent != null)
            closedEvent.accept(pl);
    }

    public void setAirClicked(Consumer<InventoryClickEvent> e) {
        airClickEvent = e;
    }
    private Consumer<InventoryClickEvent> airClickEvent;
    public void onAirClicked(InventoryClickEvent e) {
        if (airClickEvent != null)
            airClickEvent.accept(e);
    }
}