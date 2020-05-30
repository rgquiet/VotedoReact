package com.rgq.votedoreact.service;

import com.rgq.votedoreact.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SseService {
    private ReactiveMongoTemplate template;

    @Autowired
    public SseService(ReactiveMongoTemplate template) {
        this.template = template;
    }

    public void dropCollectionByName(String name) {
        template.dropCollection(name).subscribe();
    }

    public void createCollectionWithName(String name) {
        template.createCollection(
            name,
            CollectionOptions
                .empty()
                .capped()
                .size(4)
                .maxDocuments(10)
        ).subscribe();
    }

    public Mono<Boolean> existCollectionWithName(String name) {
        return template.collectionExists(name);
    }

    public Mono<Event> saveOrUpdateEvent(Event event, String name) {
        return template.save(event, name);
    }

    public Flux<Event> subCollectionByName(String name) {
        return template.tail(new Query().addCriteria(Criteria.where("status").is(true)), Event.class, name);
    }
}
