package com.example.banking.utils;

import com.example.banking.model.Customer;

public class SessionManager {
    private static SessionManager instance;
    private Customer loggedInCustomer;
    private Thread timerThread;
    private boolean active;
    private final long SESSION_TIMEOUT = 5 * 60; // 10 minutes in seconds

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // Start session with background thread
    public void startSession(Customer customer) {
        this.loggedInCustomer = customer;
        this.active = true;

        if (timerThread != null && timerThread.isAlive()) {
            timerThread.interrupt(); // stop previous session
        }

        timerThread = new Thread(() -> {
            try {
                Thread.sleep(SESSION_TIMEOUT * 1000); // wait 10 minutes
                if (active) {
                    endSession();
                }
            } catch (InterruptedException ignored) {}
        });
        timerThread.setDaemon(true); // won’t block JVM exit
        timerThread.start();

        System.out.println("✅ Session is valid for 5 minutes.");
    }

    public void endSession() {
        this.loggedInCustomer = null;
        this.active = false;
        if (timerThread != null) {
            timerThread.interrupt();
        }
    }

    public boolean isActive() {
        return !active || loggedInCustomer == null;
    }
}

