package fi.dy.masa.minihud.event;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.IMerchant;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.client.resources.I18n;
import fi.dy.masa.minihud.config.Configs;

public class VillagerTradeRenderer
{
    private static final int TRADE_LIST_WIDTH = 150;
    private static final int TRADE_ITEM_HEIGHT = 20;
    private static final int SCROLL_BAR_WIDTH = 6;

    private final Minecraft mc;
    private int scrollOffset = 0;

    public VillagerTradeRenderer()
    {
        this.mc = Minecraft.getMinecraft();
    }

    public void renderVillagerTrades(int screenWidth, int screenHeight, 
                                     MerchantRecipeList recipes, IMerchant merchant)
    {
        if (recipes == null || recipes.size() == 0)
        {
            return;
        }

        int startX = 10;
        int startY = 10;
        int boxWidth = TRADE_LIST_WIDTH;
        int boxHeight = Math.min(TRADE_ITEM_HEIGHT * 12, screenHeight - 40);

        // Draw background panel
        Gui.drawRect(startX, startY, startX + boxWidth, startY + boxHeight + 20, 0xC0000000);
        Gui.drawRect(startX + 1, startY + 1, startX + boxWidth - 1, startY + boxHeight + 19, 0xFF1F1F1F);

        // Draw title
        FontRenderer fontRenderer = this.mc.fontRendererObj;
        String title = I18n.format("minihud.villager.trades", recipes.size());
        fontRenderer.drawStringWithShadow(title, startX + 8, startY + 5, 0xFFFFFF);

        // Draw trades
        int tradeY = startY + 20;
        int maxVisibleTrades = boxHeight / TRADE_ITEM_HEIGHT;
        int endIndex = Math.min(this.scrollOffset + maxVisibleTrades, recipes.size());

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableLighting();

        for (int i = this.scrollOffset; i < endIndex; i++)
        {
            MerchantRecipe recipe = (MerchantRecipe) recipes.get(i);
            this.renderTradeItem(recipe, startX + 4, tradeY, fontRenderer);
            tradeY += TRADE_ITEM_HEIGHT;
        }

        GlStateManager.disableLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();

        // Draw scrollbar if needed
        if (recipes.size() > maxVisibleTrades)
        {
            this.renderScrollBar(startX + boxWidth - SCROLL_BAR_WIDTH - 2, startY + 20,
                                 recipes.size(), maxVisibleTrades, boxHeight);
        }
    }

    private void renderTradeItem(MerchantRecipe recipe, int x, int y, FontRenderer fontRenderer)
    {
        ItemStack inputItem1 = recipe.getItemToBuy();
        ItemStack inputItem2 = recipe.getSecondItemToBuy();
        ItemStack outputItem = recipe.getItemToSell();

        TradeDatabase.TradeInfo info1 = TradeDatabase.getTradeInfo(inputItem1, outputItem);
        TradeDatabase.TradeInfo info2 = TradeDatabase.getTradeInfo(inputItem2, outputItem);

        int currentX = x;

        // Draw Input 1
        if (inputItem1 != null)
        {
            Integer color = null;
            if (info1 != null && !info1.isOutputRange)
            {
                color = getColorForTrade(inputItem1.stackSize, info1.min, info1.max, false);
            }
            
            renderItemWithColoredStackSize(inputItem1, currentX, y + 2, fontRenderer, color);
            
            if (info1 != null && !info1.isOutputRange)
            {
                String rangeText = info1.getRangeString();
                
                GlStateManager.pushMatrix();
                float scale = 0.75f;
                GlStateManager.scale(scale, scale, 1.0f);
                fontRenderer.drawString(rangeText, (int)((currentX + 18) / scale), (int)((y + 8) / scale), 0xFFFFFF);
                GlStateManager.popMatrix();
                currentX += 18 + (int)(fontRenderer.getStringWidth(rangeText) * scale) + 2;
            }
            else
            {
                currentX += 18;
            }
        }

        // Draw Input 2
        if (inputItem2 != null)
        {
            fontRenderer.drawStringWithShadow("+", currentX, y + 6, 0xFFFFFF);
            currentX += 8;
            
            Integer color = null;
            if (info2 != null && !info2.isOutputRange)
            {
                color = getColorForTrade(inputItem2.stackSize, info2.min, info2.max, false);
            }

            renderItemWithColoredStackSize(inputItem2, currentX, y + 2, fontRenderer, color);
            
            if (info2 != null && !info2.isOutputRange)
            {
                String rangeText = info2.getRangeString();

                GlStateManager.pushMatrix();
                float scale = 0.75f;
                GlStateManager.scale(scale, scale, 1.0f);
                fontRenderer.drawString(rangeText, (int)((currentX + 18) / scale), (int)((y + 8) / scale), 0xFFFFFF);
                GlStateManager.popMatrix();
                currentX += 18 + (int)(fontRenderer.getStringWidth(rangeText) * scale) + 2;
            }
            else
            {
                currentX += 18;
            }
        }

        // Draw Arrow
        fontRenderer.drawStringWithShadow("->", currentX, y + 6, 0xFFFFFF);
        currentX += 12;

        // Draw Output
        if (outputItem != null)
        {
            // Check if we have output-based range info from either input
            TradeDatabase.TradeInfo outputInfo = null;
            if (info1 != null && info1.isOutputRange) outputInfo = info1;
            else if (info2 != null && info2.isOutputRange) outputInfo = info2;
            
            Integer color = null;
            if (outputInfo != null)
            {
                color = getColorForTrade(outputItem.stackSize, outputInfo.min, outputInfo.max, true);
            }
            
            renderItemWithColoredStackSize(outputItem, currentX, y + 2, fontRenderer, color);
            
            if (outputInfo != null)
            {
                String rangeText = outputInfo.getRangeString();
                
                GlStateManager.pushMatrix();
                float scale = 0.75f;
                GlStateManager.scale(scale, scale, 1.0f);
                fontRenderer.drawString(rangeText, (int)((currentX + 18) / scale), (int)((y + 8) / scale), 0xFFFFFF);
                GlStateManager.popMatrix();
            }
        }

        // Draw disabled state if needed
        if (recipe.isRecipeDisabled())
        {
            Gui.drawRect(x - 2, y, x + TRADE_LIST_WIDTH - 20, y + TRADE_ITEM_HEIGHT, 0x80FF0000);
        }
    }

