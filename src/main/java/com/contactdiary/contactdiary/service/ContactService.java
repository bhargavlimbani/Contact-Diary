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
        return repo.save(contact);
    }

    public List<Contact> getByUser(User user) {
        return repo.findByUser(user);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}