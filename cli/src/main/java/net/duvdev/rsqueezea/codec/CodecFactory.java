/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.codec;

public final class CodecFactory {

  /** Do not instantiate */
  private CodecFactory() {}

  public static Codec<byte[], byte[]> getCodec(String name) {
    Class<? extends Codec<byte[], byte[]>> clazz = null;
    for (CodecType codecType : CodecType.values()) {
      if (codecType.name.equalsIgnoreCase(name)) {
        clazz = codecType.clazz;
        break;
      }
    }
    if (clazz == null) {
      throw new IllegalArgumentException("Unknown codec: " + name);
    }
    try {
      return clazz.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new IllegalArgumentException("Cannot instantiate " + clazz.getCanonicalName(), e);
    }
  }

  public enum CodecType {
    DER("DER", IdentityCodec.class),
    PEM("PEM", PEMCodec.class),
    QR("QR", QRCodeCodec.class);

    private final String name;

    private final Class<? extends Codec<byte[], byte[]>> clazz;

    CodecType(String name, Class<? extends Codec<byte[], byte[]>> clazz) {
      this.name = name;
      this.clazz = clazz;
    }

    public String formatName() {
      return name;
    }
  }
}
