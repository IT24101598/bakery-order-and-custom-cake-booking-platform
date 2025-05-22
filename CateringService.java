package com.example.bakery.service;

import com.example.bakery.model.Catering;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class CateringService {

    // File path where Catering records are stored persistently
    private static final String FILE_PATH = "data/catering.txt";

    // Internal linked list node to hold Catering data
    private static class Node {
        Catering data;
        Node next;

        Node(Catering data) {
            this.data = data;
        }
    }

    // Head node of the linked list
    private Node head;

    /**
     * Called automatically after the bean is initialized by Spring.
     * Loads Catering data from file into memory by parsing each line into Catering objects.
     */
    @PostConstruct
    public void init() throws IOException {
        File file = new File(FILE_PATH);
        if (!file.exists()) return; // If no file exists, start with empty list

        // Read all lines from the file
        List<String> lines = Files.readAllLines(Paths.get(FILE_PATH));

        for (String line : lines) {
            String[] p = line.split(",");

            // Only process lines with enough fields to avoid runtime errors
            if (p.length >= 4) {
                Catering t = new Catering(
                        p[0].trim(),                      // ID
                        p[1].trim(),                      // Title
                        p[2].trim(),                      // Description
                        Double.parseDouble(p[3].trim())   // Price
                );

                // Add to in-memory linked list
                append(t);
            } else {
                System.err.println("Skipping invalid line: " + line); // Skip invalid line
            }
        }
    }

    /**
     * Determines the next unique numeric ID for a new Catering item by traversing the list.
     */
    public synchronized String getNextId() {
        int nextId = 1;
        Node curr = head;

        // Find the highest existing ID in the list and increment it
        while (curr != null) {
            int currentId = Integer.parseInt(curr.data.getId());
            nextId = Math.max(nextId, currentId + 1);
            curr = curr.next;
        }

        return String.valueOf(nextId);
    }

    /**
     * Adds a new Catering object to the system, assigns it a unique ID, and persists it to file.
     */
    public synchronized void addCatering(Catering t) throws IOException {
        String nextId = getNextId();
        t.setId(nextId);
        append(t);

        File file = new File(FILE_PATH);
        file.getParentFile().mkdirs(); // Ensure data directory exists

        // Append new Catering entry to file
        try (BufferedWriter w = new BufferedWriter(new FileWriter(file, true))) {
            w.write(csv(t));
            w.newLine();
        }
    }

    /**
     * Returns all Catering objects stored in the linked list.
     */
    public List<Catering> getAll() {
        List<Catering> list = new ArrayList<>();
        Node curr = head;
        while (curr != null) {
            list.add(curr.data);
            curr = curr.next;
        }
        return list;
    }

    /**
     * Retrieves a Catering object by its ID from the in-memory list.
     */
    public synchronized Catering getById(String id) {
        Node curr = head;
        while (curr != null) {
            if (curr.data.getId().equals(id)) return curr.data;
            curr = curr.next;
        }
        return null; // Not found
    }

    /**
     * Updates an existing Catering record by matching ID, then overwrites the file with the updated list.
     */
    public synchronized void updateCatering(Catering t) throws IOException {
        Node curr = head;
        while (curr != null && !curr.data.getId().equals(t.getId())) {
            curr = curr.next;
        }

        if (curr == null) return; // No matching record found

        // Update in-memory record fields
        curr.data.setTitle(t.getTitle());
        curr.data.setDescription(t.getDescription());
        curr.data.setPrice(t.getPrice());

        // Persist updated list to file
        overwrite();
    }

    /**
     * Deletes a Catering record by ID and saves the updated list to the file.
     */
    public synchronized boolean deleteById(String id) throws IOException {
        if (head == null) return false;

        // Special case: deleting the head node
        if (head.data.getId().equals(id)) {
            head = head.next;
        } else {
            Node prev = head, curr = head.next;

            // Traverse to find the node to delete
            while (curr != null && !curr.data.getId().equals(id)) {
                prev = curr;
                curr = curr.next;
            }

            if (curr == null) return false; // Not found

            // Remove node from list
            prev.next = curr.next;
        }

        // Save updated list to file
        overwrite();
        return true;
    }

    // ======================== Helper Methods ========================

    /**
     * Adds a new Catering node to the end of the linked list.
     */
    private void append(Catering t) {
        Node node = new Node(t);
        if (head == null) {
            head = node;
        } else {
            Node curr = head;
            while (curr.next != null) curr = curr.next;
            curr.next = node;
        }
    }

    /**
     * Rewrites the entire file with the current contents of the linked list.
     * Used after update or delete operations to keep file in sync.
     */
    private void overwrite() throws IOException {
        File file = new File(FILE_PATH);
        file.getParentFile().mkdirs();

        try (BufferedWriter w = new BufferedWriter(new FileWriter(file, false))) {
            for (Catering t : getAll()) {
                w.write(csv(t));
                w.newLine();
            }
        }
    }

    /**
     * Formats a Catering object into a CSV line.
     * Replaces commas in text fields to prevent CSV parsing issues.
     */
    private String csv(Catering t) {
        return String.join(",",
                t.getId(),
                t.getTitle().replace(",", " "),         // Replace commas in title
                t.getDescription().replace(",", " "),   // Replace commas in description
                String.valueOf(t.getPrice()));          // Convert price to string
    }
}
