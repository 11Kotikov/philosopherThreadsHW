package org.homework;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Round {
    private volatile int round;
    private final int maxCounter;

    public Round(int maxCounter) {
        this.round = 0;
        this.maxCounter = maxCounter;
    }

    public int getRound() {
        return round;
    }

    public synchronized void next() {
        round = (round + 1) % maxCounter;
        notifyAll();
    }
}

class Philosopher implements Runnable {
    private final int maxPhilosophers;
    private int eatCounter;
    private final String name;
    private final Round round;
    private final int id;
    private final Lock leftFork;
    private final Lock rightFork;

    public Philosopher(String name, int id, final int maxPhilosophers, final Round round) {
        this.name = name;
        this.round = round;
        this.maxPhilosophers = maxPhilosophers;
        this.id = id;
        this.eatCounter = 0;
        this.leftFork = new ReentrantLock();
        this.rightFork = new ReentrantLock();
    }

    @Override
    public void run() {
        try {
            while (true) {
                think();
                takeForks();
                eat();
                putForks();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void think() throws InterruptedException {
        System.out.println(name + " размышляет");
        Thread.sleep(1000); // время размышлений
    }

    private void takeForks() throws InterruptedException {
        roundWait();
        leftFork.lock();
        rightFork.lock();
        System.out.println(name + " взял вилки");
    }

    private void eat() throws InterruptedException {
        System.out.println(name + " поел " + (++eatCounter) + " раз");
        Thread.sleep(2000); // время еды
    }

    private void putForks() {
        leftFork.unlock();
        rightFork.unlock();
        System.out.println(name + " положил вилки");
        round.next();
    }

    private void roundWait() throws InterruptedException {
        while (round.getRound() != id) {
            synchronized (round) {
                round.wait();
            }
        }
    }
}

public class PhilosopherRunner {
    public static void main(String[] args) {
        final int numberOfPhilosophers = 5;
        String[] names = {
                "Аристотель", "Пифагор", "Платон", "Сократ", "Цицерон"
        };

        Thread[] philosophers = new Thread[numberOfPhilosophers];
        Round round = new Round(numberOfPhilosophers);

        for (int i = 0; i < numberOfPhilosophers; ++i) {
            philosophers[i] = new Thread(new Philosopher(names[i], i, numberOfPhilosophers, round));
        }

        for (Thread philosopher : philosophers) {
            philosopher.start();
        }
    }
}
