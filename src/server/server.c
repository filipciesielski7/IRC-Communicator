#include <sys/types.h>
#include <sys/wait.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <netdb.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <pthread.h>

static volatile int serverRunning = 1;

#define SERVER_PORT 1234
#define QUEUE_SIZE 3

#define USER_SIZE 3
#define ROOM_SIZE 10

#define MESSAGE_SIZE 256
#define NUMBER_OF_MESSAGES 64

#define LOGIN_SIZE 10
#define ROOM_NAME_SIZE 20

pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t mutex_users = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t mutex_rooms = PTHREAD_MUTEX_INITIALIZER;

struct sockaddr_in server_address;

struct User
{
    int socket;
    char name[LOGIN_SIZE];
};
struct User users[USER_SIZE];

struct Room
{
    char name[ROOM_NAME_SIZE];
    int users[USER_SIZE];
    char messages[NUMBER_OF_MESSAGES][MESSAGE_SIZE];
};
struct Room rooms[ROOM_SIZE];

struct thread_data_t
{
    int socket;
    int userID;
    char fromClient[1024];
    int bytes;
};

void initServerSockAddr();
void initStruct();
int createSocket(int port);
void handleConnection(int connectionSocketDescriptor);
void *Thread_Behavior(void *t_data);

int getFirstFreeUserSlot();
int getFirstFreeRoomID();
int getRoomIDbyName(char name[ROOM_NAME_SIZE]);
int getUserIDbyName(char name[LOGIN_SIZE]);
int findUserInRoom(int userID, int roomID);

int getFirstFreeSlotInRoom(int roomID);
void joinRoom(int userID, int roomID);
void sendToRoom(int roomID, char message[MESSAGE_SIZE], int msg_length);
void deleteUser(int userID);
void deleteRoom(int roomID);

int update_server_response();
void broadcast_server_response(int bytes);

void initServerSockAddr()
{
    server_address.sin_family = AF_INET;
    server_address.sin_addr.s_addr = htonl(INADDR_ANY);
    server_address.sin_port = htons(SERVER_PORT);
}

void initStruct()
{
    for (int i = 0; i < USER_SIZE; i++)
    {
        deleteUser(i);
    }
    for (int i = 0; i < ROOM_SIZE; i++)
    {
        deleteRoom(i);
    }
}

int createSocket(int port)
{
    int serverSocketDescriptor;
    char reuseAddrVal = 1;

    memset(&server_address, 0, sizeof(struct sockaddr));
    initServerSockAddr();

    serverSocketDescriptor = socket(AF_INET, SOCK_STREAM, 0);
    if (serverSocketDescriptor < 0)
    {
        fprintf(stderr, "Error while creating socket\n");
        exit(1);
    }

    setsockopt(serverSocketDescriptor, SOL_SOCKET, SO_REUSEADDR, (char *)&reuseAddrVal, sizeof(reuseAddrVal));

    int bindResult = bind(serverSocketDescriptor, (struct sockaddr *)&server_address, sizeof(struct sockaddr));
    if (bindResult < 0)
    {
        fprintf(stderr, "Error while binding IP address and port number to the socket\n");
        exit(1);
    }

    int listenResult = listen(serverSocketDescriptor, QUEUE_SIZE);
    if (listenResult < 0)
    {
        fprintf(stderr, "Error while setting queue size\n");
        exit(1);
    }

    return serverSocketDescriptor;
}

int getFirstFreeUserSlot()
{
    for (int i = 0; i < USER_SIZE; i++)
    {
        if (users[i].socket == -1)
        {
            return i;
        }
    }
    return -1;
}

int getFirstFreeRoomID()
{
    for (int i = 0; i < ROOM_SIZE; i++)
    {
        if (rooms[i].name[0] == 0)
        {
            return i;
        }
    }
    return -1;
}

int getRoomIDbyName(char name[ROOM_NAME_SIZE])
{
    int exists;
    for (int i = 0; i < ROOM_SIZE; i++)
    {
        exists = i;
        for (int j = 0; j < ROOM_NAME_SIZE; j++)
        {
            if (name[j] != rooms[i].name[j])
            {
                exists = -1;
            }
        }
        if (exists > -1)
        {
            return exists;
        }
    }
    return -1;
}

