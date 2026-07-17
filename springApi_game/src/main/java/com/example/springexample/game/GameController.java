package com.example.springexample.game;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP surface for the game. Two endpoints:
 *   POST /api/game/start           -> begin a new run, returns a fresh sessionId + the opening node
 *   POST /api/game/choose {..}     -> submit a choice, returns the next node
 */
@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "http://localhost:5173")
public class GameController {

    private final GameEngine engine;

    public GameController(GameEngine engine) {
        this.engine = engine;
    }

    public record ChoiceRequest(String sessionId, String input) {}

    @PostMapping("/start")
    public GameView start() {
        return engine.start();
    }

    @PostMapping("/choose")
    public GameView choose(@RequestBody ChoiceRequest request) {
        return engine.choose(request.sessionId(), request.input());
    }
}
