package fi.dy.masa.minihud.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.IMerchant;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.minihud.config.Configs;

@SideOnly(Side.CLIENT)
public class GuiScreenEventHandler
{
    private static GuiScreenEventHandler instance;
    private final Minecraft mc;
    private MerchantRecipeList currentRecipes;
    private IMerchant currentMerchant;
    private final VillagerTradeRenderer villagerTradeRenderer;

    public GuiScreenEventHandler()
    {
        this.mc = Minecraft.getMinecraft();
        this.currentRecipes = null;
        this.currentMerchant = null;
        this.villagerTradeRenderer = new VillagerTradeRenderer();
    }

    public static GuiScreenEventHandler getInstance()
    {
        if (instance == null)
        {
            instance = new GuiScreenEventHandler();
        }
        return instance;
    }

    @SubscribeEvent
    public void onGuiScreenOpen(GuiScreenEvent.InitGuiEvent.Post event)
    {
        GuiScreen gui = event.gui;

        if (gui instanceof GuiMerchant)
        {
            GuiMerchant guiMerchant = (GuiMerchant) gui;
            
            try
            {
                // Access the merchant through reflection
                // Field name "merchant" (obfuscated name might be needed in production, e.g., "field_147037_w")
                IMerchant merchant = ReflectionHelper.getPrivateValue(GuiMerchant.class, guiMerchant, "merchant", "field_147037_w");
                MerchantRecipeList recipes = merchant.getRecipes(this.mc.thePlayer);
                
                this.currentMerchant = merchant;
                this.currentRecipes = recipes;
                System.out.println("MiniHUD: Found merchant with " + (recipes != null ? recipes.size() : 0) + " trades.");
            }
            catch (Exception e)
            {
                System.out.println("MiniHUD: Failed to access merchant data: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @SubscribeEvent
    public void onGuiScreenClose(GuiScreenEvent.InitGuiEvent.Post event)
    {
        GuiScreen gui = event.gui;

        // Clear data when GUI closes
        if (!(gui instanceof GuiMerchant))
        {
            this.currentRecipes = null;
            this.currentMerchant = null;
        }
    }

    @SubscribeEvent
    public void onDrawScreenPost(GuiScreenEvent.DrawScreenEvent.Post event)
    {
        if (Configs.showVillagerTrades && event.gui instanceof GuiMerchant)
        {
            // Update recipes as they might have been received from the server after the GUI opened
            if (this.currentMerchant != null)
            {
                this.currentRecipes = this.currentMerchant.getRecipes(this.mc.thePlayer);
            }

            if (this.currentRecipes != null && this.currentRecipes.size() > 0)
            {
                this.villagerTradeRenderer.renderVillagerTrades(event.gui.width, event.gui.height, this.currentRecipes, this.currentMerchant);
            }
        }
    }

    public MerchantRecipeList getCurrentRecipes()
    {
        return this.currentRecipes;
    }

    public IMerchant getCurrentMerchant()
    {
        return this.currentMerchant;
    }

    public void clearData()
    {
        this.currentRecipes = null;
        this.currentMerchant = null;
    }
}
