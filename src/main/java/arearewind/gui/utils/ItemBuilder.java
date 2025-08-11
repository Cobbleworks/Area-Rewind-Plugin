package arearewind.gui.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for creating ItemStacks for GUIs
 */
public class ItemBuilder {
    private ItemStack item;
    private ItemMeta meta;

    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(Material material, int amount) {
        this.item = new ItemStack(material, amount);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder name(String name) {
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        return this;
    }

    public ItemBuilder lore(String... lore) {
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(loreList);
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        List<String> coloredLore = new ArrayList<>();
        for (String line : lore) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        return this;
    }

    public ItemBuilder addLore(String... lore) {
        List<String> currentLore = meta.getLore();
        if (currentLore == null)
            currentLore = new ArrayList<>();

        for (String line : lore) {
            currentLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(currentLore);
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        meta.addEnchant(enchantment, level, true);
        return this;
    }

    public ItemBuilder glow() {
        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public ItemBuilder hideFlags(ItemFlag... flags) {
        meta.addItemFlags(flags);
        return this;
    }

    public ItemBuilder hideAll() {
        meta.addItemFlags(ItemFlag.values());
        return this;
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder skullOwner(String owner) {
        if (meta instanceof SkullMeta) {
            ((SkullMeta) meta).setOwner(owner);
        }
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }

    // Common GUI items
    public static ItemStack createBackButton() {
        return new ItemBuilder(Material.SPECTRAL_ARROW)
                .name("&7« Back")
                .lore("&7Click to go back")
                .build();
    }

    public static ItemStack createCloseButton() {
        return new ItemBuilder(Material.BARRIER)
                .name("&cClose")
                .lore("&7Click to close this menu")
                .build();
    }

    public static ItemStack createRefreshButton() {
        return new ItemBuilder(Material.EMERALD)
                .name("&aRefresh")
                .lore("&7Click to refresh this menu")
                .build();
    }

    public static ItemStack createNextPageButton() {
        return new ItemBuilder(Material.ARROW)
                .name("&7Next Page »")
                .lore("&7Click to go to the next page")
                .build();
    }

    public static ItemStack createPrevPageButton() {
        return new ItemBuilder(Material.ARROW)
                .name("&7« Previous Page")
                .lore("&7Click to go to the previous page")
                .build();
    }

    public static ItemStack createFillerGlass() {
        return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .build();
    }

    public static ItemStack createFillerGlass(Material glassType) {
        return new ItemBuilder(glassType)
                .name(" ")
                .build();
    }

    public static ItemStack createInfoItem(String title, String... info) {
        return new ItemBuilder(Material.PAPER)
                .name("&e" + title)
                .lore(info)
                .build();
    }

    public static ItemStack createToggleItem(String name, boolean enabled, String description) {
        Material material = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
        String status = enabled ? "&aEnabled" : "&cDisabled";

        return new ItemBuilder(material)
                .name("&7" + name + ": " + status)
                .lore("&7" + description, "", "&7Click to toggle")
                .build();
    }

    public static ItemStack createConfirmItem() {
        return new ItemBuilder(Material.LIME_CONCRETE)
                .name("&aConfirm")
                .lore("&7Click to confirm this action")
                .build();
    }

    public static ItemStack createCancelItem() {
        return new ItemBuilder(Material.RED_CONCRETE)
                .name("&cCancel")
                .lore("&7Click to cancel this action")
                .build();
    }
}
