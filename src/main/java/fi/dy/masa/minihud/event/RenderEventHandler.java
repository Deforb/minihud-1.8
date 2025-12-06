package fi.dy.masa.minihud.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import org.lwjgl.input.Keyboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityDropper;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.block.BlockDoor;
import net.minecraft.world.World;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import fi.dy.masa.minihud.Reference;
import fi.dy.masa.minihud.config.Configs;

public class RenderEventHandler
{
    private static final ResourceLocation TEXTURE_LIGHT_LEVEL = new ResourceLocation(Reference.MOD_ID, "textures/misc/light_level_numbers.png");
    private static final ResourceLocation TEXTURE_WIDGETS = new ResourceLocation("textures/gui/container/generic_54.png");
    private static final ResourceLocation TEXTURE_FURNACE = new ResourceLocation("textures/gui/container/furnace.png");
    private static final ResourceLocation TEXTURE_HOPPER = new ResourceLocation("textures/gui/container/hopper.png");
    private static final ResourceLocation TEXTURE_DISPENSER = new ResourceLocation("textures/gui/container/dispenser.png");
    private static final ResourceLocation TEXTURE_BREWING_STAND = new ResourceLocation("textures/gui/container/brewing_stand.png");
    private static RenderEventHandler instance;
    private final Minecraft mc;
    private boolean enabled = true;
    private int fps;
    private int fpsCounter;
    private long fpsUpdateTime = System.currentTimeMillis();

    private class StringHolder implements Comparable<StringHolder>
    {
        public final String str;

        public StringHolder(String str)
        {
            this.str = str;
        }

        @Override
        public int compareTo(StringHolder other)
        {
            int lenThis = this.str.length();
            int lenOther = other.str.length();

            if (lenThis == lenOther)
            {
                return 0;
            }

            return this.str.length() > other.str.length() ? -1 : 1;
        }
    }

