// ============================================================
//  Torre de Hanoi – Brain Training Pro
// ============================================================

let torres = { A: [], B: [], C: [] };
let discos = 4;
let movimientos = 0;
let origen = null;

function iniciarJuego() {
    discos = parseInt(document.getElementById('nivel').value);
    movimientos = 0;
    origen = null;

    torres = { A: [], B: [], C: [] };
    for (let i = discos; i >= 1; i--) torres.A.push(i);

    document.getElementById('lblMovimientos').textContent = '0';
    document.getElementById('lblMinimo').textContent = Math.pow(2, discos) - 1;
    document.getElementById('lblInfo').textContent = 'Selecciona torre origen';
    document.getElementById('resultModal').classList.add('hidden');

    renderAll();
    destacarTorres(null);
}

function renderTorre(nombre) {
    const container = document.getElementById(`discos${nombre}`);
    container.innerHTML = '';

    torres[nombre].forEach(d => {
        const el = document.createElement('div');
        el.className = `disco disco-${d}`;
        el.textContent = d;
        container.appendChild(el);
    });
}

function renderAll() {
    renderTorre('A');
    renderTorre('B');
    renderTorre('C');
}

function destacarTorres(sel) {
    ['A', 'B', 'C'].forEach(n => {
        const el = document.getElementById(`torre${n}`);
        if (sel === n) el.classList.add('seleccionada');
        else el.classList.remove('seleccionada');
    });
}

function clickTorre(nombre) {
    if (origen === null) {
        if (torres[nombre].length === 0) {
            setInfo('Esa torre está vacía');
            return;
        }
        origen = nombre;
        destacarTorres(nombre);
        setInfo(`Origen: Torre ${nombre} → Selecciona destino`);
    } else {
        if (nombre === origen) {
            origen = null;
            destacarTorres(null);
            setInfo('Selecciona torre origen');
            return;
        }
        mover(origen, nombre);
        origen = null;
        destacarTorres(null);
    }
}

function mover(org, dst) {
    const tOrig = torres[org];
    const tDest = torres[dst];

    if (tOrig.length === 0) { setInfo('Torre origen vacía'); return; }

    const disco = tOrig[tOrig.length - 1];

    if (tDest.length > 0 && tDest[tDest.length - 1] < disco) {
        setInfo('No puedes colocar un disco grande sobre uno pequeño');
        return;
    }

    tOrig.pop();
    tDest.push(disco);
    movimientos++;

    document.getElementById('lblMovimientos').textContent = movimientos;
    setInfo(`Movido disco ${disco}: ${org} → ${dst}`);
    renderAll();
    verificarVictoria();
}

function verificarVictoria() {
    if (torres.C.length === discos) {
        const minimo = Math.pow(2, discos) - 1;
        const dif = ['', '', '', 'Fácil (3 discos)', 'Medio (4 discos)', 'Difícil (5 discos)'][discos] || `${discos} discos`;

        fetch('/juegos/guardar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ juego: 'Hanoi', dificultad: dif, puntaje: movimientos })
        }).then(r => r.json()).then(data => {
            const esRecord = data.resultado === 'nuevo_record' || data.resultado === 'primer_record';
            showModal(
                `Movimientos: ${movimientos} / Mínimo posible: ${minimo}` +
                (esRecord ? ' | Nuevo récord' : '')
            );
        }).catch(() => {
            showModal(`Movimientos: ${movimientos} / Mínimo posible: ${minimo}`);
        });
    }
}

function setInfo(msg) {
    document.getElementById('lblInfo').textContent = msg;
}

function showModal(msg) {
    document.getElementById('modalMsg').textContent = msg;
    document.getElementById('resultModal').classList.remove('hidden');
}

// Auto-start
iniciarJuego();
