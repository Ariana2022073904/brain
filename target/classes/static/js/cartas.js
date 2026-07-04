// ============================================================
//  Memoria Cartas – Brain Training Pro
// ============================================================

const SIMBOLOS = [
    '🧠','👁️','💡','🎯','⚡','🔍','🧩','📖',
    '⌛','🏆','⭐','🎲','💎','🧬','🔬','📊',
    '📝','💻','📚','🎓','🚀','🌟','🔥','🧮',
    '🧠','👑','🎮','🎨','📌','🎵','🪙','🎖️'
];

let volteo = [];
let pares  = 0;
let totalPares = 12;
let intentos = 0;
let bloqueado = false;
let timerInterval = null;
let tiempoInicio = 0;
let cols = 6;

function iniciarJuego() {
    const dif = document.getElementById('dificultad').value;
    totalPares = dif === 'Fácil' ? 8 : dif === 'Medio' ? 12 : 16;
    cols = dif === 'Fácil' ? 4 : dif === 'Medio' ? 6 : 8;

    pares = 0; intentos = 0; volteo = []; bloqueado = false;

    document.getElementById('lblIntentos').textContent = '0';
    document.getElementById('lblPares').textContent    = `0 / ${totalPares}`;
    document.getElementById('lblTiempo').textContent   = '0s';
    document.getElementById('resultModal').classList.add('hidden');

    clearInterval(timerInterval);
    tiempoInicio = Date.now();
    timerInterval = setInterval(() => {
        const seg = Math.floor((Date.now() - tiempoInicio) / 1000);
        document.getElementById('lblTiempo').textContent = seg + 's';
    }, 500);

    // Build deck
    const simbolos = SIMBOLOS.slice(0, totalPares);
    let deck = [...simbolos, ...simbolos];
    deck = deck.sort(() => Math.random() - 0.5);

    const board = document.getElementById('tableroCartas');
    board.style.gridTemplateColumns = `repeat(${cols}, 80px)`;
    board.innerHTML = '';

    deck.forEach((simbolo, idx) => {
        const carta = document.createElement('div');
        carta.className = 'carta';
        carta.dataset.simbolo = simbolo;
        carta.dataset.idx   = idx;
        carta.innerHTML = `
            <div class="carta-inner">
                <div class="carta-frente">${simbolo}</div>
                <div class="carta-reverso">BT</div>
            </div>`;
        carta.addEventListener('click', () => clickCarta(carta));
        board.appendChild(carta);
    });
}

function clickCarta(carta) {
    if (bloqueado) return;
    if (carta.classList.contains('volteada') || carta.classList.contains('emparejada')) return;

    carta.classList.add('volteada');
    volteo.push(carta);

    if (volteo.length === 2) {
        bloqueado = true;
        intentos++;
        document.getElementById('lblIntentos').textContent = intentos;

        const [a, b] = volteo;
        if (a.dataset.simbolo === b.dataset.simbolo) {
            a.classList.add('emparejada');
            b.classList.add('emparejada');
            pares++;
            document.getElementById('lblPares').textContent = `${pares} / ${totalPares}`;
            volteo = [];
            bloqueado = false;

            if (pares === totalPares) ganar();
        } else {
            setTimeout(() => {
                a.classList.remove('volteada');
                b.classList.remove('volteada');
                volteo = [];
                bloqueado = false;
            }, 900);
        }
    }
}

function ganar() {
    clearInterval(timerInterval);
    const seg = Math.floor((Date.now() - tiempoInicio) / 1000);
    const dif = document.getElementById('dificultad').value;

    fetch('/juegos/guardar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ juego: 'Cartas', dificultad: dif, puntaje: intentos, tiempo: seg })
    }).then(r => r.json()).then(data => {
        const esRecord = data.resultado === 'nuevo_record' || data.resultado === 'primer_record';
        document.getElementById('modalMsg').textContent =
            `Intentos: ${intentos} | Tiempo: ${seg}s` + (esRecord ? ' | Nuevo récord' : '');
        document.getElementById('resultModal').classList.remove('hidden');
    }).catch(() => {
        document.getElementById('modalMsg').textContent = `Intentos: ${intentos} | Tiempo: ${seg}s`;
        document.getElementById('resultModal').classList.remove('hidden');
    });
}

// Auto-start
iniciarJuego();
