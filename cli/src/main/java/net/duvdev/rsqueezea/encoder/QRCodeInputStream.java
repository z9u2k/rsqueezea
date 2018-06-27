/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.encoder;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class QRCodeInputStream extends DeserializingInputStreamWrapper {

  protected QRCodeInputStream(InputStream wrapped) {
    super(wrapped);
  }

  @Override
  protected byte[] deserialize(InputStream inputStream) throws IOException {
    try {
      Result result =
          new MultiFormatReader()
              .decode(
                  new BinaryBitmap(
                      new HybridBinarizer(
                          new BufferedImageLuminanceSource(ImageIO.read(inputStream)))));
      ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
      result.getText().codePoints().forEach(byteStream::write);
      return byteStream.toByteArray();
    } catch (NotFoundException e) {
      throw new IOException(e.getMessage(), e);
    }
  }
}
