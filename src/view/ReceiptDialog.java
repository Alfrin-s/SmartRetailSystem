package view;

import model.Bill;
import util.ReceiptExporter;
import util.UIStyleUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

/**
 * ReceiptDialog displaying generated bill and providing export options.
 * Demonstrates thermal receipt layout and polymorphic exporter integration.
 */
public class ReceiptDialog extends JDialog {

    private final Bill bill;

    public ReceiptDialog(Frame parent, Bill bill) {
        super(parent, "Sales Receipt - " + (bill != null ? bill.getBillNumber() : ""), true);
        this.bill = bill;
        initComponents();
        setSize(480, 580);
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIStyleUtil.COLOR_BACKGROUND);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLabel = new JLabel("RECEIPT GENERATED SUCCESSFULLY", SwingConstants.CENTER);
        titleLabel.setFont(UIStyleUtil.FONT_HEADER);
        titleLabel.setForeground(UIStyleUtil.COLOR_SUCCESS);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Receipt Text Content
        JTextArea receiptArea = new JTextArea();
        receiptArea.setFont(UIStyleUtil.FONT_MONO);
        receiptArea.setEditable(false);
        receiptArea.setBackground(new Color(255, 255, 252));
        receiptArea.setBorder(new EmptyBorder(12, 16, 12, 16));

        if (bill != null) {
            receiptArea.setText(ReceiptExporter.TextExporter.buildReceiptString(bill));
            receiptArea.setCaretPosition(0);
        }

        JScrollPane scrollPane = new JScrollPane(receiptArea);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, UIStyleUtil.COLOR_BORDER));
        add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnExportPdf = UIStyleUtil.createPrimaryButton("Export PDF");
        JButton btnExportTxt = UIStyleUtil.createSecondaryButton("Export TXT");
        JButton btnClose = UIStyleUtil.createSecondaryButton("Close");

        btnExportPdf.addActionListener(e -> exportReceipt("pdf"));
        btnExportTxt.addActionListener(e -> exportReceipt("txt"));
        btnClose.addActionListener(e -> dispose());

        buttonPanel.add(btnExportPdf);
        buttonPanel.add(btnExportTxt);
        buttonPanel.add(btnClose);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void exportReceipt(String format) {
        if (bill == null) return;
        try {
            File receiptsDir = new File("receipts");
            ReceiptExporter exporter = ReceiptExporter.Factory.getExporter(format);
            File exportedFile = exporter.export(bill, receiptsDir);

            JOptionPane.showMessageDialog(this,
                    "Receipt successfully exported to:\n" + exportedFile.getAbsolutePath(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to export receipt: " + ex.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
