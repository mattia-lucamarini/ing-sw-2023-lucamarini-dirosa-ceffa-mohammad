# Prova Finale di Ingegneria del Software - AA 2022-2023
### Team
- [**Marini**](https://github.com/mattia-lucamarini)
- [**Dirosa**](https://github.com/Angelodirosa)
- [**Ceffa**](https://github.com/poolll98)
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
|Libreria/Plugin| Descrizione                                     |
|---------------|-------------------------------------------------|
|__Maven__| Software project management and comprehension tool|
|__JavaFx__| GUI toolkit for Java                            |
|__JUnit__| Unit testing framework for Java     |

## Documentazione
### UML
Consultare la documentazione UML [qui]().
### JavaDoc
Consultare la documentazione JavaDoc [qui]().

## Run the Application

**Warning**: Assicurati di avere i rispettivi jars nella cartella [jar](deliverables/jar). 
E' possibile produrre i jars dell'applicazione seguendo i seguenti steps:
1. Apri il progetto utilizzando IntelliJ.
2. Click sulla Maven Projects sidebar nella parte destra della finestra, espandi il progetto e identifica la voce ```Lifecycle section```. Double-click su ```package``` goal.
3. Una volta completata la fase di build, Maven creerà i JAR files nella cartella target del tuo progetto. Rinominali come segue:

### Run the Server
Il Server può essere lanciato seguendo due procedimenti:
1. Eseguendo direttamente il jar file [server_jar]():

``` ...  [--ports <socket_port_number> <rmi_port_number>]```

L'argomento ```--ports``` specifica le porte dove mettere in ascolto il Server per accettare connessioni dai client utilizzando
rispettivamente Socket ed RMI. Essendo opzionale, se omesso, i valori di default sono i seguenti:
  - <socket_port_number> = 59090
  - <rmi_port_number> = 1099
2. Utilizzando Docker: seguire l'apposita guida [qui](MyShelfie/README.md).

### Run the Client
E' possibile eseguire il client tramite interfaccia grafica (GUI), oppure tramite interfaccia testuale (TUI):
1. Eseguire il Client (TUI):

``` ...  [--ip <server_ip> ] [--ports <socket_port_number> <rmi_port_number>]```

2. Eseguire il Client (GUI):

``` ...  [--ip <server_ip>]```
L'argomento ```--ip``` specifica l'indirizzo IP del server che offre il servizio di gioco. Essendo opzionale, se omesso,
il valore di default è 
  - <server_ip> = 127.0.0.1
  - 
L'argomento ```--ports``` specifica le porte messe a disposizione dal Server per accettare connessioni dai client utilizzando
rispettivamente Socket ed RMI. Essendo opzionale, se omesso, i valori di default sono i seguenti:
  - <socket_port_number> = 59090
  - <rmi_port_number> = 1099



