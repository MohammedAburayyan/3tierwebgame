// Cinematic effects: in-browser synthesized sound (no audio files) + a tiny
// haptic-ish screen shake. Everything is generated with the Web Audio API so the
// repo stays asset-free and the whole thing works offline.

import type { Effect, Mood } from "./api";

let ctx: AudioContext | null = null;
let muted = localStorage.getItem("light.muted") === "true";

export function isMuted(): boolean {
  return muted;
}

export function setMuted(value: boolean): void {
  muted = value;
  localStorage.setItem("light.muted", String(value));
}

// Audio must be created/resumed inside a user gesture. Every transition in this
// game is a click, so lazily spinning the context up on first use is safe.
function audio(): AudioContext | null {
  if (muted) return null;
  if (!ctx) {
    const Ctor = window.AudioContext ?? (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
    if (!Ctor) return null;
    ctx = new Ctor();
  }
  if (ctx.state === "suspended") void ctx.resume();
  return ctx;
}

interface ToneOpts {
  freq: number;
  type?: OscillatorType;
  duration: number;
  gain?: number;
  glideTo?: number;
  delay?: number;
}

function tone({ freq, type = "sine", duration, gain = 0.2, glideTo, delay = 0 }: ToneOpts): void {
  const ac = audio();
  if (!ac) return;
  const start = ac.currentTime + delay;

  const osc = ac.createOscillator();
  const amp = ac.createGain();
  osc.type = type;
  osc.frequency.setValueAtTime(freq, start);
  if (glideTo) osc.frequency.exponentialRampToValueAtTime(glideTo, start + duration);

  amp.gain.setValueAtTime(0.0001, start);
  amp.gain.exponentialRampToValueAtTime(gain, start + 0.02);
  amp.gain.exponentialRampToValueAtTime(0.0001, start + duration);

  osc.connect(amp).connect(ac.destination);
  osc.start(start);
  osc.stop(start + duration + 0.05);
}

/** A bright ascending major chord — the sound of the light. */
function chime(): void {
  [523.25, 659.25, 783.99, 1046.5].forEach((f, i) =>
    tone({ freq: f, type: "triangle", duration: 1.4, gain: 0.14, delay: i * 0.06 }),
  );
}

/** A low, dissonant impact + downward slide — the sound of falling. */
function damnationHit(): void {
  tone({ freq: 140, type: "sawtooth", duration: 0.9, gain: 0.22, glideTo: 46 });
  tone({ freq: 92, type: "square", duration: 0.7, gain: 0.14, glideTo: 40 });
}

/** A sharp percussive stab for smites / crashes. */
function impact(): void {
  tone({ freq: 220, type: "square", duration: 0.28, gain: 0.28, glideTo: 60 });
  tone({ freq: 60, type: "sawtooth", duration: 0.5, gain: 0.2 });
}

/** A soft click for choice hovers/selection. */
export function click(): void {
  tone({ freq: 660, type: "sine", duration: 0.08, gain: 0.08 });
}

/** Trigger a brief screen shake by toggling a class on <body>. */
function shakeScreen(intense = false): void {
  const cls = intense ? "fx-shake-hard" : "fx-shake";
  document.body.classList.remove("fx-shake", "fx-shake-hard");
  // Force reflow so re-adding the class restarts the animation.
  void document.body.offsetWidth;
  document.body.classList.add(cls);
  window.setTimeout(() => document.body.classList.remove(cls), 650);
}

// Signals a full-viewport flash overlay; App listens for this and renders it.
function flash(kind: "bloom" | "ember"): void {
  window.dispatchEvent(new CustomEvent("light-flash", { detail: kind }));
}

/** Play the sound + motion for a node's declared effect, biased by its mood. */
export function playEffect(effect: Effect, mood: Mood): void {
  switch (effect) {
    case "BLOOM":
      flash("bloom");
      chime();
      break;
    case "RADIANCE":
      flash("bloom");
      chime();
      window.setTimeout(chime, 260);
      break;
    case "EMBERS":
      flash("ember");
      damnationHit();
      break;
    case "SHAKE":
      shakeScreen(true);
      flash("ember");
      impact();
      break;
    case "NONE":
    default:
      if (mood === "DAMNATION") damnationHit();
      break;
  }
}
