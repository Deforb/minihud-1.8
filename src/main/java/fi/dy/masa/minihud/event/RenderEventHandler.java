package fi.dy.masa.minihud.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.minihud.config.Configs;

public class RenderEventHandler
{
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
}
