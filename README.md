# P2P-Chat

## Introduction
This is an exercise in distributed systems learning.This project designed a client/server structure of the chat room system.Realized the local multi-client group chat, private chat, view instructions and kick the function.

## Technoloty
- JFrame
- Socket

## Using
* Server
  1. Start the Server program in the Server via Server.java, and then see the dialog box showing the Server online time is completed. 
  2. In the server input box can enter the client user name to achieve the specified user kick
  3. After the client connects, there is a record on the server

* Client
  1. Once the server is online, you can start the program using Register.java.To log in, enter a user name in the registration dialog box.Click on the avatar to view specific information about the program and the author
  2. In the chat interface, enter content in the input box and click Send, which defaults to group chat and everyone in the chat room will see it
  3. Enter the user name - message content to achieve private chat
  4. Enter the user name -state to view the instruction record for the specified user
