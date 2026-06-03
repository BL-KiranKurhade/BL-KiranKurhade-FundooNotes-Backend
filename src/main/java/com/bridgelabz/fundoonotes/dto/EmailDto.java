package com.bridgelabz.fundoonotes.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailDto implements Serializable {
    private String to;
    private String subject;
    private String body;
}
