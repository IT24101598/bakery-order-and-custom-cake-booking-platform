package com.example.bakery.controller;

import com.example.bakery.model.Catering;
import com.example.bakery.service.CateringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
@RequestMapping("/catering")
public class CateringController {

    @Autowired
    private CateringService svc;

    @GetMapping("/add")
    public String showAdd(Model m) throws IOException {
        Catering t = new Catering();
        t.setId(svc.getNextId());
        m.addAttribute("catering", t);
        return "add-catering";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute Catering t, Model m) throws IOException {
        svc.addCatering(t);
        m.addAttribute("successMessage", "Menu submitted!");
        Catering fresh = new Catering();
        fresh.setId(svc.getNextId());
        m.addAttribute("catering", fresh);
        return "redirect:/catering/admin";
    }

    @GetMapping("/admin")
    public String list(Model m) {
        m.addAttribute("catering", svc.getAll());
        return "admin-catering";
    }

    @GetMapping("/menu")
    public String menuList(Model m) {
        m.addAttribute("catering", svc.getAll());
        return "customer-catering";
    }

    @GetMapping("/update/{id}")
    public String showUpdate(@PathVariable String id, Model m) {
        m.addAttribute("catering", svc.getById(id));
        return "update-catering";
    }

    // Changed from @PostMapping to @PutMapping and added @PathVariable for ID
    @PutMapping("/update/{id}")
    public String update(@PathVariable String id, @ModelAttribute Catering t) throws IOException {
        // Ensure that the Catering ID in the path variable is the same as the Catering object ID
        t.setId(id);  // Set the ID to ensure we're updating the correct Catering
        svc.updateCatering(t);
        return "redirect:/catering/admin";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable String id) throws IOException {
        svc.deleteById(id);
        return "redirect:/catering/admin";
    }
}
