package com.ursugoiangel;

import java.awt.*;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.util.ArrayList;
import java.util.List;

public class Chatbubble extends JPanel {
    private String text;
    private boolean isUser;

    private static final Color USER_COLOR = new Color(106, 235, 153);
    private static final Color BOT_COLOR = new Color(106, 106, 106);

    // Bubble drawing dimensions (not including shadow)
    private static final int BUBBLE_WIDTH = 250;
    private static final int BUBBLE_HEIGHT = 60;
    private static final int SHADOW_OFFSET = 3;
    private static final int ARC = 20;

    public Chatbubble(String text, boolean isUser) {
        this.text = text;
        this.isUser = isUser;
        setOpaque(false);
        // Reserve space for the shadow by adding an EmptyBorder.
        setBorder(BorderFactory.createEmptyBorder(SHADOW_OFFSET, SHADOW_OFFSET, SHADOW_OFFSET, SHADOW_OFFSET));
        // Align bubbles: right for user, left for bot.
        setAlignmentX(isUser ? RIGHT_ALIGNMENT : LEFT_ALIGNMENT);
    }

    public String getText() {
        return text;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setText(String text) {
        this.text = text;
        revalidate();
        repaint();
    }

    public void setUser(boolean user) {
        isUser = user;
        setAlignmentX(isUser ? RIGHT_ALIGNMENT : LEFT_ALIGNMENT);
        repaint();
    }

    private List<String> getWrappedLines(FontMetrics fm, int maxTextWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        String currentLine = "";
        for (String word : words) {
            String testLine = currentLine.isEmpty() ? word : currentLine + " " + word;
            if (fm.stringWidth(testLine) > maxTextWidth) {
                if (!currentLine.isEmpty()) {
                    lines.add(currentLine);
                }
                currentLine = word;
            } else {
                currentLine = testLine;
            }
        }
        if (!currentLine.isEmpty()) {
            lines.add(currentLine);
        }
        return lines;
    }

    @Override
    public Dimension getPreferredSize() {
        // Add the shadow space (from the border) to the fixed bubble dimensions.
        Insets insets = getInsets();
        return new Dimension(BUBBLE_WIDTH + insets.left + insets.right, BUBBLE_HEIGHT + insets.top + insets.bottom);
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Use the border insets to avoid clipping the shadow.
        Insets insets = getInsets();
        int x0 = insets.left;
        int y0 = insets.top;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw drop shadow.
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillRoundRect(x0 + SHADOW_OFFSET, y0 + SHADOW_OFFSET, BUBBLE_WIDTH, BUBBLE_HEIGHT, ARC, ARC);

        // Determine bubble color and draw a subtle gradient.
        Color bubbleColor = isUser ? USER_COLOR : BOT_COLOR;
        Color gradientEnd = getSubtleDarker(bubbleColor);
        GradientPaint gradient = new GradientPaint(
                x0, y0, bubbleColor,
                x0, y0 + BUBBLE_HEIGHT, gradientEnd);
        g2d.setPaint(gradient);
        g2d.fillRoundRect(x0, y0, BUBBLE_WIDTH, BUBBLE_HEIGHT, ARC, ARC);

        // Draw centered text in white.
        g2d.setColor(Color.WHITE);
        FontMetrics fm = g2d.getFontMetrics();
        int horizontalPadding = 20;
        int maxTextWidth = BUBBLE_WIDTH - horizontalPadding;
        List<String> lines = getWrappedLines(fm, maxTextWidth);
        int lineHeight = fm.getHeight();
        int textBlockHeight = lines.size() * lineHeight;
        int y = y0 + (BUBBLE_HEIGHT - textBlockHeight) / 2 + fm.getAscent();
        for (String line : lines) {
            int lineWidth = fm.stringWidth(line);
            int x = x0 + (BUBBLE_WIDTH - lineWidth) / 2;
            g2d.drawString(line, x, y);
            y += lineHeight;
        }
        g2d.dispose();
    }

    /**
     * Returns a slightly darker version of the color to create a subtle gradient effect.
     */
    private Color getSubtleDarker(Color c) {
        int r = (int) (c.getRed() * 0.95);
        int g = (int) (c.getGreen() * 0.95);
        int b = (int) (c.getBlue() * 0.95);
        return new Color(r, g, b);
    }

    @Override
    public String toString() {
        return "Chatbubble{" +
                "text='" + text + '\'' +
                ", isUser=" + isUser +
                '}';
    }
}