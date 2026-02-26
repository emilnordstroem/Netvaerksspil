# Distribueret Netværksspil

Dette projekt er en obligatorisk opgave i faget **DIS (Distribution, Integration og Sikkerhed)** på 3. semester. Formålet var at transformere et lokalt single-player spil til et distribueret multiplayer-spil, hvor spillere kan interagere på en fælles spilleplade over et netværk.

## Funktioner
* **Multiplayer Interaktion:** Spillere kan bevæge sig på en fælles plade, støde ind i hinanden og udveksle point.
* **Distribueret Logik:** Implementering af netværkskommunikation til synkronisering af spillertilstande (positioner og point) på tværs af forskellige klienter.
* **Protokolhåndtering:** Design og implementering af en simpel tekstbaseret protokol (f.eks. `MOVE`, `QUIT`, `POINT`) til udveksling af spildata.
* **Samtidighed:** Håndtering af race-conditions og samtidighedsproblemer, der opstår, når flere spillere flytter sig samtidigt i et distribueret system.

## Teknologier
* **Sprog:** Java
* **Netværk:** TCP Sockets til pålidelig dataoverførsel mellem klienter og server.
* **Concurrency:** Java Threads til håndtering af indkommende netværksbeskeder uden at blokere for brugerinteraktion (GUI).
* **Værktøjer:** IntelliJ IDEA, Git.

## Arkitektur
Projektet udforsker forskellige modeller for distribuerede systemer, herunder:
* **Centraliseret styring:** En server fungerer som autoritet for spillets tilstand.
* **Peer-to-peer tendenser:** Direkte kommunikation og synkronisering mellem klienter.

## Kørsel
1. Start `Server` applikationen for at oprette spilsessionen.
2. Kør flere instanser af `App` (klienten) for at forbinde spillere til netværket.

## Forfatter
Emil Nordstrøm Støve, Govher Veliyeva og Sidse Borch Mogensen
