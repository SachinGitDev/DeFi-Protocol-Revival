import React, { useState, useEffect, useRef, useCallback } from 'react';

const CONTRACTS = ['ChickenRiceBox.sol', 'ChickenLoadedFries.sol', 'PiriPiriWings.sol', 'GarlicMayoDip.sol', 'Chips.sol'];

interface Zombie {
  id: number;
  x: number;
  top: number;
  name: string;
  duration: number;
  startTime: number;
}

export const Games = () => {
  const [score, setScore] = useState(0);
  const [lives, setLives] = useState(3);
  const [missed, setMissed] = useState(0);
  const [level, setLevel] = useState(1);
  const [gameState, setGameState] = useState<'start' | 'playing' | 'over'>('start');
  const [zombies, setZombies] = useState<Zombie[]>([]);
  const [flashes, setFlashes] = useState<{ id: number; x: number; top: number }[]>([]);
  const [explosions, setExplosions] = useState<{ id: number; x: number; y: number }[]>([]);
  const [levelBadge, setLevelBadge] = useState('Level 1 — Click the pepes!');

  const scoreRef = useRef(0);
  const livesRef = useRef(3);
  const levelRef = useRef(1);
  const gameRunningRef = useRef(false);
  const zombieIdRef = useRef(0);
  const spawnTimeoutRef = useRef<any>(null);
  const levelIntervalRef = useRef<any>(null);
  const animFrameRef = useRef<any>(null);
  const dvdAnimRef = useRef<any>(null);

  const endGame = useCallback(() => {
    gameRunningRef.current = false;
    clearTimeout(spawnTimeoutRef.current);
    clearInterval(levelIntervalRef.current);
    cancelAnimationFrame(animFrameRef.current);
    setZombies([]);
    setGameState('over');
  }, []);

  const explodeZombie = useCallback(
    (id: number, x: number, y: number) => {
      setZombies(prev => prev.filter(z => z.id !== id));
      const expId = Date.now() + Math.random();
      setExplosions(prev => [...prev, { id: expId, x, y }]);
      setTimeout(() => setExplosions(prev => prev.filter(e => e.id !== expId)), 600);

      livesRef.current -= 1;
      setLives(livesRef.current);
      setMissed(prev => prev + 1);

      if (livesRef.current <= 0) endGame();
    },
    [endGame],
  );

  const tick = useCallback(() => {
    if (!gameRunningRef.current) return;
    const now = Date.now();
    setZombies(prev => {
      const updated: Zombie[] = [];
      for (const z of prev) {
        const elapsed = now - z.startTime;
        const newTop = (elapsed / z.duration) * 520;
        if (newTop >= 520) {
          explodeZombie(z.id, z.x, 460);
        } else {
          updated.push({ ...z, top: newTop });
        }
      }
      return updated;
    });
    animFrameRef.current = requestAnimationFrame(tick);
  }, [explodeZombie]);

  const spawnZombie = useCallback(() => {
    if (!gameRunningRef.current) return;
    const name = CONTRACTS[Math.floor(Math.random() * CONTRACTS.length)];
    const duration = Math.max(2000, 5000 - levelRef.current * 400);
    const x = Math.random() * 85;
    const id = zombieIdRef.current++;
    setZombies(prev => [...prev, { id, x, top: -80, name, duration, startTime: Date.now() }]);

    const delay = Math.max(400, 1400 - levelRef.current * 120);
    spawnTimeoutRef.current = setTimeout(spawnZombie, delay);
  }, []);

  const startGame = useCallback(() => {
    scoreRef.current = 0;
    livesRef.current = 3;
    levelRef.current = 1;
    gameRunningRef.current = true;
    setScore(0);
    setLives(3);
    setMissed(0);
    setLevel(1);
    setZombies([]);
    setFlashes([]);
    setExplosions([]);
    setLevelBadge('Level 1 — Click the pepes!');
    setGameState('playing');

    spawnTimeoutRef.current = setTimeout(spawnZombie, 500);
    levelIntervalRef.current = setInterval(() => {
      levelRef.current += 1;
      setLevel(levelRef.current);
      setLevelBadge(`Level ${levelRef.current} — faster pepes!`);
    }, 15000);

    animFrameRef.current = requestAnimationFrame(tick);
  }, [spawnZombie, tick]);

  const resurrect = useCallback((id: number, x: number, y: number) => {
    setZombies(prev => prev.filter(z => z.id !== id));
    scoreRef.current += 1;
    setScore(scoreRef.current);

    const flashId = Date.now() + Math.random();
    setFlashes(prev => [...prev, { id: flashId, x, top: y }]);
    setTimeout(() => setFlashes(prev => prev.filter(f => f.id !== flashId)), 800);

    if (scoreRef.current % 10 === 0) {
      setLevelBadge(`${scoreRef.current} pepes saved!`);
    }
  }, []);

  useEffect(() => {
    return () => {
      clearTimeout(spawnTimeoutRef.current);
      clearInterval(levelIntervalRef.current);
      cancelAnimationFrame(animFrameRef.current);
    };
  }, []);

  // DVD bouncer effect
  useEffect(() => {
    const container = document.getElementById('dvd-container');
    const logo = document.getElementById('dvd-logo');
    const flash = document.getElementById('corner-flash');
    if (!container || !logo || !flash) return;

    let x = 10,
      y = 10,
      dx = 2,
      dy = 2;
    const size = 60;

    const colors = ['#ff6b00', '#ff0000', '#ffff00', '#00ff00', '#00ffff', '#ff00ff'];
    let colorIdx = 0;

    const step = () => {
      const maxX = container.offsetWidth - size;
      const maxY = container.offsetHeight - size;
      x += dx;
      y += dy;

      let hitCorner = false;
      if (x <= 0 || x >= maxX) {
        dx = -dx;
        x = Math.max(0, Math.min(x, maxX));
        colorIdx = (colorIdx + 1) % colors.length;
        logo.style.color = colors[colorIdx];
        if ((x <= 2 || x >= maxX - 2) && (y <= 2 || y >= maxY - 2)) hitCorner = true;
      }
      if (y <= 0 || y >= maxY) {
        dy = -dy;
        y = Math.max(0, Math.min(y, maxY));
        colorIdx = (colorIdx + 1) % colors.length;
        logo.style.color = colors[colorIdx];
        if ((x <= 2 || x >= maxX - 2) && (y <= 2 || y >= maxY - 2)) hitCorner = true;
      }

      logo.style.left = x + 'px';
      logo.style.top = y + 'px';

      if (hitCorner) {
        flash.style.opacity = '1';
        setTimeout(() => {
          flash.style.opacity = '0';
        }, 1500);
      }

      dvdAnimRef.current = requestAnimationFrame(step);
    };

    dvdAnimRef.current = requestAnimationFrame(step);
    return () => cancelAnimationFrame(dvdAnimRef.current);
  }, []);

  return (
    <div style={{ maxWidth: '700px', margin: '0 auto', padding: '20px' }}>
      <h2 style={{ textAlign: 'center', color: '#ff6b00', marginBottom: '16px' }}>Pepe&apos;s Contract Clicker</h2>

      {/* HUD */}
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          padding: '12px 16px',
          background: 'rgba(255,255,255,0.1)',
          borderRadius: '12px',
          marginBottom: '8px',
        }}
      >
        <div style={{ textAlign: 'center' }}>
          <div style={{ fontSize: '11px', color: '#aaa' }}>Resurrected</div>
          <div style={{ fontSize: '20px', fontWeight: 500, color: '#ff6b00' }}>{score}</div>
        </div>
        <div style={{ textAlign: 'center' }}>
          <div style={{ fontSize: '11px', color: '#aaa' }}>Lives</div>
          <div style={{ fontSize: '18px' }}>
            {[0, 1, 2].map(i => (
              <span key={i} style={{ color: i < lives ? '#f44336' : '#555' }}>
                {i < lives ? '♥' : '♡'}
              </span>
            ))}
          </div>
        </div>
        <div style={{ textAlign: 'center' }}>
          <div style={{ fontSize: '11px', color: '#aaa' }}>Level</div>
          <div style={{ fontSize: '20px', fontWeight: 500, color: '#ffc107' }}>{level}</div>
        </div>
        <div style={{ textAlign: 'center' }}>
          <div style={{ fontSize: '11px', color: '#aaa' }}>Missed</div>
          <div style={{ fontSize: '20px', fontWeight: 500, color: '#f44336' }}>{missed}</div>
        </div>
      </div>

      {/* Game Area */}
      <div
        style={{
          position: 'relative',
          width: '100%',
          height: '500px',
          background: 'linear-gradient(180deg, #1a0a00 0%, #2e0f00 100%)',
          borderRadius: '12px',
          overflow: 'hidden',
          cursor: 'crosshair',
          userSelect: 'none',
        }}
      >
        {/* Level Badge */}
        <div
          style={{
            position: 'absolute',
            top: '12px',
            left: '50%',
            transform: 'translateX(-50%)',
            background: 'rgba(0,0,0,0.5)',
            color: '#ffc107',
            fontSize: '13px',
            padding: '4px 12px',
            borderRadius: '20px',
            pointerEvents: 'none',
            zIndex: 10,
          }}
        >
          {levelBadge}
        </div>

        {/* Zombies */}
        {zombies.map(z => (
          <div
            key={z.id}
            onClick={() => resurrect(z.id, z.x, z.top)}
            style={{
              position: 'absolute',
              left: `${z.x}%`,
              top: `${z.top}px`,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              cursor: 'pointer',
              zIndex: 5,
            }}
          >
            <img src="content/images/pepes-logo.png" alt="pepes" style={{ width: '40px', height: '40px', objectFit: 'contain' }} />
            <span
              style={{
                fontSize: '9px',
                color: '#7fff7f',
                background: 'rgba(0,0,0,0.6)',
                padding: '2px 4px',
                borderRadius: '4px',
                whiteSpace: 'nowrap',
                marginTop: '2px',
              }}
            >
              {z.name}
            </span>
            <div style={{ width: '40px', height: '4px', background: '#333', borderRadius: '2px', marginTop: '2px' }}>
              <div
                style={{
                  height: '100%',
                  borderRadius: '2px',
                  background: z.top < 200 ? '#4caf50' : z.top < 380 ? '#ffc107' : '#f44336',
                  width: `${Math.max(0, 100 - (z.top / 520) * 100)}%`,
                  transition: 'width 0.1s',
                }}
              />
            </div>
          </div>
        ))}

        {/* Resurrect flashes */}
        {flashes.map(f => (
          <div
            key={f.id}
            style={{
              position: 'absolute',
              left: `${f.x}%`,
              top: `${f.top}px`,
              color: '#7fff7f',
              fontSize: '14px',
              fontWeight: 500,
              pointerEvents: 'none',
              animation: 'floatUp 0.8s forwards',
              zIndex: 10,
            }}
          >
            +1 Served! 🍗
          </div>
        ))}

        {/* Explosions */}
        {explosions.map(e => (
          <div
            key={e.id}
            style={{
              position: 'absolute',
              left: `${e.x}%`,
              top: `${e.y}px`,
              fontSize: '48px',
              pointerEvents: 'none',
              animation: 'explode 0.6s forwards',
              zIndex: 10,
            }}
          >
            💥
          </div>
        ))}

        {/* Start Screen */}
        {gameState === 'start' && (
          <div
            style={{
              position: 'absolute',
              inset: 0,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              background: 'rgba(10,10,30,0.92)',
              borderRadius: '12px',
              gap: '16px',
            }}
          >
            <h2 style={{ color: '#ff6b00', fontSize: '22px', fontWeight: 500, margin: 0 }}>Pepes Contract Clicker</h2>
            <p style={{ color: '#aaa', fontSize: '14px', margin: 0, textAlign: 'center', maxWidth: '300px' }}>
              Pepes are counting on you! Save the pepes from the greedy decentralized finance contracts by clicking them before they hit the
              ground and explode!
            </p>
            <button
              onClick={startGame}
              style={{
                background: '#8b1a00',
                color: '#ff6b00',
                border: '1px solid #ff6b00',
                padding: '10px 32px',
                borderRadius: '8px',
                fontSize: '15px',
                cursor: 'pointer',
              }}
            >
              Start Game
            </button>
          </div>
        )}

        {/* Game Over Screen */}
        {gameState === 'over' && (
          <div
            style={{
              position: 'absolute',
              inset: 0,
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              justifyContent: 'center',
              background: 'rgba(10,10,30,0.92)',
              borderRadius: '12px',
              gap: '16px',
            }}
          >
            <h2 style={{ color: '#f44336', fontSize: '22px', fontWeight: 500, margin: 0 }}>Game Over!</h2>
            <p style={{ color: '#aaa', fontSize: '14px', margin: 0, textAlign: 'center', maxWidth: '300px' }}>
              You resurrected {score} contracts across {level} levels. {missed} contracts exploded!
            </p>
            <button
              onClick={startGame}
              style={{
                background: '#2d6a2d',
                color: '#7fff7f',
                border: '1px solid #4caf50',
                padding: '10px 32px',
                borderRadius: '8px',
                fontSize: '15px',
                cursor: 'pointer',
              }}
            >
              Play Again
            </button>
          </div>
        )}
      </div>

      {/* DVD BOUNCER */}
      <div style={{ marginTop: '48px' }}>
        <h2 style={{ textAlign: 'center', color: '#ff6b00', marginBottom: '4px' }}>🍗 Bouncing Pepe&apos;s Logo</h2>
        <p style={{ textAlign: 'center', fontSize: '13px', color: '#aaa', marginBottom: '12px' }}>Wait for it to hit the corner...</p>
        <div
          id="dvd-container"
          style={{
            position: 'relative',
            width: '100%',
            height: '300px',
            background: '#000',
            borderRadius: '12px',
            overflow: 'hidden',
            border: '1px solid #ff6b00',
          }}
        >
          <div id="dvd-logo" style={{ position: 'absolute', userSelect: 'none' }}>
            <img src="content/images/pepes-logo.png" alt="pepes" style={{ width: '60px', height: '60px', objectFit: 'contain' }} />
          </div>
          <div
            id="corner-flash"
            style={{
              position: 'absolute',
              inset: 0,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              opacity: 0,
              pointerEvents: 'none',
              fontSize: '32px',
              color: '#ff6b00',
              fontWeight: 500,
              transition: 'opacity 0.2s',
              background: 'rgba(0,0,0,0.5)',
            }}
          >
            IT HIT THE CORNER!! 🎉🍗
          </div>
        </div>
      </div>

      <style>{`
        @keyframes floatUp { 0% { transform: translateY(0); opacity: 1; } 100% { transform: translateY(-60px); opacity: 0; } }
        @keyframes explode { 0% { transform: scale(0.5); opacity: 1; } 100% { transform: scale(2); opacity: 0; } }
      `}</style>
    </div>
  );
};

export default Games;
