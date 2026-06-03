package com.bridgelabz.fundoonotes.service;

import com.bridgelabz.fundoonotes.dto.NoteDto;
import com.bridgelabz.fundoonotes.model.Note;
import com.bridgelabz.fundoonotes.model.User;
import com.bridgelabz.fundoonotes.repository.NoteRepository;
import com.bridgelabz.fundoonotes.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoteServiceImpl implements NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Note getNoteByIdAndUser(Long noteId, User user) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        boolean isOwner = note.getUser().getId().equals(user.getId());
        boolean isCollaborator = note.getCollaborators().stream()
                .anyMatch(c -> c.getId().equals(user.getId()));
        
        if (!isOwner && !isCollaborator) {
            throw new RuntimeException("Unauthorized to access this note");
        }
        return note;
    }

    private NoteDto mapToDto(Note note) {
        NoteDto dto = new NoteDto();
        dto.setId(note.getId());
        dto.setTitle(note.getTitle());
        dto.setDescription(note.getDescription());
        dto.setColor(note.getColor());
        dto.setPinned(note.isPinned());
        dto.setArchived(note.isArchived());
        dto.setTrashed(note.isTrashed());
        dto.setReminderDate(note.getReminderDate());
        dto.setCreatedAt(note.getCreatedAt());
        dto.setUpdatedAt(note.getUpdatedAt());
        if (note.getCollaborators() != null) {
            dto.setCollaborators(note.getCollaborators().stream().map(User::getEmail).collect(Collectors.toList()));
        }
        return dto;
    }

    @Override
    @CacheEvict(value = "notes_v4", allEntries = true)
    public NoteDto createNote(NoteDto noteDto, String userEmail) {
        User user = getUserByEmail(userEmail);
        Note note = new Note();
        note.setTitle(noteDto.getTitle());
        note.setDescription(noteDto.getDescription());
        note.setColor(noteDto.getColor());
        note.setArchived(noteDto.isArchived());
        note.setPinned(noteDto.isPinned());
        note.setReminderDate(noteDto.getReminderDate());
        note.setUser(user);
        
        return mapToDto(noteRepository.save(note));
    }

    @Override
    @CacheEvict(value = "notes_v4", allEntries = true)
    public NoteDto updateNote(Long noteId, NoteDto noteDto, String userEmail) {
        User user = getUserByEmail(userEmail);
        Note note = getNoteByIdAndUser(noteId, user);
        
        note.setTitle(noteDto.getTitle());
        note.setDescription(noteDto.getDescription());
        if(noteDto.getColor() != null) {
            note.setColor(noteDto.getColor());
        }
        if(noteDto.getReminderDate() != null) {
            note.setReminderDate(noteDto.getReminderDate());
        }
        
        return mapToDto(noteRepository.save(note));
    }

    @Override
    public void deleteNote(Long noteId, String userEmail) {
        toggleTrash(noteId, userEmail);
    }

    @Override
    @Cacheable(value = "notes_v4", key = "#userEmail")
    public List<NoteDto> getAllActiveNotes(String userEmail) {
        User user = getUserByEmail(userEmail);
        List<Note> notes = noteRepository.findByUserIdAndIsTrashedFalseAndIsArchivedFalse(user.getId());
        return notes.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<NoteDto> getAllArchivedNotes(String userEmail) {
        User user = getUserByEmail(userEmail);
        List<Note> notes = noteRepository.findByUserIdAndIsArchivedTrueAndIsTrashedFalse(user.getId());
        return notes.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<NoteDto> getAllTrashedNotes(String userEmail) {
        User user = getUserByEmail(userEmail);
        List<Note> notes = noteRepository.findByUserIdAndIsTrashedTrue(user.getId());
        return notes.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<NoteDto> searchNotes(String keyword, String userEmail) {
        User user = getUserByEmail(userEmail);
        return noteRepository.searchNotesByKeyword(user.getId(), keyword).stream()
                .map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @CacheEvict(value = "notes_v4", allEntries = true)
    public NoteDto togglePin(Long noteId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Note note = getNoteByIdAndUser(noteId, user);
        note.setPinned(!note.isPinned());
        if(note.isPinned()) {
            note.setArchived(false);
            note.setTrashed(false);
        }
        return mapToDto(noteRepository.save(note));
    }

    @Override
    @CacheEvict(value = "notes_v4", allEntries = true)
    public NoteDto toggleArchive(Long noteId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Note note = getNoteByIdAndUser(noteId, user);
        note.setArchived(!note.isArchived());
        if(note.isArchived()) {
            note.setPinned(false);
            note.setTrashed(false);
        }
        return mapToDto(noteRepository.save(note));
    }

    @Override
    @CacheEvict(value = "notes_v4", allEntries = true)
    public NoteDto toggleTrash(Long noteId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Note note = getNoteByIdAndUser(noteId, user);
        note.setTrashed(!note.isTrashed());
        if(note.isTrashed()) {
            note.setPinned(false);
            note.setArchived(false);
        }
        return mapToDto(noteRepository.save(note));
    }

    @Override
    @CacheEvict(value = "notes_v4", allEntries = true)
    public void deleteNoteForever(Long noteId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Note note = getNoteByIdAndUser(noteId, user);
        noteRepository.delete(note);
    }

    @Override
    @CacheEvict(value = "notes_v4", allEntries = true)
    public NoteDto addCollaborator(Long noteId, String collaboratorEmail, String userEmail) {
        User owner = getUserByEmail(userEmail);
        Note note = getNoteByIdAndUser(noteId, owner);
        if (!note.getUser().getId().equals(owner.getId())) {
            throw new RuntimeException("Only the owner can add collaborators");
        }
        User collaborator = getUserByEmail(collaboratorEmail);
        if (note.getUser().getId().equals(collaborator.getId())) {
            throw new RuntimeException("Cannot add owner as collaborator");
        }
        note.getCollaborators().add(collaborator);
        return mapToDto(noteRepository.save(note));
    }

    @Override
    @CacheEvict(value = "notes_v4", allEntries = true)
    public NoteDto removeCollaborator(Long noteId, String collaboratorEmail, String userEmail) {
        User owner = getUserByEmail(userEmail);
        Note note = getNoteByIdAndUser(noteId, owner);
        if (!note.getUser().getId().equals(owner.getId())) {
            throw new RuntimeException("Only the owner can remove collaborators");
        }
        User collaborator = getUserByEmail(collaboratorEmail);
        note.getCollaborators().remove(collaborator);
        return mapToDto(noteRepository.save(note));
    }
}
