package net.duvdev.rsqueezea.encoder;

import net.duvdev.rsqueezea.SqueezeType;

import java.io.InputStream;
import java.io.OutputStream;

public final class EncoderFactory {

    /** Do not instantiate */
    private EncoderFactory() {}

    public static OutputStream wrapOutputStream(String format, SqueezeType squeezeType, OutputStream outputStream) {
        if ("DER".equalsIgnoreCase(format)) {
            return outputStream;
        } else if ("PEM".equalsIgnoreCase(format)) {
            return new PEMOutputStream(outputStream, squeezeType);
        } else if ("QR".equalsIgnoreCase(format)) {
            return new QRCodeOutputStream(outputStream);
        } else {
            throw new IllegalArgumentException("Unknown format: " + format);
        }
    }

    public static InputStream wrapInputStream(String format, InputStream inputStream) {
        if ("DER".equalsIgnoreCase(format)) {
            return inputStream;
        } else if ("PEM".equalsIgnoreCase(format)) {
            return new PEMInputStream(inputStream);
        } else if ("QR".equalsIgnoreCase(format)) {
            return new QRCodeInputStream(inputStream);
        } else {
            throw new IllegalArgumentException("Unknown format: " + format);
        }
    }
}
