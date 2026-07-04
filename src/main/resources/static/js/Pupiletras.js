// ============================================================
//  Pupiletras (Sopa de letras) – Brain Training Pro
// ============================================================

const BANCO_PALABRAS = {
    'Fácil':   ['GATO', 'PERRO', 'LEON', 'OSO', 'PATO', 'RATON'],
    'Medio':   ['ELEFANTE', 'JIRAFA', 'TIGRE', 'DELFIN', 'TORTUGA', 'CONEJO', 'CABALLO', 'MONO'],
    'Difícil': ['MURCIELAGO', 'COCODRILO', 'HIPOPOTAMO', 'RINOCERONTE', 'ARDILLA', 'CANGURO', 'ESCORPION', 'MARIPOSA', 'SERPIENTE', 'TIBURON']
};

const TAM_GRID = { 'Fácil': 10, 'Medio': 12, 'Difícil': 15 };

// Direcciones permitidas según dificultad: [dRow, dCol]
const DIRECCIONES = {
    'Fácil':   [[0, 1], [1, 0]],
    'Medio':   [[0, 1], [1, 0], [1, 1]],
    'Difícil': [[0, 1], [1, 0], [1, 1], [-1, 1]]
};

const ALFABETO = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';

let grid = [];
let tamGrid = 12;
let palabrasPendientes = [];
let celdasPorPalabra = {}; // palabra -> array de {row,col}
let timerInterval = null;
let tiempoInicio = 0;
let seleccionando = false;
let celdaInicio = null;
let celdaActual = null;

function quitarAcentos(txt) {
    return txt.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
}

function iniciarJuego() {
    const dif = document.getElementById('dificultad').value;
    tamGrid = TAM_GRID[dif];
    const palabras = BANCO_PALABRAS[dif];

    clearInterval(timerInterval);
    document.getElementById('resultModal').classList.add('hidden');
    document.getElementById('lblTiempo').textContent = '0s';

    construirGrid(palabras, dif);
    renderizarGrid();
    renderizarLista();

    tiempoInicio = Date.now();
    timerInterval = setInterval(function () {
        const seg = Math.floor((Date.now() - tiempoInicio) / 1000);
        document.getElementById('lblTiempo').textContent = seg + 's';
    }, 500);
}

function construirGrid(palabras, dificultad) {
    grid = [];
    for (let r = 0; r < tamGrid; r++) {
        const fila = [];
        for (let c = 0; c < tamGrid; c++) {
            fila.push(null);
        }
        grid.push(fila);
    }

    celdasPorPalabra = {};
    palabrasPendientes = [];

    const direcciones = DIRECCIONES[dificultad];

    // Palabras más largas primero: encajan mejor
    const ordenadas = palabras.slice().sort(function (a, b) { return b.length - a.length; });

    ordenadas.forEach(function (palabraOriginal) {
        const palabra = quitarAcentos(palabraOriginal).toUpperCase();
        let colocada = false;
        let intentos = 0;

        while (!colocada && intentos < 300) {
            intentos++;

            const dir = direcciones[Math.floor(Math.random() * direcciones.length)];
            const invertida = Math.random() < 0.5;
            const letras = invertida ? palabra.split('').reverse() : palabra.split('');

            const filaMax = tamGrid - (dir[0] !== 0 ? (letras.length - 1) * Math.abs(dir[0]) : 0) - 1;
            const colMax = tamGrid - (dir[1] !== 0 ? (letras.length - 1) * Math.abs(dir[1]) : 0) - 1;

            let filaMin = 0, colMin = 0;
            if (dir[0] < 0) { filaMin = (letras.length - 1); }
            if (dir[1] < 0) { colMin = (letras.length - 1); }

            if (filaMax < filaMin || colMax < colMin) continue;

            const startRow = filaMin + Math.floor(Math.random() * (filaMax - filaMin + 1));
            const startCol = colMin + Math.floor(Math.random() * (colMax - colMin + 1));

            const posiciones = [];
            let cabe = true;

            for (let i = 0; i < letras.length; i++) {
                const r = startRow + dir[0] * i;
                const c = startCol + dir[1] * i;
                if (r < 0 || r >= tamGrid || c < 0 || c >= tamGrid) { cabe = false; break; }
                const existente = grid[r][c];
                if (existente !== null && existente !== letras[i]) { cabe = false; break; }
                posiciones.push({ row: r, col: c });
            }

            if (!cabe) continue;

            for (let i = 0; i < letras.length; i++) {
                grid[posiciones[i].row][posiciones[i].col] = letras[i];
            }

            celdasPorPalabra[palabra] = posiciones;
            colocada = true;
        }

        if (colocada) {
            palabrasPendientes.push(palabraOriginal);
        }
    });

    // Rellenar celdas vacías con letras aleatorias
    for (let r = 0; r < tamGrid; r++) {
        for (let c = 0; c < tamGrid; c++) {
            if (grid[r][c] === null) {
                grid[r][c] = ALFABETO[Math.floor(Math.random() * ALFABETO.length)];
            }
        }
    }
}

