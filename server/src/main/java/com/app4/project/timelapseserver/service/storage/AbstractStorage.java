package com.app4.project.timelapseserver.service.storage;

import com.app4.project.timelapse.model.FileData;
import org.jcodec.api.SequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Rational;

import java.io.IOException;
import java.nio.channels.Channels;
import java.util.Iterator;

import static org.jcodec.common.Codec.H264;
import static org.jcodec.common.Format.MOV;

abstract class AbstractStorage implements StorageService {

  SequenceEncoder newSequenceEncoder(SeekableByteChannel channel, int fps) throws IOException {
    return new SequenceEncoder(channel, Rational.R(fps, 1), MOV, H264, null);

  }
}
