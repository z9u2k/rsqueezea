/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.codec;

public class CodecException extends Exception {
  public CodecException(String message) {
    super(message);
  }

  public CodecException(Throwable cause) {
    super(cause);
  }

  public CodecException(String message, Throwable cause) {
    super(message, cause);
  }
}