function renderizarGrid() {
    const tablero = document.getElementById('tableroPupiletras');
    tablero.innerHTML = '';
    tablero.style.gridTemplateColumns = 'repeat(' + tamGrid + ', 1fr)';
    tablero.style.setProperty('--tam-grid', tamGrid);

    for (let r = 0; r < tamGrid; r++) {
        for (let c = 0; c < tamGrid; c++) {
            const celda = document.createElement('div');
            celda.className = 'pl-celda';
            celda.textContent = grid[r][c];
            celda.dataset.row = r;
            celda.dataset.col = c;

            celda.addEventListener('mousedown', function (e) { e.preventDefault(); iniciarSeleccion(celda); });
            celda.addEventListener('mouseenter', function () { extenderSeleccion(celda); });
            celda.addEventListener('touchstart', function (e) { e.preventDefault(); iniciarSeleccion(celda); }, { passive: false });

            tablero.appendChild(celda);
        }
    }

    document.addEventListener('mouseup', finalizarSeleccion);
    document.addEventListener('touchend', finalizarSeleccion);
    document.addEventListener('touchmove', function (e) {
        if (!seleccionando) return;
        const touch = e.touches[0];
        const el = document.elementFromPoint(touch.clientX, touch.clientY);
        if (el && el.classList.contains('pl-celda')) {
            extenderSeleccion(el);
        }
    }, { passive: true });
}

function renderizarLista() {
    const lista = document.getElementById('listaPalabras');
    lista.innerHTML = '';
    palabrasPendientes.forEach(function (palabra) {
        const li = document.createElement('li');
        li.textContent = palabra;
        li.dataset.palabra = quitarAcentos(palabra).toUpperCase();
        lista.appendChild(li);
    });
    actualizarContador();
}

function actualizarContador() {
    const total = palabrasPendientes.length;
    const encontradas = document.querySelectorAll('#listaPalabras li.encontrada').length;
    document.getElementById('lblPalabras').textContent = encontradas + ' / ' + total;
    if (total > 0 && encontradas === total) {
        ganar();
    }
}

function iniciarSeleccion(celda) {
    seleccionando = true;
    celdaInicio = celda;
    celdaActual = celda;
    limpiarSeleccionTemporal();
    celda.classList.add('pl-seleccionada');
}

function extenderSeleccion(celda) {
    if (!seleccionando) return;
    celdaActual = celda;
    pintarLinea();
}

function pintarLinea() {
    limpiarSeleccionTemporal();
    const celdas = obtenerLineaEntre(celdaInicio, celdaActual);
    celdas.forEach(function (c) { c.classList.add('pl-seleccionada'); });
}

function obtenerLineaEntre(inicio, fin) {
    const r1 = parseInt(inicio.dataset.row, 10), c1 = parseInt(inicio.dataset.col, 10);
    const r2 = parseInt(fin.dataset.row, 10), c2 = parseInt(fin.dataset.col, 10);

    const dr = r2 - r1, dc = c2 - c1;

    // Debe ser una línea recta: horizontal, vertical o diagonal exacta
    if (dr !== 0 && dc !== 0 && Math.abs(dr) !== Math.abs(dc)) {
        return [inicio];
    }

    const pasos = Math.max(Math.abs(dr), Math.abs(dc));
    const stepR = pasos === 0 ? 0 : dr / pasos;
    const stepC = pasos === 0 ? 0 : dc / pasos;

    const resultado = [];
    for (let i = 0; i <= pasos; i++) {
        const r = r1 + stepR * i;
        const c = c1 + stepC * i;
        const el = document.querySelector('.pl-celda[data-row="' + r + '"][data-col="' + c + '"]');
        if (el) resultado.push(el);
    }
    return resultado;
}

function limpiarSeleccionTemporal() {
    document.querySelectorAll('.pl-celda.pl-seleccionada').forEach(function (c) {
        c.classList.remove('pl-seleccionada');
    });
}

function finalizarSeleccion() {
    if (!seleccionando) return;
    seleccionando = false;

    const celdas = obtenerLineaEntre(celdaInicio, celdaActual);
    const texto = celdas.map(function (c) { return c.textContent; }).join('');
    const textoInv = texto.split('').reverse().join('');

    limpiarSeleccionTemporal();

    const li = Array.from(document.querySelectorAll('#listaPalabras li')).find(function (item) {
        return !item.classList.contains('encontrada') &&
               (item.dataset.palabra === texto || item.dataset.palabra === textoInv);
    });

    if (li) {
        li.classList.add('encontrada');
        celdas.forEach(function (c) { c.classList.add('pl-encontrada'); });
        actualizarContador();
    }
}

function ganar() {
    clearInterval(timerInterval);
    const seg = Math.floor((Date.now() - tiempoInicio) / 1000);
    const dif = document.getElementById('dificultad').value;

    fetch('/juegos/guardar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ juego: 'Pupiletras', dificultad: dif, puntaje: seg, tiempo: seg })
    }).then(function (r) { return r.json(); }).then(function (data) {
        const esRecord = data.resultado === 'nuevo_record' || data.resultado === 'primer_record';
        document.getElementById('modalMsg').textContent =
            'Tiempo: ' + seg + 's' + (esRecord ? ' | Nuevo récord' : '');
        document.getElementById('resultModal').classList.remove('hidden');
    }).catch(function () {
        document.getElementById('modalMsg').textContent = 'Tiempo: ' + seg + 's';
        document.getElementById('resultModal').classList.remove('hidden');
    });
}

// Auto-start
iniciarJuego();