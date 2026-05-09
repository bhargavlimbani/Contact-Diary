package com.contactdiary.contactdiary.service;

import com.contactdiary.contactdiary.model.Contact;
import com.contactdiary.contactdiary.model.User;
import com.contactdiary.contactdiary.repository.ContactRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ContactServiceTest {

    private final ContactRepository repo = mock(ContactRepository.class);
    private final ContactService service = new ContactService(repo);

    @Test
    void updatePreservesExistingUser() {
        User owner = new User();
        owner.setId(1L);

        Contact existing = new Contact();
        existing.setId(10L);
        existing.setUser(owner);

        Contact changes = new Contact();
        changes.setName("Rahul");
        changes.setPhone("9876543210");
        changes.setEmail("rahul@example.com");
        changes.setAddress("Ahmedabad");
        changes.setRelation("Friend");

        when(repo.findById(10L)).thenReturn(Optional.of(existing));
        when(repo.save(existing)).thenReturn(existing);

        Contact updated = service.update(10L, changes);

        assertEquals(owner, updated.getUser());
        assertEquals("Rahul", updated.getName());
        assertEquals("Friend", updated.getRelation());
    }

    @Test
    void shareCopiesContactToTargetUser() {
        User target = new User();
        target.setId(2L);

        Contact original = new Contact();
        original.setId(10L);
        original.setName("Priya");
        original.setPhone("9876543210");
        original.setEmail("priya@example.com");
        original.setAddress("Surat");
        original.setRelation("Sister");

        when(repo.findById(10L)).thenReturn(Optional.of(original));
        when(repo.save(any(Contact.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Contact shared = service.share(10L, target);

        assertEquals(target, shared.getUser());
        assertEquals(original.getName(), shared.getName());
        assertEquals(original.getRelation(), shared.getRelation());
    }
}
