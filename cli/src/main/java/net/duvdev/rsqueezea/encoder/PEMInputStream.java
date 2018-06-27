/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.encoder;

import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.io.pem.PemObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class PEMInputStream extends DeserializingInputStreamWrapper {

  protected PEMInputStream(InputStream wrapped) {
    super(wrapped);
  }

  @Override
  protected byte[] deserialize(InputStream inputStream) throws IOException {
    PEMParser pemParser = new PEMParser(new InputStreamReader(inputStream));
    PemObject pemObject = pemParser.readPemObject();
    if (pemObject == null) {
      throw new IOException("Invalid PEM");
    }
    if (!PEMOutputStream.TYPE.equals(pemObject.getType())) {
      throw new IOException(
          "Wrong object type. Was expecting: "
              + PEMOutputStream.TYPE
              + ", but encountered: "
              + pemObject.getType());
    }
    return pemObject.getContent();
  }
}
