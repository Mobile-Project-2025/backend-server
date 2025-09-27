package com.mobile.server;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppController {

    @GetMapping(value = "/",consumes = "application/json", produces = "application/json")
    @Operation(summary = "Test API", description = "Returns a simple Hello World message.")
    public ResponseEntity<String> home(){
        return ResponseEntity.ok("Hello World");
    }
}
