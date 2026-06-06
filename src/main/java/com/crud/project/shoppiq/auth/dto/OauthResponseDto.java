package com.crud.project.shoppiq.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OauthResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private String email;
}