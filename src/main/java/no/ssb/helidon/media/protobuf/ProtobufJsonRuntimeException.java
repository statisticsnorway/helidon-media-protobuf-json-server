package no.ssb.helidon.media.protobuf;

public class ProtobufJsonRuntimeException extends RuntimeException {
    ProtobufJsonRuntimeException() {
    }

    ProtobufJsonRuntimeException(String message) {
        super(message);
    }

    ProtobufJsonRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    ProtobufJsonRuntimeException(Throwable cause) {
        super(cause);
    }

    ProtobufJsonRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
