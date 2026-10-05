package com.stepm.techgunsupgrade.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class CardRarityTextureGenerator {

    private static final int WIDTH = 512;
    private static final int HEIGHT = 384;

    public static void main(String[] args) throws Exception {
        createRarityCard("bg", new Color(40, 40, 60));
        createRarityCard("common", new Color(85, 255, 85));
        createRarityCard("uncommon", new Color(85, 255, 255));
        createRarityCard("epic", new Color(170, 85, 255));
        createRarityCard("legendary", new Color(255, 170, 0));
        createRarityCard("mythic", new Color(255, 85, 85));

        System.out.println("All card textures created!");
        System.out.println("Folder: src/main/resources/assets/techgunsupgrade/textures/gui/");
    }

    private static void createRarityCard(String name, Color rarityColor) throws Exception {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setComposite(AlphaComposite.SrcOver);

        g.setColor(new Color(25, 25, 35, 240));
        g.fillRoundRect(0, 0, WIDTH, HEIGHT, 20, 20);

        g.setColor(new Color(rarityColor.getRed(), rarityColor.getGreen(), rarityColor.getBlue(), 200));
        g.setStroke(new BasicStroke(4));
        g.drawRoundRect(2, 2, WIDTH - 4, HEIGHT - 4, 20, 20);

        g.setColor(new Color(rarityColor.getRed(), rarityColor.getGreen(), rarityColor.getBlue(), 30));
        g.fillRoundRect(10, 10, WIDTH - 20, HEIGHT - 20, 15, 15);

        String rarityName = name.toUpperCase();
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 32));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(rarityName, (WIDTH - fm.stringWidth(rarityName)) / 2, 55);

        g.setColor(new Color(rarityColor.getRed(), rarityColor.getGreen(), rarityColor.getBlue(), 150));
        g.setStroke(new BasicStroke(2));
        g.drawLine(80, 70, WIDTH - 80, 70);

        g.setColor(new Color(rarityColor.getRed(), rarityColor.getGreen(), rarityColor.getBlue(), 100));
        int cx = WIDTH / 2;
        int cy = HEIGHT / 2 + 10;
        drawStar(g, cx, cy, 60);

        g.setColor(new Color(200, 200, 200, 180));
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        String text = "CLICK TO SELECT";
        fm = g.getFontMetrics();
        g.drawString(text, (WIDTH - fm.stringWidth(text)) / 2, HEIGHT - 30);

        g.dispose();

        File output = new File("src/main/resources/assets/techgunsupgrade/textures/gui/card_" + name + ".png");
        output.getParentFile().mkdirs();
        ImageIO.write(img, "png", output);
        System.out.println("Created: card_" + name + ".png");
    }

    private static void drawStar(Graphics2D g, int cx, int cy, int size) {
        int[] xPoints = new int[10];
        int[] yPoints = new int[10];
        for (int i = 0; i < 10; i++) {
            double angle = Math.PI / 2 - i * Math.PI / 5;
            int r = (i % 2 == 0) ? size : size / 2;
            xPoints[i] = cx + (int) (r * Math.cos(angle));
            yPoints[i] = cy - (int) (r * Math.sin(angle));
        }
        g.fillPolygon(xPoints, yPoints, 10);
    }
}