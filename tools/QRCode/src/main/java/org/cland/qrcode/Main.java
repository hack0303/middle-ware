package org.cland.qrcode;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class Main {


    public static void main(String[] args) {
        String qrCodeText = "https://www.baidu.com";
        String filePath = "QRCode.png";
        int width = 300;
        int height = 300;

        try {
            generateQRCodeImage(qrCodeText, width, height, filePath);
            System.out.println("QR Code generated successfully.");
        } catch (WriterException | IOException e) {
            System.err.println("Could not generate QR Code: " + e.getMessage());
        }
    }

    private static void generateQRCodeImage(String text, int width, int height, String filePath)
            throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        Path path = FileSystems.getDefault().getPath(filePath);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
    }
}
