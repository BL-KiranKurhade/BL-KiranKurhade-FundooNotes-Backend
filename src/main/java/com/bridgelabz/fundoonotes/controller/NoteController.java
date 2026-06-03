package com.bridgelabz.fundoonotes.controller;

import com.bridgelabz.fundoonotes.dto.ApiResponse;
import com.bridgelabz.fundoonotes.dto.NoteDto;
import com.bridgelabz.fundoonotes.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @PostMapping
    public ResponseEntity<ApiResponse> createNote(@Valid @RequestBody NoteDto noteDto, Authentication authentication) {
        return new ResponseEntity<>(new ApiResponse("Note created successfully", noteService.createNote(noteDto, authentication.getName())), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllActiveNotes(Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse("Notes retrieved successfully", noteService.getAllActiveNotes(authentication.getName())));
    }

    @GetMapping("/archive")
    public ResponseEntity<ApiResponse> getArchivedNotes(Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse("Archived notes retrieved successfully", noteService.getAllArchivedNotes(authentication.getName())));
    }

    @GetMapping("/trash")
    public ResponseEntity<ApiResponse> getTrashedNotes(Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse("Trashed notes retrieved successfully", noteService.getAllTrashedNotes(authentication.getName())));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchNotes(@RequestParam String keyword, Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse("Notes searched successfully", noteService.searchNotes(keyword, authentication.getName())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateNote(@PathVariable Long id, @Valid @RequestBody NoteDto noteDto, Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse("Note updated successfully", noteService.updateNote(id, noteDto, authentication.getName())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteNote(@PathVariable Long id, Authentication authentication) {
        noteService.deleteNote(id, authentication.getName());
        return ResponseEntity.ok(new ApiResponse("Note moved to trash successfully.", null));
    }

    @DeleteMapping("/{id}/forever")
    public ResponseEntity<ApiResponse> deleteNoteForever(@PathVariable Long id, Authentication authentication) {
        noteService.deleteNoteForever(id, authentication.getName());
        return ResponseEntity.ok(new ApiResponse("Note deleted forever successfully.", null));
    }

    @PatchMapping("/{id}/pin")
    public ResponseEntity<ApiResponse> togglePin(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse("Note pin status toggled", noteService.togglePin(id, authentication.getName())));
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<ApiResponse> toggleArchive(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse("Note archive status toggled", noteService.toggleArchive(id, authentication.getName())));
    }
    
    @PatchMapping("/{id}/trash")
    public ResponseEntity<ApiResponse> toggleTrash(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse("Note trash status toggled", noteService.toggleTrash(id, authentication.getName())));
    }

    @PostMapping("/{id}/collaborators")
    public ResponseEntity<ApiResponse> addCollaborator(@PathVariable Long id, @RequestParam String email, Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse("Collaborator added successfully", noteService.addCollaborator(id, email, authentication.getName())));
    }

    @DeleteMapping("/{id}/collaborators")
    public ResponseEntity<ApiResponse> removeCollaborator(@PathVariable Long id, @RequestParam String email, Authentication authentication) {
        return ResponseEntity.ok(new ApiResponse("Collaborator removed successfully", noteService.removeCollaborator(id, email, authentication.getName())));
    }
}
