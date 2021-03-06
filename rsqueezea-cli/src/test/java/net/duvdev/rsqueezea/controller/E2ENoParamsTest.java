/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.controller;

import net.duvdev.rsqueezea.TestKey;
import net.duvdev.rsqueezea.codec.CodecFactory;
import net.duvdev.rsqueezea.protocol.SqueezeType;
import org.junit.Test;

public class E2ENoParamsTest {
  @Test(expected = IllegalArgumentException.class)
  public void withoutModulusNoExternalPublicKey() throws Exception {
    EndToEndRunner.doEndToEnd(
        TestKey.PRIVATE_KEY_SPEC, SqueezeType.PRIME_P, CodecFactory.CodecType.DER, null);
  }
}
