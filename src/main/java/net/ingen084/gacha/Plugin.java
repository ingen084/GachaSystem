package net.ingen084.gacha;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.google.gson.Gson;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import net.ingen084.gacha.gui.GuiItem;
import net.ingen084.gacha.gui.GuiService;
import net.ingen084.gacha.gui.GuiWindow;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;

public class Plugin extends JavaPlugin implements Listener {
    public static Plugin Instance;

    List<GachaConfig> gachas = new ArrayList<>();
    Gson gson;
    GuiService guiService;

    @Override
    public void onEnable() {
        Instance = this;
        guiService = new GuiService(this);

        if (!getDataFolder().exists())
            getDataFolder().mkdir();

        Bukkit.getPluginManager().registerEvents(this, this);

        gson = new Gson();
        loadFile();

        for (var p : Bukkit.getOnlinePlayers())
            guiService.addPlayer(p);
    }

    @Override
    public void onDisable() {
        saveFile();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length <= 0) {
            sendUsage(sender);
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("このコマンドはプレイヤーから送信してください。");
            return true;
        }
        Player player = (Player) sender;

        switch (args[0]) {
            case "list": {
                sender.sendMessage(ChatColor.BOLD + "" + ChatColor.GREEN + "--== gachalist start ==--");
                for (var gacha : gachas) {
                    sender.spigot().sendMessage(new ComponentBuilder("- ").color(ChatColor.GRAY)

                            .append("[↓]")
                            .event(new HoverEvent(Action.SHOW_TEXT,
                                    new ComponentBuilder("アイテムを取得").color(ChatColor.DARK_PURPLE).create()))
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gacha get " + gacha.id)).italic(true)
                            .color(ChatColor.RED)

                            .append(" ")

                            .append("✎")
                            .event(new HoverEvent(Action.SHOW_TEXT,
                                    new ComponentBuilder("編集").color(ChatColor.DARK_PURPLE).create()))
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gacha edit " + gacha.id))
                            .italic(true).color(ChatColor.GREEN)

                            .append(" " + gacha.id).italic(true).color(ChatColor.WHITE)

                            .append(" ")

                            .append("✖")
                            .event(new HoverEvent(Action.SHOW_TEXT,
                                    new ComponentBuilder("削除").color(ChatColor.DARK_PURPLE).create()))
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/gacha remove " + gacha.id))
                            .italic(true).color(ChatColor.DARK_RED).create());
                }
                sender.sendMessage(ChatColor.BOLD + "" + ChatColor.GREEN + "--== gachalist end ==--");
            }
                break;
            case "remove":
                if (args.length <= 1) {
                    sendUsage(sender);
                    return true;
                } {
                var id = args[1];
                GachaConfig config = null;
                for (var g : gachas) {
                    if (g.id.equals(id)) {
                        config = g;
                        break;
                    }
                }
                if (config == null) {
                    sender.sendMessage(ChatColor.RED + "指定したガチャIDが見つかりませんでした。");
                    return true;
                }

                final var c = config;
                var window = new GuiWindow(config.id + " を削除してもよろしいですか？", 3, null);
                window.setItem(3, 1,
                        new GuiItem("はい", Arrays.asList(new String[] { ChatColor.DARK_PURPLE + "このガチャセットは削除されます。",
                                ChatColor.DARK_PURPLE + "ガチャアイテムが使用できなくなります。" }), Material.LIME_WOOL, 1, e -> {
                                    if (e.getClick() != ClickType.LEFT)
                                        return;
                                    var p = ((Player) e.getWhoClicked());
                                    p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

                                    if (gachas.contains(c)) {
                                        gachas.remove(c);
                                        e.getWhoClicked().sendMessage(ChatColor.RED + c.id + " は削除されました。");
                                    }
                                    e.getWhoClicked().closeInventory();
                                }));
                window.setItem(5, 1, new GuiItem("いいえ", Arrays.asList(new String[] { "何もせずこの画面を閉じます。" }),
                        Material.RED_WOOL, 1, e -> {
                            if (e.getClick() != ClickType.LEFT)
                                return;
                            e.getWhoClicked().closeInventory();
                        }));
                guiService.openGui(player, window);
            }
                break;
            case "get":
                if (args.length <= 1) {
                    sendUsage(sender);
                    return true;
                } {
                var id = args[1];
                GachaConfig config = null;
                for (var g : gachas) {
                    if (g.id.equals(id)) {
                        config = g;
                        break;
                    }
                }
                if (config == null) {
                    sender.sendMessage(ChatColor.RED + "指定したガチャIDが見つかりませんでした。");
                    return true;
                }

                var stack = new ItemStack(Material.TRIPWIRE_HOOK);
                var meta = stack.getItemMeta();
                meta.getPersistentDataContainer().set(new NamespacedKey(this, "GachaId"), PersistentDataType.STRING,
                        id);
                meta.setDisplayName(ChatColor.RESET + "" + ChatColor.LIGHT_PURPLE + "ガチャ『" + id + "』トークン");
                meta.setLore(Arrays.asList(new String[] { "右クリックで開封" }));
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                stack.setItemMeta(meta);

                player.getInventory().addItem(stack);
            }
                break;
            case "edit":
                if (args.length <= 1) {
                    sendUsage(sender);
                    return true;
                } {
                var id = args[1];
                GachaConfig config = null;
                for (var g : gachas) {
                    if (g.id.equals(id)) {
                        config = g;
                        break;
                    }
                }
                if (config == null) {
                    sender.sendMessage(ChatColor.RED + "指定したガチャIDが見つかりませんでした。");
                    return true;
                }

                OpenGachaEditWindow(player, config);
            }
                break;
            case "create":
                if (args.length <= 1) {
                    sendUsage(sender);
                    return true;
                } {
                var id = args[1];
                for (var g : gachas) {
                    if (g.id.equals(id)) {
                        sender.sendMessage(ChatColor.RED + "すでにそのガチャIDは存在しています。");
                        return true;
                    }
                }

                var config = new GachaConfig();
                config.id = id;
                config.items = new ArrayList<>();

                gachas.add(config);
                OpenGachaEditWindow(player, config);
            }
                break;
        }
        return true;
    }

    private void sendUsage(CommandSender sender) {
        if (!sender.isOp()) {
            sender.sendMessage(ChatColor.RED + "Permission denied.");
            return;
        }
        sender.sendMessage("== " + ChatColor.BOLD + "ingenGachaSystem" + ChatColor.RESET + " ==");
        sender.sendMessage("gacha list        ガチャ一覧");
        sender.sendMessage("gacha get <id>    ガチャアイテムを取得");
        sender.sendMessage("gacha remove <id> ガチャを削除");
        sender.sendMessage("gacha create <id> ガチャを作成");
        sender.sendMessage("gacha edit <id>   ガチャを編集");
    }

    private void OpenGachaEditWindow(Player player, GachaConfig config) {
        var window = new GuiWindow("ガチャ " + config.id + " の設定", 6, p -> {
            for (var i : config.items) {
                var meta = i.itemStack.getItemMeta();
                if (meta != null) {
                    meta.setLore(null);
                    i.itemStack.setItemMeta(meta);
                }
            }
            saveFile();
            p.sendMessage("[Gacha]設定ファイルを保存しました。");
        });
        window.setAirClicked(e -> {
            if (e.getClick() != ClickType.LEFT || e.getCursor() == null || e.getCursor().getType().isAir())
                return;

            var i = new GachaItem();
            i.weight = 1;
            i.itemStack = new ItemStack(e.getCursor());
            config.items.add(i);

            updateItems(window, config);
        });
        updateItems(window, config);
        guiService.openGui(player, window);
    }

    private void updateItems(GuiWindow window, GachaConfig config) {
        int totalWeight = 0;
        for (var i : config.items)
            totalWeight += i.weight;

        for (var i = 0; i < window.getRowSize() * 9; i++) {
            if (config.items.size() <= i) {
                window.setItem(i, null);
                continue;
            }
            final var ci = config.items.get(i);
            var meta = ci.itemStack.getItemMeta();
            if (meta == null)
                meta = Bukkit.getItemFactory().getItemMeta(ci.itemStack.getType());

            meta.setLore(Arrays.asList(new String[] {
                    ChatColor.DARK_PURPLE + "重み: " + ci.weight + "("
                            + String.format("%.2f", ((double) ci.weight / totalWeight) * 100) + "%)",
                    ChatColor.DARK_PURPLE + "左クリック: 1増加 +Shiftで10", ChatColor.DARK_PURPLE + "右クリック: 1減少 +Shiftで10",
                    ChatColor.DARK_PURPLE + "0未満でアイテムを消去します。", }));
            ci.itemStack.setItemMeta(meta);

            window.setItem(i, new GuiItem(ci.itemStack, e -> {
                if (e.getClick() == ClickType.LEFT)
                    ci.weight++;
                else if (e.getClick() == ClickType.SHIFT_LEFT)
                    ci.weight += 10;
                else if (e.getClick() == ClickType.RIGHT)
                    ci.weight--;
                else if (e.getClick() == ClickType.RIGHT)
                    ci.weight -= 10;
                else
                    return;

                if (ci.weight <= 0)
                    config.items.remove(ci);

                var p = ((Player) e.getWhoClicked());
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);

                updateItems(window, config);
            }));
        }
    }

    Random random = new Random();

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTokenUse(PlayerInteractEvent event) {
        if ((event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR
         && event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)
         || !event.hasItem())
            return;
        
        var is = event.getItem();
        var meta = is.getItemMeta();
        
        if (is.getType() != Material.TRIPWIRE_HOOK
         || meta == null
         || !meta.getPersistentDataContainer().has(new NamespacedKey(this, "GachaId"), PersistentDataType.STRING))
            return;

        event.setCancelled(true);
        
        var player = event.getPlayer();
        var id = meta.getPersistentDataContainer().get(new NamespacedKey(this, "GachaId"), PersistentDataType.STRING);
        GachaConfig config = null;
        for (var g : gachas) {
            if (g.id.equals(id)) {
                config = g;
                break;
            }
        }
        if (config == null) {
            player.sendMessage(ChatColor.RED + "ガチャ『" + id + "』はすでに期限が切れています。");
            is.setAmount(0);
            return;
        }

        int totalWeight = 0;
        for (var i : config.items)
            totalWeight += i.weight;

        var num = (int)Math.round(random.nextDouble() * totalWeight);
        
        int tmpWeight = 0;
        for (var i : config.items) {
            tmpWeight += i.weight;
            if (tmpWeight >= num) {
                var addResult = player.getInventory().addItem(i.itemStack.clone());
                for (var k : addResult.keySet())
                    player.getWorld().dropItem(player.getLocation(), addResult.get(k));
                
                is.setAmount(is.getAmount() - 1);
                
                var percent = ((double)i.weight / totalWeight) * 100;
                //player.sendMessage(percent + " " + num + "/" + totalWeight);
                if (percent <= 1) {
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    var builder = new StringBuilder(player.getDisplayName() + "さんがガチャ『" + config.id + "』でレアアイテム");
                    if (i.itemStack.getItemMeta().getDisplayName() != "") {
                        builder.append(" " + i.itemStack.getItemMeta().getDisplayName() + " ");
                    }
                    builder.append("を入手しました！");

                    Bukkit.broadcastMessage(builder.toString());
                    return;
                }
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
                return;
            }
        }
        player.sendMessage(ChatColor.RED + "はずれ！！！");
    }

    private void loadFile() {
        gachas.clear();
        for (var k : getConfig().getKeys(false)) {
            var c = new GachaConfig();
            c.id = k;
            c.items = new ArrayList<>();
            for (var s : getConfig().getStringList(k)) {
                var index = s.indexOf(",");
                var item = new GachaItem();
                item.weight = Integer.parseInt(s.substring(0, index));
                item.itemStack = ItemStackSerializer.deserialize(s.substring(index + 1));
                c.items.add(item);
            }
            gachas.add(c);
        }
    }

    private void saveFile() {
        for (var g : gachas) {
            var list = new ArrayList<String>();
            for (var i : g.items)
                list.add(i.weight + "," + ItemStackSerializer.serialize(i.itemStack));
            getConfig().set(g.id, list);
        }
        saveConfig();
    }
}