int getUserIDbyName(char name[LOGIN_SIZE])
{
    int exists;
    for (int i = 0; i < USER_SIZE; i++)
    {
        exists = i;
        for (int j = 0; j < LOGIN_SIZE; j++)
        {
            if (name[j] != users[i].name[j])
            {
                exists = -1;
            }
        }
        if (exists > -1)
        {
            return exists;
        }
    }
    return -1;
}

int findUserInRoom(int userID, int roomID)
{
    printf("Searching for user %d in room %d\n", userID + 1, roomID + 1);
    for (int i = 0; i < USER_SIZE; i++)
    {
        if (rooms[roomID].users[i] == userID)
        {
            return i;
        }
    }
    return -1;
}

int getFirstFreeSlotInRoom(int roomID)
{
    for (int i = 0; i < USER_SIZE; i++)
    {
        if (rooms[roomID].users[i] == -1)
        {
            return i;
        }
    }
    return -1;
}

void joinRoom(int userID, int roomID)
{
    if (findUserInRoom(userID, roomID) == -1)
    {
        int freeSlot = getFirstFreeSlotInRoom(roomID);
        if (freeSlot != -1)
        {
            rooms[roomID].users[freeSlot] = userID;
        }
    }
}

void sendToRoom(int roomID, char message[MESSAGE_SIZE], int msg_length)
{
    int i, j, spot = -1;
    for (i = 0; i < NUMBER_OF_MESSAGES; i++)
        if (rooms[roomID].messages[i][0] == 0)
        {
            spot = i;
            break;
        }

    if (spot > -1)
    {
        for (i = 0; i < msg_length; i++)
        {
            rooms[roomID].messages[spot][i] = message[i];
        }
    }
    else
    {
        for (i = 0; i < NUMBER_OF_MESSAGES - 1; i++)
        {
            for (j = 0; j < MESSAGE_SIZE; j++)
            {
                rooms[roomID].messages[i][j] = rooms[roomID].messages[i + 1][j];
            }
        }
        memset(rooms[roomID].messages[NUMBER_OF_MESSAGES - 1], 0, sizeof(rooms[roomID].messages[NUMBER_OF_MESSAGES - 1]));
        for (j = 0; j < msg_length; j++)
        {
            rooms[roomID].messages[NUMBER_OF_MESSAGES - 1][j] = message[j];
        }
    }
}

void deleteUser(int userID)
{
    int i = userID;
    if (i >= 0)
    {
        users[i].socket = -1;
        memset(users[i].name, 0, sizeof(users[i].name));
    }
}

void deleteRoom(int roomID)
{
    int i = roomID;
    if (i > -1)
    {
        memset(rooms[i].name, 0, sizeof(rooms[i].name));
        for (int j = 0; j < USER_SIZE; j++)
        {
            rooms[i].users[j] = -1;
        }
        memset(rooms[i].messages, 0, sizeof(rooms[i].messages[0][0] * NUMBER_OF_MESSAGES * MESSAGE_SIZE));
    }
}

char server_response[262144];
int update_server_response()
{
    memset(server_response, 0, sizeof(server_response));
    server_response[0] = '#';
    int cc = 1;
    for (int i = 0; i < ROOM_SIZE; i++)
    {
        if (rooms[i].name[0] != 0)
        {
            server_response[cc++] = '@';
            for (int j = 0; j < ROOM_NAME_SIZE; j++)
            {
                if (rooms[i].name[j] == 0)
                {
                    break;
                }
                else
                {
                    server_response[cc++] = rooms[i].name[j];
                }
            }

            server_response[cc++] = '%';
            for (int j = 0; j < USER_SIZE; j++)
            {
                int userID = rooms[i].users[j];
                if (userID > -1)
                {
                    for (int k = 0; k < LOGIN_SIZE; k++)
                    {
                        if (users[userID].name[k] == 0)
                        {
                            break;
                        }
                        else
                        {
                            server_response[cc++] = users[userID].name[k];
                        }
                    }
                    server_response[cc++] = ';';
                }
            }

            server_response[cc - 1] = '%';
            for (int j = 0; j < NUMBER_OF_MESSAGES; j++)
            {
                if (rooms[i].messages[j][0] != 0)
                {
                    for (int k = 0; k < MESSAGE_SIZE; k++)
                    {
                        if (rooms[i].messages[j][k] == 0)
                        {
                            break;
                        }
                        else
                        {
                            server_response[cc++] = rooms[i].messages[j][k];
                        }
                    }
                    server_response[cc++] = ';';
                }
            }
            cc--;
        }
    }

    server_response[cc++] = '$';
    server_response[cc] = '\n';

    return cc + 1;
}

