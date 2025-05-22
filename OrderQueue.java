package com.example.bakery.service;

import com.example.bakery.model.OrderRequest;

/**
 * OrderQueue is a circular queue implementation designed to store and manage OrderRequest objects.
 * This queue handles bakery order requests in a First-In-First-Out (FIFO) fashion.
 */
public class OrderQueue {
    private int maxSize;                 // Maximum number of elements the queue can hold
    private OrderRequest[] queueArray;  // Array to store order requests
    private int front;                  // Index of the front item (next to be removed)
    private int rear;                   // Index of the rear item (last inserted)
    private int nItems;                 // Current number of items in the queue

    /**
     * Constructor to initialize the queue with a fixed size.
     * @param size The maximum size of the queue
     */
    public OrderQueue(int size) {
        maxSize = size;
        queueArray = new OrderRequest[maxSize];
        front = 0;
        rear = -1;
        nItems = 0;
    }

    /**
     * Inserts a new OrderRequest into the queue.
     * If the queue is full, insertion is not allowed.
     * @param orderRequest The order request to be inserted
     */
    public void insert(OrderRequest orderRequest) {
        if (isFull()) {
            System.out.println("Queue is full. Cannot add more OrderRequests.");
            return;
        }

        // If rear has reached the end of the array, wrap it around to the beginning
        if (rear == maxSize - 1) {
            rear = -1;
        }

        // Increment rear and insert the new order
        queueArray[++rear] = orderRequest;
        nItems++;
    }

    /**
     * Removes and returns the OrderRequest from the front of the queue.
     * If the queue is empty, returns null.
     * @return The OrderRequest at the front, or null if queue is empty
     */
    public OrderRequest remove() {
        if (isEmpty()) {
            return null;
        }

        // Retrieve the front item and clear the reference
        OrderRequest temp = queueArray[front];
        queueArray[front] = null;

        // Move front pointer to the next element, wrapping around if needed
        if (front == maxSize - 1) {
            front = 0;
        } else {
            front++;
        }

        nItems--;
        return temp;
    }

    /**
     * Returns the OrderRequest at the front of the queue without removing it.
     * Useful for peeking at the next item to be processed.
     * @return The front OrderRequest or null if the queue is empty
     */
    public OrderRequest peekFront() {
        if (isEmpty()) {
            return null;
        }
        return queueArray[front];
    }

    /**
     * Checks if the queue is currently empty.
     * @return true if there are no items in the queue
     */
    public boolean isEmpty() {
        return nItems == 0;
    }

    /**
     * Checks if the queue is currently full.
     * @return true if the queue has reached its maximum capacity
     */
    public boolean isFull() {
        return nItems == maxSize;
    }

    /**
     * Displays all elements in the queue from front to rear.
     * This is mainly used for debugging or admin purposes.
     */
    public void displayQueue() {
        if (isEmpty()) {
            System.out.println("Queue is empty.");
            return;
        }

        System.out.print("Queue elements: ");
        int count = 0;
        int index = front;

        while (count < nItems) {
            System.out.print(queueArray[index] + " ");
            index = (index + 1) % maxSize;  // Handle circular wrap-around
            count++;
        }
        System.out.println();
    }

    /**
     * Retrieves all current OrderRequest elements in the queue as an array.
     * This can be used for reporting or batch processing of orders.
     * @return An array of OrderRequest objects in the queue
     */
    public OrderRequest[] getAllElements() {
        OrderRequest[] result = new OrderRequest[nItems];
        int count = 0;
        int index = front;

        while (count < nItems) {
            result[count] = queueArray[index];
            index = (index + 1) % maxSize;  // Maintain circular structure
            count++;
        }

        return result;
    }
}
