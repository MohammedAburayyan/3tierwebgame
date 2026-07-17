package com.example.springexample.game;

import java.util.List;
import java.util.Map;

/**
 * The whole narrative lives here as an immutable graph of {@link Node}s.
 *
 * <p>The server is authoritative for the story: the frontend never decides what
 * happens next, it only reports which choice the traveler made. Each node carries
 * the DM's voice ({@code text}), a {@code mood} that drives the Divine-vs-Infernal
 * theming, and an {@code effect} hint the frontend plays on arrival.
 *
 * <p>Some nodes are {@code roll} nodes: when the engine lands on one it resolves a
 * weighted dice roll and forwards to one of the outcome nodes (this is how the
 * lucky-punch secret ending is reached). Terminal nodes carry an {@link Ending}.
 */
public final class Story {

    private Story() {}

    public enum Mood { NEUTRAL, SALVATION, DAMNATION }

    /** Transient effect the frontend plays when a node arrives. */
    public enum Effect { NONE, BLOOM, EMBERS, SHAKE, RADIANCE }

    public enum EndingType { TRUE, SECRET, BITTERSWEET, BAD, DEFIANT, DARK }

    public record Choice(String id, String label, List<String> aliases, String target) {}

    public record Outcome(int weight, String target) {}

    public record Ending(String title, EndingType type, int number) {}

    public record Node(
            String id,
            String speaker,
            String text,
            Mood mood,
            Effect effect,
            List<Choice> choices,
            List<Outcome> roll,
            Ending ending) {

        boolean isRoll() { return roll != null && !roll.isEmpty(); }
        boolean isEnding() { return ending != null; }
    }

    /** Total number of distinct endings — surfaced to the UI for the "X / TOTAL" collectible counter. */
    public static final int TOTAL_ENDINGS = 6;

    public static final String START_ID = "start";

    private static Node story(String id, String speaker, String text, Mood mood, Effect effect, List<Choice> choices) {
        return new Node(id, speaker, text, mood, effect, choices, null, null);
    }

    private static Node rollNode(String id, Mood mood, List<Outcome> roll) {
        return new Node(id, "THE LIGHT", "", mood, Effect.NONE, List.of(), roll, null);
    }

    private static Node ending(String id, String text, Mood mood, Effect effect, Ending ending) {
        return new Node(id, "THE LIGHT", text, mood, effect, List.of(), null, ending);
    }

    private static Choice choice(String id, String label, String target, String... aliases) {
        return new Choice(id, label, List.of(aliases), target);
    }

    private static final Map<String, Node> NODES = buildGraph();

    public static Node node(String id) { return NODES.get(id); }

