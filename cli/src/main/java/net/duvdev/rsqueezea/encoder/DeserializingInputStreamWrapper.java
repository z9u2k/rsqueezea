/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.encoder;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class DeserializingInputStreamWrapper extends InputStream {

  private final InputStream wrapped;

  private @Nullable ByteArrayInputStream buffer;

  protected DeserializingInputStreamWrapper(InputStream wrapped) {
    this.wrapped = wrapped;
  }

  @Override
  public int read() throws IOException {
    if (buffer == null) {
      buffer = new ByteArrayInputStream(deserialize(wrapped));
    }
    return buffer.read();
  }

  protected abstract byte[] deserialize(InputStream inputStream) throws IOException;
}
