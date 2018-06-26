package net.duvdev.rsqueezea.encoder;

import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An {@link OutputStream} wrapper that accumulates written bytes in an internal buffer, for later
 * format conversion on {@link #flush()}.
 *
 * <p>Note that this wrapper supports a single flush. Once the buffer contents have bee written to
 * the wrapped stream, no additional writes are allowed.
 */
public abstract class SerializingOutputStreamWrapper extends OutputStream {

  /** Wrapped {@link java.io.OutputStream} */
  private final OutputStream wrapped;

  /** Buffer to accumulate written bytes to */
  private @Nullable ByteArrayOutputStream buffer = new ByteArrayOutputStream();

  protected SerializingOutputStreamWrapper(OutputStream wrapped) {
    this.wrapped = wrapped;
  }

  @Override
  public final void write(int b) {
    if (buffer == null) {
      throw new IllegalStateException("Write after flush");
    }
    buffer.write(b);
  }

  @Override
  public final void flush() throws IOException {
    if (buffer == null) {
      return;
    }

    serialize(buffer.toByteArray(), wrapped);
    wrapped.flush();

    buffer = null;
  }

  @Override
  public final void close() throws IOException {
    flush();
    wrapped.close();
  }

  /**
   * Called on {@link #flush()} or {@link #close()}.
   *
   * @param bytes Bytes written to this stream
   * @param outputStream Stream to write result to
   * @throws IOException
   */
  protected abstract void serialize(byte[] bytes, OutputStream outputStream) throws IOException;
}
