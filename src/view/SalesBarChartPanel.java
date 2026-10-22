package view;

import model.SalesReport;
import util.UIStyleUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom 2D Bar Chart styled with the custom designer palette:
 * - #04151F (Obsidian text)
 * - #183A37 (Pine base)
 * - #EFD6AC (Cream highlights)
 * - #C44900 (Rust accent bars)
 */
public class SalesBarChartPanel extends JPanel {

    private List<SalesReport.SalesEntry> data = new ArrayList<>();
    private String title = "Sales Performance";

    public SalesBarChartPanel() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(500, 240));
    }

    public void setData(List<SalesReport.SalesEntry> entries, String title) {
        this.data = (entries != null) ? new ArrayList<>(entries) : new ArrayList<SalesReport.SalesEntry>();
        this.title = title != null ? title : "Sales Performance";
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Title
        g2.setFont(UIStyleUtil.FONT_HEADER);
        g2.setColor(UIStyleUtil.COLOR_OBSIDIAN);
        g2.drawString(title, 20, 25);

        if (data.isEmpty()) {
            g2.setFont(UIStyleUtil.FONT_REGULAR);
            g2.setColor(UIStyleUtil.COLOR_TEXT_MUTED);
            String emptyMsg = "No sales records available for this period.";
            FontMetrics fm = g2.getFontMetrics();
            int msgX = (width - fm.stringWidth(emptyMsg)) / 2;
            g2.drawString(emptyMsg, msgX, height / 2);
            g2.dispose();
            return;
        }

        // Margins
        int left = 55;
        int right = 30;
        int top = 50;
        int bottom = 50;
        int chartWidth = width - left - right;
        int chartHeight = height - top - bottom;

        // Find max value
        double maxVal = 100.0;
        for (SalesReport.SalesEntry entry : data) {
            double v = entry.getTotalAmount().doubleValue();
            if (v > maxVal) maxVal = v;
        }
        maxVal = Math.ceil(maxVal / 1000.0) * 1000.0;
        if (maxVal == 0) maxVal = 1000.0;

        // Draw horizontal grid lines
        g2.setFont(UIStyleUtil.FONT_SMALL);
        FontMetrics fm = g2.getFontMetrics();
        int gridSteps = 4;
        for (int i = 0; i <= gridSteps; i++) {
            int y = top + chartHeight - (int) ((i / (double) gridSteps) * chartHeight);
            g2.setColor(new Color(236, 230, 218));
            g2.drawLine(left, y, width - right, y);

            double gridVal = (i / (double) gridSteps) * maxVal;
            String label = String.format("\u20B9%.0f", gridVal);
            g2.setColor(UIStyleUtil.COLOR_TEXT_MUTED);
            g2.drawString(label, left - fm.stringWidth(label) - 8, y + 4);
        }

        // Render bars
        int count = data.size();
        int slotWidth = chartWidth / count;
        int barWidth = Math.max(16, Math.min(48, slotWidth - 16));

        for (int i = 0; i < count; i++) {
            SalesReport.SalesEntry entry = data.get(i);
            double val = entry.getTotalAmount().doubleValue();
            int barHeight = (int) ((val / maxVal) * chartHeight);
            int x = left + i * slotWidth + (slotWidth - barWidth) / 2;
            int y = top + chartHeight - barHeight;

            // Bar Gradient from Rust (#C44900) down to Pine (#183A37)
            GradientPaint gp = new GradientPaint(x, y, UIStyleUtil.COLOR_RUST, x, top + chartHeight, UIStyleUtil.COLOR_PINE);
            g2.setPaint(gp);
            g2.fillRoundRect(x, y, barWidth, barHeight, 6, 6);

            // Value label above bar
            if (val > 0) {
                g2.setColor(UIStyleUtil.COLOR_RUST);
                g2.setFont(UIStyleUtil.FONT_BOLD);
                String valStr = String.format("\u20B9%.0f", val);
                int valX = x + (barWidth - fm.stringWidth(valStr)) / 2;
                g2.drawString(valStr, valX, y - 5);
            }

            // Period label below bar
            g2.setColor(UIStyleUtil.COLOR_TEXT_MUTED);
            g2.setFont(UIStyleUtil.FONT_SMALL);
            String pStr = entry.getDatePeriod();
            if (pStr.length() > 10) {
                pStr = pStr.substring(5);
            }
            int pX = x + (barWidth - fm.stringWidth(pStr)) / 2;
            g2.drawString(pStr, pX, top + chartHeight + 18);
        }

        g2.dispose();
    }
}
