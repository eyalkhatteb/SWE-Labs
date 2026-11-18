package com.example.swelabs;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * One failed login of an existing email, waiting in the queue for a FailureWorker.
 * It carries a small queue of its own (size 1) for the answer, so the LoginTask
 * that sent it knows at once whether this failure caused a block.
 */
public class FailedAttempt {
    private final String email;
    private final BlockingQueue<LoginResult> answer = new ArrayBlockingQueue<>(1);

    public FailedAttempt(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    // Called by the FailureWorker after it has counted the failure.
    public void answer(LoginResult result) throws InterruptedException {
        answer.put(result);
    }

    // Called by the LoginTask. The thread sleeps here until the worker answers.
    public LoginResult waitForAnswer() throws InterruptedException {
        return answer.take();
    }
}
