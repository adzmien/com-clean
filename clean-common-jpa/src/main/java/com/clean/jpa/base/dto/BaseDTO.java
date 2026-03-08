package com.clean.jpa.base.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private LocalDateTime createdOn;
    private String createdBy;

    private LocalDateTime updatedOn;
    private String updatedBy;
}
