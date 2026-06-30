package tech.elicase.minecraftosc;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import tech.elicase.minecraftosc.core.config.ConfigManager;
import tech.elicase.minecraftosc.platform.forge.ReloadableAdapter;

/**
 * MinecraftOSC 模组入口
 */
@Mod(MinecraftOscMod.MOD_ID)
public final class MinecraftOscMod {

    public static final String MOD_ID = "minecraftosc";
    public static final Logger LOGGER = LogUtils.getLogger();

    private final ConfigManager configManager;
    private final ReloadableAdapter platformAdapter;

    public MinecraftOscMod(FMLJavaModLoadingContext context) {
        configManager = new ConfigManager(MinecraftOscForgeConfig.defaults());
        platformAdapter = createPlatformAdapter(configManager);
        context.registerConfig(ModConfig.Type.COMMON, MinecraftOscForgeConfig.SPEC);
        context.getModEventBus().addListener(this::onConfigChanged);
        LOGGER.info("MinecraftOSC 配置已注册");
    }

    private void onConfigChanged(ModConfigEvent event) {
        if (event.getConfig().getSpec() != MinecraftOscForgeConfig.SPEC) {
            return;
        }
        configManager.update(MinecraftOscForgeConfig.snapshot());
        if (platformAdapter != null) {
            platformAdapter.reload();
        }
    }

    private ReloadableAdapter createPlatformAdapter(ConfigManager configManager) {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return null;
        }

        try {
            Class<?> bootstrapClass = Class.forName("tech.elicase.minecraftosc.platform.forge.MinecraftOscClientBootstrap");
            return (ReloadableAdapter) bootstrapClass
                    .getMethod("initialize", ConfigManager.class, Logger.class)
                    .invoke(null, configManager, LOGGER);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("客户端桥接适配器初始化失败", exception);
        }
    }
}
