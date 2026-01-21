package fi.dy.masa.minihud.event;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class TradeDatabase
{
    public static class TradeInfo
    {
        public final int min;
        public final int max;
        public final boolean isOutputRange; // true if range applies to output quantity (Yield), false if input (Cost)

        public TradeInfo(int min, int max, boolean isOutputRange)
        {
            this.min = min;
            this.max = max;
            this.isOutputRange = isOutputRange;
        }
        
        public String getRangeString()
        {
            if (min == max) return String.valueOf(min);
            return min + "-" + max;
        }
    }

    private static final Map<String, TradeInfo> TRADE_INFO_MAP = new HashMap<String, TradeInfo>();

    static
    {
        // Farmer (Input Varies)
        addInputRange(Items.wheat, Items.emerald, 18, 22);
        addInputRange(Items.potato, Items.emerald, 15, 19);
        addInputRange(Items.carrot, Items.emerald, 15, 19);
        addInputRange(Item.getItemFromBlock(Blocks.pumpkin), Items.emerald, 8, 13);
        addInputRange(Items.melon, Items.emerald, 7, 12);
        
        // Fisherman / Fletcher (Input Varies)
        addInputRange(Items.string, Items.emerald, 15, 20);
        
        // Shepherd (Input Varies)
        addInputRange(Item.getItemFromBlock(Blocks.wool), Items.emerald, 16, 22);
        
        // Butcher / Blacksmiths (Input Varies)
        addInputRange(Items.coal, Items.emerald, 16, 24);
        addInputRange(Items.porkchop, Items.emerald, 14, 18);
        addInputRange(Items.chicken, Items.emerald, 14, 18);
        
        // Leatherworker (Input Varies)
        addInputRange(Items.leather, Items.emerald, 9, 12);
        
        // Librarian (Input Varies)
        addInputRange(Items.paper, Items.emerald, 24, 36);
        addInputRange(Items.book, Items.emerald, 8, 10);
        addInputRange(Items.written_book, Items.emerald, 2, 2);
        
        // Priest (Input Varies)
        addInputRange(Items.rotten_flesh, Items.emerald, 36, 40);
        addInputRange(Items.gold_ingot, Items.emerald, 8, 10);
        
        // Blacksmiths (Input Varies)
        addInputRange(Items.iron_ingot, Items.emerald, 7, 9);
        addInputRange(Items.diamond, Items.emerald, 3, 4);
        
        // Buying items (Emerald -> Item)
        
        // Librarian
        addOutputRange(Items.emerald, Item.getItemFromBlock(Blocks.glass), 3, 5); // Yield varies
        addInputRange(Items.emerald, Items.compass, 10, 12); // Cost varies
        addInputRange(Items.emerald, Item.getItemFromBlock(Blocks.bookshelf), 3, 4); // Cost varies
        addInputRange(Items.emerald, Items.clock, 10, 12); // Cost varies
        addInputRange(Items.emerald, Items.name_tag, 20, 22); // Cost varies
        
        // Priest
        addOutputRange(Items.emerald, Items.redstone, 1, 4); // Yield varies
        addOutputRange(Items.emerald, Items.dye, 1, 2); // Lapis Lazuli (dye damage 4), Yield varies
        addInputRange(Items.emerald, Items.ender_pearl, 4, 7); // Cost varies
        addOutputRange(Items.emerald, Items.glowstone_dust, 1, 3); // Yield varies
        addInputRange(Items.emerald, Items.experience_bottle, 3, 11); // Cost varies
        
        // Blacksmiths (Armor) - Cost varies
        addInputRange(Items.emerald, Items.iron_helmet, 4, 6);
        addInputRange(Items.emerald, Items.iron_chestplate, 10, 14);
        addInputRange(Items.emerald, Items.iron_leggings, 8, 10);
        addInputRange(Items.emerald, Items.iron_boots, 4, 6);
        addInputRange(Items.emerald, Items.chainmail_helmet, 5, 7);
        addInputRange(Items.emerald, Items.chainmail_chestplate, 11, 15);
        addInputRange(Items.emerald, Items.chainmail_leggings, 9, 11);
        addInputRange(Items.emerald, Items.chainmail_boots, 5, 7);
        
        // Tools/Weapons - Cost varies
        addInputRange(Items.emerald, Items.iron_shovel, 5, 7);
        addInputRange(Items.emerald, Items.iron_pickaxe, 9, 11);
        addInputRange(Items.emerald, Items.iron_axe, 6, 8);
        addInputRange(Items.emerald, Items.iron_sword, 9, 10);
        
        addInputRange(Items.emerald, Items.diamond_shovel, 8, 10);
        addInputRange(Items.emerald, Items.diamond_pickaxe, 12, 15);
        addInputRange(Items.emerald, Items.diamond_axe, 9, 12);
        addInputRange(Items.emerald, Items.diamond_sword, 12, 15);
        addInputRange(Items.emerald, Items.diamond_chestplate, 16, 19);
        
        // Leather Armor - Cost varies
        addInputRange(Items.emerald, Items.leather_leggings, 2, 4);
        addInputRange(Items.emerald, Items.leather_chestplate, 7, 12);
        addInputRange(Items.emerald, Items.saddle, 8, 10);
        
        // Enchanted Book - Cost varies
        addInputRange(Items.emerald, Items.enchanted_book, 5, 64);
    }

    private static void addInputRange(Item input, Item output, int min, int max)
    {
        TRADE_INFO_MAP.put(getKey(input, output), new TradeInfo(min, max, false));
    }

    private static void addOutputRange(Item input, Item output, int min, int max)
    {
        TRADE_INFO_MAP.put(getKey(input, output), new TradeInfo(min, max, true));
    }

    public static TradeInfo getTradeInfo(ItemStack input, ItemStack output)
    {
        if (input == null || output == null)
        {
            return null;
        }
        
        String key = getKey(input.getItem(), output.getItem());
        return TRADE_INFO_MAP.get(key);
    }

    private static String getKey(Item input, Item output)
    {
        return Item.getIdFromItem(input) + "|" + Item.getIdFromItem(output);
    }
}
