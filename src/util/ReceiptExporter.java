package util;

import model.Bill;
import model.BillItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Demonstrates Abstraction, Polymorphism, and Strategy Pattern for Receipt Generation.
 */
public interface ReceiptExporter {

    File export(Bill bill, File outputDir) throws IOException;

    /**
     * Plain Text Receipt Exporter.
     */
    class TextExporter implements ReceiptExporter {
        @Override
        public File export(Bill bill, File outputDir) throws IOException {
            if (!outputDir.exists()) outputDir.mkdirs();
            File file = new File(outputDir, "Receipt_" + bill.getBillNumber() + ".txt");

            try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
                writer.println(buildReceiptString(bill));
            }
            return file;
        }

        public static String buildReceiptString(Bill bill) {
            StringBuilder sb = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String dateStr = (bill.getCreatedAt() != null) ? sdf.format(bill.getCreatedAt()) : sdf.format(new Date());

            sb.append("====================================================\n");
            sb.append("               SMART RETAIL STORE                   \n");
            sb.append("                 BILL / RECEIPT                     \n");
            sb.append("====================================================\n");
            sb.append(String.format("Bill No   : %-20s\n", bill.getBillNumber()));
            sb.append(String.format("Date      : %-20s\n", dateStr));
            sb.append(String.format("Cashier   : %-20s\n", bill.getCashierName() != null ? bill.getCashierName() : "Cashier"));
            sb.append(String.format("Payment   : %-20s\n", bill.getPaymentMethod()));
            sb.append("----------------------------------------------------\n");
            sb.append(String.format("%-22s %5s %10s %10s\n", "Product", "Qty", "Price", "Total"));
            sb.append("----------------------------------------------------\n");

            if (bill.getItems() != null) {
                for (BillItem item : bill.getItems()) {
                    String name = item.getProductName();
                    if (name != null && name.length() > 22) {
                        name = name.substring(0, 19) + "...";
                    }
                    sb.append(String.format("%-22s %5d %10.2f %10.2f\n",
                            name,
                            item.getQuantity(),
                            item.getUnitPrice(),
                            item.getTotalPrice()));
                }
            }

            sb.append("----------------------------------------------------\n");
            sb.append(String.format("%-38s Rs %8.2f\n", "Subtotal:", bill.getSubtotal()));
            if (bill.getTaxAmount() != null && bill.getTaxAmount().doubleValue() > 0) {
                sb.append(String.format("%-38s Rs %8.2f\n", "Tax (" + bill.getTaxRate() + "%):", bill.getTaxAmount()));
            }
            sb.append("====================================================\n");
            sb.append(String.format("%-38s Rs %8.2f\n", "TOTAL DUE:", bill.getGrandTotal()));
            sb.append("====================================================\n");
            sb.append("             Thank you for shopping!                \n");
            sb.append("           Please visit us again soon.              \n");
            sb.append("====================================================\n");

            return sb.toString();
        }
    }

    /**
     * Pure Java Standard PDF 1.4 Document Generator.
     * Generates a compliant PDF receipt without requiring any third-party external dependencies.
     */
    class PdfExporter implements ReceiptExporter {
        @Override
        public File export(Bill bill, File outputDir) throws IOException {
            if (!outputDir.exists()) outputDir.mkdirs();
            File file = new File(outputDir, "Receipt_" + bill.getBillNumber() + ".pdf");

            String content = TextExporter.buildReceiptString(bill);
            String[] lines = content.split("\n");

            // Build pure PDF 1.4 stream
            StringBuilder streamContent = new StringBuilder();
            streamContent.append("BT\n");
            streamContent.append("/F1 10 Tf\n");
            streamContent.append("14 TL\n"); // line leading
            streamContent.append("50 780 Td\n"); // initial coordinates

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
                if (i > 0) {
                    streamContent.append("T*\n"); // next line
                }
                streamContent.append("(").append(line).append(") Tj\n");
            }
            streamContent.append("ET\n");

            byte[] streamBytes = streamContent.toString().getBytes(StandardCharsets.US_ASCII);

            List<Long> offsets = new ArrayList<>();
            try (FileOutputStream fos = new FileOutputStream(file)) {
                offsets.add(0L); // 0th object is dummy

                writeString(fos, "%PDF-1.4\n");

                // Object 1: Catalog
                offsets.add((long) fos.getChannel().position());
                writeString(fos, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

                // Object 2: Pages
                offsets.add((long) fos.getChannel().position());
                writeString(fos, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");

                // Object 3: Page
                offsets.add((long) fos.getChannel().position());
                writeString(fos, "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n");

                // Object 4: Font
                offsets.add((long) fos.getChannel().position());
                writeString(fos, "4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>\nendobj\n");

                // Object 5: Contents
                offsets.add((long) fos.getChannel().position());
                writeString(fos, "5 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n");
                fos.write(streamBytes);
                writeString(fos, "\nendstream\nendobj\n");

                // XRef table
                long xrefOffset = fos.getChannel().position();
                writeString(fos, "xref\n0 6\n");
                writeString(fos, "0000000000 65535 f \n");
                for (int i = 1; i <= 5; i++) {
                    writeString(fos, String.format("%010d 00000 n \n", offsets.get(i)));
                }

                // Trailer
                writeString(fos, "trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n" + xrefOffset + "\n%%EOF\n");
            }

            return file;
        }

        private static void writeString(FileOutputStream fos, String s) throws IOException {
            fos.write(s.getBytes(StandardCharsets.US_ASCII));
        }
    }

    /**
     * Factory demonstrating Polymorphism.
     */
    class Factory {
        public static ReceiptExporter getExporter(String type) {
            if ("pdf".equalsIgnoreCase(type)) {
                return new PdfExporter();
            }
            return new TextExporter();
        }
    }
}
