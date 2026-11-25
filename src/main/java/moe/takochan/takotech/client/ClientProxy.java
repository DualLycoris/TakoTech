package moe.takochan.takotech.client;

import net.minecraftforge.client.MinecraftForgeClient;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import gregtech.common.render.items.ToolboxRenderer;
import moe.takochan.takotech.client.settings.GameSettings;
import moe.takochan.takotech.common.CommonProxy;
import moe.takochan.takotech.common.loader.ItemLoader;

public class ClientProxy extends CommonProxy {

    // Override CommonProxy methods here, if you want a different behaviour on the client (e.g. registering renders).
    // Don't forget to call the super methods as well.

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        MinecraftForgeClient.registerItemRenderer(ItemLoader.ITEM_TOOLBOX_PLUS, new ToolboxRenderer());

        // 创建游戏设置实例并注册按键绑定
        GameSettings gameSettings = new GameSettings();
        gameSettings.register();
    }
}
