package net.lomeli.grandmoon;

import net.minecraft.client.renderer.GlStateManager;

public class MoonClientHooks {

    public static MoonClientHooks INSTANCE = new MoonClientHooks();

    private float moonSize = 0;

    public void changeMoonSize() {
        moonSize += 6f;
        if (moonSize >= 90f)
            moonSize = 0f;
    }

    public void resetSize() {
        moonSize = 0f;
    }

    public void moonSizeHook() {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, moonSize, 0);
    }

    public void moonSizeReset() {
        GlStateManager.popMatrix();
    }
}
