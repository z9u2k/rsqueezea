/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.codec;

import org.bouncycastle.util.encoders.Base64;

public class BinaryToQRCodeStringCodec implements Codec<byte[], String> {

  public static final String CHARSET = "ISO-8859-1";

  @Override
  public byte[] decode(String encoded) throws DecoderException {
    return Base64.decode(encoded);
  }

  @Override
  public String encode(byte[] data) throws EncoderException {
    return Base64.toBase64String(data);
  }
}
