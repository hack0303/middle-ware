package org.cloud.contollers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RefreshScope
@RestController
public class SayHelloController {

    @Value("${k1.v1}")
    String v1;

    @Value("${name}")
    String name;
    @GetMapping("{key}")
    public String k1Value(@PathVariable("key") String key){
        return key+" is "+v1 +" "+name;
    }

}
