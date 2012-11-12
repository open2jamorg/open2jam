package com.github.dtinth.partytime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Thai Pangsakulyanont
 */
public class Client implements Runnable {
    private final int port;
    private final String host;
    
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private String status;
    
    private boolean ready = false;
    private long ownLatency;
    
    public Client(String host, int port, long ownLatency) {
        this.host = host;
        this.port = port;
        this.ownLatency = ownLatency;
    }

    @Override
    public void run() {
        try {
            setStatus("Connecting...");
            socket = new Socket(host, port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            setStatus("Connected!");
            handle();
            if (!socket.isClosed()) socket.close();
        } catch (UnknownHostException ex) {
            setStatus("Unknown host: " + ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ex) {
            setStatus("IO Exception: " + ex.getMessage());
            ex.printStackTrace();
        }
        ready = true;
    }

    private String read() throws IOException {
        String line = reader.readLine();
        if (line != null) {
            if (line.startsWith("status:")) {
                setStatus("remote: " + line.trim().substring(7));
            }
        }
        return line;
    }

    private void setStatus(String status) {
        this.status = status;
        System.out.println(status);
    }

    public String getStatus() {
        return status;
    }

    public boolean isReady() {
        return ready;
    }

    private void handle() throws IOException {
        for (;;) {
            String line = read();
            if (line == null) return;
            line = line.trim();
            if (line.equals("synced")) break;
            if (line.equals("time?")) {
                writer.println(System.currentTimeMillis());
            }
        }
        for (;;) {
            String line = read();
            if (line == null) return;
            line = line.trim();
            if (line.startsWith("play:")) {
                long time = Long.parseLong(line.substring(5));
                try {
                    Thread.sleep(Math.max(0, time - System.currentTimeMillis() - ownLatency));
                    setStatus("Game start!");
                } catch (InterruptedException ex) {
                    setStatus("Interrupted!");
                }
                return;
            }
        }
    }
    
}
