package tech.elicase.minecraftosc.core.config;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 保存运行时配置快照
 */
public final class ConfigManager {

    private final AtomicReference<MinecraftOscSettings> currentSettings;

    public ConfigManager(MinecraftOscSettings initialSettings) {
        currentSettings = new AtomicReference<>(Objects.requireNonNull(initialSettings, "initialSettings"));
    }

    /**
     * 读取当前配置
     */
    public MinecraftOscSettings current() {
        return currentSettings.get();
    }

    /**
     * 更新当前配置
     */
    public void update(MinecraftOscSettings newSettings) {
        currentSettings.set(Objects.requireNonNull(newSettings, "newSettings"));
    }
}
