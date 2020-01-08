package no.ssb.helidon.media.protobuf;


import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import io.helidon.webserver.Handler;
import io.helidon.webserver.JsonService;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;

import java.util.Objects;
import java.util.function.BiFunction;

import static io.helidon.media.common.ContentTypeCharset.determineCharset;

/**
 * A {@link Service} and a {@link Handler} that provides Jackson
 * support to Helidon.
 */
public final class ProtobufJsonSupport extends JsonService {
    private final BiFunction<? super ServerRequest, ? super ServerResponse, ? extends JsonFormat.Parser> parserProvider;
    private final BiFunction<? super ServerRequest, ? super ServerResponse, ? extends JsonFormat.Printer> printerProvider;

    /**
     * Creates a new {@link ProtobufJsonSupport}.
     *
     * @param parserProvider  a {@link BiFunction} that returns
     *                        an {@link JsonFormat.Parser} when given a {@link ServerRequest} and
     *                        a {@link ServerResponse}; must not be {@code null}
     * @param printerProvider a {@link BiFunction} that returns
     *                        an {@link JsonFormat.Printer} when given a {@link ServerRequest} and
     *                        a {@link ServerResponse}; must not be {@code null}
     * @throws NullPointerException if {@code objectMapperProvider}
     *                              is {@code null}
     */
    private ProtobufJsonSupport(final BiFunction<? super ServerRequest, ? super ServerResponse, ? extends JsonFormat.Parser> parserProvider,
                                final BiFunction<? super ServerRequest, ? super ServerResponse, ? extends JsonFormat.Printer> printerProvider) {
        this.parserProvider = Objects.requireNonNull(parserProvider);
        this.printerProvider = Objects.requireNonNull(printerProvider);
    }

    @Override
    public void accept(final ServerRequest request, final ServerResponse response) {
        // Don't register reader/writer if content is a CharSequence (likely String) (see #645)
        request.content()
                .registerReader(cls -> !CharSequence.class.isAssignableFrom(cls)
                                && MessageOrBuilder.class.isAssignableFrom(cls),
                        ProtobufJsonProcessing.reader(() -> this.parserProvider.apply(request, response)));
        response.registerWriter(payload -> !(payload instanceof CharSequence)
                        && MessageOrBuilder.class.isAssignableFrom(payload.getClass())
                        && acceptsJson(request, response),
                ProtobufJsonProcessing.writer(() -> this.printerProvider.apply(request, response), determineCharset(response.headers())));
        request.next();
    }

    /**
     * Creates a new {@link ProtobufJsonSupport}.
     *
     * @return a new {@link ProtobufJsonSupport}
     */
    public static ProtobufJsonSupport create() {
        return create((req, res) -> JsonFormat.parser(), (req, res) -> JsonFormat.printer());
    }

    /**
     * Creates a new {@link ProtobufJsonSupport}.
     *
     * @param parserProvider  a {@link BiFunction} that returns
     *                        an {@link JsonFormat.Parser} when given a {@link ServerRequest} and
     *                        a {@link ServerResponse}; must not be {@code null}
     * @param printerProvider a {@link BiFunction} that returns
     *                        an {@link JsonFormat.Printer} when given a {@link ServerRequest} and
     *                        a {@link ServerResponse}; must not be {@code null}
     * @return a new {@link ProtobufJsonSupport}
     * @throws NullPointerException if {@code objectMapperProvider}
     *                              is {@code null}
     */
    public static ProtobufJsonSupport create(final BiFunction<? super ServerRequest, ? super ServerResponse, ? extends JsonFormat.Parser> parserProvider,
                                             final BiFunction<? super ServerRequest, ? super ServerResponse, ? extends JsonFormat.Printer> printerProvider) {
        return new ProtobufJsonSupport(parserProvider, printerProvider);
    }
}

