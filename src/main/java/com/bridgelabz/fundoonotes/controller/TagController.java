package com.bridgelabz.fundoonotes.controller;

import com.bridgelabz.fundoonotes.dto.TagDto;
import com.bridgelabz.fundoonotes.service.TagService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    @PostMapping
    public ResponseEntity<TagDto> createTag(@Valid @RequestBody TagDto tagDto, Authentication authentication) {
        return new ResponseEntity<>(tagService.createTag(tagDto, authentication.getName()), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TagDto>> getAllTags(Authentication authentication) {
        return ResponseEntity.ok(tagService.getAllTags(authentication.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TagDto> updateTag(@PathVariable Long id, @Valid @RequestBody TagDto tagDto, Authentication authentication) {
        return ResponseEntity.ok(tagService.updateTag(id, tagDto, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTag(@PathVariable Long id, Authentication authentication) {
        tagService.deleteTag(id, authentication.getName());
        return ResponseEntity.ok("Tag deleted successfully");
    }

    @PostMapping("/notes/{noteId}/{tagId}")
    public ResponseEntity<String> addTagToNote(@PathVariable Long noteId, @PathVariable Long tagId, Authentication authentication) {
        tagService.addTagToNote(noteId, tagId, authentication.getName());
        return ResponseEntity.ok("Tag added to note");
    }

    @DeleteMapping("/notes/{noteId}/{tagId}")
    public ResponseEntity<String> removeTagFromNote(@PathVariable Long noteId, @PathVariable Long tagId, Authentication authentication) {
        tagService.removeTagFromNote(noteId, tagId, authentication.getName());
        return ResponseEntity.ok("Tag removed from note");
    }
}
