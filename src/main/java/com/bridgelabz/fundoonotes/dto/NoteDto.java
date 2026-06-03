package com.bridgelabz.fundoonotes.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

import java.io.Serializable;

@Data
public class NoteDto implements Serializable {
    private Long id;
    
    private String title;
    
    private String description;
    
    private String color;
    private boolean isPinned;
    private boolean isArchived;
    private boolean isTrashed;
    
    private LocalDateTime reminderDate;
    
    private java.util.List<String> collaborators;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
