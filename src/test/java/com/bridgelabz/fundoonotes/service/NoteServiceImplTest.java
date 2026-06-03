package com.bridgelabz.fundoonotes.service;

import com.bridgelabz.fundoonotes.dto.NoteDto;
import com.bridgelabz.fundoonotes.model.Note;
import com.bridgelabz.fundoonotes.model.User;
import com.bridgelabz.fundoonotes.repository.NoteRepository;
import com.bridgelabz.fundoonotes.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NoteServiceImplTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NoteServiceImpl noteService;

    private User user;
    private Note note;
    private NoteDto noteDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        note = new Note();
        note.setId(10L);
        note.setTitle("Test Title");
        note.setDescription("Test Description");
        note.setUser(user);

        noteDto = new NoteDto();
        noteDto.setTitle("Test Title");
        noteDto.setDescription("Test Description");
    }

    @Test
    void testCreateNote_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(noteRepository.save(any(Note.class))).thenReturn(note);

        NoteDto result = noteService.createNote(noteDto, "test@example.com");

        assertNotNull(result);
        assertEquals("Test Title", result.getTitle());
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    void testCreateNote_UserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> noteService.createNote(noteDto, "unknown@example.com"));
        
        assertEquals("User not found", exception.getMessage());
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void testUpdateNote_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(noteRepository.findById(10L)).thenReturn(Optional.of(note));
        
        Note updatedNote = new Note();
        updatedNote.setId(10L);
        updatedNote.setTitle("Updated Title");
        updatedNote.setUser(user);
        
        when(noteRepository.save(any(Note.class))).thenReturn(updatedNote);

        NoteDto updateDto = new NoteDto();
        updateDto.setTitle("Updated Title");

        NoteDto result = noteService.updateNote(10L, updateDto, "test@example.com");

        assertEquals("Updated Title", result.getTitle());
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    void testUpdateNote_UnauthorizedUser() {
        User hacker = new User();
        hacker.setId(99L); // Different ID
        hacker.setEmail("hacker@example.com");

        when(userRepository.findByEmail("hacker@example.com")).thenReturn(Optional.of(hacker));
        when(noteRepository.findById(10L)).thenReturn(Optional.of(note)); // Note belongs to user ID 1

        Exception exception = assertThrows(RuntimeException.class, () -> noteService.updateNote(10L, noteDto, "hacker@example.com"));
        
        assertEquals("Unauthorized to access this note", exception.getMessage());
        verify(noteRepository, never()).save(any(Note.class));
    }

    @Test
    void testGetAllActiveNotes_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(noteRepository.findByUserIdAndIsTrashedFalseAndIsArchivedFalse(1L)).thenReturn(Collections.singletonList(note));

        List<NoteDto> result = noteService.getAllActiveNotes("test@example.com");

        assertEquals(1, result.size());
        assertEquals("Test Title", result.get(0).getTitle());
    }

    @Test
    void testTogglePin_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(noteRepository.findById(10L)).thenReturn(Optional.of(note));
        
        note.setPinned(false); // Initially false
        
        when(noteRepository.save(any(Note.class))).thenAnswer(i -> i.getArguments()[0]);

        NoteDto result = noteService.togglePin(10L, "test@example.com");

        assertTrue(result.isPinned());
        assertFalse(result.isArchived());
        assertFalse(result.isTrashed());
    }

    @Test
    void testDeleteNoteForever_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(noteRepository.findById(10L)).thenReturn(Optional.of(note));

        noteService.deleteNoteForever(10L, "test@example.com");

        verify(noteRepository, times(1)).delete(note);
    }
}
