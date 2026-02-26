# Distributed Network Game

This project is a mandatory assignment for the course **DIS (Distribution, Integration, and Security)** during the 3rd semester. The objective was to transform a local single-player game into a distributed multiplayer game where players interact on a shared game board over a network.

## Features
* **Multiplayer Interaction:** Players can move on a shared board, collide with each other, and exchange points.
* **Distributed Logic:** Implementation of network communication to synchronize player states (positions and points) across different clients.
* **Protocol Handling:** Design and implementation of a simple text-based protocol (e.g., `MOVE`, `QUIT`, `POINT`) for exchanging game data.
* **Concurrency:** Handling of race conditions and concurrency issues that arise when multiple players move simultaneously in a distributed system.

## Technologies
* **Language:** Java
* **Networking:** TCP Sockets for reliable data transfer between clients and the server.
* **Concurrency:** Java Threads for handling incoming network messages without blocking user interaction (GUI).
* **Tools:** IntelliJ IDEA, Git.

## Architecture
The project explores different models for distributed systems, including:
* **Centralized Control:** A server acts as the authority for the game state.
* **Peer-to-Peer Tendencies:** Direct communication and synchronization between clients.

## Running the Application
1. Start the `Server` application to create the game session.
2. Run multiple instances of `App` (the client) to connect players to the network.

## Authors
Emil Nordstrøm Støve, Govher Veliyeva, and Sidse Borch Mogensen
