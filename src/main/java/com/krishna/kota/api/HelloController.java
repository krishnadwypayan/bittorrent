package com.krishna.kota.api;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import reactor.core.publisher.Mono;

@Controller("/hello")
public class HelloController {

    @Get
    Mono<String> sayHello() {
        return Mono.just("Hello");
    }

}
