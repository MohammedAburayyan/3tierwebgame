import { useEffect, useRef } from "react";
import type { Mood } from "../game/api";

// A full-viewport canvas that renders drifting particles whose colour and motion
// follow the current mood: cosmic dust when neutral, gold motes rising toward the
// light on salvation, embers falling into the dark on damnation.

interface Particle {
  x: number;
  y: number;
  vx: number;
  vy: number;
  r: number;
  life: number;
  maxLife: number;
}

const palette: Record<Mood, string> = {
  NEUTRAL: "180, 170, 210",
  SALVATION: "255, 214, 120",
  DAMNATION: "255, 110, 60",
};

export default function Background({ mood }: { mood: Mood }) {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const moodRef = useRef<Mood>(mood);
  moodRef.current = mood;

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx: CanvasRenderingContext2D | null = canvas.getContext("2d");
    if (!ctx) return;
    // Bind a non-null const so narrowing survives inside the animation closure.
    const g: CanvasRenderingContext2D = ctx;

    let width = (canvas.width = window.innerWidth);
    let height = (canvas.height = window.innerHeight);
    const onResize = () => {
      width = canvas.width = window.innerWidth;
      height = canvas.height = window.innerHeight;
    };
    window.addEventListener("resize", onResize);

    const particles: Particle[] = [];
    const MAX = 90;

    function spawn(): Particle {
      const m = moodRef.current;
      if (m === "SALVATION") {
        return {
          x: Math.random() * width,
          y: height + 10,
          vx: (Math.random() - 0.5) * 0.3,
          vy: -(0.4 + Math.random() * 0.9),
          r: 1 + Math.random() * 2.5,
          life: 0,
          maxLife: 220 + Math.random() * 160,
        };
      }
      if (m === "DAMNATION") {
        return {
          x: Math.random() * width,
          y: -10,
          vx: (Math.random() - 0.5) * 0.5,
          vy: 0.5 + Math.random() * 1.2,
          r: 1 + Math.random() * 2.8,
          life: 0,
          maxLife: 200 + Math.random() * 140,
        };
      }
      return {
        x: Math.random() * width,
        y: Math.random() * height,
        vx: (Math.random() - 0.5) * 0.15,
        vy: (Math.random() - 0.5) * 0.15,
        r: 0.6 + Math.random() * 1.6,
        life: 0,
        maxLife: 300 + Math.random() * 200,
      };
    }

    let raf = 0;
    function frame() {
      g.clearRect(0, 0, width, height);
      const rgb = palette[moodRef.current];

      while (particles.length < MAX) particles.push(spawn());

      for (let i = particles.length - 1; i >= 0; i--) {
        const p = particles[i];
        p.x += p.vx;
        p.y += p.vy;
        p.life++;

        const t = p.life / p.maxLife;
        const alpha = Math.sin(Math.min(t, 1) * Math.PI) * 0.9;

        g.beginPath();
        const glow = g.createRadialGradient(p.x, p.y, 0, p.x, p.y, p.r * 4);
        glow.addColorStop(0, `rgba(${rgb}, ${alpha})`);
        glow.addColorStop(1, `rgba(${rgb}, 0)`);
        g.fillStyle = glow;
        g.arc(p.x, p.y, p.r * 4, 0, Math.PI * 2);
        g.fill();

        if (p.life >= p.maxLife || p.y < -20 || p.y > height + 20) {
          particles[i] = spawn();
        }
      }
      raf = requestAnimationFrame(frame);
    }
    frame();

    return () => {
      cancelAnimationFrame(raf);
      window.removeEventListener("resize", onResize);
    };
  }, []);

  return <canvas ref={canvasRef} className="bg-canvas" aria-hidden="true" />;
}
