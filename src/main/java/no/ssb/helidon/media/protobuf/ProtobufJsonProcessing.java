/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.ssb.helidon.media.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import io.helidon.common.http.DataChunk;
import io.helidon.common.http.Reader;
import io.helidon.media.common.ContentReaders;
import io.helidon.media.common.ContentWriters;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.concurrent.Flow;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Utility methods for Jackson integration.
 */
public final class ProtobufJsonProcessing {

    private ProtobufJsonProcessing() {
        super();
    }

    /**
     * Returns a {@link Reader} that converts a {@link Flow.Publisher Publisher} of {@link java.nio.ByteBuffer}s to
     * a Java object.
     *
     * <p>This method is intended for the derivation of other, more specific readers.</p>
     *
     * @param parserSupplier the {@link Supplier< JsonFormat.Parser>} to use; must not be {@code null}
     * @return the byte array content reader that transforms a publisher of byte buffers to a completion stage that
     * might end exceptionally with a {@link RuntimeException} in case of I/O error
     * @throws NullPointerException if {@code objectMapper} is {@code null}
     */
    public static Reader<Object> reader(final Supplier<JsonFormat.Parser> parserSupplier) {
        Objects.requireNonNull(parserSupplier);
        return (publisher, cls) -> ContentReaders.byteArrayReader()
                .apply(publisher)
                .thenApply(bytes -> {
                    try {
                        Method newBuilderMethod = cls.getMethod("newBuilder", null);
                        Message.Builder builder = (Message.Builder) newBuilderMethod.invoke(null);
                        JsonFormat.Parser parser = parserSupplier.get();
                        InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(bytes), UTF_8);
                        parser.merge(reader, builder);
                        Message message = builder.build();
                        if (!cls.isAssignableFrom(message.getClass())) {
                            throw new ProtobufJsonRuntimeException(String.format("Incompatible type: %s -- %s", cls.getName(), message.getClass().getName()));
                        }
                        return message;
                    } catch (final IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException wrapMe) {
                        throw new ProtobufJsonRuntimeException(wrapMe.getMessage(), wrapMe);
                    }
                });
    }

    /**
     * Returns a function (writer) converting {@link Object}s to {@link Flow.Publisher Publisher}s
     * of {@link DataChunk}s by using the supplied {@link JsonFormat.Printer}.
     *
     * @param printerSupplier the {@link Supplier< JsonFormat.Printer>} to use; must not be {@code null}
     * @param charset         the charset to use; may be null
     * @return created function
     * @throws NullPointerException if {@code objectMapper} is {@code null}
     */
    public static Function<Object, Flow.Publisher<DataChunk>> writer(final Supplier<JsonFormat.Printer> printerSupplier, final Charset charset) {
        Objects.requireNonNull(printerSupplier);
        return payload -> {
            String json;
            try {
                JsonFormat.Printer printer = printerSupplier.get();
                json = printer.print((MessageOrBuilder) payload);
            } catch (final InvalidProtocolBufferException wrapMe) {
                throw new ProtobufJsonRuntimeException(wrapMe.getMessage(), wrapMe);
            }
            return ContentWriters.charSequenceWriter(charset == null ? UTF_8 : charset).apply(json);
        };
    }
}
