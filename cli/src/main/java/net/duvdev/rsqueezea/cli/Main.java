/*
 * Copyright (c) Itay Duvdevani and contributors. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for details.
 */
package net.duvdev.rsqueezea.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import net.duvdev.rsqueezea.SqueezeType;
import net.duvdev.rsqueezea.controller.ReassembleController;
import net.duvdev.rsqueezea.controller.SqueezeController;
import net.duvdev.rsqueezea.encoder.EncoderFactory;
import net.duvdev.rsqueezea.loader.PKCS1PrivateKeyLoader;
import net.duvdev.rsqueezea.loader.PKCS1PublicKeyLoader;
import net.duvdev.rsqueezea.loader.X509CertificatePublicKeyLoader;

import java.io.*;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

public final class Main {

  public static final void main(String[] args) {
    MainArgs mainArgs = new MainArgs();
    SqueezeCommand squeezeCommand = new SqueezeCommand();
    ReassembleCommand reassembleCommand = new ReassembleCommand();
    JCommander jc =
        JCommander.newBuilder()
            .addObject(mainArgs)
            .addCommand("squeeze", squeezeCommand)
            .addCommand("reassemble", reassembleCommand)
            .build();
    try {
      jc.parse(args);
    } catch (ParameterException e) {
      System.err.println(e.getMessage());
      jc.usage();
      System.exit(1);
    }

    if (mainArgs.help) {
      jc.usage();
      return;
    }

    String parsedCommand = jc.getParsedCommand();

    try {
      if ("squeeze".equalsIgnoreCase(parsedCommand)) {
        doSqueeze(squeezeCommand);
      } else if ("reassemble".equalsIgnoreCase(parsedCommand)) {
        doReassemble(reassembleCommand);
      } else {
        if (mainArgs.verbose) {
          throw new IllegalArgumentException(parsedCommand);
        } else {
          System.err.println("No such command: " + parsedCommand);
          System.exit(1);
        }
      }
    } catch (IOException | IllegalArgumentException e) {
      if (mainArgs.verbose) {
        throw new RuntimeException(e);
      } else {
        System.err.println(e.getMessage());
      }
      System.exit(1);
    }
  }

  private static void doSqueeze(SqueezeCommand args) throws IOException {
    InputStream pemStream;
    if ("-".equals(args.inputFile)) {
      pemStream = System.in;
    } else {
      pemStream = new FileInputStream(new File(args.inputFile));
    }

    OutputStream outputStream;
    if ("-".equals(args.outputFile)) {
      outputStream = System.out;
    } else {
      outputStream = new FileOutputStream(new File(args.outputFile));
    }

    try {
      SqueezeType squeezeType =
          args.noModulus ? SqueezeType.PRIME_P : SqueezeType.PRIME_WITH_MODULUS;
      SqueezeController controller =
          new SqueezeController(
              new PKCS1PrivateKeyLoader(pemStream),
              EncoderFactory.wrapOutputStream(args.format, squeezeType, outputStream),
              squeezeType);
      controller.run();
    } finally {
      try {
        if (pemStream != null) {
          pemStream.close();
        }
      } catch (IOException e) {
        // ignored
      }
      try {
        if (outputStream != null) {
          outputStream.close();
        }
      } catch (IOException e) {
        // ignored
      }
    }
  }

  private static void doReassemble(ReassembleCommand args) throws IOException {
    InputStream inputStream;
    if ("-".equals(args.inputFile)) {
      inputStream = System.in;
    } else {
      inputStream = new FileInputStream(new File(args.inputFile));
    }

    OutputStream outputStream;
    if ("-".equals(args.outputFile)) {
      outputStream = System.out;
    } else {
      outputStream = new FileOutputStream(new File(args.outputFile));
    }

    RSAPublicKey publicKey = null;

    if (args.modulus != null || args.exponent != null) {
      if (args.exponent == null || args.modulus == null) {
        throw new IOException("Must specify exponent and modulus");
      }
      try {
        publicKey =
            (RSAPublicKey)
                KeyFactory.getInstance("RSA")
                    .generatePublic(
                        new RSAPublicKeySpec(
                            new BigInteger(args.modulus, 16), new BigInteger(args.exponent, 16)));
      } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
        throw new IOException(e);
      }
    } else if (args.crtFile != null) {
      try (FileInputStream fis = new FileInputStream(args.crtFile)) {
        publicKey = new X509CertificatePublicKeyLoader(fis).load();
      }
    } else if (args.pkcs1File != null) {
      try (FileInputStream fis = new FileInputStream(args.pkcs1File)) {
        publicKey = new PKCS1PublicKeyLoader(fis).load();
      }
    }

    ReassembleController controller =
        new ReassembleController(
            publicKey, EncoderFactory.wrapInputStream(args.format, inputStream), outputStream);
    controller.run();
  }

  @Parameters
  private static final class MainArgs {
    @Parameter(
      names = {"-h", "--help"},
      help = true,
      description = "This help message"
    )
    private boolean help = false;

    @Parameter(
      names = {"-v", "--verbose"},
      description = "Be verbose"
    )
    private boolean verbose = false;
  }

  @Parameters(commandDescription = "Squeeze an RSA private key")
  private static final class SqueezeCommand {
    @Parameter(
      names = {"-i", "--input"},
      description = "PKCS#1 PEM RSA private key file. Use \"-\" for STDIN"
    )
    private String inputFile = "-";

    @Parameter(
      names = {"-o", "--output"},
      description = "File to write squeezed key to. Use \"-\" for STDOUT"
    )
    private String outputFile = "-";

    @Parameter(
      names = {"-x", "--no-modulus"},
      description =
          "Don't write public modulus an exponent to output file. Results in a smaller file, but reassembly will need the public key from external source"
    )
    private Boolean noModulus = false;

    @Parameter(
      names = {"-f", "--format"},
      description = "Output format (PEM, DER or QR)"
    )
    private String format = "DER";
  }

  @Parameters(commandDescription = "Reassemble an RSA private key from a squeezed key")
  private static final class ReassembleCommand {
    @Parameter(
      names = {"-i", "--input"},
      description = "PKCS#1 PEM RSA private key file. Use \"-\" for STDIN"
    )
    private String inputFile = "-";

    @Parameter(
      names = {"-o", "--output"},
      description = "File to write squeezed key to. Use \"-\" for STDOUT"
    )
    private String outputFile = "-";

    @Parameter(
      names = {"-n", "--modulus"},
      description = "Public modulus (hex), if not found in squeezed key"
    )
    private String modulus;

    @Parameter(
      names = {"-e", "--exponent"},
      description = "Public exponent (hex), if not found in squeezed key"
    )
    private String exponent;

    @Parameter(
      names = {"-c", "--crt"},
      description = "Path to X.509 certificate to get public key from"
    )
    private String crtFile;

    @Parameter(
      names = {"-p", "--private"},
      description = "Path to PKCS#1 PEM file to get public key from"
    )
    private String pkcs1File;

    @Parameter(
      names = {"-f", "--format"},
      description = "Input format (PEM, DER or QR)"
    )
    private String format = "DER";
  }
}
