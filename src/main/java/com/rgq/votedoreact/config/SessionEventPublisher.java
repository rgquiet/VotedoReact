package com.rgq.votedoreact.config;

import com.rgq.votedoreact.sse.SessionSSE;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.FluxSink;

public class SessionEventPublisher {
    private final FluxProcessor processor;
    private final FluxSink sink;

    public SessionEventPublisher() {
        this.processor = DirectProcessor.<SessionSSE>create().serialize();
        this.sink = processor.sink();
    }

    public void publishEvent(SessionSSE event) {
        sink.next(event);
    }

    public Flux<ServerSentEvent> subPublisher() {
        return processor.map(e -> ServerSentEvent.builder(e).build());
    }
}
