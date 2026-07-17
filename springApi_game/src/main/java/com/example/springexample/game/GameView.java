package com.example.springexample.game;

import java.util.List;

import com.example.springexample.game.Story.Choice;
import com.example.springexample.game.Story.Ending;
import com.example.springexample.game.Story.Node;

/**
 * The JSON contract sent to the frontend. Deliberately flat and view-only:
 * it exposes what to render, never the graph wiring (targets/aliases stay server-side).
 */
public record GameView(
        String sessionId,
        String nodeId,
        String speaker,
        String text,
        String mood,
        String effect,
        List<ChoiceView> choices,
        EndingView ending,
        String snark) {

    public record ChoiceView(String id, String label) {}

    public record EndingView(String title, String type, int number, int total) {}

    static GameView of(String sessionId, Node node, String snark) {
        List<ChoiceView> choices = node.choices().stream()
                .map(GameView::toChoiceView)
                .toList();

        EndingView ending = node.isEnding() ? toEndingView(node.ending()) : null;

        return new GameView(
                sessionId,
                node.id(),
                node.speaker(),
                node.text(),
                node.mood().name(),
                node.effect().name(),
                choices,
                ending,
                snark);
    }

    private static ChoiceView toChoiceView(Choice c) {
        return new ChoiceView(c.id(), c.label());
    }

    private static EndingView toEndingView(Ending e) {
        return new EndingView(e.title(), e.type().name(), e.number(), Story.TOTAL_ENDINGS);
    }
}
