package com.rgq.votedoreact.service;

import com.rgq.votedoreact.sse.UserSSE;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserEventService {
    private ReactiveMongoTemplate template;

    public UserEventService(ReactiveMongoTemplate template) {
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

    public Mono<UserSSE> saveOrUpdateEvent(UserSSE userSSE, String name) {
        return template.save(userSSE, name);
    }

    public Mono<UserSSE> getById(String id, String name) {
        return template.findById(id, UserSSE.class, name);
    }

    public Flux<UserSSE> subCollectionByName(String name) {
        return template.tail(new Query().addCriteria(Criteria.where("status").is(true)), UserSSE.class, name);
    }
}
