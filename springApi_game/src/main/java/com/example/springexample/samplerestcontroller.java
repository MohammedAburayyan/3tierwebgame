package com.example.springexample;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Small liveness/greeting endpoint kept for backward compatibility.
 *
 * <p>The game itself moved to {@code com.example.springexample.game.GameController}
 * ({@code /api/game/*}). The old {@code /api/echo} state machine was removed: it
 * held a single shared {@code userState} field, so every player mutated one global
 * game at once. The new engine keeps per-session state instead.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class samplerestcontroller {

    @GetMapping("/greeting")
    public String getGreeting() {
        return "Hello, Traveler";
    }
}
