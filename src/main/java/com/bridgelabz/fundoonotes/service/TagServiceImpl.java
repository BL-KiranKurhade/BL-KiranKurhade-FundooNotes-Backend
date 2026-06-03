package com.bridgelabz.fundoonotes.service;

import com.bridgelabz.fundoonotes.dto.TagDto;
import com.bridgelabz.fundoonotes.model.Note;
import com.bridgelabz.fundoonotes.model.Tag;
import com.bridgelabz.fundoonotes.model.User;
import com.bridgelabz.fundoonotes.repository.NoteRepository;
import com.bridgelabz.fundoonotes.repository.TagRepository;
import com.bridgelabz.fundoonotes.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    }

    private TagDto mapToDto(Tag tag) {
        TagDto dto = new TagDto();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        return dto;
    }

    @Override
    public TagDto createTag(TagDto tagDto, String userEmail) {
        User user = getUserByEmail(userEmail);
        Tag tag = new Tag();
        tag.setName(tagDto.getName());
        tag.setUser(user);
        return mapToDto(tagRepository.save(tag));
    }

    @Override
    public List<TagDto> getAllTags(String userEmail) {
        User user = getUserByEmail(userEmail);
        return tagRepository.findByUserId(user.getId()).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public TagDto updateTag(Long tagId, TagDto tagDto, String userEmail) {
        User user = getUserByEmail(userEmail);
        Tag tag = tagRepository.findByIdAndUserId(tagId, user.getId())
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        tag.setName(tagDto.getName());
        return mapToDto(tagRepository.save(tag));
    }

    @Override
    public void deleteTag(Long tagId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Tag tag = tagRepository.findByIdAndUserId(tagId, user.getId())
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        tagRepository.delete(tag);
    }

    @Override
    public void addTagToNote(Long noteId, Long tagId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Note note = noteRepository.findById(noteId).orElseThrow(() -> new RuntimeException("Note not found"));
        if(!note.getUser().getId().equals(user.getId())) throw new RuntimeException("Unauthorized");
        
        Tag tag = tagRepository.findByIdAndUserId(tagId, user.getId())
                .orElseThrow(() -> new RuntimeException("Tag not found"));
                
        note.getTags().add(tag);
        noteRepository.save(note);
    }

    @Override
    public void removeTagFromNote(Long noteId, Long tagId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Note note = noteRepository.findById(noteId).orElseThrow(() -> new RuntimeException("Note not found"));
        if(!note.getUser().getId().equals(user.getId())) throw new RuntimeException("Unauthorized");
        
        Tag tag = tagRepository.findByIdAndUserId(tagId, user.getId())
                .orElseThrow(() -> new RuntimeException("Tag not found"));
                
        note.getTags().remove(tag);
        noteRepository.save(note);
    }
}
