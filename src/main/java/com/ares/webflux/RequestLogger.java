package com.ares.webflux;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(-1)
public class RequestLogger implements WebFilter {

  private static final Logger logger = LoggerFactory.getLogger(RequestLogger.class);

  @Override
  public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange,
      @NonNull WebFilterChain chain) {

    return Mono.deferContextual(contextView -> Mono.just(contextView)
        .doOnNext(ctx -> {
          String requestMethod = ctx.get(CustomMdcFields.REQUEST_METHOD);
          String requestPath = ctx.get(CustomMdcFields.REQUEST_PATH);
          logger.info("Received request [method={}, path={}]", requestMethod, requestPath);
        }).then(chain.filter(exchange)));
  }
}
