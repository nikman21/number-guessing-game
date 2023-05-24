/*
Name: Nikolas Manuel
Date: 04/12/2023
Description: Simple number guessing game. The server maintains the "number guessing game" that clients will connect to. 
The server picks a number and the client will send guesses. The server will send feedback
Usage: To use the program first run the server. Then run the client to connect to the server enter your name and start guessing. 
if you want to exit the game type -1.
*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NumberGuessingClient {
    private String name;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    public NumberGuessingClient(String serverAddress, int port) {
        try {
        	 // Create a new socket and input/output streams for communication with the server
            socket = new Socket(serverAddress, port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            System.out.println("Connected to server on port " + socket.getPort());
            setName();// Set the name of the client
            setupServerListener(); // Start listening for messages from the server
            setupClientInput(); // Start reading input from the user and sending it to the server
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setName() throws IOException {
    	System.out.println("Enter your name:");
        BufferedReader nameReader = new BufferedReader(new InputStreamReader(System.in));
        String input = nameReader.readLine();
        while (input == null || input.trim().isEmpty()) {
            System.out.println("Please enter a valid name:");
            input = nameReader.readLine();
        }
        name = input.trim();
        writer.println(name); //Send the name to the server
    }
    public String getName()
    {
    	return name;
    }
    //Listens for messages from the server
    private void setupServerListener() {
        Thread listener = new Thread(new Runnable() {
            public void run() {
                try {
                    while (true) {
                        String message = reader.readLine();
                        if (message == null) {
                            return;
                        }
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        listener.start(); //Start the thread
    }
    
    // Method to take user input and send it to the server.
    private void setupClientInput() {
        Thread input = new Thread(new Runnable() {
            public void run() {
                try {
                	System.out.print("Guess a number between 1 and 1000000: ");
                    BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
                    while (true) {
                        String guessString = inputReader.readLine();
                        if (guessString == null) {
                            System.out.println("Input is null, please try again.");
                            continue;
                        }
                        if (guessString.equals("-1")) {
                            System.out.println("Exiting program.");
                            System.exit(0);
                        }
                        if (!guessString.isEmpty()) {
                            try {
                                int guess = Integer.parseInt(guessString);
                                writer.println(guess);
                            } catch (NumberFormatException ex) {
                            	writer.println("Invalid input. Please enter a valid number.");
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        input.start(); // starts the thread
    }
    public static void main(String[] args) {
        NumberGuessingClient client = new NumberGuessingClient("localhost", 8888);
        client.start();
    }
}

