/*
Name: Nikolas Manuel
Date: 04/12/2023
Description: Simple number guessing game. The server maintains the "number guessing game" that clients will connect to. 
The server picks a number and the client will send guesses. The server will send feedback
Usage: To use the program first run the server. Then run the client to connect to the server enter your name and start guessing. 
if you want to exit the game type -1.
*/



import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

public class NumberGuessingServer {
    private int port;
    private int hiddenNumber;
    private ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();
    private ClientHandler winner = null;
    
    //Default Constructor
    public NumberGuessingServer() {
        this.port = 8888;
    }

    public NumberGuessingServer(int port) {
        this.port = port;
    }

    /*
     * Method listens for client on this.port
     * it will send a message
     * go back to listening
     */
    public void go() {

        ServerSocket serverSocket = null;
        Random random = new Random();
        this.hiddenNumber = random.nextInt(1000000) + 1;

        try {
            serverSocket = new ServerSocket(this.port);
            System.out.println("Server started on port " + this.port);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            while (true) {
            	System.out.println("Waiting for client...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected");
                ClientHandler client = new ClientHandler(clientSocket);
                clients.add(client);
                
                // Set the name of the client
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String name = reader.readLine();
                client.setName(name);
                // Start a new thread to handle the client's request
                client.start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //Send a message to all clients connected to the server
    public synchronized void sendToAll() {
        for (ClientHandler client : clients) {
        	if (client == winner) {
        		client.sendMessage("You are the winner!! A new round has started.");
        	} else {
        		client.sendMessage("The winner is " + winner.getName() + ". A new round has started.");
        	}
        }
    }

    //Generates new number for the game
    public synchronized void redoNumber() {
        Random random = new Random();
        this.hiddenNumber = random.nextInt(1000000) + 1;
    }

    class ClientHandler extends Thread {

        private Socket clientSocket;
        private PrintWriter writer;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            
        }

        public void run() {
        	try {
        		//Reads input from client
        		BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        		//Sends output to the client
        	    writer = new PrintWriter(clientSocket.getOutputStream(), true);
        	    writer.println("Welcome to the Number Guessing Game! To exit the game type -1");
        	    
        	    while (true) {
        	        String input = reader.readLine();
        	        //Handles null input
        	        if (input == null) {
        	            System.out.println("Input is null, please try again.");
        	            continue;
        	        }
        	        //Exits the program
        	        if (input.equals("-1")) {
        	            System.out.println("Exiting program.");
        	            break;
        	        }
        	        try {
        	        	int guess = Integer.parseInt(input);
        	        	if (guess == hiddenNumber) {
        	        		writer.println("Your number: " + guess + " was Correct!!");
        	        		winner = this;
        	        		sendToAll();
        	        		redoNumber();
        	        		writer.println("Guess a number between 1 and 1000000:");
        	        	} else if (guess < hiddenNumber) {
        	        		writer.println("Your number: " + guess + " was Too Low");
        	        	} else {
        	        		writer.println("Your number: " + guess + " was Too High");
        	        	}
        	        }catch (NumberFormatException e) {
                      writer.println("Invalid input. Please enter a valid number.");
        	        }
        	    }
        	}catch (IOException e) {
        	    System.out.println("Error handling client: " + e);
        	} finally {
        	    try {
        	        clientSocket.close();
        	    } catch (IOException e) {
        	        System.out.println("Error closing client socket: " + e);
        	        }
        	    }
        	}
        	//Method to send a message to the client
        	public void sendMessage(String message) {
        		writer.println(message);
        	}
    	}
    public static void main(String[] args) {
    	
    	NumberGuessingServer server = new NumberGuessingServer();
    	server.go();
    }
}




