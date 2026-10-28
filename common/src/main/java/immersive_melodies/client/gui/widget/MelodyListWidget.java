package immersive_melodies.client.gui.widget;

import immersive_melodies.client.gui.ImmersiveMelodiesScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Objects;

public class MelodyListWidget extends ObjectSelectionList<MelodyListWidget.MelodyEntry> {
    private final ImmersiveMelodiesScreen currentScreen;
    private final boolean showSelection;

    public MelodyListWidget(Minecraft client, ImmersiveMelodiesScreen currentScreen, int left, int width, int listY, int listH, boolean showSelection) {
        super(client, width, currentScreen.height, listY, listY + listH, 10);

        this.currentScreen = currentScreen;

        this.x0 = left;
        this.x1 = this.x0 + width;

        this.showSelection = showSelection;

        setRenderBackground(false);
        setRenderTopAndBottom(false);
        setRenderHeader(false, 0);
    }

    @Override
    public void clearEntries() {
        super.clearEntries();
    }

    public void addEntry(ResourceLocation identifier, Component name, Runnable onPress) {
        super.addEntry(new MelodyEntry(identifier, name, onPress));
    }

    @Override
    public void replaceEntries(Collection<MelodyEntry> newEntries) {
        super.replaceEntries(newEntries);
    }

    @Override
    protected int getScrollbarPosition() {
        return x0 + width + 2;
    }

    @Override
    public int getRowWidth() {
        return width;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x0 && mouseX <= x0 + width + 10 && mouseY >= this.y0 && mouseY <= this.y1;
    }

    @Override
    protected void enableScissor(GuiGraphics context) {
        context.enableScissor(x0 - 15, this.y0, x0 + width, this.y1);
    }

    @Override
    protected void renderSelection(GuiGraphics context, int y, int entryWidth, int entryHeight, int borderColor, int fillColor) {
        if (showSelection) {
            context.fill(x0 - 1, y - 1, x0 + width, y + entryHeight + 3, 0x40000000);
        }
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f) {
        this.setScrollAmount(this.getScrollAmount() - f * (double) this.itemHeight);
        return true;
    }

    public class MelodyEntry extends ObjectSelectionList.Entry<MelodyEntry> {
        final ResourceLocation identifier;
        final Component name;
        final Runnable onPress;

        public MelodyEntry(ResourceLocation identifier, Component melody, Runnable onPress) {
            this.identifier = identifier;
            this.name = melody;
            this.onPress = onPress;
        }

        @Override
        public void render(GuiGraphics context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            context.drawString(currentScreen.getTextRenderer(), name, x0 + (onPress == null ? -2 : 2), y + 1, 0x404040, false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0 && onPress != null) {
                onPress.run();
                return true;
            }
            return false;
        }

        @Override
        public Component getNarration() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MelodyEntry that)) return false;
            return Objects.equals(identifier, that.identifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier);
        }
    }
}