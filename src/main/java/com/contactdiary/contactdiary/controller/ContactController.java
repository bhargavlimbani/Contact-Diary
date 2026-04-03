package com.contactdiary.contactdiary.controller;

import com.contactdiary.contactdiary.model.Contact;
import com.contactdiary.contactdiary.model.User;
import com.contactdiary.contactdiary.repository.UserRepository;
import com.contactdiary.contactdiary.service.ContactService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@CrossOrigin("*")
public class ContactController {

    private final ContactService service;
    private final UserRepository userRepo;

    public ContactController(ContactService service, UserRepository userRepo) {
        this.service = service;
        this.userRepo = userRepo;
    }

    @PostMapping("/{userId}")
    public Contact add(@PathVariable Long userId, @RequestBody Contact contact) {
        User user = userRepo.findById(userId).orElseThrow();
        contact.setUser(user);
        return service.save(contact);
    }

    @GetMapping("/{userId}")
    public List<Contact> get(@PathVariable Long userId) {
        User user = userRepo.findById(userId).orElseThrow();
        return service.getByUser(user);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PutMapping("/{id}")
    public Contact update(@PathVariable Long id, @RequestBody Contact contact) {
        contact.setId(id);
        return service.save(contact);
    }
}