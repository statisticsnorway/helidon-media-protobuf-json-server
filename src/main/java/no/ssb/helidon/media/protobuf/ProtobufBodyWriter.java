package no.ssb.helidon.media.protobuf;

import io.helidon.common.GenericType;
import io.helidon.common.http.DataChunk;
import io.helidon.common.http.MediaType;
import io.helidon.common.mapper.Mapper;
import io.helidon.common.reactive.Single;
import io.helidon.media.common.CharBuffer;
import io.helidon.media.common.ContentWriters;
import io.helidon.media.common.MessageBodyWriter;
import io.helidon.media.common.MessageBodyWriterContext;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.Flow;

final class ProtobufBodyWriter implements MessageBodyWriter<Object> {

    private ProtobufBodyWriter() {
    }

    @Override
    public PredicateResult accept(GenericType<?> type, MessageBodyWriterContext context) {
        return !CharSequence.class.isAssignableFrom(type.rawType())
                ? PredicateResult.COMPATIBLE
                : PredicateResult.NOT_SUPPORTED;
    }

    @Override
    public Flow.Publisher<DataChunk> write(Single<? extends Object> content, GenericType<? extends Object> type,
                                           MessageBodyWriterContext context) {

        MediaType contentType = context.findAccepted(MediaType.JSON_PREDICATE, MediaType.APPLICATION_JSON);
        context.contentType(contentType);
        return content.flatMap(new ProtobufBodyWriter.ObjectToChunks(context.charset()));
    }

    /**
     * Create a new {@link ProtobufBodyWriter} instance.
     *
     * @return ProtobufBodyWriter
     */
    public static ProtobufBodyWriter create() {
        return new ProtobufBodyWriter();
    }

    private static final class ObjectToChunks implements Mapper<Object, Flow.Publisher<DataChunk>> {

        private final Charset charset;

        ObjectToChunks(Charset charset) {
            this.charset = charset;
        }

        @Override
        public Flow.Publisher<DataChunk> map(Object content) {
            try {
                CharBuffer buffer = new CharBuffer();
                buffer.write(ProtobufJsonUtils.toString(content));
                return ContentWriters.writeCharBuffer(buffer, charset);
            } catch (IOException wrapMe) {
                throw new RuntimeException(wrapMe.getMessage(), wrapMe);
            }
        }
    }
}