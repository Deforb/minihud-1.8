package fi.dy.masa.minihud.config;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.minihud.Reference;
import fi.dy.masa.minihud.event.RenderEventHandler;

public class Configs
{
    public static boolean sortLinesByLength;
    public static boolean sortLinesReversed;
    public static boolean coordinateFormatCustomized;
    public static boolean showCoordinatesScaled;
    public static boolean useFontShadow;
    public static float fontScale;
    public static boolean useTextBackground;

    public static boolean showCoordinates;
    public static boolean showYaw;
    public static boolean showPitch;
    public static boolean showSpeed;
    public static boolean showBiome;
    public static boolean showLight;
    public static boolean showFacing;
    public static boolean showBlockPos;
    public static boolean showChunkPos;
    public static boolean showLookingAt;
    public static boolean showFPS;
    public static boolean showEntities;
    public static boolean showRealTime;
    public static boolean showGameTime;
    public static boolean showMemory;
    public static boolean showValidDoor;
    public static boolean lightLevelOverlayEnabled;

    public static int fontColor;
    public static int textBackgroundColor;
    public static int textPosX;
    public static int textPosY;

    public static String coordinateFormat;
    public static String dateFormatReal;

    public static File configurationFile;
    public static Configuration config;
    
    public static final String CATEGORY_GENERIC = "Generic";
    public static final String CATEGORY_INFO_TOGGLE = "InfoTypes";

    @SubscribeEvent
    public void onConfigChangedEvent(OnConfigChangedEvent event)
    {
        if (Reference.MOD_ID.equals(event.modID) == true)
        {
            loadConfigs(config);
        }
    }

    public static void loadConfigsFromFile(File configFile)
    {
        configurationFile = configFile;
        config = new Configuration(configFile, null, true);
        config.load();

        loadConfigs(config);
    }

