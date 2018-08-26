/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.codec;

public final class IdentityCodec implements Codec<byte[], byte[]> {
  @Override
  public byte[] decode(byte[] encoded) throws DecoderException {
    return encoded;
  }

  @Override
  public byte[] encode(byte[] decoded) throws EncoderException {
    return decoded;
  }
}
