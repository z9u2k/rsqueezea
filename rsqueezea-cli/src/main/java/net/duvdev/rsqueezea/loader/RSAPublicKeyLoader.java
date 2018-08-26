/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.loader;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;

public interface RSAPublicKeyLoader {
  RSAPublicKey load() throws IOException;
}
