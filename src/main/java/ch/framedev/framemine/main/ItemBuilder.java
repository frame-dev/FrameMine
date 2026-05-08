package ch.framedev.framemine.main;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

public final class ItemBuilder {

    private final ItemStack itemStack;
    private final ItemMeta itemMeta;

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder(Material material) {
        this.itemStack = new ItemStack(material);
        this.itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder setDisplayName(String displayName) {
        if (itemMeta == null) return this;
        itemMeta.setDisplayName(displayName);
        return this;
    }

    public ItemBuilder addEnchantment(Enchantment enchantment, int level, boolean ignoreLevelRestriction) {
        if (itemMeta == null || enchantment == null) return this;
        itemMeta.addEnchant(enchantment, level, ignoreLevelRestriction);
        return this;
    }

    public ItemStack build() {
        if (itemMeta != null) {
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    public ItemBuilder hideEnchantments() {
        if (itemMeta == null) return this;
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }
}
