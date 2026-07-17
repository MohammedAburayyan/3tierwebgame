package com.example.springexample.game;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.example.springexample.game.Story.Choice;
import com.example.springexample.game.Story.Node;
import com.example.springexample.game.Story.Outcome;

/**
 * Owns per-session game state and all story navigation.
 *
 * <p>Every browser gets its own {@code sessionId}, so any number of travelers can
 * play at once without stepping on each other — this replaces the old single shared
 * {@code userState} field, which let one player's progress overwrite everyone's.
 * State is intentionally in-memory (a restart wipes runs in progress); the story is
 * the product, not the save file.
 */
@Service
public class GameEngine {

    /** sessionId -> current node id. */
    private final Map<String, String> sessions = new ConcurrentHashMap<>();
    private final Random rng = new Random();

    public GameView start() {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, Story.START_ID);
        return GameView.of(sessionId, Story.node(Story.START_ID), null);
    }

    /**
     * Advance the session by resolving the traveler's input against the current node's choices.
     * Input may be a choice id, a choice label, or one of its typed aliases (case-insensitive).
     * An unrecognized input does not advance the story — the DM just gets snippy.
     */
    public GameView choose(String sessionId, String rawInput) {
        String currentId = sessions.get(sessionId);
        if (currentId == null) {
            // Unknown or expired session — quietly restart so the UI never dead-ends.
            return start();
        }

        Node current = Story.node(currentId);
        if (current.isEnding()) {
            // Already at an ending; nothing to choose. Echo it back so the UI stays consistent.
            return GameView.of(sessionId, current, null);
        }

        Choice picked = match(current, rawInput);
        if (picked == null) {
            return GameView.of(sessionId, current, snark());
        }

        Node next = resolve(picked.target());
        sessions.put(sessionId, next.id());
        return GameView.of(sessionId, next, null);
    }

    /** Follow roll nodes to a concrete story/ending node. */
    private Node resolve(String targetId) {
        Node node = Story.node(targetId);
        // Guard against a mis-authored cycle; a real story tree resolves in one hop.
        for (int guard = 0; node != null && node.isRoll() && guard < 16; guard++) {
            node = Story.node(rollOutcome(node.roll()));
        }
        return node;
    }

    private String rollOutcome(List<Outcome> outcomes) {
        int total = outcomes.stream().mapToInt(Outcome::weight).sum();
        int pick = rng.nextInt(Math.max(total, 1));
        int cursor = 0;
        for (Outcome o : outcomes) {
            cursor += o.weight();
            if (pick < cursor) {
                return o.target();
            }
        }
        return outcomes.get(outcomes.size() - 1).target();
    }

    private Choice match(Node node, String rawInput) {
        if (rawInput == null) {
            return null;
        }
        String input = rawInput.trim().toLowerCase();
        if (input.isEmpty()) {
            return null;
        }
        for (Choice c : node.choices()) {
            if (c.id().equalsIgnoreCase(input) || c.label().equalsIgnoreCase(input)) {
                return c;
            }
            for (String alias : c.aliases()) {
                if (alias.equalsIgnoreCase(input)) {
                    return c;
                }
            }
        }
        return null;
    }

    private static final String[] SNARK = {
        "Guess you don't speak my language. Come back when you've learned it.",
        "That's not a road, traveler. That's noise. Try again.",
        "I've smitten souls for less gibberish. Pick a path.",
        "Bold. Wrong, but bold. Choose one of the doors in front of you.",
    };

    private String snark() {
        return SNARK[rng.nextInt(SNARK.length)];
    }
}
