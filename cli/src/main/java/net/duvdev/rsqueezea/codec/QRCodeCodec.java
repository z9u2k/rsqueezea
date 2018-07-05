/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.codec;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

public final class QRCodeCodec implements Codec<byte[], byte[]> {

  private Level level;

  public QRCodeCodec setLevel(Level level) {
    this.level = level;
    return this;
  }

  @Override
  public byte[] decode(byte[] encoded) throws DecoderException {
    try {
      MultiFormatReader reader = new MultiFormatReader();
      HashMap<DecodeHintType, Object> hints = new HashMap<>();
      hints.put(DecodeHintType.CHARACTER_SET, BinaryToQRCodeStringCodec.CHARSET);
      reader.setHints(hints);

      Result result =
          reader.decode(
              new BinaryBitmap(
                  new HybridBinarizer(
                      new BufferedImageLuminanceSource(
                          ImageIO.read(new ByteArrayInputStream(encoded))))));
      BinaryToQRCodeStringCodec codec = new BinaryToQRCodeStringCodec();
      return codec.decode(result.getText());
    } catch (NotFoundException | IOException e) {
      throw new DecoderException(e.getMessage(), e);
    }
  }

  @Override
  public byte[] encode(byte[] decoded) throws EncoderException {
    BinaryToQRCodeStringCodec codec = new BinaryToQRCodeStringCodec();
    String text = codec.encode(decoded);

    QRCodeWriter barcodeWriter = new QRCodeWriter();
    HashMap<EncodeHintType, Object> hints = new HashMap<>();
    hints.put(EncodeHintType.CHARACTER_SET, BinaryToQRCodeStringCodec.CHARSET);
    hints.put(EncodeHintType.ERROR_CORRECTION, getZXingLevel());
    BitMatrix bitMatrix;
    try {
      bitMatrix = barcodeWriter.encode(text, BarcodeFormat.QR_CODE, 0, 0, hints);
    } catch (WriterException e) {
      throw new EncoderException(e.getMessage(), e);
    }
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    try {
      MatrixToImageWriter.writeToStream(bitMatrix, "PNG", buffer);
    } catch (IOException e) {
      throw new EncoderException(e.getMessage(), e);
    }
    return buffer.toByteArray();
  }

  private ErrorCorrectionLevel getZXingLevel() {
    switch (level) {
      case L:
        return ErrorCorrectionLevel.L;
      case M:
        return ErrorCorrectionLevel.M;
      case Q:
        return ErrorCorrectionLevel.Q;
      case H:
        return ErrorCorrectionLevel.H;
      default:
        throw new IllegalArgumentException(level.name());
    }
  }

  public enum Level {
    L,
    M,
    Q,
    H;
  }
}
