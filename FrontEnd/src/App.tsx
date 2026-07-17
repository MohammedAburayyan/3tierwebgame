import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import "./App.css";
import Background from "./components/Background";
import Typewriter from "./components/Typewriter";
import { choose as apiChoose, startGame, type GameView, type Mood } from "./game/api";
import { click, isMuted, playEffect, setMuted } from "./game/effects";

type Phase = "title" | "story";

const ENDINGS_KEY = "light.endings";

function loadDiscovered(): number[] {
  try {
    const raw = localStorage.getItem(ENDINGS_KEY);
    return raw ? (JSON.parse(raw) as number[]) : [];
  } catch {
    return [];
  }
}

function saveDiscovered(nums: number[]): void {
  localStorage.setItem(ENDINGS_KEY, JSON.stringify([...new Set(nums)].sort((a, b) => a - b)));
}

export default function App() {
  const [phase, setPhase] = useState<Phase>("title");
  const [view, setView] = useState<GameView | null>(null);
  const [busy, setBusy] = useState(false);
  const [typed, setTyped] = useState("");
  const [skip, setSkip] = useState(0);
  const [textDone, setTextDone] = useState(false);
  const [snark, setSnark] = useState<string | null>(null);
  const [inputShake, setInputShake] = useState(0);
  const [flash, setFlash] = useState<{ kind: "bloom" | "ember"; key: number } | null>(null);
  const [muted, setMutedState] = useState(isMuted());
  const [discovered, setDiscovered] = useState<number[]>(loadDiscovered());
  const [error, setError] = useState<string | null>(null);

  const mood: Mood = view?.mood ?? "NEUTRAL";
  const flashKey = useRef(0);

  // Bridge the effects module's flash events into React state so we can render the overlay.
  useEffect(() => {
    const onFlash = (e: Event) => {
      const kind = (e as CustomEvent).detail as "bloom" | "ember";
      flashKey.current += 1;
      setFlash({ kind, key: flashKey.current });
    };
    window.addEventListener("light-flash", onFlash);
    return () => window.removeEventListener("light-flash", onFlash);
  }, []);

  // Record a newly reached ending for the collectible counter.
  useEffect(() => {
    const num = view?.ending?.number;
    if (num && !discovered.includes(num)) {
      const next = [...discovered, num];
      setDiscovered(next);
      saveDiscovered(next);
    }
  }, [view?.ending?.number, discovered]);

  const applyView = useCallback((next: GameView) => {
    if (next.snark) {
      // Story didn't advance — the DM is unimpressed. Nudge the input, keep the node.
      setSnark(next.snark);
      setInputShake((n) => n + 1);
      return;
    }
    setSnark(null);
    setTyped("");
    setTextDone(false);
    setView(next);
    playEffect(next.effect, next.mood);
  }, []);

  const begin = useCallback(async () => {
    setBusy(true);
    setError(null);
    click();
    try {
      const v = await startGame();
      setPhase("story");
      applyView(v);
    } catch {
      setError("The Light is unreachable. Start the Spring backend on :8080, then try again.");
    } finally {
      setBusy(false);
    }
  }, [applyView]);

  const submit = useCallback(
    async (input: string) => {
      if (!view || busy) return;
      setBusy(true);
      setError(null);
      click();
      try {
        const v = await apiChoose(view.sessionId, input);
        applyView(v);
      } catch {
        setError("Lost contact with the Light. Is the backend still running?");
      } finally {
        setBusy(false);
      }
    },
    [view, busy, applyView],
  );

  const onTypedSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const val = typed.trim();
    if (val) void submit(val);
  };

  const restart = useCallback(() => {
    click();
    setView(null);
    setPhase("title");
    setSnark(null);
    setTyped("");
  }, []);

  const toggleMute = () => {
    const next = !muted;
    setMuted(next);
    setMutedState(next);
    if (!next) click();
  };

  const discoveredCount = discovered.length;
  const total = view?.ending?.total ?? 6;

  const rootClass = useMemo(
    () => `stage mood-${mood.toLowerCase()}${view?.ending ? " is-ending" : ""}`,
    [mood, view?.ending],
  );

  return (
    <div className={rootClass}>
      <Background mood={mood} />
      <div className="vignette" aria-hidden="true" />
      {flash && (
        <div key={flash.key} className={`flash flash-${flash.kind}`} aria-hidden="true" />
      )}

      <button className="mute-btn" onClick={toggleMute} aria-label={muted ? "Unmute" : "Mute"}>
        {muted ? "🔇" : "🔊"}
      </button>

      {phase === "title" && (
        <TitleScreen onBegin={begin} busy={busy} discovered={discoveredCount} error={error} />
      )}

      {phase === "story" && view && (
        <main className="panel" onClick={() => !textDone && setSkip((s) => s + 1)}>
          <div className="speaker">{view.speaker}</div>

          <p className="story-text">
            <Typewriter text={view.text} skipSignal={skip} onDone={() => setTextDone(true)} />
          </p>

          {view.ending ? (
            <EndingBlock
              title={view.ending.title}
              type={view.ending.type}
              number={view.ending.number}
              total={total}
              discovered={discoveredCount}
              onRestart={restart}
              visible={textDone}
            />
          ) : (
            <div className={`choices ${textDone ? "" : "choices-hidden"}`}>
              {view.choices.map((c) => (
                <button
                  key={c.id}
                  className="choice"
                  disabled={busy}
                  onClick={(e) => {
                    e.stopPropagation();
                    void submit(c.id);
                  }}
                >
                  <span className="choice-label">{c.label}</span>
                </button>
              ))}

              <form className="type-row" onSubmit={onTypedSubmit} onClick={(e) => e.stopPropagation()}>
                <input
                  key={inputShake}
                  className={`type-input ${inputShake ? "shake" : ""}`}
                  value={typed}
                  onChange={(e) => setTyped(e.target.value)}
                  placeholder="…or speak to the Light"
                  disabled={busy}
                  aria-label="Type your answer"
                />
              </form>

              {snark && <div className="snark">{snark}</div>}
            </div>
          )}

          {error && <div className="error">{error}</div>}
        </main>
      )}
    </div>
  );
}

function TitleScreen({
  onBegin,
  busy,
  discovered,
  error,
}: {
  onBegin: () => void;
  busy: boolean;
  discovered: number;
  error: string | null;
}) {
  return (
    <main className="title">
      <div className="halo" aria-hidden="true" />
      <h1 className="title-word">THE LIGHT</h1>
      <p className="title-sub">salvation · damnation · one traveler</p>
      <button className="begin-btn" onClick={onBegin} disabled={busy}>
        {busy ? "…" : "Approach the Light"}
      </button>
      <div className="collectible">Endings discovered · {discovered} / 6</div>
      {error && <div className="error">{error}</div>}
    </main>
  );
}

function EndingBlock({
  title,
  type,
  number,
  total,
  discovered,
  onRestart,
  visible,
}: {
  title: string;
  type: string;
  number: number;
  total: number;
  discovered: number;
  onRestart: () => void;
  visible: boolean;
}) {
  return (
    <div className={`ending ${visible ? "ending-in" : "ending-hidden"}`}>
      <div className={`ending-badge type-${type.toLowerCase()}`}>{type} ENDING</div>
      <div className="ending-title">{title}</div>
      <div className="ending-meta">
        Ending {number} of {total} · you've found {discovered} / {total}
      </div>
      <button className="begin-btn" onClick={onRestart}>
        Walk the path again
      </button>
    </div>
  );
}
