package tech.elicase.minecraftosc;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import tech.elicase.minecraftosc.core.config.ConfigManager;
import tech.elicase.minecraftosc.platform.forge.ForgeAdapter;

/**
 * MinecraftOSC 模组入口
 */
@Mod(MinecraftOscMod.MOD_ID)
public final class MinecraftOscMod {

    public static final String MOD_ID = "minecraftosc";
    public static final Logger LOGGER = LogUtils.getLogger();

    private final ConfigManager configManager;
    private final ForgeAdapter forgeAdapter;

    public MinecraftOscMod(FMLJavaModLoadingContext context) {
        configManager = new ConfigManager(MinecraftOscForgeConfig.snapshot());
        forgeAdapter = new ForgeAdapter(configManager, LOGGER);
        context.registerConfig(ModConfig.Type.COMMON, MinecraftOscForgeConfig.SPEC);
        context.getModEventBus().addListener(this::onConfigChanged);
        MinecraftForge.EVENT_BUS.register(forgeAdapter);
        LOGGER.info("MinecraftOSC 配置已注册");
    }

    private void onConfigChanged(ModConfigEvent event) {
        if (event.getConfig().getSpec() != MinecraftOscForgeConfig.SPEC) {
            return;
        }
        configManager.update(MinecraftOscForgeConfig.snapshot());
        forgeAdapter.reload();
    }
}
