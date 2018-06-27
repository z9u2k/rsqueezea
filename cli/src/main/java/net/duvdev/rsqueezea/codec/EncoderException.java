/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.codec;

public class EncoderException extends CodecException {
  public EncoderException(Throwable cause) {
    super(cause);
  }

  public EncoderException(String message, Throwable cause) {
    super(message, cause);
  }
}
