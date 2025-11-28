package fi.dy.masa.minihud.event;

import org.lwjgl.input.Keyboard;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.minihud.config.Configs;
import fi.dy.masa.minihud.proxy.ClientProxy;

@SideOnly(Side.CLIENT)
public class InputEventHandler
{
    @SubscribeEvent
    public void onKeyInputEvent(KeyInputEvent event)
    {
        int key = Keyboard.getEventKey();
        boolean state = Keyboard.getEventKeyState();

        if (state == true && key == ClientProxy.keyToggleMode.getKeyCode())
        {
            RenderEventHandler.getInstance().toggleEnabled();
        }

        if (state == true && key == ClientProxy.keyLightLevelOverlay.getKeyCode())
        {
            Configs.lightLevelOverlayEnabled = !Configs.lightLevelOverlayEnabled;
            Configs.config.get(Configs.CATEGORY_GENERIC, "lightLevelOverlayEnabled", false).set(Configs.lightLevelOverlayEnabled);
            Configs.config.save();
        }
    }
}
