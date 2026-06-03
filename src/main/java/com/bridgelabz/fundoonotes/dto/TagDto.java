package com.bridgelabz.fundoonotes.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TagDto {
    private Long id;
    
    @NotBlank(message = "Tag name cannot be empty")
    private String name;
}
