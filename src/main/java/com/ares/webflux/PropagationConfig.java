package com.ares.webflux;


import io.micrometer.context.ContextRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.MDC;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;

@Configuration
public class PropagationConfig {

  @PostConstruct
  public void registerMdcAccessors() {
    Hooks.enableAutomaticContextPropagation();
    ContextRegistry.getInstance().registerThreadLocalAccessor(
        CustomMdcFields.REQUEST_ID,
        () -> MDC.get(CustomMdcFields.REQUEST_ID),
        requestIdValue -> MDC.put(CustomMdcFields.REQUEST_ID, requestIdValue),
        () -> MDC.remove(CustomMdcFields.REQUEST_ID)
    );
  }
}
