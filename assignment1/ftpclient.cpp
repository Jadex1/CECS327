//Assignment 1 - FTP Client
//CECS 327
//Brendan McMahon

#include <iostream>    //cout
#include <string>
#include <stdio.h> //printf
#include <stdlib.h>
#include <string.h>    //strlen
#include <sys/socket.h>    //socket
#include <arpa/inet.h> //inet_addr
#include <netinet/in.h>
#include <sys/types.h>
#include <unistd.h>
#include <netdb.h>

using namespace std;

#define BUFFER_LENGTH 2048



int createConnection(std::string host, int port)
{
    int sock;
    struct sockaddr_in sockaddr;

    memset(&sockaddr,0, sizeof(sockaddr));
    sock = socket(AF_INET,SOCK_STREAM,0);
    sockaddr.sin_family=AF_INET;
    sockaddr.sin_port= htons(port);

    int a1,a2,a3,a4;
    if (sscanf(host.c_str(), "%d.%d.%d.%d", &a1, &a2, &a3, &a4 ) == 4)
    {
        cout << "by ip";
        sockaddr.sin_addr.s_addr =  inet_addr(host.c_str());
    }
    else {
        cout << "by name";
        hostent * record = gethostbyname(host.c_str());
        in_addr * addressptr = (in_addr *) record->h_addr;
        sockaddr.sin_addr = *addressptr;
    }
    if(connect(sock,(struct sockaddr *)&sockaddr,sizeof(struct sockaddr))==-1)
    {
        perror("connection fail");
        exit(1);
        return -1;
    }
    return sock;
}

std::string requestReply(int sock, std::string message)
{
    char buffer[BUFFER_LENGTH];
    std::string reply;
    int count = send(sock, message.c_str(), message.size(), 0);
    if (count > 0)
    {
        usleep(1000);
        do {
            count = recv(sock, buffer, BUFFER_LENGTH-1, 0);
            buffer[count] = '\0';
            reply += buffer;
        }while (count ==  BUFFER_LENGTH-1);
    }
    return buffer;
}


int request(int sock, std::string message)
{
    char buffer[BUFFER_LENGTH];
    std::string reply;
    return send(sock, message.c_str(), message.size(), 0);
}

std::string reply(int sock)
{
    std::string strReply;
    int count;
    char buffer[BUFFER_LENGTH];

    do {
        count = recv(sock, buffer, BUFFER_LENGTH-1, 0);
        buffer[count] = '\0';
        strReply += buffer;
    }while (count ==  BUFFER_LENGTH-1);
    return strReply;
}

int main(int argc , char *argv[])
{
    int sockpi;
    std::string strReply;
    std::string myinput;

    //TODO  arg[1] can be a dns or an IP address using gethostbyname.
    if (argc > 2)
    {
        sockpi = createConnection(argv[1], atoi(argv[2]));
    }
    if (argc == 2)
        sockpi = createConnection(argv[1], 21);
    else
        sockpi = createConnection("130.179.16.134", 21);
    strReply = reply(sockpi);
    cout << strReply  << endl;


    strReply = requestReply(sockpi, "USER anonymous\r\n");
    //TODO parse srtReply to obtain the status. Let the system act according to the status and display
    // friendly user to the user
    cout << strReply  << endl;

    strReply = requestReply(sockpi, "PASS asa@asas.com\r\n");
    cout << strReply  << endl;
    strReply = requestReply(sockpi, "USER anonymous\r\n");
    cout << strReply  << endl;
    //TODO parse srtReply to obtain the status. Let the system act according to the status and display
    // friendly user to the user



    cout << "Please enter a command: (ls,passive,quit,get)" << endl;

    while(cin >> myinput)
    {


        strReply = reply(sockpi);//clear the socket

        //PASV
        if(myinput == "passive")
        {
            strReply = requestReply(sockpi, "PASV\r\n");
            cout << strReply;
        }
        //LIST
        if(myinput == "ls")
        {
            strReply = requestReply(sockpi, "LIST\r\n");
            cout << "List reply:" << strReply << endl;
        }
        //RETR
        if(myinput == "get")
        {
            strReply = requestReply(sockpi, "RTR\r\n");
            cout << strReply;
        }
    }
    //TODO implement PASV, LIST, RETR


    return 0;
}
