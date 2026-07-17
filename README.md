# ✦ THE LIGHT 🔥

A cinematic, choice-driven text adventure. You are a traveler at the threshold, and
a sardonic, fourth-wall-breaking Dungeon Master — *the Light* — makes you choose
between **salvation** and **damnation**. Every path shifts the whole screen between a
radiant, gold divine mood and an ember-red infernal one, with sound and effects to match.

Built as a **3-tier web app**: a React frontend, a Spring Boot API that owns the story,
and an optional Postgres tier.

---

## Play

- **Click** a choice card, **or type** your answer in the *"…or speak to the Light"* box
  (the story accepts the phrase, `1`/`2`, or a keyword). Type gibberish and the DM will
  let you know what it thinks of your grasp of its language.
- Hunt all **6 endings** — the counter in the corner tracks how many you've found.
  One is secret: **FIGHT THE LIGHT** on the salvation path has a 30% lucky roll.
- Toggle sound with the 🔊 button (top-right).

| # | Ending | How you get there |
|---|--------|-------------------|
| 1 | **Ascension** *(true)* | Salvation → surrender → keep your name |
| 2 | **The Hollow Warrior** *(bittersweet)* | Salvation → surrender → burn your name |
| 3 | **The Equal** *(secret)* | Salvation → fight the light → win the 30% roll |
| 4 | **Eternal Damnation** *(bad)* | Salvation → fight the light → lose the roll |
| 5 | **Smyte** *(defiant)* | Damnation → SCREW YOU |
| 6 | **The Conscript** *(dark)* | Damnation → FORGIVE ME |

---

## Architecture

```
FrontEnd/                 Tier 1 — React + Vite + TypeScript
  src/App.tsx               orchestrator (title / story / ending screens)
  src/components/           Background particles, typewriter
  src/game/api.ts           calls the Spring API
  src/game/effects.ts       Web Audio synth + flash / shake effects

springApi_game/           Tier 2 — Spring Boot (Java 17)
  .../game/Story.java       the whole narrative as an immutable graph
  .../game/GameEngine.java  per-session state + navigation
  .../game/GameController    POST /api/game/start, POST /api/game/choose
  .../Test*.java            Tier 3 demo (Postgres via JPA) — opt-in

Postgres                  Tier 3 — optional, behind the `db` profile
```

The **server is authoritative** for the story: the frontend only reports which choice
the traveler made and renders whatever the API returns. Each browser gets its own
`sessionId`, so any number of people can play at once.

---

## Run

**No database required** — the game engine is fully in-memory.

```bash
# Tier 2 — API on :8080
cd springApi_game
./mvnw spring-boot:run

# Tier 1 — dev server on :5173
cd FrontEnd
npm install
npm run dev
```

Open http://localhost:5173.

### Optional: the Postgres tier

The `Test` entity / `/api/tests` endpoint demonstrate the data tier. It's off by
default; enable it with the `db` profile (expects Postgres on `localhost:5432`):

```bash
cd springApi_game
./mvnw spring-boot:run -Dspring-boot.run.profiles=db
```

---

## API

| Method | Path | Body | Returns |
|--------|------|------|---------|
| `POST` | `/api/game/start` | — | new `sessionId` + opening node |
| `POST` | `/api/game/choose` | `{ "sessionId": "...", "input": "salvation" }` | the next node |
| `GET` | `/api/greeting` | — | a plain greeting (liveness) |
| `GET` | `/api/tests` | — | *(db profile only)* rows from Postgres |

A node includes its `mood` (`NEUTRAL` / `SALVATION` / `DAMNATION`) and an `effect`
hint the frontend plays on arrival. Unrecognized `input` doesn't advance the story —
it comes back with a `snark` line instead.
