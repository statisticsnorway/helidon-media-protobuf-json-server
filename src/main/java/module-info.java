module no.ssb.helidon.media.protobuf.json.server {
    requires io.helidon.common.http;
    requires io.helidon.common.mapper;
    requires io.helidon.common.reactive;
    requires io.helidon.media.common;
    requires io.helidon.webserver;
    requires com.google.protobuf;
    requires com.google.protobuf.util;

    exports no.ssb.helidon.media.protobuf;
}