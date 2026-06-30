package tech.elicase.minecraftosc.platform.forge;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import tech.elicase.minecraftosc.MinecraftOscForgeConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * MinecraftOSC Forge 配置界面
 */
public final class MinecraftOscConfigScreen extends Screen {

    private static final int PANEL_WIDTH = 420;
    private static final int ENTRY_HEIGHT = 64;
    private static final int LIST_TOP = 44;
    private static final int FOOTER_HEIGHT = 60;

    private final Screen parent;
    private final List<EditBox> editBoxes = new ArrayList<>();

    private EditBox listenPortBox;
    private EditBox acceptedAddressesBox;
    private CycleButton<Boolean> allowAnySenderButton;
    private EditBox allowedSendersBox;
    private EditBox chatPrefixBox;
    private EditBox maxMessageLengthBox;
    private EditBox maxQueueSizeBox;
    private EditBox maxRetriesBox;
    private EditBox retryDelayTicksBox;
    private EditBox rateLimitMessagesBox;
    private EditBox rateLimitWindowSecondsBox;
    private EditBox maxDispatchPerTickBox;

    private ConfigList configList;
    private Button saveButton;
    private Component validationError;

    public MinecraftOscConfigScreen(Screen parent) {
        super(Component.translatable("screen.minecraftosc.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        editBoxes.clear();
        int footerTop = this.height - FOOTER_HEIGHT;

        createWidgets();

        configList = addRenderableWidget(new ConfigList());
        configList.addConfigEntry(new TextFieldEntry(
                Component.translatable("screen.minecraftosc.config.label.listen_port"),
                Component.translatable("screen.minecraftosc.config.hint.listen_port"),
                listenPortBox
        ));
        configList.addConfigEntry(new TextFieldEntry(
                Component.translatable("screen.minecraftosc.config.label.accepted_addresses"),
                Component.translatable("screen.minecraftosc.config.hint.accepted_addresses"),
                acceptedAddressesBox
        ));
        configList.addConfigEntry(new ToggleEntry(
                Component.translatable("screen.minecraftosc.config.label.allow_any_sender"),
                Component.translatable("screen.minecraftosc.config.hint.allow_any_sender"),
                allowAnySenderButton
        ));
        configList.addConfigEntry(new TextFieldEntry(
                Component.translatable("screen.minecraftosc.config.label.allowed_senders"),
                Component.translatable("screen.minecraftosc.config.hint.allowed_senders"),
                allowedSendersBox
        ));
        configList.addConfigEntry(new TextFieldEntry(
                Component.translatable("screen.minecraftosc.config.label.chat_prefix"),
                Component.translatable("screen.minecraftosc.config.hint.chat_prefix"),
                chatPrefixBox
        ));
        configList.addConfigEntry(new TextFieldEntry(
                Component.translatable("screen.minecraftosc.config.label.max_message_length"),
                Component.translatable("screen.minecraftosc.config.hint.max_message_length"),
                maxMessageLengthBox
        ));
        configList.addConfigEntry(new TextFieldEntry(
                Component.translatable("screen.minecraftosc.config.label.max_queue_size"),
                Component.translatable("screen.minecraftosc.config.hint.max_queue_size"),
                maxQueueSizeBox
        ));
        configList.addConfigEntry(new TextFieldEntry(
                Component.translatable("screen.minecraftosc.config.label.max_retries"),
                Component.translatable("screen.minecraftosc.config.hint.max_retries"),
                maxRetriesBox
        ));
        configList.addConfigEntry(new TextFieldEntry(
                Component.translatable("screen.minecraftosc.config.label.retry_delay_ticks"),
                Component.translatable("screen.minecraftosc.config.hint.retry_delay_ticks"),
                retryDelayTicksBox
        ));
        configList.addConfigEntry(new TextFieldEntry(
                Component.translatable("screen.minecraftosc.config.label.rate_limit_messages"),
                Component.translatable("screen.minecraftosc.config.hint.rate_limit_messages"),
                rateLimitMessagesBox
        ));
        configList.addConfigEntry(new TextFieldEntry(
                Component.translatable("screen.minecraftosc.config.label.rate_limit_window_seconds"),
                Component.translatable("screen.minecraftosc.config.hint.rate_limit_window_seconds"),
                rateLimitWindowSecondsBox
        ));
        configList.addConfigEntry(new TextFieldEntry(
                Component.translatable("screen.minecraftosc.config.label.max_dispatch_per_tick"),
                Component.translatable("screen.minecraftosc.config.hint.max_dispatch_per_tick"),
                maxDispatchPerTickBox
        ));

        saveButton = addRenderableWidget(Button.builder(Component.translatable("screen.minecraftosc.config.save"), button -> saveAndClose())
                .pos(this.width / 2 - 154, footerTop + 28)
                .size(96, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("screen.minecraftosc.config.reset"), button -> resetFields())
                .pos(this.width / 2 - 48, footerTop + 28)
                .size(96, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("screen.minecraftosc.config.cancel"), button -> onClose())
                .pos(this.width / 2 + 58, footerTop + 28)
                .size(96, 20)
                .build());

        validate();
    }

    @Override
    public void tick() {
        for (EditBox editBox : editBoxes) {
            editBox.tick();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        int panelLeft = (this.width - PANEL_WIDTH) / 2;
        int panelRight = panelLeft + PANEL_WIDTH;
        int footerTop = this.height - FOOTER_HEIGHT;

        guiGraphics.fill(panelLeft - 8, LIST_TOP - 10, panelRight + 8, footerTop - 6, 0x7A111827);
        guiGraphics.fill(panelLeft - 8, footerTop, panelRight + 8, this.height - 8, 0xA0121825);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 14, 0xFFFFFF);
        guiGraphics.drawCenteredString(
                this.font,
                Component.translatable("screen.minecraftosc.config.subtitle"),
                this.width / 2,
                28,
                0xB9C1CC
        );

        if (validationError != null) {
            guiGraphics.drawCenteredString(this.font, validationError, this.width / 2, footerTop + 10, 0xFF7B7B);
        } else {
            guiGraphics.drawCenteredString(
                    this.font,
                    Component.translatable("screen.minecraftosc.config.footer_hint"),
                    this.width / 2,
                    footerTop + 10,
                    0x9FB0C0
            );
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    private void createWidgets() {
        listenPortBox = createNumberBox(Integer.toString(MinecraftOscForgeConfig.LISTEN_PORT.get()), 5);
        acceptedAddressesBox = createTextBox(joinList(MinecraftOscForgeConfig.ACCEPTED_OSC_ADDRESSES.get()), 512);
        allowAnySenderButton = CycleButton.onOffBuilder(MinecraftOscForgeConfig.ALLOW_ANY_SENDER.get())
                .create(0, 0, 160, 20, Component.translatable("screen.minecraftosc.config.label.allow_any_sender"), (button, value) -> validate());
        allowedSendersBox = createTextBox(joinList(MinecraftOscForgeConfig.ALLOWED_SENDERS.get()), 512);
        chatPrefixBox = createTextBox(MinecraftOscForgeConfig.CHAT_PREFIX.get(), 128);
        maxMessageLengthBox = createNumberBox(Integer.toString(MinecraftOscForgeConfig.MAX_MESSAGE_LENGTH.get()), 5);
        maxQueueSizeBox = createNumberBox(Integer.toString(MinecraftOscForgeConfig.MAX_QUEUE_SIZE.get()), 5);
        maxRetriesBox = createNumberBox(Integer.toString(MinecraftOscForgeConfig.MAX_RETRIES.get()), 3);
        retryDelayTicksBox = createNumberBox(Integer.toString(MinecraftOscForgeConfig.RETRY_DELAY_TICKS.get()), 5);
        rateLimitMessagesBox = createNumberBox(Integer.toString(MinecraftOscForgeConfig.RATE_LIMIT_MESSAGES.get()), 4);
        rateLimitWindowSecondsBox = createNumberBox(Integer.toString(MinecraftOscForgeConfig.RATE_LIMIT_WINDOW_SECONDS.get()), 4);
        maxDispatchPerTickBox = createNumberBox(Integer.toString(MinecraftOscForgeConfig.MAX_DISPATCH_PER_TICK.get()), 4);
    }

    private EditBox createTextBox(String value, int maxLength) {
        EditBox box = new EditBox(this.font, 0, 0, PANEL_WIDTH - 64, 20, Component.empty());
        box.setMaxLength(maxLength);
        box.setValue(value);
        box.setResponder(ignored -> validate());
        editBoxes.add(box);
        return box;
    }

    private EditBox createNumberBox(String value, int maxLength) {
        EditBox box = createTextBox(value, maxLength);
        box.setFilter(candidate -> candidate.isEmpty() || candidate.matches("-?\\d+"));
        return box;
    }

    private void saveAndClose() {
        validate();
        if (validationError != null) {
            return;
        }

        MinecraftOscForgeConfig.LISTEN_PORT.set(parseInt(listenPortBox.getValue(), label("listen_port"), 1, 65535));
        MinecraftOscForgeConfig.ACCEPTED_OSC_ADDRESSES.set(parseList(acceptedAddressesBox.getValue()));
        MinecraftOscForgeConfig.ALLOW_ANY_SENDER.set(allowAnySenderButton.getValue());
        MinecraftOscForgeConfig.ALLOWED_SENDERS.set(parseList(allowedSendersBox.getValue()));
        MinecraftOscForgeConfig.CHAT_PREFIX.set(chatPrefixBox.getValue());
        MinecraftOscForgeConfig.MAX_MESSAGE_LENGTH.set(parseInt(maxMessageLengthBox.getValue(), label("max_message_length"), 1, 4096));
        MinecraftOscForgeConfig.MAX_QUEUE_SIZE.set(parseInt(maxQueueSizeBox.getValue(), label("max_queue_size"), 1, 4096));
        MinecraftOscForgeConfig.MAX_RETRIES.set(parseInt(maxRetriesBox.getValue(), label("max_retries"), 0, 32));
        MinecraftOscForgeConfig.RETRY_DELAY_TICKS.set(parseInt(retryDelayTicksBox.getValue(), label("retry_delay_ticks"), 1, 20 * 60));
        MinecraftOscForgeConfig.RATE_LIMIT_MESSAGES.set(parseInt(rateLimitMessagesBox.getValue(), label("rate_limit_messages"), 1, 512));
        MinecraftOscForgeConfig.RATE_LIMIT_WINDOW_SECONDS.set(parseInt(rateLimitWindowSecondsBox.getValue(), label("rate_limit_window_seconds"), 1, 3600));
        MinecraftOscForgeConfig.MAX_DISPATCH_PER_TICK.set(parseInt(maxDispatchPerTickBox.getValue(), label("max_dispatch_per_tick"), 1, 256));
        MinecraftOscForgeConfig.SPEC.save();
        onClose();
    }

    private void resetFields() {
        listenPortBox.setValue(Integer.toString(MinecraftOscForgeConfig.LISTEN_PORT.getDefault()));
        acceptedAddressesBox.setValue(joinList(MinecraftOscForgeConfig.ACCEPTED_OSC_ADDRESSES.getDefault()));
        allowAnySenderButton.setValue(MinecraftOscForgeConfig.ALLOW_ANY_SENDER.getDefault());
        allowedSendersBox.setValue(joinList(MinecraftOscForgeConfig.ALLOWED_SENDERS.getDefault()));
        chatPrefixBox.setValue(MinecraftOscForgeConfig.CHAT_PREFIX.getDefault());
        maxMessageLengthBox.setValue(Integer.toString(MinecraftOscForgeConfig.MAX_MESSAGE_LENGTH.getDefault()));
        maxQueueSizeBox.setValue(Integer.toString(MinecraftOscForgeConfig.MAX_QUEUE_SIZE.getDefault()));
        maxRetriesBox.setValue(Integer.toString(MinecraftOscForgeConfig.MAX_RETRIES.getDefault()));
        retryDelayTicksBox.setValue(Integer.toString(MinecraftOscForgeConfig.RETRY_DELAY_TICKS.getDefault()));
        rateLimitMessagesBox.setValue(Integer.toString(MinecraftOscForgeConfig.RATE_LIMIT_MESSAGES.getDefault()));
        rateLimitWindowSecondsBox.setValue(Integer.toString(MinecraftOscForgeConfig.RATE_LIMIT_WINDOW_SECONDS.getDefault()));
        maxDispatchPerTickBox.setValue(Integer.toString(MinecraftOscForgeConfig.MAX_DISPATCH_PER_TICK.getDefault()));
        validate();
    }

    private void validate() {
        try {
            parseInt(listenPortBox.getValue(), label("listen_port"), 1, 65535);
            parseInt(maxMessageLengthBox.getValue(), label("max_message_length"), 1, 4096);
            parseInt(maxQueueSizeBox.getValue(), label("max_queue_size"), 1, 4096);
            parseInt(maxRetriesBox.getValue(), label("max_retries"), 0, 32);
            parseInt(retryDelayTicksBox.getValue(), label("retry_delay_ticks"), 1, 20 * 60);
            parseInt(rateLimitMessagesBox.getValue(), label("rate_limit_messages"), 1, 512);
            parseInt(rateLimitWindowSecondsBox.getValue(), label("rate_limit_window_seconds"), 1, 3600);
            parseInt(maxDispatchPerTickBox.getValue(), label("max_dispatch_per_tick"), 1, 256);
            validationError = null;
        } catch (ValidationException exception) {
            validationError = exception.message;
        }

        if (saveButton != null) {
            saveButton.active = validationError == null;
        }
    }

    private static int parseInt(String value, Component label, int min, int max) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new ValidationException(Component.translatable("screen.minecraftosc.config.error.required", label));
        }

        int parsed;
        try {
            parsed = Integer.parseInt(trimmed);
        } catch (NumberFormatException exception) {
            throw new ValidationException(Component.translatable("screen.minecraftosc.config.error.number", label));
        }

        if (parsed < min || parsed > max) {
            throw new ValidationException(Component.translatable("screen.minecraftosc.config.error.range", label, min, max));
        }
        return parsed;
    }

    private static List<String> parseList(String raw) {
        List<String> values = new ArrayList<>();
        for (String token : raw.split("[,\\n]")) {
            String trimmed = token.trim();
            if (!trimmed.isEmpty()) {
                values.add(trimmed);
            }
        }
        return values;
    }

    private static String joinList(List<? extends String> values) {
        return String.join(", ", values);
    }

    private static Component label(String keySuffix) {
        return Component.translatable("screen.minecraftosc.config.label." + keySuffix);
    }

    private abstract static class ConfigEntry extends ContainerObjectSelectionList.Entry<ConfigEntry> {

        protected final Component label;
        protected final Component hint;

        protected ConfigEntry(Component label, Component hint) {
            this.label = label;
            this.hint = hint;
        }

        @Override
        public void renderBack(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
            int background = hovered ? 0x5E243244 : 0x44202A36;
            guiGraphics.fill(left + 6, top + 4, left + width - 10, top + height - 4, background);
        }
    }

    private final class TextFieldEntry extends ConfigEntry {

        private final EditBox editBox;

        private TextFieldEntry(Component label, Component hint, EditBox editBox) {
            super(label, hint);
            this.editBox = editBox;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
            int contentLeft = left + 18;
            guiGraphics.drawString(MinecraftOscConfigScreen.this.font, label, contentLeft, top + 10, 0xFFFFFF, false);
            guiGraphics.drawString(MinecraftOscConfigScreen.this.font, hint, contentLeft, top + 24, 0x98A5B3, false);

            editBox.setX(contentLeft);
            editBox.setY(top + 38);
            editBox.setWidth(width - 44);
            editBox.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(editBox);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(editBox);
        }
    }

    private final class ToggleEntry extends ConfigEntry {

        private final CycleButton<Boolean> button;

        private ToggleEntry(Component label, Component hint, CycleButton<Boolean> button) {
            super(label, hint);
            this.button = button;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
            int contentLeft = left + 18;
            guiGraphics.drawString(MinecraftOscConfigScreen.this.font, label, contentLeft, top + 10, 0xFFFFFF, false);
            guiGraphics.drawString(MinecraftOscConfigScreen.this.font, hint, contentLeft, top + 24, 0x98A5B3, false);

            button.setX(contentLeft);
            button.setY(top + 38);
            button.setWidth(width - 44);
            button.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return List.of(button);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(button);
        }
    }

    private final class ConfigList extends ContainerObjectSelectionList<ConfigEntry> {

        private ConfigList() {
            super(MinecraftOscConfigScreen.this.minecraft, MinecraftOscConfigScreen.this.width, MinecraftOscConfigScreen.this.height - FOOTER_HEIGHT - 6, LIST_TOP, MinecraftOscConfigScreen.this.height - FOOTER_HEIGHT - 10, ENTRY_HEIGHT);
            setRenderBackground(false);
        }

        private void addConfigEntry(ConfigEntry entry) {
            addEntry(entry);
        }

        @Override
        public int getRowWidth() {
            return PANEL_WIDTH;
        }

        @Override
        protected int getScrollbarPosition() {
            return (MinecraftOscConfigScreen.this.width + PANEL_WIDTH) / 2 - 10;
        }
    }

    private static final class ValidationException extends RuntimeException {

        private final Component message;

        private ValidationException(Component message) {
            this.message = message;
        }
    }
}
