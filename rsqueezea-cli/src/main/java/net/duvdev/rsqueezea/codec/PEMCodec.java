/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.codec;

import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.*;

public final class PEMCodec implements Codec<byte[], byte[]> {

  private static final String TYPE = "SQUEEZED RSA PRIVATE KEY";

  @Override
  public byte[] decode(byte[] encoded) throws DecoderException {
    PEMParser pemParser = new PEMParser(new InputStreamReader(new ByteArrayInputStream(encoded)));
    PemObject pemObject;
    try {
      pemObject = pemParser.readPemObject();
    } catch (IOException e) {
      throw new DecoderException("Invalid PEM", e);
    }
    if (pemObject == null) {
      throw new DecoderException("No object in PEM document");
    }
    if (!TYPE.equals(pemObject.getType())) {
      throw new DecoderException(
          "Wrong object type. Expecting: " + TYPE + ", but got: " + pemObject.getType());
    }
    return pemObject.getContent();
  }

  @Override
  public byte[] encode(byte[] decoded) throws EncoderException {
    PemObject pemObject = new PemObject(TYPE, decoded);
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    PemWriter pemWriter = new PemWriter(new OutputStreamWriter(buffer));
    try {
      pemWriter.writeObject(pemObject);
      pemWriter.flush();
    } catch (IOException e) {
      throw new EncoderException(e);
    }
    return buffer.toByteArray();
  }
}
