//Assignment 1 - FTP Client
//CECS 327
//Brendan McMahon, James Hall, Andrew Camarena

#include <iostream>        //cout
#include <string>
#include <stdio.h>         //printf
#include <stdlib.h>
#include <string.h>        //strlen
#include <sys/socket.h>    //socket
#include <arpa/inet.h>     //inet_addr
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
        cout << "by ip";
        sockaddr.sin_addr.s_addr =  inet_addr(host.c_str());
    } else {
        cout << "by name";
        hostent * record = gethostbyname(host.c_str());
        in_addr * addressptr = (in_addr *) record->h_addr;
        sockaddr.sin_addr = *addressptr;
    }
    if (connect(sock,(struct sockaddr *)&sockaddr,sizeof(struct sockaddr))==-1) {
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
        } while (count ==  BUFFER_LENGTH-1);
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
	do {
		count = recv(sock, buffer, BUFFER_LENGTH - 1, 0);
		buffer[count] = '\0';
		strReply += buffer;
	} while (count == BUFFER_LENGTH - 1);
	return strReply;
}

short PASV(int sockpi) {
    std::string response = requestReply(sockpi, "PASV\r\n");
	std::string ip = response.substr(response.find('('), response.length());
	ip.erase(std::remove(ip.begin(), ip.end(), ')'), ip.end());
	ip.erase(std::remove(ip.begin(), ip.end(), '('), ip.end());
	ip = ip.substr(0, ip.length() - 3);
	std::cout << ip << std::endl;
	for (int i = 0; i < ip.length(); i++)
		if (ip[i] == ',')
			ip[i] = '.';
	//ip.erase(ip.end()-1);
	std::cout << "Parsed IP: " << ip << std::endl;
	std::string port = ip.substr(15, ip.length());
	std::cout << "port stuff: "<< port << std::endl;
	short nport = std::stoi(port.substr(0, 3));
	short nport2 = std::stoi(port.substr(4, port.length()));
	std::bitset<16> bits = nport;
	std::bitset<16> bit = nport2;
	bits = bits << 8;
	bits = bits | bit;
	return (short)bits.to_ulong();
}

void LIST(int port, int sockpi) {
	std::string strReply;
	int nsock;
	nsock = createConnection("130.179.16.134", port);
	request(sockpi, "LIST \r\n");
	std::cout << reply(sockpi) << std::endl;
	std::cout << reply(nsock) << std::endl;
	request(nsock, "CLOSE \r\n");
	std::cout << reply(sockpi) << std::endl;
}
void RETR(int port, int sockpi) {
	int nsock;
	std::string file;
	std::cout << "Enter the name of the file (not a directory) with file extension: ";
	std::cin >> file;
	nsock = createConnection("130.179.16.134", port);
	request(sockpi, "RETR /"+file+"\r\n");
	std::cout << reply(sockpi) << std::endl;
	std::cout << reply(nsock) << std::endl;
	request(nsock, "CLOSE \r\n");
	std::cout << reply(sockpi) << std::endl;

}
void QUIT(int nsock) {
	request(nsock, "QUIT \r\n");
	std::cout << reply(nsock) << std::endl;
}
int main(int argc, char *argv[]) {
	int sockpi;
	std::string strReply;

	//TODO  arg[1] can be a dns or an IP address using gethostbyname.
	if (argc > 2){
		sockpi = createConnection(argv[1], atoi(argv[2]));
	}
	if (argc == 2) {
        sockpi = createConnection(argv[1], 21);
    } else {
        sockpi = createConnection("130.179.16.134", 21);
    }

	strReply = reply(sockpi);
	std::cout << strReply << std::endl;

	strReply = requestReply(sockpi, "USER anonymous\r\n");
	//TODO parse srtReply to obtain the status. Let the system act according to the status and display
	// friendly user to the user
	std::cout << strReply << std::endl;

	strReply = requestReply(sockpi, "PASS asa@asas.com\r\n");
	std::cout << strReply << std::endl;
	//TODO parse srtReply to obtain the status. Let the system act according to the status and display
	// friendly user to the user
	short port = PASV(sockpi);
	LIST(port, sockpi);
	port = PASV(sockpi);
	RETR(port, sockpi);
	QUIT(sockpi);

	system("Pause");

	//TODO implement PASV, LIST, RETR
	return 0;
}
