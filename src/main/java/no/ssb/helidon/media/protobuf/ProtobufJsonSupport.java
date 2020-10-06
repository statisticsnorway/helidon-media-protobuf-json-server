package no.ssb.helidon.media.protobuf;


import io.helidon.common.LazyValue;
import io.helidon.media.common.MediaSupport;
import io.helidon.media.common.MessageBodyReader;
import io.helidon.media.common.MessageBodyWriter;
import io.helidon.webserver.Handler;
import io.helidon.webserver.Service;

import java.util.Collection;
import java.util.List;

/**
 * A {@link Service} and a {@link Handler} that provides Jackson
 * support to Helidon.
 */
public final class ProtobufJsonSupport implements MediaSupport {

    private static final LazyValue<ProtobufJsonSupport> DEFAULT = LazyValue.create(() -> new ProtobufJsonSupport());

    private final ProtobufBodyReader reader;
    private final ProtobufBodyWriter writer;

    private ProtobufJsonSupport() {
        this.reader = ProtobufBodyReader.create();
        this.writer = ProtobufBodyWriter.create();
    }

    /**
     * Creates a new {@link ProtobufJsonSupport}.
     *
     * @return a new {@link ProtobufJsonSupport}
     */
    public static ProtobufJsonSupport create() {
        return DEFAULT.get();
    }

    @Override
    public Collection<MessageBodyReader<?>> readers() {
        return List.of(reader);
    }

    @Override
    public Collection<MessageBodyWriter<?>> writers() {
        return List.of(writer);
    }
}

