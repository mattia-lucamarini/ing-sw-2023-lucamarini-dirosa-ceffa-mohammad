# Prova Finale di Ingegneria del Software - AA 2022-2023
### Team
- [**Marini**]()
- [**Dirosa**]()
- [**Ceffa**]()
- [**Mohammad**]()
# My Shelfie
<img src="MyShelfie/free_resources/my-shelfie.png" alt="Image description" width="350">

Implementazione in Java del gioco da tavolo [My Shelfie](https://www.craniocreations.it/prodotto/my-shelfie).

Il progetto è realizzato tramite un sistema distribuito composto da un singolo server, capace di gestire partite multiple
in contemporanea, e multipli client che possono partecipare ad una partita alla volta utilizzando una tecnologia di rete
a scelta tra RMI e Socket, entrambe supportato in contemporanea dal server.
Il client può anche decidere di giocare una partita utilizzando a suo piacimento un'interfaccia testuale (TUI) oppure un'
interfaccia grafica (GUI).
L'implementazione segue il pattern MVC (Model-View-Controller).

## Funzionalità
- __Regole Complete__: Si considerino tutte le regole per lo svolgimento di normali partite, come indicato nel manuale del gioco.
- __Interfaccia utente__: CLI & GUI
- __Rete__: Socket & RMI
- __2 FA__ (Funzionalità Avanzate):
  - __Partite multiple__: il server può gestire più partite contemporaneamente, ognuna delle quali viene gestita in maniera 
  indipendente dalla altre.
  - __Resilienza alle disconnessioni__: I giocatori disconnessi a seguito della caduta della rete o del crash del client, 
  possono ricollegarsi e continuare la partita. Mentre un giocatore non è collegato, il gioco continua saltando i turni di quel giocatore.

## Librerie e Plugins
|Libreria/Plugin|Descrizione|
|---------------|-----------|
|__Maven__|Software project management and comprehension tool|
|__JavaFx__||
|__JUnit__||

## Documentazione
### UML
### JavaDoc

## Run the Application

**Warning**: Assicurati di avere i rispettivi jars nella cartella [target](). 
E' possibile produrre i jars dell'applicazione seguendo i seguenti steps:

