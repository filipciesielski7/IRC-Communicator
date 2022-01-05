<h1 align="center">
    IRC-Communicator 💻 
</h1>

_Also available in: [Polski](README.pl.md)_

## About

IRC-Communicator project for Computer Networks at Poznan University of Technology. The application implements a text-based chat system, which is designed for group communication in discussion forums, called channels, but also allows one-on-one communication.

![GUI](https://user-images.githubusercontent.com/56769119/148282306-e2745a51-fc1d-4e21-8661-eb5d989568c2.png)

## Folder Structure

```bash
PROJECT_FOLDER
│  README.md
│  README.pl.md
└──[src]
    └──[server]
    │  └── server.c # Server implemented in C
    └──[client]
        │  pom.xml
        └──[src/main]
            └──[java] # Client implemented in Java
            └──[resources/com/example/client]
                └── client.fxml     # JavaFX
```

## Getting Started

### Server

1. Clone the repo
   ```sh
   git clone https://github.com/filipciesielski7/IRC-Communicator.git
   ```
2. Navigate into the server directory
   ```
   cd src/server
   ```
3. Run this command to compile server:
   ```
   gcc -pthread server.c -o server -Wall
   ```
4. After server is compiled you can now run it using:
   ```
   ./server <port number> lub ./server (by default port 1234).
   ```

### Client

The project is a standard Maven project. To run it from the command line, type `mvn clean javafx:run` in `src/client` directory or import the project to your IDE of choice as you would with any Maven project.

## Contributors

- Filip Ciesielski 145257
- Michał Ciesielski 145325
