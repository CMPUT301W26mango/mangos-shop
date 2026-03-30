package com.example.myapplication;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Utility class responsible for generating QR code images used in the application.
 *
 * This helper converts a string value into a QR code Bitmap
 * that can be displayed in the UI. It uses the ZXing library to encode the data
 * into a QR code format.
 * @author Sayuj
 */

public class QrHelper {
    /**
     * Generates a QR code Bitmap from the provided string value.
     *
     * @param qrValue The string data to encode inside the QR code
     * @return A Bitmap representing the generated QR code
     * @throws WriterException If the QR code encoding fails
     */
    public static Bitmap generateQrCode(String qrValue) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(qrValue, BarcodeFormat.QR_CODE, 512, 512);

        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }

        return bitmap;
    }
}