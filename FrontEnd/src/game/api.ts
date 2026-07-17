// Thin client for the Spring game API. The server is authoritative for the story;
// the frontend only reports which choice the traveler made and renders what comes back.

const BASE = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";

export type Mood = "NEUTRAL" | "SALVATION" | "DAMNATION";
export type Effect = "NONE" | "BLOOM" | "EMBERS" | "SHAKE" | "RADIANCE";

export interface ChoiceView {
  id: string;
  label: string;
}

export interface EndingView {
  title: string;
  type: "TRUE" | "SECRET" | "BITTERSWEET" | "BAD" | "DEFIANT" | "DARK";
  number: number;
  total: number;
}

export interface GameView {
  sessionId: string;
  nodeId: string;
  speaker: string;
  text: string;
  mood: Mood;
  effect: Effect;
  choices: ChoiceView[];
  ending: EndingView | null;
  /** Present only when a typed answer wasn't understood — the DM's snark. Story does not advance. */
  snark: string | null;
}

async function post<T>(path: string, body?: unknown): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) {
    throw new Error(`Request to ${path} failed: ${res.status}`);
  }
  return res.json() as Promise<T>;
}

export function startGame(): Promise<GameView> {
  return post<GameView>("/api/game/start");
}

export function choose(sessionId: string, input: string): Promise<GameView> {
  return post<GameView>("/api/game/choose", { sessionId, input });
}
