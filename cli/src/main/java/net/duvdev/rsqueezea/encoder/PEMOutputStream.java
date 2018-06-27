/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.encoder;

import net.duvdev.rsqueezea.protocol.SqueezeType;
import org.bouncycastle.util.io.pem.PemHeader;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;

/** Encodes DER structures in <code>SQUEEZED RSA PRIVATE KEY</code> PEM envelope. */
public final class PEMOutputStream extends SerializingOutputStreamWrapper {

  public static final String TYPE = "SQUEEZED RSA PRIVATE KEY";

  private final SqueezeType squeezeType;

  public PEMOutputStream(OutputStream wrapped, SqueezeType squeezeType) {
    super(wrapped);
    this.squeezeType = squeezeType;
  }

  @Override
  protected void serialize(byte[] bytes, OutputStream outputStream) throws IOException {
    PemObject pemObject =
        new PemObject(
            TYPE,
            Arrays.asList(
                new PemHeader("Has-Modulus", squeezeType == SqueezeType.PRIME_P ? "0" : "1")),
            bytes);
    PemWriter pemWriter = new PemWriter(new OutputStreamWriter(outputStream));
    pemWriter.writeObject(pemObject);
    pemWriter.flush();
  }
}