    public RenderEventHandler()
    {
        this.mc = Minecraft.getMinecraft();
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event)
    {
        if (this.enabled == false || event.type != ElementType.ALL || this.mc.gameSettings.showDebugInfo == true)
        {
            return;
        }

        List<StringHolder> lines = new ArrayList<StringHolder>();

        this.getLines(lines);

        if (Configs.sortLinesByLength == true)
        {
            Collections.sort(lines);

            if (Configs.sortLinesReversed == true)
            {
                Collections.reverse(lines);
            }
        }

        this.renderText(Configs.textPosX, Configs.textPosY, lines);

        if (Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU))
        {
            this.renderContainerPreview(event.resolution.getScaledWidth(), event.resolution.getScaledHeight());
        }
    }

    public static RenderEventHandler getInstance()
    {
        if (instance == null)
        {
            instance = new RenderEventHandler();
        }

        return instance;
    }

    public void toggleEnabled()
    {
        this.enabled = ! this.enabled;
    }

    private void getLines(List<StringHolder> lines)
    {
        Entity entity = this.mc.getRenderViewEntity();
        BlockPos pos = new BlockPos(entity.posX, entity.getEntityBoundingBox().minY, entity.posZ);

        this.fpsCounter++;

        while (System.currentTimeMillis() >= this.fpsUpdateTime + 1000L)
        {
            this.fps = this.fpsCounter;
            this.fpsUpdateTime += 1000L;
            this.fpsCounter = 0;
        }

        if (Configs.showFPS)
        {
            lines.add(new StringHolder(I18n.format("minihud.format.fps", this.fps)));
        }

        if (Configs.showMemory)
        {
            long max = Runtime.getRuntime().maxMemory();
            long total = Runtime.getRuntime().totalMemory();
            long free = Runtime.getRuntime().freeMemory();
            long used = total - free;

            lines.add(new StringHolder(I18n.format("minihud.format.memory", used * 100L / max, used / 1024 / 1024, max / 1024 / 1024)));
        }

        if (Configs.showGameTime)
        {
            long totalTime = this.mc.theWorld.getWorldTime();
            long day = totalTime / 24000L;
            long timeOfDay = totalTime % 24000L;
            long t = (timeOfDay + 6000L) % 24000L;
            long hours = t / 1000L;
            long minutes = (t % 1000L) * 60L / 1000L;

            lines.add(new StringHolder(I18n.format("minihud.format.game_time", day, String.format("%02d", hours), String.format("%02d", minutes))));
        }

        if (Configs.showCoordinates)
        {
            String coordsText = null;

            if (Configs.coordinateFormatCustomized == true)
            {
                try
                {
                    coordsText = String.format(Configs.coordinateFormat,
                        entity.posX, entity.getEntityBoundingBox().minY, entity.posZ);
                }
                // Uh oh, someone done goofed their format string... :P
                catch (Exception e) { }
            }
            else
            {
                coordsText = I18n.format("minihud.format.coordinates",
                    String.format("%.1f", entity.posX),
                    String.format("%.1f", entity.getEntityBoundingBox().minY),
                    String.format("%.1f", entity.posZ));
            }

            if (coordsText != null)
            {
                if (Configs.showCoordinatesScaled)
                {
                    int dim = entity.worldObj.provider.getDimensionId();
                    if (dim == -1)
                    {
                        coordsText += String.format(" / %s: x: %.1f y: %.1f z: %.1f",
                                I18n.format("minihud.dimension.overworld"),
                                entity.posX * 8.0, entity.getEntityBoundingBox().minY, entity.posZ * 8.0);
                    }
                    else if (dim == 0)
                    {
                        coordsText += String.format(" / %s: x: %.1f y: %.1f z: %.1f",
                                I18n.format("minihud.dimension.nether"),
                                entity.posX / 8.0, entity.getEntityBoundingBox().minY, entity.posZ / 8.0);
                    }
                }

                lines.add(new StringHolder(coordsText));
            }
        }

        if (Configs.showBlockPos)
        {
            lines.add(new StringHolder(I18n.format("minihud.format.block", pos.getX(), pos.getY(), pos.getZ())));
        }

        if (Configs.showChunkPos)
        {
            lines.add(new StringHolder(I18n.format("minihud.format.chunk",
                    pos.getX() & 0xF, pos.getY() & 0xF, pos.getZ() & 0xF,
                    pos.getX() >> 4, pos.getY() >> 4, pos.getZ() >> 4)));
        }

        if (Configs.showYaw || Configs.showPitch || Configs.showSpeed)
        {
            String pre = "";
            StringBuilder str = new StringBuilder(128);

            if (Configs.showYaw)
            {
                str.append(I18n.format("minihud.format.yaw", String.format("%.1f", MathHelper.wrapAngleTo180_float(entity.rotationYaw))));
                pre = " / ";
            }

            if (Configs.showPitch)
            {
                if (pre.length() > 0) str.append(pre);
                str.append(I18n.format("minihud.format.pitch", String.format("%.1f", MathHelper.wrapAngleTo180_float(entity.rotationPitch))));
                pre = " / ";
            }

            if (Configs.showSpeed)
            {
                double dx = entity.posX - entity.lastTickPosX;
                double dy = entity.posY - entity.lastTickPosY;
                double dz = entity.posZ - entity.lastTickPosZ;
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (pre.length() > 0) str.append(pre);
                str.append(I18n.format("minihud.format.speed", String.format("%.3f", dist * 20)));
                pre = " / ";
            }

            lines.add(new StringHolder(str.toString()));
        }

        if (Configs.showFacing)
        {
            EnumFacing facing = entity.getHorizontalFacing();
            String str = I18n.format("minihud.facing.invalid");

            switch (facing)
            {
                case NORTH: str = I18n.format("minihud.facing.north"); break;
                case SOUTH: str = I18n.format("minihud.facing.south"); break;
                case WEST:  str = I18n.format("minihud.facing.west"); break;
                case EAST:  str = I18n.format("minihud.facing.east"); break;
                default:
            }

            lines.add(new StringHolder(I18n.format("minihud.format.facing", facing, str)));
        }

        if (Configs.showLookingAt)
        {
            if (this.mc.objectMouseOver != null &&
                this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK &&
                this.mc.objectMouseOver.getBlockPos() != null)
            {
                BlockPos lookPos = this.mc.objectMouseOver.getBlockPos();
                lines.add(new StringHolder(I18n.format("minihud.format.looking_at", lookPos.getX(), lookPos.getY(), lookPos.getZ())));
            }
        }

        if (this.mc.objectMouseOver != null &&
            this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK &&
            this.mc.objectMouseOver.getBlockPos() != null)
        {
            BlockPos lookPos = this.mc.objectMouseOver.getBlockPos();
            if (this.mc.theWorld.getBlockState(lookPos).getBlock() instanceof BlockDoor)
            {
                boolean isValid = isVillageDoor(this.mc.theWorld, lookPos);
                lines.add(new StringHolder(I18n.format("minihud.format.valid_door", isValid ? I18n.format("gui.yes") : I18n.format("gui.no"))));
            }
        }

        if (Configs.showBiome || Configs.showLight)
        {
            // Prevent a crash when outside of world
            if (pos.getY() >= 0 && pos.getY() < 256 && this.mc.theWorld.isBlockLoaded(pos) == true)
            {
                Chunk chunk = this.mc.theWorld.getChunkFromBlockCoords(pos);

                if (chunk.isEmpty() == false)
                {
                    if (Configs.showBiome)
                    {
                        lines.add(new StringHolder(I18n.format("minihud.format.biome", chunk.getBiome(pos, this.mc.theWorld.getWorldChunkManager()).biomeName)));
                    }

                    if (Configs.showLight)
                    {
                        lines.add(new StringHolder(I18n.format("minihud.format.light",
                                chunk.getLightSubtracted(pos, 0),
                                chunk.getLightFor(EnumSkyBlock.SKY, pos),
                                chunk.getLightFor(EnumSkyBlock.BLOCK, pos))));
                    }
                }
            }
        }

        if (Configs.showEntities)
        {
            String ent = this.mc.renderGlobal.getDebugInfoEntities();

            int p = ent.indexOf(",");
            if (p != -1)
            {
                ent = ent.substring(0, p);
            }

            lines.add(new StringHolder(ent));
        }

        if (Configs.showRealTime)
        {
            lines.add(new StringHolder(I18n.format("minihud.format.time_real", new SimpleDateFormat(Configs.dateFormatReal).format(new Date()))));
        }
    }

    private void renderContainerPreview(int screenWidth, int screenHeight)
    {
        if (this.mc.objectMouseOver == null ||
            this.mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK ||
            this.mc.objectMouseOver.getBlockPos() == null)
        {
            return;
        }

        BlockPos lookPos = this.mc.objectMouseOver.getBlockPos();
        TileEntity te = this.mc.theWorld.getTileEntity(lookPos);

        IInventory inv = null;

        // Try to get the container
        if (this.mc.isSingleplayer())
        {
            try
            {
                MinecraftServer server = this.mc.getIntegratedServer();
                if (server != null)
                {
                    WorldServer worldServer = server.worldServerForDimension(this.mc.theWorld.provider.getDimensionId());
                    if (worldServer != null)
                    {
                        Block block = worldServer.getBlockState(lookPos).getBlock();
                        if (block instanceof BlockChest)
                        {
                            inv = ((BlockChest)block).getLockableContainer(worldServer, lookPos);
                        }
                        else
                        {
                            TileEntity serverTE = worldServer.getTileEntity(lookPos);
                            if (serverTE instanceof IInventory)
                            {
                                inv = (IInventory) serverTE;
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                // Ignore errors accessing server thread data
            }
        }

        if (inv == null)
        {
            Block block = this.mc.theWorld.getBlockState(lookPos).getBlock();
            if (block instanceof BlockChest)
            {
                inv = ((BlockChest)block).getLockableContainer(this.mc.theWorld, lookPos);
            }
            else if (te instanceof IInventory)
            {
                inv = (IInventory) te;
            }
        }

        if (inv == null)
        {
            return;
        }

        int invSize = inv.getSizeInventory();
        
        ResourceLocation texture = TEXTURE_WIDGETS;
        int guiWidth = 176;
        int containerHeight = 0;
        List<int[]> slotPositions = new ArrayList<int[]>();

        if (inv instanceof TileEntityFurnace)
        {
            texture = TEXTURE_FURNACE;
            containerHeight = 71;
            slotPositions.add(new int[]{56, 17}); // Input
            slotPositions.add(new int[]{56, 53}); // Fuel
            slotPositions.add(new int[]{116, 35}); // Output
        }
        else if (inv instanceof TileEntityHopper)
        {
            texture = TEXTURE_HOPPER;
            containerHeight = 40;
            for (int i = 0; i < 5; i++) slotPositions.add(new int[]{44 + i * 18, 20});
        }
        else if (inv instanceof TileEntityDispenser || inv instanceof TileEntityDropper)
        {
            texture = TEXTURE_DISPENSER;
            containerHeight = 71;
            for (int i = 0; i < 9; i++) slotPositions.add(new int[]{62 + (i % 3) * 18, 17 + (i / 3) * 18});
        }
        else if (inv instanceof TileEntityBrewingStand)
        {
            texture = TEXTURE_BREWING_STAND;
            containerHeight = 71;
            slotPositions.add(new int[]{56, 46}); // Bottle 1
            slotPositions.add(new int[]{79, 53}); // Bottle 2
            slotPositions.add(new int[]{102, 46}); // Bottle 3
            slotPositions.add(new int[]{79, 17}); // Ingredient
        }
        else // Chests and others
        {
            int cols = 9;
            int rows = invSize / 9;
            containerHeight = rows * 18 + 17;
            
            for (int i = 0; i < invSize; i++)
            {
                int col = i % cols;
                int row = i / cols;
                slotPositions.add(new int[]{8 + col * 18, 18 + row * 18});
            }
        }
        
        int guiHeight = containerHeight + 7;
        int startX = (screenWidth - guiWidth) / 2;
        int startY = (screenHeight - guiHeight) / 2;

        // Draw background
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(texture);
        this.mc.ingameGUI.drawTexturedModalRect(startX, startY, 0, 0, guiWidth, containerHeight);
        
        // Draw bottom border
        this.mc.getTextureManager().bindTexture(TEXTURE_WIDGETS);
        this.mc.ingameGUI.drawTexturedModalRect(startX, startY + containerHeight, 0, 215, guiWidth, 7);

        // Prepare for item rendering
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 32.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableLighting();
        
        // Force full brightness to ensure items are visible
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
        
        // Ensure Texture2D is enabled
        GlStateManager.enableTexture2D();
        
        // Items need depth test to render correctly
        GlStateManager.enableDepth();
        
        this.mc.getRenderItem().zLevel = 200.0F;
        
        for (int i = 0; i < invSize && i < slotPositions.size(); i++)
        {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack != null)
            {
                int[] pos = slotPositions.get(i);
                int x = startX + pos[0];
                int y = startY + pos[1];
                
                this.mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
                this.mc.getRenderItem().renderItemOverlays(this.mc.fontRendererObj, stack, x, y);
            }
        }
        
        this.mc.getRenderItem().zLevel = 0.0F;
        
        GlStateManager.disableDepth();
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
    }

    private void renderText(int xOff, int yOff, List<StringHolder> lines)
    {
        GlStateManager.pushMatrix();

        if (Configs.fontScale != 1.0f)
        {
            GlStateManager.scale(Configs.fontScale, Configs.fontScale, Configs.fontScale);
        }

        FontRenderer fontRenderer = this.mc.fontRendererObj;

        for (StringHolder holder : lines)
        {
            String line = holder.str;

            if (Configs.useTextBackground == true)
            {
                Gui.drawRect(xOff - 2, yOff - 2, xOff + fontRenderer.getStringWidth(line) + 2, yOff + fontRenderer.FONT_HEIGHT, Configs.textBackgroundColor);
            }

            if (Configs.useFontShadow == true)
            {
                this.mc.ingameGUI.drawString(fontRenderer, line, xOff, yOff, Configs.fontColor);
            }
            else
            {
                fontRenderer.drawString(line, xOff, yOff, Configs.fontColor);
            }

            yOff += fontRenderer.FONT_HEIGHT + 2;
        }

        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event)
    {
        if (Configs.lightLevelOverlayEnabled == false)
        {
            return;
        }

        Entity entity = this.mc.getRenderViewEntity();
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * event.partialTicks;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * event.partialTicks;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * event.partialTicks;

        BlockPos pos = new BlockPos(entity.posX, entity.getEntityBoundingBox().minY, entity.posZ);
        int range = 8; // smaller radius for performance

        GlStateManager.pushMatrix();
        GlStateManager.translate(-x, -y, -z);
        
        GlStateManager.disableLighting();
        // GlStateManager.disableDepth(); // Removed to fix x-ray effect
        GlStateManager.enableDepth(); // Ensure depth testing is enabled
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        
        this.mc.getTextureManager().bindTexture(TEXTURE_LIGHT_LEVEL);

        for (int dx = -range; dx <= range; dx++)
        {
            for (int dy = -range; dy <= range; dy++)
            {
                for (int dz = -range; dz <= range; dz++)
                {
                    BlockPos p = pos.add(dx, dy, dz);
                    if (this.mc.theWorld.isAirBlock(p) && this.mc.theWorld.isSideSolid(p.down(), EnumFacing.UP))
                    {
                        Chunk chunk = this.mc.theWorld.getChunkFromBlockCoords(p);
                        int blockLight = chunk.getLightFor(EnumSkyBlock.BLOCK, p);
                        
                        int color = 0xFFFFFF;
                        if (blockLight <= 7) color = 0xFF0000;
                        else if (blockLight < 14) color = 0xFFFF00;
                        else color = 0x00FF00;

                        // Center of the block
                        // Snap rotation to 90 degree steps
                        float rotation = 180.0F - (float)(Math.round(entity.rotationYaw / 90.0) * 90.0);
                        this.renderNumberTextureAt(blockLight, p.getX() + 0.5, p.getY() + 0.05, p.getZ() + 0.5, color, rotation);
                    }
                }
            }
        }

        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private void renderNumberTextureAt(int number, double x, double y, double z, int color, float rotation)
    {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        
        // Rotate to face player
        GlStateManager.rotate(rotation, 0.0F, 1.0F, 0.0F);
        
        // Shift the quad to center the number (assuming number is in top-left of texture)
        GlStateManager.translate(0.25, 0.0, 0.3);
        
        float r = (float)((color >> 16) & 255) / 255.0F;
        float g = (float)((color >> 8) & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;
        GlStateManager.color(r, g, b, 1.0F);

        int col = number % 4;
        int row = number / 4;
        float uMin = col * 0.25F;
        float uMax = uMin + 0.25F;
        float vMin = row * 0.25F;
        float vMax = vMin + 0.25F;

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        
        float size = 0.5F; // 1.0 block width
        
        // Draw flat on XZ plane (North is -Z)
        // Bottom Left (-X, +Z)
        worldrenderer.pos(-size, 0.0D, size).tex(uMin, vMax).endVertex();
        // Bottom Right (+X, +Z)
        worldrenderer.pos(size, 0.0D, size).tex(uMax, vMax).endVertex();
        // Top Right (+X, -Z)
        worldrenderer.pos(size, 0.0D, -size).tex(uMax, vMin).endVertex();
        // Top Left (-X, -Z)
        worldrenderer.pos(-size, 0.0D, -size).tex(uMin, vMin).endVertex();
        
        tessellator.draw();
        
        GlStateManager.popMatrix();
    }

    private boolean isVillageDoor(World world, BlockPos pos)
    {
        EnumFacing enumfacing = BlockDoor.getFacing(world, pos);
        EnumFacing enumfacing1 = enumfacing.getOpposite();
        int i = this.countBlocksCanSeeSky(world, pos, enumfacing, 5);
        int j = this.countBlocksCanSeeSky(world, pos, enumfacing1, i + 1);

        return i != j;
    }

    private int countBlocksCanSeeSky(World world, BlockPos centerPos, EnumFacing direction, int limitation)
    {
        int i = 0;

        for (int j = 1; j <= 5; ++j)
        {
            if (world.canSeeSky(centerPos.offset(direction, j)))
            {
                ++i;

                if (i >= limitation)
                {
                    return i;
                }
            }
        }

        return i;
    }
}
