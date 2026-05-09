package com.contactdiary.contactdiary.repository;

import com.contactdiary.contactdiary.model.Contact;
import com.contactdiary.contactdiary.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findByUser(User user);

    List<Contact> findByUserAndNameContainingIgnoreCase(User user, String name);
}