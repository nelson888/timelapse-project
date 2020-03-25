package com.app4.project.timelapseserver.codec;

import org.jcodec.api.SequenceEncoder;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.Rational;
import org.jcodec.scale.AWTUtil;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;

import static org.jcodec.common.Codec.H264;
import static org.jcodec.common.Format.MOV;

public class JpgSequenceEncoder extends SequenceEncoder implements Closeable {

  public JpgSequenceEncoder(SeekableByteChannel channel, int fps) throws IOException {
    super(channel, Rational.R(fps, 1), MOV, H264, null);
  }

  public void addFrame(byte[] bytes) throws IOException {
    Picture picture = AWTUtil.fromBufferedImage(
      ImageIO.read(new ByteArrayInputStream(bytes)), ColorSpace.RGB);
    encodeNativeFrame(picture);
  }

  @Override
  public void close() throws IOException {
    finish();
  }
}
