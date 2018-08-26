/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.codec;

public class DecoderException extends CodecException {
  public DecoderException(String message) {
    super(message);
  }

  public DecoderException(String message, Throwable cause) {
    super(message, cause);
  }
}