    public static void loadConfigs(Configuration conf)
    {
        Property prop;

        conf.setCategoryLanguageKey(CATEGORY_GENERIC, "minihud.config.category.generic");
        conf.setCategoryLanguageKey(CATEGORY_INFO_TOGGLE, "minihud.config.category.info_types");

        // Remove old/unused config options
        if (conf.hasKey(CATEGORY_GENERIC, "defaultMode"))
        {
            conf.getCategory(CATEGORY_GENERIC).remove("defaultMode");
        }
        if (conf.hasKey(CATEGORY_GENERIC, "defaultModeNumeric"))
        {
            conf.getCategory(CATEGORY_GENERIC).remove("defaultModeNumeric");
        }
        if (conf.hasKey(CATEGORY_GENERIC, "useScaledFont"))
        {
            conf.getCategory(CATEGORY_GENERIC).remove("useScaledFont");
        }
        if (conf.hasKey(CATEGORY_INFO_TOGGLE, "infoTimeWorld"))
        {
            conf.getCategory(CATEGORY_INFO_TOGGLE).remove("infoTimeWorld");
        }

        prop = conf.get(CATEGORY_GENERIC, "coordinateFormat", "x: %.1f y: %.1f z: %.1f");
        prop.setLanguageKey("minihud.config.prop.coordinate_format");
        prop.comment = "The format string for the coordinate line (needs to have three %f format strings!) Default: x: %.1f y: %.1f z: %.1f";
        coordinateFormat = prop.getString();

        prop = conf.get(CATEGORY_GENERIC, "dateFormatReal", "HH:mm:ss");
        prop.setLanguageKey("minihud.config.prop.date_format_real");
        prop.comment = "The format string for the real time (Java SimpleDateFormat) Default: HH:mm:ss";
        dateFormatReal = prop.getString();

        prop = conf.get(CATEGORY_GENERIC, "coordinateFormatCustomized", false);
        prop.setLanguageKey("minihud.config.prop.coordinate_format_customized");
        prop.comment = "Use the customized coordinate format string";
        coordinateFormatCustomized = prop.getBoolean();

        prop = conf.get(CATEGORY_GENERIC, "showCoordinatesScaled", false);
        prop.setLanguageKey("minihud.config.prop.show_coordinates_scaled");
        prop.comment = "Show scaled coordinates (Nether <-> Overworld)";
        showCoordinatesScaled = prop.getBoolean();

        prop = conf.get(CATEGORY_GENERIC, "fontColor", "0xE0E0E0");
        prop.setLanguageKey("minihud.config.prop.font_color");
        prop.comment = "Font color (default: 0xE0E0E0 = 14737632)";
        fontColor = getColor(prop.getString(), 0xE0E0E0);

        prop = conf.get(CATEGORY_GENERIC, "sortLinesByLength", false);
        prop.setLanguageKey("minihud.config.prop.sort_lines_by_length");
        prop.comment = "Sort the lines by their text's length";
        sortLinesByLength = prop.getBoolean();

        prop = conf.get(CATEGORY_GENERIC, "sortLinesReversed", false);
        prop.setLanguageKey("minihud.config.prop.sort_lines_reversed");
        prop.comment = "Reverse the line sorting order";
        sortLinesReversed = prop.getBoolean();

        prop = conf.get(CATEGORY_GENERIC, "textBackgroundColor", "0x70505050");
        prop.setLanguageKey("minihud.config.prop.text_background_color");
        prop.comment = "Text background color (default: 0x70505050 = 1884311632)";
        textBackgroundColor = getColor(prop.getString(), 0x70505050);

        prop = conf.get(CATEGORY_GENERIC, "textPosX", 4);
        prop.setLanguageKey("minihud.config.prop.text_pos_x");
        prop.comment = "Text X position (default: 4)";
        textPosX = prop.getInt();

        prop = conf.get(CATEGORY_GENERIC, "textPosY", 4);
        prop.setLanguageKey("minihud.config.prop.text_pos_y");
        prop.comment = "Text Y position (default: 4)";
        textPosY = prop.getInt();

        prop = conf.get(CATEGORY_GENERIC, "useFontShadow", false);
        prop.setLanguageKey("minihud.config.prop.use_font_shadow");
        prop.comment = "Use font shadow";
        useFontShadow = prop.getBoolean();

        prop = conf.get(CATEGORY_GENERIC, "fontScale", 1.0);
        prop.setLanguageKey("minihud.config.prop.font_scale");
        prop.comment = "Font scale factor (default: 1.0)";
        fontScale = (float) prop.getDouble();

        prop = conf.get(CATEGORY_GENERIC, "useTextBackground", true);
        prop.setLanguageKey("minihud.config.prop.use_text_background");
        prop.comment = "Use a solid background color behind the text";
        useTextBackground = prop.getBoolean();

        // Information types individual toggle

        prop = conf.get(CATEGORY_INFO_TOGGLE, "infoFPS", false);
        prop.setLanguageKey("minihud.config.prop.info_fps");
        prop.comment = "Show current FPS";
        showFPS = prop.getBoolean();

        prop = conf.get(CATEGORY_INFO_TOGGLE, "infoMemory", false);
        prop.setLanguageKey("minihud.config.prop.info_memory");
        prop.comment = "Show memory usage";
        showMemory = prop.getBoolean();

        prop = conf.get(CATEGORY_INFO_TOGGLE, "infoGameTime", false);
        prop.setLanguageKey("minihud.config.prop.info_game_time");
        prop.comment = "Show game time (day and time)";
        showGameTime = prop.getBoolean();

        prop = conf.get(CATEGORY_INFO_TOGGLE, "infoCoordinates", false);
        prop.setLanguageKey("minihud.config.prop.info_coordinates");
        prop.comment = "Show player coordinates";
        showCoordinates = prop.getBoolean();

        prop = conf.get(CATEGORY_INFO_TOGGLE, "infoFacing", false);
        prop.setLanguageKey("minihud.config.prop.info_facing");
        prop.comment = "Show player facing";
        showFacing = prop.getBoolean();

        prop = conf.get(CATEGORY_INFO_TOGGLE, "infoBiome", false);
        prop.setLanguageKey("minihud.config.prop.info_biome");
        prop.comment = "Show the current biome";
        showBiome = prop.getBoolean();

        prop = conf.get(CATEGORY_INFO_TOGGLE, "infoEntities", false);
        prop.setLanguageKey("minihud.config.prop.info_entities");
        prop.comment = "Show the entity count";
        showEntities = prop.getBoolean();

        prop = conf.get(CATEGORY_INFO_TOGGLE, "infoLightLevel", false);
        prop.setLanguageKey("minihud.config.prop.info_light_level");
        prop.comment = "Show the current light level";
        showLight = prop.getBoolean();

        prop = conf.get(CATEGORY_INFO_TOGGLE, "infoBlockPosition", false);
        prop.setLanguageKey("minihud.config.prop.info_block_position");
        prop.comment = "Show player's block position";
        showBlockPos = prop.getBoolean();

        prop = conf.get(CATEGORY_INFO_TOGGLE, "infoChunkPosition", false);
        prop.setLanguageKey("minihud.config.prop.info_chunk_position");
        prop.comment = "Show player's current position in the chunk";
        showChunkPos = prop.getBoolean();

        prop = conf.get(CATEGORY_INFO_TOGGLE, "infoRotationYaw", false);
        prop.setLanguageKey("minihud.config.prop.info_rotation_yaw");
        prop.comment = "Show player yaw rotation";
        showYaw = prop.getBoolean();

        prop = conf.get(CATEGORY_INFO_TOGGLE, "infoRotationPitch", false);
        prop.setLanguageKey("minihud.config.prop.info_rotation_pitch");
        prop.comment = "Show player pitch rotation";
        showPitch = prop.getBoolean();

        prop = conf.get(CATEGORY_INFO_TOGGLE, "infoSpeed", false);
        prop.setLanguageKey("minihud.config.prop.info_speed");
        prop.comment = "Show player moving speed";
        showSpeed = prop.getBoolean();

        prop = conf.get(CATEGORY_INFO_TOGGLE, "infoTimeReal", false);
        prop.setLanguageKey("minihud.config.prop.info_time_real");
        prop.comment = "Show real world time";
        showRealTime = prop.getBoolean();

        prop = conf.get(CATEGORY_INFO_TOGGLE, "infoLookingAt", false);
        prop.setLanguageKey("minihud.config.prop.info_looking_at");
        prop.comment = "Show which block the player is looking at";
        showLookingAt = prop.getBoolean();

        prop = conf.get(CATEGORY_INFO_TOGGLE, "infoValidDoor", false);
        prop.setLanguageKey("minihud.config.prop.info_valid_door");
        prop.comment = "Show if the door being looked at is a valid village door";
        showValidDoor = prop.getBoolean();

        prop = conf.get(CATEGORY_GENERIC, "lightLevelOverlayEnabled", false);
        prop.setLanguageKey("minihud.config.prop.light_level_overlay_enabled");
        prop.comment = "Show light level overlay on blocks";
        lightLevelOverlayEnabled = prop.getBoolean();

        if (conf.hasChanged() == true)
        {
            conf.save();
        }
    }

    private static int getColor(String colorStr, int defaultColor)
    {
        Pattern pattern = Pattern.compile("0x([0-9A-F]{1,8})");
        Matcher matcher = pattern.matcher(colorStr);

        if (matcher.matches())
        {
            try { return Integer.parseInt(matcher.group(1), 16); }
            catch (NumberFormatException e) { return defaultColor; }
        }

        try { return Integer.parseInt(colorStr, 10); }
        catch (NumberFormatException e) { return defaultColor; }
    }
}
