/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.selftest;

public class RSASelfTestException extends Exception {
  RSASelfTestException(Throwable cause) {
    super("RSA self-test failed", cause);
  }
}
