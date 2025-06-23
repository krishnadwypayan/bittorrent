package com.krishna.kota.api;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

@Controller("/hello")
public class HelloController {

    @Get
    HttpResponse<String> sayHello() {
        return HttpResponse.ok("Hello");
    }

}
