package com.stepm.techgunsupgrade.gui;

import com.stepm.techgunsupgrade.items.ItemTicket;
import com.stepm.techgunsupgrade.network.NetworkHandler;
import com.stepm.techgunsupgrade.network.PacketResetUpgrades;
import com.stepm.techgunsupgrade.network.PacketStartUpgrade;
import com.stepm.techgunsupgrade.tileentity.TileEntityUpgradeTable;
import com.stepm.techgunsupgrade.upgrade.UpgradeBuff;
import com.stepm.techgunsupgrade.upgrade.UpgradeData;
import com.stepm.techgunsupgrade.upgrade.UpgradeRarity;
import com.stepm.techgunsupgrade.upgrade.WeaponUpgrades;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@SideOnly(Side.CLIENT)
public class GuiUpgradeTable extends GuiContainer {

    private static final int GUI_WIDTH = 248;
    private static final int GUI_HEIGHT = 198;

    private static final int BUTTON_X = 209;
    private static final int BUTTON_Y = 18;
    private static final int BUTTON_W = 31;
    private static final int BUTTON_H = 63;

    private static final int RESET_X = 7;
    private static final int RESET_Y = 90;
    private static final int RESET_W = 35;
    private static final int RESET_H = 16;

    private static final int CAROUSEL_X = 49;
    private static final int CAROUSEL_Y = 27;
    private static final int CARD_WIDTH = 28;
    private static final int CARD_HEIGHT = 46;
    private static final int CARD_GAP = 2;
    private static final int VISIBLE_CARDS = 5;

    private static final int BUFF_CARD_WIDTH = 48;
    private static final int BUFF_CARD_GAP = 2;
    private static final int BUFF_VISIBLE_CARDS = 3;
    private static final int ROULETTE_DURATION_TICKS = 42;
    private static final int ROULETTE_TOTAL_STEPS = 36;

    private static final UpgradeRarity[] RARITIES = {
            UpgradeRarity.COMMON,
            UpgradeRarity.UNCOMMON,
            UpgradeRarity.RARE,
            UpgradeRarity.EPIC,
            UpgradeRarity.LEGENDARY,
            UpgradeRarity.MYTHIC,
            UpgradeRarity.ULTRA_MYTHIC
    };