    private void renderItemWithColoredStackSize(ItemStack stack, int x, int y, FontRenderer fontRenderer, Integer color)
    {
        if (stack == null) return;

        this.mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);

        if (color != null && stack.stackSize > 1)
        {
            ItemStack copy = stack.copy();
            copy.stackSize = 1;
            this.mc.getRenderItem().renderItemOverlays(fontRenderer, copy, x, y);
            
            String s = String.valueOf(stack.stackSize);
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.disableBlend();
            fontRenderer.drawStringWithShadow(s, x + 19 - 2 - fontRenderer.getStringWidth(s), y + 6 + 3, color);
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            GlStateManager.enableBlend();
        }
        else
        {
            this.mc.getRenderItem().renderItemOverlays(fontRenderer, stack, x, y);
        }
    }

    private int getColorForTrade(int value, int min, int max, boolean higherIsBetter)
    {
        if (min == max) return 0xFFFFFF;
        
        int range = max - min;
        float normalized = (float)(value - min) / range;
        if (normalized < 0) normalized = 0;
        if (normalized > 1) normalized = 1;
        
        // 0.0 - 0.33: Bad/Good
        // 0.33 - 0.66: Average
        // 0.66 - 1.0: Good/Bad
        
        if (higherIsBetter)
        {
            if (normalized > 0.66) return 0x55FF55; // Green (Good)
            if (normalized > 0.33) return 0xFFFF55; // Yellow (Average)
            return 0xFF5555; // Red (Bad)
        }
        else
        {
            if (normalized < 0.33) return 0x55FF55; // Green (Good)
            if (normalized < 0.66) return 0xFFFF55; // Yellow (Average)
            return 0xFF5555; // Red (Bad)
        }
    }

    private void renderScrollBar(int x, int y, int totalTrades, int maxVisible, int trackHeight)
    {
        int scrollBarHeight = trackHeight;
        float scrollPercentage = (float) this.scrollOffset / (totalTrades - maxVisible);
        int thumbHeight = Math.max(10, (int)((float)maxVisible / totalTrades * trackHeight));
        int thumbY = (int) (y + scrollPercentage * (trackHeight - thumbHeight));

        // Draw scroll track
        Gui.drawRect(x, y, x + SCROLL_BAR_WIDTH, y + trackHeight, 0xFF8B8B8B);
        
        // Draw scroll thumb
        Gui.drawRect(x, thumbY, x + SCROLL_BAR_WIDTH, thumbY + thumbHeight, 0xFFFFFFFF);
    }

    public void handleMouseScroll(int direction, int totalTrades, int maxVisibleTrades)
    {
        if (totalTrades <= maxVisibleTrades)
        {
            return;
        }

        int maxScroll = totalTrades - maxVisibleTrades;
        this.scrollOffset += direction;
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, maxScroll));
    }

    public void resetScroll()
    {
        this.scrollOffset = 0;
    }
}
