/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.controller;

import net.duvdev.rsqueezea.KeyReassembler;
import net.duvdev.rsqueezea.SqueezeFormat;
import net.duvdev.rsqueezea.SqueezedKey;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.util.io.pem.PemObject;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateCrtKeySpec;

public final class ReassembleController {

  private final @Nullable RSAPublicKey publicKey;

  private final InputStream inputStream;

  private final OutputStream outputStream;

  public ReassembleController(
      @Nullable RSAPublicKey publicKey, InputStream inputStream, OutputStream outputStream) {
    this.publicKey = publicKey;
    this.inputStream = inputStream;
    this.outputStream = outputStream;
  }

  public void run() throws IOException {
    SqueezedKey key = SqueezeFormat.read(inputStream, publicKey);
    RSAPrivateCrtKeySpec privateKeySpec =
        KeyReassembler.reassemble(key.getModulus(), key.getPublicExponent(), key.getPrimeP());

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
  }
}
