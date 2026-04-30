package it.lesstheory.fotoalbum.controller;

import it.lesstheory.fotoalbum.model.Photo;
import it.lesstheory.fotoalbum.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdminController {

    @Autowired
    private PhotoRepository photoRepo;

    /**
     * 1. GALLERIA PUBBLICA
     * Risponde a: http://localhost:8080/
     * Carica il file: src/main/resources/templates/index.html
     */
    @GetMapping("/")
    public String showPublicGallery(Model model) {
        model.addAttribute("photos", photoRepo.findAll());
        return "index"; 
    }

    /**
     * 2. AREA DI GESTIONE (ADMIN)
     * Risponde a: http://localhost:8080/admin
     * Carica il file: src/main/resources/templates/admin/index.html
     */
    @GetMapping("/admin")
    public String showAdminPage(Model model) {
        model.addAttribute("photos", photoRepo.findAll());
        return "admin/index"; 
    }

    /**
     * 3. SALVATAGGIO NUOVA FOTO
     * Risponde a: POST su http://localhost:8080/admin/save
     */
    @PostMapping("/admin/save")
    public String savePhoto(@ModelAttribute Photo photo) {
        photoRepo.save(photo);
        return "redirect:/admin";
    }

    /**
     * 4. ELIMINAZIONE FOTO
     * Risponde a: http://localhost:8080/admin/delete/{id}
     */
    @GetMapping("/admin/delete/{id}")
    public String deletePhoto(@PathVariable Long id) {
        photoRepo.deleteById(id);
        return "redirect:/admin";
    }
}