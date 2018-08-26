/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea;

import net.duvdev.rsqueezea.model.SqueezedKey;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;

public final class TestKey {
  public static final BigInteger N =
      new BigInteger(
          "C0A5FC54E72BE64289CF785D647CCD96FF3A1864BA00FA9A60D073BE446FF0FE"
              + "65DA1D7F0E019166454ABFA6FBD5A7D2F03A0E349FF52EB3D6A9F4B9FDB9D188"
              + "1706A21DB3B184BA9F10C16113F951B24055561925297E51C31A6ED82937974A"
              + "51A98108C6103F6ABCF766F3E4B96C1415D4CC1C3AB598547A5CC6AD22C081EB"
              + "FDCFB4D85F907056DC2564098045AFEC962D54EA599C40F686906E844D47F5B6"
              + "042CD0352FD858ED145422FD9CE9E816E3B27169A603899964F4BF363C551E24"
              + "36984E4746E42E5A3D1F2FD2303F57CE689C3B0EB6E64F83F622B3D8340DB175"
              + "C860BB29D44A1BE1B75562518010B60374D818991653F54B633A749CDA35CAD1",
          16);

  public static final BigInteger E = new BigInteger("65537");

  public static final BigInteger P =
      new BigInteger(
          "E0E4AC66C7F018E682953A241055F3E71F0FF451A08031E60567B8150C63EF24"
              + "36DD020E9BE792FE5745BCE5C850FE190529CE49D3BE71A26E869EB287F59F4B"
              + "FAF069A433EA158A950F2A829687810D2778C85937BA206C9A7A4FCF10408496"
              + "760252DC40C3ADC396B6EE094E5CB66F87ADF8F5EFF747E1F664498BC3E7A65B",
          16);

  public static final SqueezedKey SQUEEZE_KEY = new SqueezedKey(P, N, E);

  public static final RSAPublicKey PUBLIC_KEY;

  public static final RSAPrivateCrtKeySpec PRIVATE_KEY_SPEC;

  static {
    try {
      PUBLIC_KEY =
          (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(N, E));
      PRIVATE_KEY_SPEC = KeyReassembler.reassemble(PUBLIC_KEY, P);
    } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }

  /** Do not instantiate */
  private TestKey() {}
}
