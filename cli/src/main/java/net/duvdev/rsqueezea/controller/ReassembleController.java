/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.controller;

import net.duvdev.rsqueezea.KeyReassembler;
import net.duvdev.rsqueezea.codec.Codec;
import net.duvdev.rsqueezea.codec.DecoderException;
import net.duvdev.rsqueezea.model.SqueezedKey;
import net.duvdev.rsqueezea.protocol.Protocol;
import net.duvdev.rsqueezea.protocol.ProtocolFactory;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1StreamParser;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemObject;

import javax.annotation.Nullable;
import java.io.*;
import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateCrtKeySpec;

public final class ReassembleController {

  private final @Nullable RSAPublicKey publicKey;

  private final InputStream inputStream;

  private final Codec<byte[], byte[]> codec;

  private final OutputStream outputStream;

  public ReassembleController(
      @Nullable RSAPublicKey publicKey,
      InputStream inputStream,
      Codec<byte[], byte[]> codec,
      OutputStream outputStream) {
    this.publicKey = publicKey;
    this.inputStream = inputStream;
    this.codec = codec;
    this.outputStream = outputStream;
  }

  public void run() throws IOException {
    byte[] encoded = IOUtils.toByteArray(inputStream);
    byte[] data;
    try {
      data = codec.decode(encoded);
    } catch (DecoderException e) {
      throw new IOException(e.getMessage(), e.getCause());
    }
    ASN1StreamParser parser = new ASN1StreamParser(new ByteArrayInputStream(data));
    ASN1Integer version = (ASN1Integer) parser.readObject();
    Protocol protocol = ProtocolFactory.getInstance(version.getValue().intValueExact());

    SqueezedKey key = protocol.decodeSqueezedKey(data);

    BigInteger modulus = key.getModulus();
    BigInteger publicExponent = key.getPublicExponent();

    if (modulus == null || publicExponent == null) {
      if (publicKey == null) {
        throw new IllegalArgumentException(
            "Key does not have public exponent, and no external public key provided");
      } else {
        modulus = publicKey.getModulus();
        publicExponent = publicKey.getPublicExponent();
      }
    }

    RSAPrivateCrtKeySpec privateKeySpec =
        KeyReassembler.reassemble(modulus, publicExponent, key.getPrimeP());

    PrivateKeyInfo pkInfo =
        PrivateKeyInfoFactory.createPrivateKeyInfo(
            new RSAPrivateCrtKeyParameters(
                privateKeySpec.getModulus(),
                privateKeySpec.getPublicExponent(),
                privateKeySpec.getPrivateExponent(),
                privateKeySpec.getPrimeP(),
                privateKeySpec.getPrimeQ(),
                privateKeySpec.getPrimeExponentP(),
                privateKeySpec.getPrimeExponentQ(),
                privateKeySpec.getCrtCoefficient()));

    ASN1Encodable encodable = pkInfo.parsePrivateKey();
    ASN1Primitive primitive = encodable.toASN1Primitive();
    JcaPEMWriter pemWriter = new JcaPEMWriter(new OutputStreamWriter(outputStream));
    pemWriter.writeObject(new PemObject("RSA PRIVATE KEY", primitive.getEncoded()));
    pemWriter.flush();
    outputStream.flush();
  }
}
