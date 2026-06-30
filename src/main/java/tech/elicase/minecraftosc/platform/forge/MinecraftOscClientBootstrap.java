package tech.elicase.minecraftosc.platform.forge;

import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import org.slf4j.Logger;
import tech.elicase.minecraftosc.core.config.ConfigManager;

/**
 * Forge 客户端初始化入口
 */
public final class MinecraftOscClientBootstrap {

    private MinecraftOscClientBootstrap() {
    }

    public static ReloadableAdapter initialize(ConfigManager configManager, Logger logger) {
        ForgeAdapter adapter = new ForgeAdapter(configManager, logger);
        MinecraftForge.EVENT_BUS.register(adapter);
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(MinecraftOscConfigScreen::new)
        );
        return adapter;
    }
}
