import { useEffect, useRef, useState } from "react";

// Reveals text one character at a time. Clicking anywhere (handled by the parent
// via the `skip` prop bump) or pressing the whole thing finishes it instantly, so
// impatient travelers never wait on the animation.

export default function Typewriter({
  text,
  speed = 18,
  skipSignal = 0,
  onDone,
}: {
  text: string;
  speed?: number;
  skipSignal?: number;
  onDone?: () => void;
}) {
  const [count, setCount] = useState(0);
  const doneRef = useRef(false);

  // Restart whenever the text changes.
  useEffect(() => {
    setCount(0);
    doneRef.current = false;
  }, [text]);

  useEffect(() => {
    if (count >= text.length) {
      if (!doneRef.current) {
        doneRef.current = true;
        onDone?.();
      }
      return;
    }
    const id = window.setTimeout(() => setCount((c) => c + 1), speed);
    return () => window.clearTimeout(id);
  }, [count, text, speed, onDone]);

  // Parent bumps skipSignal to reveal everything at once.
  useEffect(() => {
    if (skipSignal > 0) setCount(text.length);
  }, [skipSignal, text.length]);

  const shown = text.slice(0, count);
  const typing = count < text.length;

  return (
    <span className="typewriter">
      {shown.split("\n").map((line, i, arr) => (
        <span key={i}>
          {line}
          {i < arr.length - 1 && <br />}
        </span>
      ))}
      {typing && <span className="caret" aria-hidden="true">▍</span>}
    </span>
  );
}
