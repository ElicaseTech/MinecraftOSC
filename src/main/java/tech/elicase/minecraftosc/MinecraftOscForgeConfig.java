package tech.elicase.minecraftosc;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import tech.elicase.minecraftosc.core.config.MinecraftOscSettings;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Forge 公共配置
 */
@Mod.EventBusSubscriber(modid = MinecraftOscMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class MinecraftOscForgeConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final List<String> DEFAULT_OSC_ADDRESSES = List.of("/vrchat/chat", "/chatbox/input");
    private static final List<String> DEFAULT_ALLOWED_SENDERS = List.of("127.0.0.1", "::1");

    public static final ForgeConfigSpec.IntValue LISTEN_PORT = BUILDER
            .comment("OSC UDP 监听端口")
            .defineInRange("listenPort", 9000, 1, 65535);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ACCEPTED_OSC_ADDRESSES = BUILDER
            .comment("允许转换为 Minecraft 聊天的 OSC 地址")
            .defineListAllowEmpty("acceptedOscAddresses", DEFAULT_OSC_ADDRESSES, MinecraftOscForgeConfig::validateNonBlank);

    public static final ForgeConfigSpec.BooleanValue ALLOW_ANY_SENDER = BUILDER
            .comment("是否允许任意来源地址发送 OSC 消息")
            .define("allowAnySender", false);

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ALLOWED_SENDERS = BUILDER
            .comment("允许的来源 IP 列表")
            .defineListAllowEmpty("allowedSenders", DEFAULT_ALLOWED_SENDERS, MinecraftOscForgeConfig::validateNonBlank);

    public static final ForgeConfigSpec.ConfigValue<String> CHAT_PREFIX = BUILDER
            .comment("注入到 Minecraft 时使用的聊天前缀")
            .define("chatPrefix", "[VRChat] ");

    public static final ForgeConfigSpec.IntValue MAX_MESSAGE_LENGTH = BUILDER
            .comment("聊天消息最大长度")
            .defineInRange("maxMessageLength", 256, 1, 4096);

    public static final ForgeConfigSpec.IntValue MAX_QUEUE_SIZE = BUILDER
            .comment("待派发消息队列容量")
            .defineInRange("maxQueueSize", 200, 1, 4096);

    public static final ForgeConfigSpec.IntValue MAX_RETRIES = BUILDER
            .comment("聊天注入失败后的最大重试次数")
            .defineInRange("maxRetries", 2, 0, 32);

    public static final ForgeConfigSpec.IntValue RETRY_DELAY_TICKS = BUILDER
            .comment("重试延迟 tick 数")
            .defineInRange("retryDelayTicks", 20, 1, 20 * 60);

    public static final ForgeConfigSpec.IntValue RATE_LIMIT_MESSAGES = BUILDER
            .comment("速率限制窗口内允许的最大消息数")
            .defineInRange("rateLimitMessages", 8, 1, 512);

    public static final ForgeConfigSpec.IntValue RATE_LIMIT_WINDOW_SECONDS = BUILDER
            .comment("速率限制窗口秒数")
            .defineInRange("rateLimitWindowSeconds", 5, 1, 3600);

    public static final ForgeConfigSpec.IntValue MAX_DISPATCH_PER_TICK = BUILDER
            .comment("每个服务器 tick 最多注入的聊天数")
            .defineInRange("maxDispatchPerTick", 8, 1, 256);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private MinecraftOscForgeConfig() {
    }

    /**
     * 构建核心层配置快照
     */
    public static MinecraftOscSettings snapshot() {
        return new MinecraftOscSettings(
                LISTEN_PORT.get(),
                normalizeSet(ACCEPTED_OSC_ADDRESSES.get()),
                ALLOW_ANY_SENDER.get(),
                normalizeSet(ALLOWED_SENDERS.get()),
                CHAT_PREFIX.get(),
                MAX_MESSAGE_LENGTH.get(),
                MAX_QUEUE_SIZE.get(),
                MAX_RETRIES.get(),
                RETRY_DELAY_TICKS.get(),
                RATE_LIMIT_MESSAGES.get(),
                RATE_LIMIT_WINDOW_SECONDS.get(),
                MAX_DISPATCH_PER_TICK.get()
        );
    }

    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            MinecraftOscMod.LOGGER.info("MinecraftOSC 配置已加载: 端口={}, 队列容量={}", LISTEN_PORT.get(), MAX_QUEUE_SIZE.get());
        }
    }

    private static boolean validateNonBlank(Object value) {
        return value instanceof String string && !string.trim().isEmpty();
    }

    private static Set<String> normalizeSet(List<? extends String> values) {
        Set<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            String trimmed = value.trim();
            if (!trimmed.isEmpty()) {
                normalized.add(trimmed);
            }
        }
        return normalized;
    }
}