void broadcast_server_response(int bytes)
{
    for (int i = 0; i < USER_SIZE; i++)
    {
        if (users[i].socket != -1)
        {
            write(users[i].socket, server_response, bytes);
        }
    }
}

void *ThreadBehavior(void *t_data)
{
    pthread_detach(pthread_self());
    struct thread_data_t *th_data = (struct thread_data_t *)t_data;

    users[(*th_data).userID].name[0] = '_';

    int i, j, k, l, bytes;
    char c, string[MESSAGE_SIZE];

    while (1)
    {
        memset((*th_data).fromClient, 0, sizeof((*th_data).fromClient));
        (*th_data).bytes = read((*th_data).socket, (*th_data).fromClient, sizeof((*th_data).fromClient));

        if ((*th_data).bytes > 0)
        {
            if ((*th_data).fromClient[0] == '#')
            {
                pthread_mutex_lock(&mutex);
                memset(string, 0, sizeof(string));
                switch ((*th_data).fromClient[1])
                {
                case '0': // Modifying user name
                    memset(users[(*th_data).userID].name, 0, sizeof(users[(*th_data).userID].name));
                    for (i = 3; i < (*th_data).bytes; i++)
                    {
                        c = (*th_data).fromClient[i];
                        if (c != '$')
                        {
                            users[(*th_data).userID].name[i - 3] = c;
                        }
                        else
                        {
                            printf("Modified user %d name to \"%s\"\n", (*th_data).userID + 1, users[(*th_data).userID].name);
                            write((*th_data).socket, "Name was changed succesfully!\n", 31);
                            break;
                        }
                    }
                    break;

                case '1': // Adding new room
                    for (i = 3; i < (*th_data).bytes; i++)
                    {
                        c = (*th_data).fromClient[i];
                        if (c != '$')
                        {
                            string[i - 3] = c;
                        }
                        else
                        {
                            break;
                        }
                    }

                    if (getRoomIDbyName(string) == -1)
                    {
                        i = getFirstFreeRoomID();
                        if (i > -1)
                        {
                            for (j = 0; j < ROOM_NAME_SIZE; j++)
                            {
                                rooms[i].name[j] = string[j];
                            }

                            joinRoom((*th_data).userID, i);
                            printf("User \"%s\" added to new created \"%s\" room\n", users[(*th_data).userID].name, string);
                            write((*th_data).socket, "New room was added succesfully!\n", 33);
                        }
                        else
                        {
                            printf("Room \"%s\" can't be added due to room number limit (%d)\n", string, ROOM_SIZE);
                            write((*th_data).socket, "There is no empty place on server to add new room!\n", 52);
                        }
                    }
                    else
                    {
                        printf("Room \"%s\" already exists\n", string);
                        write((*th_data).socket, "Room with typed name already exists!\n", 38);
                    }
                    break;

                case '2': // Joining the room
                    for (i = 3; i < (*th_data).bytes; i++)
                    {
                        c = (*th_data).fromClient[i];
                        if (c != '$')
                        {
                            string[i - 3] = c;
                        }
                        else
                        {
                            break;
                        }
                    }

                    i = getRoomIDbyName(string);
                    if (i > -1)
                    {
                        if (findUserInRoom((*th_data).userID, i) == -1)
                        {
                            joinRoom((*th_data).userID, i);
                            printf("User \"%s\" added to \"%s\" room\n", users[(*th_data).userID].name, string);
                            write((*th_data).socket, "You were added succesfully to new room!\n", 41);
                        }
                        else
                        {
                            printf("User \"%s\" is already in \"%s\" room\n", users[(*th_data).userID].name, string);
                            write((*th_data).socket, "You are already in this room!\n", 31);
                        }
                    }
                    else
                    {
                        printf("Room \"%s\" doesn't exist\n", string);
                        write((*th_data).socket, "Room with typed name doesn't exist!\n", 37);
                    }
                    break;

                case '3': // Leaving the room
                    for (i = 3; i < (*th_data).bytes; i++)
                    {
                        c = (*th_data).fromClient[i];
                        if (c != '$')
                        {
                            string[i - 3] = c;
                        }
                        else
                        {
                            break;
                        }
                    }

                    i = getRoomIDbyName(string);
                    j = findUserInRoom((*th_data).userID, i);

                    if (i > -1 && j > 0)
                    {
                        printf("User \"%s\" removed from \"%s\" room\n", users[(*th_data).userID].name, string);
                        write((*th_data).socket, "You were removed succesfully from this room!\n", 46);
                        rooms[i].users[j] = -1;
                    }

                    if (i == -1)
                    {
                        printf("Room \"%s\" doesn't exist\n", string);
                        write((*th_data).socket, "Room with typed name doesn't exist!\n", 37);
                    }
                    else if (j == -1)
                    {
                        printf("User \"%s\" doesn't belong to \"%s\" room\n", users[(*th_data).userID].name, string);
                        write((*th_data).socket, "You are not a member of this room!\n", 36);
                    }
                    else if (j == 0)
                    {
                        printf("User \"%s\" is \"%s\" room's admin. Room also deleted\n", users[(*th_data).userID].name, string);
                        write((*th_data).socket, "You have deleted succesfully your room!\n", 41);
                        deleteRoom(i);
                    }
                    break;

                case '4': // Sending message
                    for (i = 3; i < (*th_data).bytes; i++)
                    {
                        c = (*th_data).fromClient[i];
                        if (c != '%')
                        {
                            string[i - 3] = c;
                        }
                        else
                        {
                            break;
                        }
                    }

                    j = i + 1;
                    i = getRoomIDbyName(string);
                    if (i == -1)
                    {
                        printf("Room \"%s\" doesn't exist\n", string);
                        write((*th_data).socket, "Room with typed name doesn't exist!\n", 37);
                        break;
                    }

                    if (findUserInRoom((*th_data).userID, i) == -1)
                    {
                        printf("User \"%s\" doesn't belong to \"%s\" room\n", users[(*th_data).userID].name, string);
                        write((*th_data).socket, "You can't send a message, because you are not a member of this room!\n", 70);
                        break;
                    }

                    memset(string, 0, sizeof(string));
                    for (l = 0; l < LOGIN_SIZE; l++)
                    {
                        c = users[(*th_data).userID].name[l];
                        if (c != 0)
                        {
                            string[l] = c;
                        }
                        else
                        {
                            break;
                        }
                    }

                    string[l++] = ';';

                    for (k = 0; k < MESSAGE_SIZE; k++)
                    {
                        c = (*th_data).fromClient[j + k];
                        if (c != '$')
                        {
                            string[l + k] = (*th_data).fromClient[j + k];
                        }
                        else
                        {
                            break;
                        }
                    }

                    sendToRoom(i, string, l + k + 1);
                    write((*th_data).socket, "You have sent message succesfully!\n", 36);
                    break;

                case '5': // Removing user from room as admin
                    for (i = 3; i < (*th_data).bytes; i++)
                    {
                        c = (*th_data).fromClient[i];
                        if (c != '%')
                        {
                            string[i - 3] = c;
                        }
                        else
                        {
                            break;
                        }
                    }

                    j = i + 1;
                    char roomName[ROOM_NAME_SIZE];
                    strcpy(roomName, string);
                    i = getRoomIDbyName(string);
                    if (i == -1)
                    {
                        printf("Room \"%s\" doesn't exist\n", string);
                        write((*th_data).socket, "Room with typed name doesn't exist!\n", 37);
                        break;
                    }

                    if (findUserInRoom((*th_data).userID, i) == -1)
                    {
                        printf("User \"%s\" doesn't belong to \"%s\" room\n", users[(*th_data).userID].name, string);
                        write((*th_data).socket, "You can't remove user from this room, because you are not a member of this room!\n", 82);
                        break;
                    }

                    if (findUserInRoom((*th_data).userID, i) > 0)
                    {
                        printf("User \"%s\" is not an admin of \"%s\" room\n", users[(*th_data).userID].name, string);
                        write((*th_data).socket, "You can't remove user from this room, because you are not an admin of this room!\n", 82);
                        break;
                    }

                    memset(string, 0, sizeof(string));
                    for (k = 0; k < LOGIN_SIZE; k++)
                    {
                        c = (*th_data).fromClient[j + k];
                        if (c != '$')
                        {
                            string[l + k] = (*th_data).fromClient[j + k];
                        }
                        else
                        {
                            break;
                        }
                    }

                    int l = getUserIDbyName(string);
                    if (l == -1)
                    {
                        printf("User \"%s\" doesn't exist\n", string);
                        write((*th_data).socket, "User with typed name doesn't exist!\n", 37);
                        break;
                    }

                    j = findUserInRoom(l, i);
                    if (j == 0)
                    {
                        printf("User \"%s\" can't delete himself\n", users[(*th_data).userID].name);
                        write((*th_data).socket, "You can't remove yourself. If you want to leave and delete this room, please use option 3!\n", 92);
                        break;
                    }

                    if (j > 0)
                    {
                        printf("User \"%s\" removed user \"%s\" from \"%s\" room\n", users[(*th_data).userID].name, string, roomName);
                        write((*th_data).socket, "You have succesfully removed user from your room!\n", 51);
                        rooms[i].users[j] = -1;
                    }
                    break;

                    // case 'x': // Displaying info
                    //     printf("Users:\n");
                    //     for (i = 0; i < getFirstFreeUserSlot(); i++)
                    //     {
                    //         for (j = 0; j < LOGIN_SIZE; j++)
                    //         {
                    //             printf("%c", users[i].name[j]);
                    //         }
                    //         printf("\n");
                    //     }
                    //     printf("Rooms:\n");
                    //     for (i = 0; i < getFirstFreeRoomID(); i++)
                    //     {
                    //         for (j = 0; j < ROOM_NAME_SIZE; j++)
                    //         {
                    //             printf("%c", rooms[i].name[j]);
                    //         }
                    //         printf("\n");
                    //     }
                    //     break;
                }

                bytes = update_server_response();
                broadcast_server_response(bytes);
                pthread_mutex_unlock(&mutex);
            }
        }
        else
        {
            printf("User \"%s\" disconnected and deleted from server memory\n", users[(*th_data).userID].name);
            deleteUser((*th_data).userID);
            free(t_data);
            pthread_exit(NULL);
        }
    }
}

