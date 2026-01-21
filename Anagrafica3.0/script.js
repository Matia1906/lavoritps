
const persona = [];

function stampa() {
    const oggetto = {
        nome: document.getElementById("input-nome").value,
        cognome: document.getElementById("input-cognome").value,
        eta: document.getElementById("input-eta").value,
        colore: document.getElementById("input-colore").value
    };

    if (
        oggetto.nome === "" ||
        oggetto.cognome === "" ||
        oggetto.eta === "" ||
        oggetto.colore === ""
    ) {
        alert("Compila tutti i campi!");
        return;
    }

    persona.push(oggetto);

    if (document.getElementById("dati").innerHTML !== "") {
        document.getElementById("dati").innerHTML += "<hr><br>";
    }

    document.getElementById("dati").innerHTML +=
        "Nome: " + oggetto.nome + "<br>" +
        "Cognome: " + oggetto.cognome + "<br>" +
        "Età: " + oggetto.eta + "<br>" +
        "Colore preferito: " + oggetto.colore + "<br><br>";

    document.getElementById("input-nome").value = "";
    document.getElementById("input-cognome").value = "";
    document.getElementById("input-eta").value = "";
    document.getElementById("input-colore").value = "";
}

function cancella() {
    persona.length = 0;
    document.getElementById("dati").innerHTML = "";

    document.getElementById("input-nome").value = "";
    document.getElementById("input-cognome").value = "";
    document.getElementById("input-eta").value = "";
    document.getElementById("input-colore").value = "";
}