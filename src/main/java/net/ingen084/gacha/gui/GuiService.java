package net.ingen084.gacha.gui;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType.PrimitivePersistentDataType;

import net.ingen084.gacha.Plugin;

public class GuiService implements Listener {
    private Map<UUID, GuiWindow> openingGuiMap = new HashMap<>();

    public GuiService(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openGui(Player pl, GuiWindow w) {
        if (!openingGuiMap.containsKey(pl.getUniqueId()))
            return;
        pl.openInventory(w.getInventory());
        openingGuiMap.put(pl.getUniqueId(), w);
    }
    public void openGui(Player pl, GuiWindow w, int time) {
        Bukkit.getScheduler().runTaskLater(Plugin.Instance, () -> openGui(pl, w), time);
    }

    @EventHandler
    public void playerAdded(PlayerJoinEvent event) {
        addPlayer(event.getPlayer());
    }
    public void addPlayer(Player p) {
        openingGuiMap.put(p.getUniqueId(), null);
    }
    @EventHandler
    public void playerRemoved(PlayerQuitEvent event) {
        openingGuiMap.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClosed(InventoryCloseEvent event) {
        GuiWindow window;
        if ((window = openingGuiMap.get(event.getPlayer().getUniqueId())) == null)
            return;
        openingGuiMap.put(event.getPlayer().getUniqueId(), null);
        window.onClosed((Player)event.getPlayer());
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClicked(InventoryClickEvent event) {
        if (!openingGuiMap.containsKey(event.getWhoClicked().getUniqueId()))
            return;

        GuiWindow window = null;
        if (event.getSlotType() != SlotType.CONTAINER // Containerでないなら無視
         || (window = openingGuiMap.get(event.getWhoClicked().getUniqueId())) == null // そもそもGUIを開いたことになってないなら無視
         || event.getRawSlot() >= window.getRowSize() * 9) // インベントリ領域がクリックされていたら無視
            return;

        if (event.getWhoClicked().getGameMode() == GameMode.CREATIVE && event.getClick() == ClickType.MIDDLE) // クリエ中ボタン用
            return;
        
        event.setCancelled(true); // ここでキャンセル
        
        //event.getWhoClicked().sendMessage(event.getSlotType() + ": " + event.getSlot() + "(" + event.getRawSlot() + ") " + event.getClick() + "  " + window.getItem(event.getSlot()));

        // 空気クリックチェック
        if (window.getItem(event.getSlot()) == null
         || window.getItem(event.getSlot()).getItemStack() == null
         || window.getItem(event.getSlot()).getItemStack().getType() == Material.AIR) {
             window.onAirClicked(event);
            return;
        }
        
        // 本当にそのGUIかチェック
        if (!window.getItem(event.getSlot()).getItemStack().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.Instance, "guid"), PrimitivePersistentDataType.INTEGER)
            .equals(event.getCurrentItem().getItemMeta().getPersistentDataContainer().get(new NamespacedKey(Plugin.Instance, "guid"), PrimitivePersistentDataType.INTEGER))) {
            // 違うようであればインベントリを開いてないことにする
            openingGuiMap.put(event.getWhoClicked().getUniqueId(), null);
            window.onClosed((Player)event.getWhoClicked());
            return;
        }
        
        window.getItem(event.getSlot()).onClicked(event);
    }
}