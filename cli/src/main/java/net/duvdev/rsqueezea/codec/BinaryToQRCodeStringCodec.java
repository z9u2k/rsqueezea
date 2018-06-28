/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.codec;

import java.io.ByteArrayOutputStream;
import java.util.stream.IntStream;

public class BinaryToQRCodeStringCodec implements Codec<byte[], String> {

  public static final String CHARSET = "UTF-8";

  @Override
  public byte[] decode(String encoded) throws DecoderException {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    encoded.codePoints().forEach(byteStream::write);
    return byteStream.toByteArray();
  }

  @Override
  public String encode(byte[] decoded) throws EncoderException {
    int[] codepoints = IntStream.range(0, decoded.length).map(i -> decoded[i] & 0xff).toArray();
    return new String(codepoints, 0, codepoints.length);
  }
}
