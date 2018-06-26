package net.duvdev.rsqueezea.encoder;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.stream.IntStream;

public final class QRCodeOutputStream extends SerializingOutputStreamWrapper {

    protected QRCodeOutputStream(OutputStream wrapped) {
        super(wrapped);
    }

    @Override
    protected void serialize(byte[] bytes, OutputStream outputStream) throws IOException {
        int[] codepoints = IntStream.range(0, bytes.length).map(i -> bytes[i] & 0xff).toArray();
        String text = new String(codepoints, 0, codepoints.length);

        QRCodeWriter barcodeWriter = new QRCodeWriter();
        HashMap<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        BitMatrix bitMatrix;
        try {
            bitMatrix = barcodeWriter.encode(text, BarcodeFormat.QR_CODE, 0, 0, hints);
        } catch (WriterException e) {
            throw new IOException(e.getMessage(), e);
        }
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
    }
}
