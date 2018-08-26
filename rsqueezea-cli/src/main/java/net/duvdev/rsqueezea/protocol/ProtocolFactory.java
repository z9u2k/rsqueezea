/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.protocol;

public final class ProtocolFactory {
  /** Do not instantiate */
  private ProtocolFactory() {}

  public static Protocol getInstance(int version) {
    switch (version) {
      case 0:
        return new V0Protocol();
      default:
        throw new IllegalArgumentException("Unsupported version: " + version);
    }
  }

  public static Protocol getLatest() {
    return getInstance(0);
  }
}
