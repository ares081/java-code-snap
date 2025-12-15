package com.ares.webflux;

import java.util.Objects;
import java.util.Optional;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class ContextModifierFilter implements WebFilter {

  @Override
  public @NonNull Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {

    Optional<String> requestMethod = Optional.of(exchange)
        .map(ServerWebExchange::getRequest)
        .map(HttpRequest::getMethod)
        .map(HttpMethod::toString);

    Optional<String> requestPath = Optional.of(exchange)
        .map(ServerWebExchange::getRequest)
        .map(ServerHttpRequest::getPath)
        .map(Objects::toString);

    Optional<String> requestId = Optional.of(exchange)
        .map(ServerWebExchange::getRequest)
        .map(ServerHttpRequest::getId);

    return Objects.requireNonNull(chain.filter(exchange).contextWrite(context -> {
      context = context.put(CustomMdcFields.REQUEST_ID, requestId.get());
      if (requestPath.isPresent()) {
        context = context.put(CustomMdcFields.REQUEST_PATH, requestPath.get());
      }
      context = context.put(CustomMdcFields.REQUEST_METHOD, requestMethod.get());
      return context;
    }));
  }
}