void handleConnection(int connectionSocketDescriptor)
{
    pthread_mutex_lock(&mutex);
    int freeSlot = getFirstFreeUserSlot();

    if (freeSlot != -1)
    {
        users[freeSlot].socket = connectionSocketDescriptor;
        write(users[freeSlot].socket, "You are connected to the server!\n", 33);
        write(users[freeSlot].socket, "[0] - Modify username\n[1] - Create new room\n[2] - Join room\n[3] - Leave room\n[4] - Send message\n[5] - Removing users\n[x] - Show info\n", 134);
        printf("Added new user to slot: %d / %d\n", freeSlot + 1, USER_SIZE);

        struct thread_data_t *t_data1;
        t_data1 = malloc(sizeof(struct thread_data_t));

        t_data1->socket = connectionSocketDescriptor;
        t_data1->userID = freeSlot;

        pthread_t thread1;
        if (pthread_create(&thread1, NULL, ThreadBehavior, (void *)t_data1))
        {
            fprintf(stderr, "Error while creating thread\n");
            exit(1);
        };
    }
    else
    {
        write(connectionSocketDescriptor, "At this moment there are to many users connected to the server. Please try again later!\n", 90);
        close(connectionSocketDescriptor);
        printf("New user can't be added due to slots limit\n");
    }

    pthread_mutex_unlock(&mutex);
}

int main(int argc, char *argv[])
{
    printf("Server is running\n");

    int serverSocketDescriptor = createSocket(SERVER_PORT);
    initStruct();

    while (serverRunning)
    {
        int connectionSocketDescriptor = accept(serverSocketDescriptor, NULL, NULL);
        if (connectionSocketDescriptor < 0)
        {
            fprintf(stderr, "Error while creating socket for connection\n");
            exit(1);
        }

        handleConnection(connectionSocketDescriptor);
    }

    close(serverSocketDescriptor);
    return (0);
}
