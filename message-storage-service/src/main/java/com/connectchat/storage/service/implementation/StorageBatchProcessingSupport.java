package com.connectchat.storage.service.implementation;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
final class StorageBatchProcessingSupport {

    static <T> void processBatch(
        List<T> items,
        Consumer<T> handler,
        Consumer<T> onSuccess,
        BiConsumer<T, RuntimeException> onFailure,
        Function<T, UUID> idExtractor,
        String logMessage
    ) {
        for (T item : items) {
            try {
                handler.accept(item);
                onSuccess.accept(item);
            } catch (RuntimeException exception) {
                onFailure.accept(item, exception);
                log.warn(logMessage, idExtractor.apply(item), exception);
            }
        }
    }
}
