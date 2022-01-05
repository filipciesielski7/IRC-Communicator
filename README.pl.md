<h1 align="center">
    IRC-Komunikator 💻 
</h1>

_Dostępne również w wersji po: [English](README.md)_

## O projekcie

IRC-Komunikator - projekt w ramach przedmiotu Sieci Komputerowe na Politechnice Poznańskiej. Aplikacja implementuje komunikator grupowy typu IRC z możliwością dołączenia do pokoju, tworzenia pokoju, wysyłania wiadomości w pokoju, odbierania wiadomości z pokoju, wyjścia z pokoju czy usunięcia danego użytkownika z pokoju przez jego właściciela.

![GUI](https://user-images.githubusercontent.com/56769119/148282468-763f3019-a198-4b50-bd19-ada9ed91d85b.png)

Celem projektu było zaimplementowanie serwera TCP w [C](https://en.wikipedia.org/wiki/C_(programming_language)) oraz klienta w [Javie](https://www.java.com/) z możliwością wykonywania wyżej wymienionych aktywności zgodnie z definicją komunikatora IRC. GUI klienta zostało zaimplementowane przy pomocy [JavaFX](https://openjfx.io/).

## Struktura folderu

```bash
PROJECT_FOLDER
│  README.md
│  README.pl.md
└──[src]
    └──[server]
    │  └── server.c # Serwer zaimplementowany w C
    └──[client]
        │  pom.xml
        └──[src/main]
            └──[java] # Klient zaimplementowany w Javie
            └──[resources/com/example/client]
                └── client.fxml # JavaFX
```

## Uruchamianie

### Serwer

1. Klonowanie repozytorium
   ```sh
   git clone https://github.com/filipciesielski7/IRC-Communicator.git
   ```
2. Przejście do folderu server
   ```
   cd src/server
   ```
3. Kompilacja
   ```
   gcc -pthread server.c -o server -Wall
   ```
4. Uruchomienie
   ```
   ./server <numer portu> lub ./server (domyślnie port 1234).
   ```

### Klient

Klienta uruchamiamy poleceniem `mvn clean javafx:run` w katalogu `src/client` lub przy pomocy wybranego środowiska po otworzeniu projektu ze wspomnianego katalogu.

## Autorzy

- Filip Ciesielski 145257
- Michał Ciesielski 145325
