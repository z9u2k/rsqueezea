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

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.stream.IntStream;

public final class QRCodeCodec implements Codec<byte[], byte[]> {
  @Override
  public byte[] decode(byte[] encoded) throws DecoderException {
    try {
      Result result =
          new MultiFormatReader()
              .decode(
                  new BinaryBitmap(
                      new HybridBinarizer(
                          new BufferedImageLuminanceSource(
                              ImageIO.read(new ByteArrayInputStream(encoded))))));
      ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
      result.getText().codePoints().forEach(byteStream::write);
      return byteStream.toByteArray();
    } catch (NotFoundException | IOException e) {
      throw new DecoderException(e.getMessage(), e);
    }
  }

  @Override
  public byte[] encode(byte[] decoded) throws EncoderException {
    int[] codepoints = IntStream.range(0, decoded.length).map(i -> decoded[i] & 0xff).toArray();
    String text = new String(codepoints, 0, codepoints.length);

    QRCodeWriter barcodeWriter = new QRCodeWriter();
    HashMap<EncodeHintType, Object> hints = new HashMap<>();
    hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
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
}
