package com.bridgelabz.fundoonotes.service;

import com.bridgelabz.fundoonotes.dto.TagDto;
import java.util.List;

public interface TagService {
    TagDto createTag(TagDto tagDto, String userEmail);
    List<TagDto> getAllTags(String userEmail);
    TagDto updateTag(Long tagId, TagDto tagDto, String userEmail);
    void deleteTag(Long tagId, String userEmail);
    
    void addTagToNote(Long noteId, Long tagId, String userEmail);
    void removeTagFromNote(Long noteId, Long tagId, String userEmail);
}
