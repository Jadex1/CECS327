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

int createConnection(std::string host, int port) {
    int sock;
    struct sockaddr_in sockaddr;

    memset(&sockaddr,0, sizeof(sockaddr));
    sock = socket(AF_INET,SOCK_STREAM,0);
    sockaddr.sin_family=AF_INET;
    sockaddr.sin_port= htons(port);

    int a1,a2,a3,a4;
    if (sscanf(host.c_str(), "%d.%d.%d.%d", &a1, &a2, &a3, &a4 ) == 4) {
        sockaddr.sin_addr.s_addr =  inet_addr(host.c_str());
    } else {
        cout << "by name";
        hostent * record = gethostbyname(host.c_str());
        in_addr * addressptr = (in_addr *) record->h_addr;
        sockaddr.sin_addr = *addressptr;
    }
    if(connect(sock,(struct sockaddr *)&sockaddr,sizeof(struct sockaddr))==-1) {
        perror("connection fail");
        exit(1);
        return -1;
    }
    return sock;
}

std::string requestReply(int sock, std::string message) {
    char buffer[BUFFER_LENGTH];
    std::string reply;
    int count = send(sock, message.c_str(), message.size(), 0);
    if (count > 0) {
        usleep(1000);
        do {
            count = recv(sock, buffer, BUFFER_LENGTH-1, 0);
            buffer[count] = '\0';
            reply += buffer;
        }while (count ==  BUFFER_LENGTH-1);
    }
    return buffer;
}

int request(int sock, std::string message) {
    char buffer[BUFFER_LENGTH];
    std::string reply;
    return send(sock, message.c_str(), message.size(), 0);
}

std::string reply(int sock) {
    std::string strReply;
    int count;
    char buffer[BUFFER_LENGTH];
    usleep(1000);
    do {
        count = recv(sock, buffer, BUFFER_LENGTH-1, 0);
        buffer[count] = '\0';
        strReply += buffer;
    }while (count ==  BUFFER_LENGTH-1);
    return strReply;
}

int responseToPort(string response) {
    int parenIndex = static_cast<int>(response.find("("));
    string parsedIP, strReply;
    uint16_t a, b, c, d, e, f, first,second;

    response = response.substr(parenIndex+1,static_cast<int>(response.size()));
    int responseSize = static_cast<int>(response.find(")"));
    std::replace(response.begin(), response.end(), ',', '.');
    parsedIP = response.substr(0,responseSize);
    sscanf(parsedIP.c_str(), "%hu.%hu.%hu.%hu.%hu.%hu.", &a, &b, &c, &d, &e, &f);
    first = e << 8;
    second = f;
    uint16_t port = first | second;

    return port;
}

string responseToIp(string response) {
    int parenIndex = static_cast<int>(response.find("("));
    string parsedIP, strReply;
    int a1,a2,a3,a4;
    char buffer[30];

    response = response.substr(parenIndex+1,static_cast<int>(response.size()));
    int responseSize = static_cast<int>(response.find(")"));
    std::replace(response.begin(), response.end(), ',', '.');
    parsedIP = response.substr(0,responseSize);
    sscanf(parsedIP.c_str(), "%d.%d.%d.%d", &a1, &a2, &a3, &a4 );
    sprintf(buffer, "%d.%d.%d.%d",a1,a2,a3,a4);
    return buffer;
}

int PASV(int sockpi) {
    string strReply = requestReply(sockpi, "PASV\r\n");
    return createConnection(responseToIp(strReply),responseToPort(strReply));
}
void LIST(int sockpi) {
  int sockdtp = PASV(sockpi);
  request(sockpi, "LIST /\r\n");
  cout << "Server response: " << reply(sockpi) << endl;
  cout << "DTP response:" << endl << reply(sockdtp) << endl;
  request(sockdtp,"CLOSE \r\n");
  cout << "Server response: " << reply(sockpi) << endl;
}
void RETR(int sockpi) {
  string filename;
  cout << "Enter the name of the File you wish to retrieve" << endl;
  cin >> filename;
  int sockdtp = PASV(sockpi);
  request(sockpi, "RETR "+filename+"\r\n");
  cout << "Server response: " << reply(sockpi) << endl;
  cout << "DTP response:" << reply(sockdtp) << endl;
  request(sockdtp,"CLOSE \r\n");
  cout << "Server response: " << reply(sockpi) << endl;
}
void QUIT(int sockpi) {
    cout << requestReply(sockpi, "QUIT\r\n");
}
int main(int argc , char *argv[]) {
    int sockpi,sockdtp;
    std::string strReply;
    std::string myinput;

    //TODO  arg[1] can be a dns or an IP address using gethostbyname.
    if (argc > 2){ }
        sockpi = createConnection(argv[1], atoi(argv[2]));
    }
    if (argc == 2){
        sockpi = createConnection(argv[1], 21);
    } else {
        sockpi = createConnection("130.179.16.134", 21);
    }

    strReply = reply(sockpi);
    cout << strReply  << endl;

    strReply = requestReply(sockpi, "USER anonymous\r\n");
    //TODO parse srtReply to obtain the status. Let the system act according to the status and display
    // friendly user to the user
    cout << strReply  << endl;

    strReply = requestReply(sockpi, "PASS asa@asas.com\r\n");
    cout << strReply  << endl;
    usleep(3000);
    cout << reply(sockpi);

    //TODO parse srtReply to obtain the status. Let the system act according to the status and display
    // friendly user to the user

    cout << "Please enter a command: (ls,passive,quit,get)" << endl;

    while (true) {
        cin >> myinput;
        //LIST
        if (myinput == "ls") {
            LIST(sockpi);
        } else if (myinput == "get") {
        //RETR
            RETR(sockpi);
        } else if(myinput == "quit") {
            QUIT(sockpi);
            return 0;
        }
    }
}