    private static Map<String, Node> buildGraph() {
        return Map.ofEntries(

            Map.entry(START_ID, story(START_ID, "THE LIGHT",
                "Hello, traveler. You've wandered farther than the living usually manage. "
                + "Two roads open beneath your feet — the path of salvation, and the path of damnation. "
                + "Choose. I haven't got all eternity.\n\n...actually, I do. But you don't.",
                Mood.NEUTRAL, Effect.NONE, List.of(
                    choice("salvation", "The Path of Salvation", "salvation_intro",
                        "salvation", "the path of salvation", "the path of salvation", "light", "1"),
                    choice("damnation", "The Path of Damnation", "damnation_intro",
                        "damnation", "the path of damnation", "dark", "2")))),

            // ---- SALVATION ARC ----
            Map.entry("salvation_intro", story("salvation_intro", "THE LIGHT",
                "You have chosen the path of salvation. Let the light guide you — warm, and gentle, "
                + "and utterly without mercy. Now, traveler: do you kneel... or do you swing?",
                Mood.SALVATION, Effect.BLOOM, List.of(
                    choice("fight", "FIGHT THE LIGHT", "salvation_fight_roll",
                        "fight the light", "fight", "swing", "1"),
                    choice("surrender", "Let the light take control...", "salvation_trial",
                        "let the light take control", "let the light take control...", "surrender", "kneel", "2")))),

            Map.entry("salvation_fight_roll", rollNode("salvation_fight_roll", Mood.SALVATION, List.of(
                new Outcome(70, "ending_eternal_damnation"),
                new Outcome(30, "ending_secret_equal")))),

            Map.entry("ending_eternal_damnation", ending("ending_eternal_damnation",
                "*You throw a punch at the light — and it misses so badly the wind of it barely stirs the dust.*\n\n"
                + "YOU DARE CHALLENGE ME?! Fine. Fall. Fall, and keep falling, and when you're certain you've hit "
                + "the bottom, discover there was never a bottom at all.\n\nI SENTENCE YOU TO ETERNAL DAMNATION.",
                Mood.DAMNATION, Effect.SHAKE,
                new Ending("ETERNAL DAMNATION", EndingType.BAD, 4))),

            Map.entry("ending_secret_equal", ending("ending_secret_equal",
                "*You throw a punch — and it lands. Square on the chin of God.*\n\n"
                + "...AHHH. IT BURNS. Damn you. *The Light staggers.* For one impossible second you are not beneath "
                + "it — you are level with it.\n\n"
                + "DUNGEON MASTER: ...dude. That was a 3% roll. I did not write a branch for this. You know what? "
                + "You earned it. Walk out the front door. Tell no one it was this easy.\n\n"
                + "You step past the Light into a morning that has no name yet. You are free — and you are the only "
                + "one who will ever know how.",
                Mood.SALVATION, Effect.RADIANCE,
                new Ending("THE EQUAL", EndingType.SECRET, 3))),

            Map.entry("salvation_trial", story("salvation_trial", "THE LIGHT",
                "*You let the light pour in.* Good. Obedient. It fills your lungs, your eyes, the hollow behind your "
                + "ribs. It asks one small thing before you ascend: your name.\n\n"
                + "Keep it — and remember who climbed. Or burn it away, and rise as something that never had to.",
                Mood.SALVATION, Effect.BLOOM, List.of(
                    choice("keep", "Keep my name", "ending_ascension_named", "keep my name", "keep", "name", "1"),
                    choice("burn", "Burn it away", "ending_ascension_hollow", "burn it away", "burn", "2")))),

            Map.entry("ending_ascension_named", ending("ending_ascension_named",
                "*You hold your name like a coal in the flood.* It hurts. You keep it anyway.\n\n"
                + "You ascend — a true warrior, blazing, and still, unmistakably, you. When the light asks the next "
                + "traveler to choose, it is your voice it borrows.\n\n"
                + "DUNGEON MASTER: the good ending. The rare kind of good — the kind you had to bleed a little to keep. "
                + "Well done, traveler.",
                Mood.SALVATION, Effect.RADIANCE,
                new Ending("ASCENSION", EndingType.TRUE, 1))),

            Map.entry("ending_ascension_hollow", ending("ending_ascension_hollow",
                "*You let the name go.* It costs nothing — which is exactly the problem.\n\n"
                + "You ascend: radiant, powerful, magnificent, and no one — not even you — remembers who you were "
                + "before. A perfect soldier for a war you'll never think to question.\n\n"
                + "DUNGEON MASTER: strong. Bright. Empty. The Light adores this one. You should probably be more "
                + "worried than you are.",
                Mood.SALVATION, Effect.BLOOM,
                new Ending("THE HOLLOW WARRIOR", EndingType.BITTERSWEET, 2))),

            // ---- DAMNATION ARC ----
            Map.entry("damnation_intro", story("damnation_intro", "THE LIGHT",
                "You have chosen the path of damnation. Die as if you had no right to live... unless you reeeaalllly "
                + "regret your initial choice.\n\nSo — go on. Last words. Make them count. Or don't.",
                Mood.DAMNATION, Effect.EMBERS, List.of(
                    choice("screw", "SCREW YOUUUUUUUU", "ending_defiance",
                        "screw you", "screw youuuuuuuu", "screw", "1"),
                    choice("forgive", "FORGIVE MEEEEE", "ending_drafted",
                        "forgive me", "forgive meeeee", "forgive", "2")))),

            Map.entry("ending_defiance", ending("ending_defiance",
                "*You scream SCREW YOU with everything you have left — and it comes out as a squeak. A little one. "
                + "Almost cute.*\n\n"
                + "...wow. That was it? You could have lived, you realize. Could have begged, been someone's warrior "
                + "by now. Instead you get this.\n\nAnyways. SMYTE.\n\n"
                + "DUNGEON MASTER: anticlimactic. Deeply. But — and I mean this — you went out on your own terms, "
                + "which is more than most of them manage. Respect.",
                Mood.DAMNATION, Effect.SHAKE,
                new Ending("SMYTE", EndingType.DEFIANT, 5))),

            Map.entry("ending_drafted", ending("ending_drafted",
                "*You have no spine, and you beg — beautifully, at length.*\n\n"
                + "...yeah. I thought so. Lucky for you, I'm short on warriors this century and long on cowards who'll "
                + "do anything to keep breathing. On your feet, conscript.\n\n"
                + "ASCEND — the cheap way. The borrowed way. The way you'll spend forever pretending you chose.\n\n"
                + "DUNGEON MASTER: you lived! ...sort of. Enjoy the eternity of small print.",
                Mood.DAMNATION, Effect.RADIANCE,
                new Ending("THE CONSCRIPT", EndingType.DARK, 6)))
        );
    }
}
