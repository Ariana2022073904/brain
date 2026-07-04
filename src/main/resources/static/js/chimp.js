// ============================================================
//  Chimp Test Progresivo – Brain Training Pro
//  Empieza en 4 números, sube 1 por cada acierto (infinito)
//  El tiempo de visualización baja con cada nivel
// ============================================================

let nivel     = 4;   // cantidad actual de números
let mejor     = 0;   // mejor nivel alcanzado
let siguiente = 1;   // próximo número a tocar
let faseVer   = false;
let mapa      = {};  // num -> {r, c}
let bloqueado = false;

// Grid crece con el nivel
function gridSize() {
    if (nivel <= 4)  return { filas: 3, cols: 4 };
    if (nivel <= 6)  return { filas: 3, cols: 5 };
    if (nivel <= 9)  return { filas: 4, cols: 5 };
    if (nivel <= 12) return { filas: 4, cols: 6 };
    return              { filas: 5, cols: 7 };
}

// Tiempo de visualización baja con nivel (mín 600ms)
function tiempoVer() {
    return Math.max(600, 3000 - (nivel - 4) * 120);
}

function resetJuego() {
    nivel   = 4;
    mejor   = 0;
    document.getElementById('resultModal').classList.add('hidden');
    document.getElementById('lblMejor').textContent = '-';
    document.getElementById('lblNivel').textContent = nivel;
    document.getElementById('lblEstado').textContent = 'Listo';
    document.getElementById('startWrap').style.display = 'flex';
    buildBoard();
}

function iniciarRonda() {
    document.getElementById('startWrap').style.display = 'none';
    document.getElementById('resultModal').classList.add('hidden');
    siguiente = 1;
    faseVer   = true;
    bloqueado = false;
    mapa      = {};

    document.getElementById('lblNivel').textContent  = nivel;
    document.getElementById('lblEstado').textContent = 'Memoriza...';

    buildBoard();
    colocarNumeros();
}

function buildBoard() {
    const { filas, cols } = gridSize();
    const board = document.getElementById('tablero');
    board.style.gridTemplateColumns = `repeat(${cols}, 1fr)`;
    board.innerHTML = '';

    for (let r = 0; r < filas; r++) {
        for (let c = 0; c < cols; c++) {
            const cel = document.createElement('div');
            cel.className = 'chimp-cell';
            cel.dataset.r = r;
            cel.dataset.c = c;
            cel.addEventListener('click', () => clickCelda(r, c));
            board.appendChild(cel);
        }
    }
}

function getCell(r, c) {
    return document.querySelector(`.chimp-cell[data-r='${r}'][data-c='${c}']`);
}

function colocarNumeros() {
    const { filas, cols } = gridSize();
    const usados = new Set();

    for (let n = 1; n <= nivel; n++) {
        let r, c, key;
        do {
            r   = Math.floor(Math.random() * filas);
            c   = Math.floor(Math.random() * cols);
            key = `${r},${c}`;
        } while (usados.has(key));

        usados.add(key);
        mapa[n] = { r, c };
        const cel = getCell(r, c);
        cel.textContent = n;
        cel.classList.add('visible-num');
    }

    // Ocultar después del tiempo de visualización
    setTimeout(() => {
        for (let n = 1; n <= nivel; n++) {
            const { r, c } = mapa[n];
            const cel = getCell(r, c);
            cel.textContent = '';
            cel.classList.remove('visible-num');
            cel.classList.add('oculta');
        }
        faseVer = false;
        document.getElementById('lblEstado').textContent = `Toca 1 → ${nivel}`;
    }, tiempoVer());
}

function clickCelda(r, c) {
    if (faseVer || bloqueado) return;

    const target  = mapa[siguiente];
    if (!target) return;

    const cel      = getCell(r, c);
    const esperada = getCell(target.r, target.c);

    if (cel === esperada) {
        cel.textContent = siguiente;
        cel.classList.remove('oculta');
        cel.classList.add('correcto');
        siguiente++;

        if (siguiente > nivel) {
            // ¡Nivel superado!
            bloqueado = true;
            nivel++;
            if (nivel - 1 > mejor) {
                mejor = nivel - 1;
                document.getElementById('lblMejor').textContent = mejor;
            }
            document.getElementById('lblEstado').textContent = '¡Correcto! +1';

            // Flash y siguiente ronda automática tras 800ms
            setTimeout(() => {
                document.getElementById('tablero').classList.add('level-flash');
                setTimeout(() => {
                    document.getElementById('tablero').classList.remove('level-flash');
                    iniciarRonda();
                }, 600);
            }, 200);
        }
    } else {
        perder();
    }
}

function perder() {
    bloqueado = true;
    // Revelar todas las posiciones
    for (let n = 1; n <= nivel; n++) {
        const { r, c } = mapa[n];
        const cel = getCell(r, c);
        cel.textContent = n;
        cel.classList.remove('oculta');
        cel.classList.add(n < siguiente ? 'correcto' : 'incorrecto');
    }
    document.getElementById('lblEstado').textContent = 'Perdiste';

    // Guardar récord (puntaje = nivel alcanzado)
    fetch('/juegos/guardar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ juego: 'Chimp', dificultad: 'Progresivo', puntaje: nivel })
    }).catch(() => {});

    const nivelActual = nivel;
    // Bajar nivel si falló (mínimo 4)
    nivel = Math.max(4, nivel - 1);

    document.getElementById('modalMsg').textContent =
        `Llegaste al nivel ${nivelActual}. Tu mejor: ${mejor || nivelActual} números.`;
    document.getElementById('resultModal').classList.remove('hidden');
}

// Inicializar tablero vacío al cargar
buildBoard();
document.getElementById('lblNivel').textContent = nivel;