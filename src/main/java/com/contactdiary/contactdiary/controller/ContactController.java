package com.contactdiary.contactdiary.controller;

import com.contactdiary.contactdiary.model.Contact;
import com.contactdiary.contactdiary.model.User;
import com.contactdiary.contactdiary.repository.UserRepository;
import com.contactdiary.contactdiary.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public Contact add(@PathVariable Long userId, @Valid @RequestBody Contact contact) {
        User user = userRepo.findById(userId).orElseThrow();
        contact.setUser(user);
        return service.save(contact);
    }

    @GetMapping("/{userId}")
    public List<Contact> get(@PathVariable Long userId) {
        User user = userRepo.findById(userId).orElseThrow();
        return service.getByUser(user);
    }

    @GetMapping("/{userId}/search")
    public List<Contact> search(@PathVariable Long userId, @RequestParam String name) {
        User user = userRepo.findById(userId).orElseThrow();
        return service.searchByName(user, name);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PutMapping("/{id}")
    public Contact update(@PathVariable Long id, @Valid @RequestBody Contact contact) {
        return service.update(id, contact);
    }

    @PostMapping("/{id}/share")
    public Contact share(@PathVariable Long id, @RequestParam String email) {
        User targetUser = userRepo.findByEmail(email).orElseThrow();
        return service.share(id, targetUser);
    }

    @PatchMapping("/{id}/favorite")
    public Contact toggleFavorite(@PathVariable Long id) {
        return service.toggleFavorite(id);
    }

    @GetMapping("/{userId}/export")
    public ResponseEntity<String> exportCsv(@PathVariable Long userId) {
        User user = userRepo.findById(userId).orElseThrow();
        List<Contact> contacts = service.getByUser(user);

        StringBuilder csv = new StringBuilder("Name,Phone,Email,Address,Relation,Favorite\n");
        for (Contact contact : contacts) {
            csv.append(csvValue(contact.getName())).append(",");
            csv.append(csvValue(contact.getPhone())).append(",");
            csv.append(csvValue(contact.getEmail())).append(",");
            csv.append(csvValue(contact.getAddress())).append(",");
            csv.append(csvValue(contact.getRelation())).append(",");
            csv.append(contact.isFavorite() ? "Yes" : "No").append("\n");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contacts.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.toString());
    }

    private String csvValue(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
