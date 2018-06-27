/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.protocol;

import net.duvdev.rsqueezea.model.SqueezedKey;

import java.io.IOException;

public interface Protocol {
  byte[] encodeSqueezedKey(SqueezedKey key, SqueezeType type) throws IOException;

  SqueezedKey decodeSqueezedKey(byte[] data) throws IOException;
}
