package com.app4.project.timelapseserver.codec;

import static org.jcodec.common.Codec.H264;
import static org.jcodec.common.Format.MOV;

import org.jcodec.api.SequenceEncoder;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.Rational;
import org.jcodec.scale.AWTUtil;

import javax.imageio.ImageIO;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class JpgSequenceEncoder implements Closeable {
  private final SequenceEncoder sequenceEncoder;
  private final Runnable endRunnable;

  public JpgSequenceEncoder(SeekableByteChannel channel, int fps, Runnable endRunnable) throws IOException {
    this.endRunnable = endRunnable;
    this.sequenceEncoder = new SequenceEncoder(channel, Rational.R(fps, 1), MOV, H264, null);
  }

  public void addFrame(InputStream inputStream) throws IOException {
    Picture picture = AWTUtil.fromBufferedImage(ImageIO.read(inputStream), ColorSpace.RGB);
    sequenceEncoder.encodeNativeFrame(picture);
  }

  @Override
  public void close() throws IOException {
    sequenceEncoder.finish();
    endRunnable.run();
  }
}
