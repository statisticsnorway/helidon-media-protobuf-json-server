package no.ssb.helidon.media.protobuf;

import com.google.protobuf.MessageOrBuilder;
import io.helidon.common.GenericType;
import io.helidon.common.http.DataChunk;
import io.helidon.common.mapper.Mapper;
import io.helidon.common.reactive.Single;
import io.helidon.media.common.ContentReaders;
import io.helidon.media.common.MessageBodyReader;
import io.helidon.media.common.MessageBodyReaderContext;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Flow;

final class ProtobufBodyReader implements MessageBodyReader<Object> {

    private ProtobufBodyReader() {
    }

    @Override
    public PredicateResult accept(GenericType<?> type, MessageBodyReaderContext context) {
        Class<?> clazz = type.rawType();
        return !CharSequence.class.isAssignableFrom(clazz)
                && MessageOrBuilder.class.isAssignableFrom(clazz)
                ? PredicateResult.COMPATIBLE
                : PredicateResult.NOT_SUPPORTED;
    }

    @Override
    public <U extends Object> Single<U> read(Flow.Publisher<DataChunk> publisher,
                                             GenericType<U> type, MessageBodyReaderContext context) {
        Charset charset = context.contentType()
                .get()
                .charset()
                .map(charsetName -> Charset.forName(charsetName))
                .orElse(StandardCharsets.UTF_8);
        return ContentReaders.readBytes(publisher).map(new ProtobufBodyReader.BytesToObject<>(type, charset));
    }

    /**
     * Create a new {@link ProtobufBodyReader} instance.
     *
     * @return JacksonBodyWriter
     */
    public static ProtobufBodyReader create() {
        return new ProtobufBodyReader();
    }

    private static final class BytesToObject<T> implements Mapper<byte[], T> {

        private final GenericType<? super T> type;
        private final Charset charset;

        BytesToObject(GenericType<T> type, Charset charset) {
            this.type = type;
            this.charset = charset;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T map(byte[] bytes) {
            return ProtobufJsonUtils.toPojo(new String(bytes, charset), (Class<T>) this.type.rawType());
        }
    }
}