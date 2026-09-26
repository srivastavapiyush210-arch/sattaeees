package com.sattaees.sattaees.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Health & Verification", description = "Base ping and status check endpoint")
public class HelloController {

    @GetMapping("/hello")
    @Operation(summary = "Application health ping")
    public Map<String, String> hello() {
        return Map.of(
                "status", "UP",
                "message", "Sattaees backend is running successfully 🚀",
                "version", "1.0.0"
        );
    }
}
