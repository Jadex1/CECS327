Assignment 2
Distributed-Chat in Java
James Hall, Brendan McMahon

I, James Hall, pledge that this is my own independent work, which conforms to the guidelines of academic honesty as describted in the course syllabus.
I, Brendan McMahon, pledge that this is my own independent work, which conforms to the guidelines of academic honesty as describted in the course syllabus.
I, Andrew Camarena, pledge that this is my own independent work, which conforms to the guidelines of academic honesty as describted in the course syllabus.

###Objectives and Project Description

The objectives of this project are:
1) Understand teh use of client and server sockets in Java.
2) Identify the advantages of overlay networks.
3) Differentiate the role of server and client. 
4) Integrate threads in network programming.

The task is to develop a distributed chat without a central coordinators. The processors will form an overlay topology known as a ring. In other words, every processor i will keep only the rightNode succ(i) and pred(i) as shown in Figure 1.
When a user i wants to talk with a friend j it sends a PUT message to the rightNode. PUT consists of the source id, the destination id as well as the text. We assume that each user knows the id of the friends.
A user that wants to join the system sends a JOIN message to a current participant, say i (if there is any participant, it will act as the only partic- ipant). Let j be the process that send the JOIN to i and let i + 1 be the rightNode of i. When i receives the request, it updates its routing table by replacing its leftNode with j and response with an ACCEPT message that contains the ip and port of the previous node in the ring.
When a user wants to leave the room, it sends a LEAV E message to the rightNode with the id, ip and port of the leftNode so that the nodes can reconstruct the ring.

### MESSAGES 

### FLOODING

The communication is based on a flooding protocol.

PROTOCOL Flooding. 
	Status Values : S = {INITIATOR, IDLE, DONE};
	$S {INIT} = {INITIATOR, IDLE};
	$S {TERM} = {DONE}$
	REstrictions : Total Reliability, Connectivity, and Unique Initiator.
INITATOR
	Spontaneously
	begin
	   send(M) to N(x);
	   become DONE:
	end
IDLE
	Receiving(I)
	being
	   Process(M);
	   send(M) to N(x) - {sender};
	   become DONE;
	end

