/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.codec;

public final class CodecFactory {

  /** Do not instantiate */
  private CodecFactory() {}

  public static Codec<byte[], byte[]> getCodec(CodecType type) {
    Class<? extends Codec<byte[], byte[]>> clazz = null;
    for (CodecType codecType : CodecType.values()) {
      if (type == codecType) {
        clazz = codecType.clazz;
        break;
      }
    }
    if (clazz == null) {
      throw new IllegalArgumentException("Unknown codec: " + type.name());
    }
    try {
      return clazz.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new IllegalArgumentException("Cannot instantiate " + clazz.getCanonicalName(), e);
    }
  }

  public enum CodecType {
    DER(IdentityCodec.class),
    PEM(PEMCodec.class),
    QR(QRCodeCodec.class);

    private final Class<? extends Codec<byte[], byte[]>> clazz;

    CodecType(Class<? extends Codec<byte[], byte[]>> clazz) {
      this.clazz = clazz;
    }
  }
}
