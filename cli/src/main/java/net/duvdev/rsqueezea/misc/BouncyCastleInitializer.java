/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.misc;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

/** Registers the BouncyCastle provider */
public final class BouncyCastleInitializer {

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  /** Do not instantiate */
  private BouncyCastleInitializer() {}

  public static void initialize() {
    // nothing to do - we register the provider in the static section, which takes care of
    // concurrency too
  }
}
