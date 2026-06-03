package com.bridgelabz.fundoonotes.service;

import com.bridgelabz.fundoonotes.dto.NoteDto;
import java.util.List;

public interface NoteService {
    NoteDto createNote(NoteDto noteDto, String userEmail);
    NoteDto updateNote(Long noteId, NoteDto noteDto, String userEmail);
    void deleteNote(Long noteId, String userEmail);
    List<NoteDto> getAllActiveNotes(String userEmail);
    List<NoteDto> getAllArchivedNotes(String userEmail);
    List<NoteDto> getAllTrashedNotes(String userEmail);
    List<NoteDto> searchNotes(String keyword, String userEmail);
    
    NoteDto togglePin(Long noteId, String userEmail);
    NoteDto toggleArchive(Long noteId, String userEmail);
    NoteDto toggleTrash(Long noteId, String userEmail);
    
    void deleteNoteForever(Long noteId, String userEmail);
    
    NoteDto addCollaborator(Long noteId, String collaboratorEmail, String userEmail);
    NoteDto removeCollaborator(Long noteId, String collaboratorEmail, String userEmail);
}