    private static final double[][] TICKET_CHANCES = {
            {47.0, 28.0, 15.0, 7.0, 2.0, 0.7, 0.1},
            {40.0, 27.0, 17.0, 9.0, 4.0, 2.5, 0.25},
            {32.0, 24.0, 19.0, 13.0, 7.0, 4.0, 0.5},
            {22.0, 20.0, 19.0, 18.0, 12.0, 7.5, 0.75},
            {0.0, 0.0, 4.0, 15.0, 50.0, 30.0, 1.0},
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 100.0}
    };

    private final TileEntityUpgradeTable tile;
    private final List<RouletteEntry> rouletteEntries = new ArrayList<>();
    private final Random rouletteRandom = new Random();
    private boolean rouletteSpinning;
    private int rouletteTicks;
    private int rouletteStartIndex;
    private int rouletteTargetIndex = -1;
    private int rouletteLastStep = -1;
    private RouletteEntry rouletteResult;

    public GuiUpgradeTable(ContainerUpgradeTable container, TileEntityUpgradeTable tile) {
        super(container);
        this.tile = tile;
        this.xSize = GUI_WIDTH;
        this.ySize = GUI_HEIGHT;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        int relativeX = mouseX - guiLeft;
        int relativeY = mouseY - guiTop;
        if (rouletteSpinning && relativeX >= 214 && relativeX < 236
                && relativeY >= 85 && relativeY < 107) {
            return;
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton != 0) {
            return;
        }
        if (relativeX >= BUTTON_X && relativeX < BUTTON_X + BUTTON_W
                && relativeY >= BUTTON_Y && relativeY < BUTTON_Y + BUTTON_H) {
            onUpgradeButtonClick();
        } else if (relativeX >= RESET_X && relativeX < RESET_X + RESET_W
                && relativeY >= RESET_Y && relativeY < RESET_Y + RESET_H) {
            onResetButtonClick();
        }
    }

    private void onUpgradeButtonClick() {
        if (!isReadyToUpgrade()) {
            return;
        }
        mc.getSoundHandler().playSound(
                PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        beginRoulette();
        NetworkHandler.INSTANCE.sendToServer(new PacketStartUpgrade(tile.getPos()));
    }

    private boolean isReadyToUpgrade() {
        return !rouletteSpinning
                && !tile.isUpgrading()
                && !tile.getStackInSlot(0).isEmpty()
                && !tile.getStackInSlot(1).isEmpty()
                && tile.getStackInSlot(2).isEmpty();
    }

    private void onResetButtonClick() {
        if (!canResetInputWeapon()) return;
        mc.getSoundHandler().playSound(
                PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 0.82F));
        NetworkHandler.INSTANCE.sendToServer(new PacketResetUpgrades(tile.getPos()));
        rouletteResult = null;
        rouletteEntries.clear();
        rouletteTargetIndex = -1;
    }

    private boolean canResetInputWeapon() {
        ItemStack weapon = tile.getStackInSlot(0);
        return !rouletteSpinning && !tile.isUpgrading()
                && tile.getStackInSlot(2).isEmpty()
                && !weapon.isEmpty() && !UpgradeData.getUpgrades(weapon).isEmpty();
    }

    private void beginRoulette() {
        rouletteEntries.clear();
        addWeaponBuffs(tile.getStackInSlot(0), true);
        rouletteSpinning = true;
        rouletteTicks = 0;
        rouletteTargetIndex = -1;
        rouletteLastStep = -1;
        rouletteResult = null;
        rouletteStartIndex = rouletteEntries.isEmpty()
                ? 0
                : rouletteRandom.nextInt(rouletteEntries.size());
    }

    private void addWeaponBuffs(ItemStack weapon, boolean respectTicketChances) {
        if (weapon.isEmpty() || weapon.getItem().getRegistryName() == null) {
            return;
        }

        String weaponId = weapon.getItem().getRegistryName().toString();
        double[] chances = getCurrentChances();
        for (int rarityIndex = 0; rarityIndex < RARITIES.length; rarityIndex++) {
            if (respectTicketChances && chances[rarityIndex] <= 0.0D) {
                continue;
            }
            for (UpgradeBuff buff : WeaponUpgrades.getUpgradesByRarity(
                    weaponId, RARITIES[rarityIndex])) {
                rouletteEntries.add(new RouletteEntry(
                        buff.getId(), buff.getDisplayName(), buff.getRarity()));
            }
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        if (!rouletteSpinning) {
            if (tile.getStackInSlot(2).isEmpty()) {
                rouletteResult = null;
                rouletteEntries.clear();
                rouletteTargetIndex = -1;
            }
            return;
        }

        rouletteTicks++;
        resolveRouletteTarget();

        int step = getRouletteStep();
        if (step != rouletteLastStep) {
            rouletteLastStep = step;
            float pitch = 0.85F + Math.min(0.55F,
                    (float) rouletteTicks / ROULETTE_DURATION_TICKS * 0.55F);
            mc.getSoundHandler().playSound(
                    PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, pitch));
        }

        if (rouletteTicks >= ROULETTE_DURATION_TICKS && rouletteTargetIndex >= 0) {
            rouletteSpinning = false;
            rouletteResult = rouletteEntries.get(rouletteTargetIndex);
            mc.getSoundHandler().playSound(
                    PositionedSoundRecord.getMasterRecord(SoundEvents.BLOCK_NOTE_PLING, 1.45F));
        }
    }

    private void resolveRouletteTarget() {
        if (rouletteTargetIndex >= 0) {
            return;
        }

        RouletteEntry target = getResultEntry();
        if (target == null) {
            return;
        }

        for (int i = 0; i < rouletteEntries.size(); i++) {
            if (rouletteEntries.get(i).id.equals(target.id)) {
                rouletteTargetIndex = i;
                rouletteStartIndex = wrapEntryIndex(i - ROULETTE_TOTAL_STEPS);
                return;
            }
        }

        rouletteEntries.add(target);
        rouletteTargetIndex = rouletteEntries.size() - 1;
        rouletteStartIndex = wrapEntryIndex(rouletteTargetIndex - ROULETTE_TOTAL_STEPS);
    }

    private int getRouletteStep() {
        float progress = Math.min(1.0F,
                (float) rouletteTicks / (float) ROULETTE_DURATION_TICKS);
        float remaining = 1.0F - progress;
        float eased = 1.0F - remaining * remaining * remaining;
        return Math.min(ROULETTE_TOTAL_STEPS,
                (int) Math.floor(eased * ROULETTE_TOTAL_STEPS));
    }

    private boolean isShowingBuffRoulette() {
        if (rouletteSpinning) {
            return true;
        }
        RouletteEntry result = getResultEntry();
        if (result != null) {
            rouletteResult = result;
            if (rouletteEntries.isEmpty()) {
                rouletteEntries.add(result);
                rouletteTargetIndex = 0;
            }
            return true;
        }
        return false;
    }

    private RouletteEntry getDisplayedRouletteEntry() {
        if (rouletteEntries.isEmpty()) {
            return getResultEntry();
        }
        if (!rouletteSpinning && rouletteResult != null) {
            return rouletteResult;
        }
        int index = wrapEntryIndex(rouletteStartIndex + getRouletteStep());
        return rouletteEntries.get(index);
    }

    private RouletteEntry getResultEntry() {
        NBTTagCompound tag = getUpgradeTag(tile.getStackInSlot(2));
        if (tag == null) {
            return null;
        }
        UpgradeRarity rarity = findRarity(tag.getString("buffRarity"));
        if (rarity == null) {
            rarity = UpgradeRarity.COMMON;
        }
        return new RouletteEntry(
                tag.getString("buffId"), tag.getString("buffName"), rarity);
    }

    private UpgradeRarity findRarity(String name) {
        for (UpgradeRarity rarity : RARITIES) {
            if (rarity.getName().equals(name)) {
                return rarity;
            }
        }
        return null;
    }

    private int wrapEntryIndex(int index) {
        if (rouletteEntries.isEmpty()) {
            return 0;
        }
        int result = index % rouletteEntries.size();
        return result < 0 ? result + rouletteEntries.size() : result;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawMachinePanel();
        drawSlotFrames();
        if (isShowingBuffRoulette()) {
            drawBuffRoulette();
        } else {
            drawRarityCarousel();
        }
        drawLever();
        drawResetButton();
    }

    private void drawMachinePanel() {
        int left = guiLeft;
        int top = guiTop;

        drawRect(left, top, left + xSize, top + ySize, 0xFF080B0F);
        drawRect(left + 1, top + 1, left + xSize - 1, top + ySize - 1, 0xFF59616A);
        drawRect(left + 3, top + 3, left + xSize - 3, top + ySize - 3, 0xFF151A20);
        drawRect(left + 5, top + 5, left + xSize - 5, top + 108, 0xFF0C1117);
        drawRect(left + 5, top + 110, left + xSize - 5, top + ySize - 5, 0xFF10151B);

        drawPanel(left + 8, top + 19, 34, 69);
        drawPanel(left + 46, top + 19, 158, 63);
        drawPanel(left + 46, top + 85, 158, 22);
        drawPanel(left + 207, top + 15, 35, 68);
        drawPanel(left + 209, top + 84, 31, 24);

        drawRect(left + 47, top + 20, left + 203, top + 22, 0xFF176E86);
        drawRect(left + 48, top + 21, left + 202, top + 22, 0xFF62D9F2);
        for (int x = left + 8; x < left + xSize - 8; x += 12) {
            drawRect(x, top + 108, Math.min(x + 6, left + xSize - 8), top + 110, 0xFFD2921D);
        }
    }

    private void drawPanel(int x, int y, int width, int height) {
        drawRect(x, y, x + width, y + height, 0xFF59616A);
        drawRect(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF070A0D);
        drawRect(x + 3, y + 3, x + width - 3, y + height - 3, 0xFF121820);
    }

    private void drawSlotFrames() {
        drawSlotFrame(guiLeft + 18, guiTop + 32, 0xFF266D7D);
        drawSlotFrame(guiLeft + 18, guiTop + 62, 0xFF9B6A18);
        drawSlotFrame(guiLeft + 216, guiTop + 87, 0xFF3B8999);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                drawSlotFrame(guiLeft + 43 + column * 18, guiTop + 116 + row * 18, 0xFF353D45);
            }
        }
        for (int column = 0; column < 9; column++) {
            drawSlotFrame(guiLeft + 43 + column * 18, guiTop + 174, 0xFF4C5660);
        }
    }

    private void drawSlotFrame(int x, int y, int accent) {
        drawRect(x - 2, y - 2, x + 18, y + 18, 0xFF06080A);
        drawRect(x - 1, y - 1, x + 17, y + 17, accent);
        drawRect(x, y, x + 16, y + 16, 0xFF0A0E13);
    }

    private void drawRarityCarousel() {
        int centerIndex = getCenterRarityIndex();
        double[] chances = getCurrentChances();

        for (int visibleIndex = 0; visibleIndex < VISIBLE_CARDS; visibleIndex++) {
            int rarityIndex = wrapRarityIndex(centerIndex + visibleIndex - 2);
            UpgradeRarity rarity = RARITIES[rarityIndex];
            int x = guiLeft + CAROUSEL_X + visibleIndex * (CARD_WIDTH + CARD_GAP);
            int y = guiTop + CAROUSEL_Y;
            drawRarityCard(x, y, rarity, chances[rarityIndex], visibleIndex == 2);
        }

        int pointerX = guiLeft + CAROUSEL_X + 2 * (CARD_WIDTH + CARD_GAP) + CARD_WIDTH / 2;
        drawRect(pointerX - 3, guiTop + CAROUSEL_Y - 4, pointerX + 4, guiTop + CAROUSEL_Y - 2, 0xFFFFC443);
        drawRect(pointerX - 2, guiTop + CAROUSEL_Y - 2, pointerX + 3, guiTop + CAROUSEL_Y, 0xFFFFC443);
        drawRect(pointerX - 3, guiTop + CAROUSEL_Y + CARD_HEIGHT + 1,
                pointerX + 4, guiTop + CAROUSEL_Y + CARD_HEIGHT + 3, 0xFFFFC443);
    }

    private void drawBuffRoulette() {
        if (rouletteEntries.isEmpty()) {
            int panelX = guiLeft + CAROUSEL_X;
            int panelY = guiTop + CAROUSEL_Y;
            drawRect(panelX, panelY, panelX + 148, panelY + CARD_HEIGHT, 0xFF050607);
            drawRect(panelX + 1, panelY + 1, panelX + 147,
                    panelY + CARD_HEIGHT - 1, 0xFF16212A);
            String scanning = I18n.format("gui.techgunsupgrade.roulette_scanning");
            fontRenderer.drawString(scanning,
                    panelX + (148 - fontRenderer.getStringWidth(scanning)) / 2,
                    panelY + 19, 0xFF62D9F2);
            return;
        }

        int centerIndex;
        if (!rouletteSpinning && rouletteTargetIndex >= 0) {
            centerIndex = rouletteTargetIndex;
        } else {
            centerIndex = wrapEntryIndex(rouletteStartIndex + getRouletteStep());
        }

        for (int visibleIndex = 0; visibleIndex < BUFF_VISIBLE_CARDS; visibleIndex++) {
            int entryIndex = wrapEntryIndex(centerIndex + visibleIndex - 1);
            RouletteEntry entry = rouletteEntries.get(entryIndex);
            int x = guiLeft + CAROUSEL_X
                    + visibleIndex * (BUFF_CARD_WIDTH + BUFF_CARD_GAP);
            int y = guiTop + CAROUSEL_Y;
            drawBuffCard(x, y, entry, visibleIndex == 1);
        }

        int pointerX = guiLeft + CAROUSEL_X + BUFF_CARD_WIDTH + BUFF_CARD_GAP
                + BUFF_CARD_WIDTH / 2;
        drawRect(pointerX - 4, guiTop + CAROUSEL_Y - 5,
                pointerX + 5, guiTop + CAROUSEL_Y - 3, 0xFFFFC443);
        drawRect(pointerX - 2, guiTop + CAROUSEL_Y - 3,
                pointerX + 3, guiTop + CAROUSEL_Y, 0xFFFFC443);
        drawRect(pointerX - 4, guiTop + CAROUSEL_Y + CARD_HEIGHT + 1,
                pointerX + 5, guiTop + CAROUSEL_Y + CARD_HEIGHT + 3, 0xFFFFC443);
    }

    private void drawBuffCard(int x, int y, RouletteEntry entry, boolean selected) {
        int color = getRarityColor(entry.rarity);
        int border = selected ? 0xFFFFC443 : darkenColor(color, 0.72F);
        int background = darkenColor(color, selected ? 0.28F : 0.16F);

        drawRect(x, y, x + BUFF_CARD_WIDTH, y + CARD_HEIGHT, 0xFF050607);
        drawRect(x + 1, y + 1, x + BUFF_CARD_WIDTH - 1,
                y + CARD_HEIGHT - 1, border);
        drawRect(x + 2, y + 2, x + BUFF_CARD_WIDTH - 2,
                y + CARD_HEIGHT - 2, background);
        drawRect(x + 2, y + 2, x + BUFF_CARD_WIDTH - 2, y + 6, color);

        String rarityCode = getRarityCode(entry.rarity);
        fontRenderer.drawString(rarityCode,
                x + (BUFF_CARD_WIDTH - fontRenderer.getStringWidth(rarityCode)) / 2,
                y + 9, color);

        List<String> nameLines = fontRenderer.listFormattedStringToWidth(
                entry.name.isEmpty() ? "?" : entry.name, BUFF_CARD_WIDTH - 6);
        int lineCount = Math.min(2, nameLines.size());
        for (int line = 0; line < lineCount; line++) {
            String text = fontRenderer.trimStringToWidth(
                    nameLines.get(line), BUFF_CARD_WIDTH - 6);
            fontRenderer.drawString(text,
                    x + (BUFF_CARD_WIDTH - fontRenderer.getStringWidth(text)) / 2,
                    y + 22 + line * 9, selected ? 0xFFF4F7F9 : 0xFFAAB2B8);
        }

        if (selected && rouletteSpinning) {
            int pulseColor = (rouletteTicks / 2) % 2 == 0 ? color : 0xFFFFC443;
            drawRect(x + 4, y + CARD_HEIGHT - 5,
                    x + BUFF_CARD_WIDTH - 4, y + CARD_HEIGHT - 3, pulseColor);
        }
    }

    private void drawRarityCard(int x, int y, UpgradeRarity rarity, double chance, boolean selected) {
        int color = getRarityColor(rarity);
        int darkColor = darkenColor(color, chance <= 0.0D ? 0.10F : 0.30F);
        int borderColor = selected ? 0xFFFFC443 : darkenColor(color, 0.72F);

        drawRect(x, y, x + CARD_WIDTH, y + CARD_HEIGHT, 0xFF050607);
        drawRect(x + 1, y + 1, x + CARD_WIDTH - 1, y + CARD_HEIGHT - 1, borderColor);
        drawRect(x + 2, y + 2, x + CARD_WIDTH - 2, y + CARD_HEIGHT - 2, darkColor);
        drawRect(x + 2, y + 2, x + CARD_WIDTH - 2, y + 6, color);
        drawRect(x + 5, y + 24, x + CARD_WIDTH - 5, y + 26, color);

        String code = getRarityCode(rarity);
        int codeWidth = fontRenderer.getStringWidth(code);
        fontRenderer.drawString(code, x + (CARD_WIDTH - codeWidth) / 2, y + 10, 0xFFF1F4F7);

        String chanceText = formatChance(chance);
        int chanceWidth = fontRenderer.getStringWidth(chanceText);
        fontRenderer.drawString(chanceText, x + (CARD_WIDTH - chanceWidth) / 2, y + 32,
                chance > 0.0D ? 0xFFD8E2E8 : 0xFF555A60);
    }

    private int darkenColor(int argb, float factor) {
        int red = (int) (((argb >> 16) & 0xFF) * factor);
        int green = (int) (((argb >> 8) & 0xFF) * factor);
        int blue = (int) ((argb & 0xFF) * factor);
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }

    private int getRarityColor(UpgradeRarity rarity) {
        TextFormatting formatting = rarity.getColor();
        if (formatting == TextFormatting.WHITE) return 0xFFFFFFFF;
        if (formatting == TextFormatting.GREEN) return 0xFF55FF55;
        if (formatting == TextFormatting.BLUE) return 0xFF55AAFF;
        if (formatting == TextFormatting.DARK_PURPLE) return 0xFFAA55AA;
        if (formatting == TextFormatting.GOLD) return 0xFFFFAA00;
        if (formatting == TextFormatting.LIGHT_PURPLE) return 0xFFFF55FF;
        if (formatting == TextFormatting.RED) return 0xFFFF4444;
        return 0xFFFFFFFF;
    }

    private void drawLever() {
        int x = guiLeft + BUTTON_X;
        int y = guiTop + BUTTON_Y;
        boolean ready = isReadyToUpgrade();
        boolean hasResult = !tile.getStackInSlot(2).isEmpty();
        boolean leverLit = ready || rouletteSpinning;

        drawRect(x + 4, y + 4, x + BUTTON_W - 4, y + BUTTON_H - 15, 0xFF090C10);
        drawRect(x + 6, y + 6, x + BUTTON_W - 6, y + BUTTON_H - 17, 0xFF303841);
        drawRect(x + 13, y + 12, x + 17, y + 45,
                leverLit ? 0xFF9AA4AD : 0xFF525A62);

        int handleY;
        if (rouletteSpinning) {
            handleY = y + 8 + Math.min(31, rouletteTicks * 6);
        } else {
            handleY = hasResult ? y + 39 : y + 8;
        }
        drawRect(x + 8, handleY, x + 23, handleY + 10, 0xFF4B1310);
        drawRect(x + 9, handleY + 1, x + 22, handleY + 8,
                leverLit ? 0xFFD45138 : 0xFF823124);
        drawRect(x + 11, handleY + 1, x + 20, handleY + 3,
                leverLit ? 0xFFFF8060 : 0xFFA94A36);

        int buttonColor = leverLit ? 0xFFFFB52D : 0xFF6A6253;
        drawRect(x + 3, y + BUTTON_H - 14, x + BUTTON_W - 3, y + BUTTON_H - 3, 0xFF080A0C);
        drawRect(x + 4, y + BUTTON_H - 13, x + BUTTON_W - 4, y + BUTTON_H - 4, 0xFF2A3036);
        fontRenderer.drawString("GO", x + (BUTTON_W - fontRenderer.getStringWidth("GO")) / 2,
                y + BUTTON_H - 12, buttonColor);
    }

    private void drawResetButton() {
        int x = guiLeft + RESET_X;
        int y = guiTop + RESET_Y;
        boolean enabled = canResetInputWeapon();
        int border = enabled ? 0xFFB94B42 : 0xFF42484E;
        int fill = enabled ? 0xFF5B1E1B : 0xFF22272C;
        int text = enabled ? 0xFFFFB6AC : 0xFF737A81;

        drawRect(x, y, x + RESET_W, y + RESET_H, 0xFF07090B);
        drawRect(x + 1, y + 1, x + RESET_W - 1, y + RESET_H - 1, border);
        drawRect(x + 2, y + 2, x + RESET_W - 2, y + RESET_H - 2, fill);
        String label = I18n.format("gui.techgunsupgrade.reset");
        label = fontRenderer.trimStringToWidth(label, RESET_W - 4);
        fontRenderer.drawString(label,
                x + (RESET_W - fontRenderer.getStringWidth(label)) / 2,
                y + 4, text);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = I18n.format("gui.techgunsupgrade.title");
        fontRenderer.drawString(title, (xSize - fontRenderer.getStringWidth(title)) / 2, 8, 0xFFF0B53C);

        fontRenderer.drawString(I18n.format("gui.techgunsupgrade.weapon"), 10, 21, 0xFF71CFE4);
        fontRenderer.drawString(I18n.format("gui.techgunsupgrade.ticket"), 10, 51, 0xFFD9A541);
        fontRenderer.drawString(I18n.format("gui.techgunsupgrade.output"), 207, 110, 0xFF65C9DF);
        fontRenderer.drawString(I18n.format("container.inventory"), 43, 105, 0xFFAEB7BF);

        RouletteEntry displayed = getDisplayedRouletteEntry();
        if (displayed != null) {
            String resultText = displayed.name.isEmpty() ? "?" : displayed.name;
            fontRenderer.drawString(fontRenderer.trimStringToWidth(resultText, 147),
                    50, 87, 0xFFF0F3F5);
            String rarityText = rouletteSpinning
                    ? "> " + displayed.rarity.getName() + " <"
                    : displayed.rarity.getName();
            fontRenderer.drawString(fontRenderer.trimStringToWidth(rarityText, 147),
                    50, 98, getRarityColor(displayed.rarity));
        } else {
            fontRenderer.drawString(I18n.format("gui.techgunsupgrade.result_waiting"), 50, 92, 0xFF68727C);
        }
    }

    private String getResultText() {
        NBTTagCompound upgradeTag = getUpgradeTag(tile.getStackInSlot(2));
        return upgradeTag == null ? "" : upgradeTag.getString("buffName");
    }

    private int getSelectedRarityColor() {
        RouletteEntry displayed = getDisplayedRouletteEntry();
        if (displayed != null) {
            return getRarityColor(displayed.rarity);
        }
        String selected = getSelectedRarityName();
        for (UpgradeRarity rarity : RARITIES) {
            if (rarity.getName().equals(selected)) {
                return getRarityColor(rarity);
            }
        }
        return 0xFFE8EDF1;
    }

    private String getSelectedRarityName() {
        NBTTagCompound upgradeTag = getUpgradeTag(tile.getStackInSlot(2));
        return upgradeTag == null ? "" : upgradeTag.getString("buffRarity");
    }

    private NBTTagCompound getUpgradeTag(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound()) {
            return null;
        }
        NBTTagCompound root = stack.getTagCompound();
        if (root == null || !root.hasKey("techgunsupgrade", 10)) {
            return null;
        }
        return root.getCompoundTag("techgunsupgrade");
    }

    private int getCenterRarityIndex() {
        String selected = getSelectedRarityName();
        for (int i = 0; i < RARITIES.length; i++) {
            if (RARITIES[i].getName().equals(selected)) {
                return i;
            }
        }

        double[] chances = getCurrentChances();
        int strongest = 0;
        for (int i = 1; i < chances.length; i++) {
            if (chances[i] > chances[strongest]) {
                strongest = i;
            }
        }
        return strongest;
    }

    private double[] getCurrentChances() {
        ItemStack ticket = tile.getStackInSlot(1);
        if (!(ticket.getItem() instanceof ItemTicket)) {
            return TICKET_CHANCES[0];
        }
        int tier = ((ItemTicket) ticket.getItem()).getTier();
        if (tier < 0 || tier >= TICKET_CHANCES.length) {
            tier = 0;
        }
        return TICKET_CHANCES[tier];
    }

    private int wrapRarityIndex(int index) {
        int result = index % RARITIES.length;
        return result < 0 ? result + RARITIES.length : result;
    }

    private String getRarityCode(UpgradeRarity rarity) {
        switch (rarity) {
            case COMMON:
                return "COM";
            case UNCOMMON:
                return "UNC";
            case RARE:
                return "RAR";
            case EPIC:
                return "EPC";
            case LEGENDARY:
                return "LEG";
            case MYTHIC:
                return "MYT";
            case ULTRA_MYTHIC:
            default:
                return "ULT";
        }
    }

    private String formatChance(double chance) {
        if (chance == Math.rint(chance)) {
            return ((int) chance) + "%";
        }
        if (chance < 1.0D) {
            return String.format(Locale.ROOT, "%.2f%%", chance);
        }
        return String.format(Locale.ROOT, "%.1f%%", chance);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
        drawRarityTooltip(mouseX, mouseY);
        drawResetTooltip(mouseX, mouseY);
    }

    private void drawResetTooltip(int mouseX, int mouseY) {
        int localX = mouseX - guiLeft;
        int localY = mouseY - guiTop;
        if (localX >= RESET_X && localX < RESET_X + RESET_W
                && localY >= RESET_Y && localY < RESET_Y + RESET_H) {
            drawHoveringText(Arrays.asList(
                    I18n.format("gui.techgunsupgrade.reset"),
                    I18n.format("gui.techgunsupgrade.reset_tooltip")), mouseX, mouseY);
        }
    }

    private void drawRarityTooltip(int mouseX, int mouseY) {
        if (isShowingBuffRoulette()) {
            drawBuffTooltip(mouseX, mouseY);
            return;
        }

        int localX = mouseX - guiLeft;
        int localY = mouseY - guiTop;
        if (localY < CAROUSEL_Y || localY >= CAROUSEL_Y + CARD_HEIGHT) {
            return;
        }

        int relativeX = localX - CAROUSEL_X;
        int cardStride = CARD_WIDTH + CARD_GAP;
        if (relativeX < 0 || relativeX >= VISIBLE_CARDS * cardStride - CARD_GAP) {
            return;
        }
        int visibleIndex = relativeX / cardStride;
        if (relativeX % cardStride >= CARD_WIDTH || visibleIndex >= VISIBLE_CARDS) {
            return;
        }

        int rarityIndex = wrapRarityIndex(getCenterRarityIndex() + visibleIndex - 2);
        UpgradeRarity rarity = RARITIES[rarityIndex];
        List<String> lines = Arrays.asList(
                rarity.getName(),
                I18n.format("gui.techgunsupgrade.chance", formatChance(getCurrentChances()[rarityIndex])));
        drawHoveringText(lines, mouseX, mouseY);
    }

    private void drawBuffTooltip(int mouseX, int mouseY) {
        if (rouletteEntries.isEmpty()) {
            return;
        }

        int localX = mouseX - guiLeft;
        int localY = mouseY - guiTop;
        if (localY < CAROUSEL_Y || localY >= CAROUSEL_Y + CARD_HEIGHT) {
            return;
        }

        int relativeX = localX - CAROUSEL_X;
        int cardStride = BUFF_CARD_WIDTH + BUFF_CARD_GAP;
        if (relativeX < 0
                || relativeX >= BUFF_VISIBLE_CARDS * cardStride - BUFF_CARD_GAP) {
            return;
        }
        int visibleIndex = relativeX / cardStride;
        if (relativeX % cardStride >= BUFF_CARD_WIDTH
                || visibleIndex >= BUFF_VISIBLE_CARDS) {
            return;
        }

        int centerIndex = !rouletteSpinning && rouletteTargetIndex >= 0
                ? rouletteTargetIndex
                : wrapEntryIndex(rouletteStartIndex + getRouletteStep());
        RouletteEntry entry = rouletteEntries.get(
                wrapEntryIndex(centerIndex + visibleIndex - 1));
        drawHoveringText(Arrays.asList(entry.name, entry.rarity.getName()), mouseX, mouseY);
    }

    private static final class RouletteEntry {
        private final String id;
        private final String name;
        private final UpgradeRarity rarity;

        private RouletteEntry(String id, String name, UpgradeRarity rarity) {
            this.id = id == null ? "" : id;
            this.name = name == null ? "" : name;
            this.rarity = rarity;
        }
    }
}