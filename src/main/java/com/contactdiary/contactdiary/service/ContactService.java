package com.contactdiary.contactdiary.service;

import com.contactdiary.contactdiary.model.Contact;
import com.contactdiary.contactdiary.model.User;
import com.contactdiary.contactdiary.repository.ContactRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactService {

    private final ContactRepository repo;

    public ContactService(ContactRepository repo) {
        this.repo = repo;
    }

    public Contact save(Contact contact) {
        normalizeOptionalFields(contact);
        return repo.save(contact);
    }

    public Contact update(Long id, Contact contact) {
        Contact existing = repo.findById(id).orElseThrow();
        existing.setName(contact.getName());
        existing.setPhone(contact.getPhone());
        existing.setEmail(contact.getEmail());
        existing.setAddress(contact.getAddress());
        existing.setRelation(contact.getRelation());
        normalizeOptionalFields(existing);
        return repo.save(existing);
    }

    public List<Contact> getByUser(User user) {
        return repo.findByUser(user);
    }

    public List<Contact> searchByName(User user, String name) {
        if (name == null || name.isBlank()) {
            return getByUser(user);
        }
        return repo.findByUserAndNameContainingIgnoreCase(user, name.trim());
    }

    public Contact share(Long contactId, User targetUser) {
        Contact original = repo.findById(contactId).orElseThrow();
        Contact shared = new Contact();
        shared.setName(original.getName());
        shared.setPhone(original.getPhone());
        shared.setEmail(original.getEmail());
        shared.setAddress(original.getAddress());
        shared.setRelation(original.getRelation());
        shared.setFavorite(original.isFavorite());
        shared.setUser(targetUser);
        return repo.save(shared);
    }

    public Contact toggleFavorite(Long id) {
        Contact contact = repo.findById(id).orElseThrow();
        contact.setFavorite(!contact.isFavorite());
        return repo.save(contact);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    private void normalizeOptionalFields(Contact contact) {
        if (contact.getEmail() != null && contact.getEmail().isBlank()) {
            contact.setEmail(null);
        }
        if (contact.getAddress() == null) {
            contact.setAddress("");
        }
        if (contact.getRelation() == null) {
            contact.setRelation("");
        }
    }
}