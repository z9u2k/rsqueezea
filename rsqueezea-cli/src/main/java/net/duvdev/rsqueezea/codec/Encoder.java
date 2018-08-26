/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.codec;

public interface Encoder<D, E> {
  E encode(D decoded) throws EncoderException;
}
