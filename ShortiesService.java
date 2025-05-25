package com.example.bakery.service;

import com.example.bakery.model.Shorties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service  // Marks this class as a Spring service component for business logic handling
public class ShortiesService {

    private static final String FILE_PATH = "data/shorties.txt";  // Path to the file storing Shorties item data

    // Linked list node to store Shorties data
    private static class Node {
        Shorties data;
        Node next;

        Node(Shorties data) {
            this.data = data;
        }
    }

    private Node head;  // Head of the linked list

    @PostConstruct
    public void init() throws IOException {
        // Load Shorties data from file into the linked list during service initialization
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        List<String> lines = Files.readAllLines(Paths.get(FILE_PATH));  // Read file line by line

        for (String line : lines) {
            String[] p = line.split(",");  // Split CSV line into fields
            Shorties s = new Shorties(
                    p[0].trim(),                      // ID
                    p[1].trim(),                      // Item name
                    Integer.parseInt(p[2].trim()),    // Quantity
                    Double.parseDouble(p[3].trim()),  // Price
                    p[4].trim()                       // Image path
            );
            append(s);  // Append item to the linked list
        }
    }

    // Generate next unique ID for a new Shorties item
    public synchronized String getNextId() {
        if (head == null) return "1";

        Node curr = head;
        while (curr.next != null) curr = curr.next;  // Traverse to the last node
        return String.valueOf(Integer.parseInt(curr.data.getId()) + 1);  // Increment last ID
    }

    // Add a new Shorties item to the list and persist it to the file
    public synchronized void addShorties(Shorties s) throws IOException {
        append(s);  // Add to linked list

        File file = new File(FILE_PATH);
        file.getParentFile().mkdirs();  // Ensure the directory structure exists

        try (BufferedWriter w = new BufferedWriter(new FileWriter(file, true))) {
            w.write(csv(s));  // Write item as CSV
            w.newLine();
        }
    }

    // Retrieve all Shorties items sorted by price (ascending)
    public synchronized List<Shorties> getSortedByPrice() {
        List<Shorties> shorties = getAll();  // Load all items
        bubbleSortByPrice(shorties);  // Sort using bubble sort
        return shorties;
    }

    // Read all Shorties items from file and return as a list
    public synchronized List<Shorties> getAll() {
        List<Shorties> list = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(FILE_PATH));

            for (String line : lines) {
                String[] p = line.split(",");
                Shorties s = new Shorties(
                        p[0].trim(),
                        p[1].trim(),
                        Integer.parseInt(p[2].trim()),
                        Double.parseDouble(p[3].trim()),
                        p[4].trim()
                );
                list.add(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Retrieve a Shorties item by its ID
    public synchronized Shorties getById(String id) {
        Node curr = head;
        while (curr != null) {
            if (curr.data.getId().equals(id)) return curr.data;  // Found match
            curr = curr.next;
        }
        return null;  // Not found
    }

    // Update an existing Shorties item and overwrite the file with new list
    public synchronized void updateShorties(Shorties s) throws IOException {
        Node curr = head;

        // Find the node with the same ID
        while (curr != null && !curr.data.getId().equals(s.getId())) {
            curr = curr.next;
        }

        if (curr == null) return;  // Item not found

        // Update fields
        curr.data.setItemName(s.getItemName());
        curr.data.setQuantity(s.getQuantity());
        curr.data.setPrice(s.getPrice());
        curr.data.setImagePath(s.getImagePath());

        overwrite();  // Save entire list to file
    }

    // Delete a Shorties item by its ID and update the file
    public synchronized boolean deleteById(String id) throws IOException {
        if (head == null) return false;

        // Special case: deleting head node
        if (head.data.getId().equals(id)) {
            head = head.next;
        } else {
            Node prev = head, curr = head.next;

            // Traverse to find the node to delete
            while (curr != null && !curr.data.getId().equals(id)) {
                prev = curr;
                curr = curr.next;
            }

            if (curr == null) return false;  // Item not found
            prev.next = curr.next;  // Unlink node
        }

        overwrite();  // Save updated list to file
        return true;
    }

    // ---- Helpers ----

    // Append a Shorties item to the end of the linked list
    private void append(Shorties s) {
        Node node = new Node(s);
        if (head == null) head = node;
        else {
            Node curr = head;
            while (curr.next != null) curr = curr.next;
            curr.next = node;
        }
    }

    // Overwrite the Shorties file with current linked list data
    private void overwrite() throws IOException {
        File file = new File(FILE_PATH);
        file.getParentFile().mkdirs();  // Ensure directories exist

        try (BufferedWriter w = new BufferedWriter(new FileWriter(file, false))) {
            Node curr = head;
            while (curr != null) {
                w.write(csv(curr.data));  // Write item in CSV format
                w.newLine();
                curr = curr.next;
            }
        }
    }

    // Convert a Shorties object to a CSV string
    private String csv(Shorties s) {
        return String.join(",",
                s.getId(),
                s.getItemName(),
                String.valueOf(s.getQuantity()),
                String.valueOf(s.getPrice()),
                s.getImagePath()
        );
    }

    // Sort Shorties items by price using Bubble Sort algorithm
    public void bubbleSortByPrice(List<Shorties> items) {
        int n = items.size();
        boolean swapped;

        for (int i = 0; i < n - 1; i++) {
            swapped = false;
            for (int j = 0; j < n - i - 1; j++) {
                if (items.get(j).getPrice() > items.get(j + 1).getPrice()) {
                    Collections.swap(items, j, j + 1);
                    swapped = true;
                }
            }
            // If no elements were swapped in the inner loop, the list is already sorted
            if (!swapped)
                break;
        }
    }

}
