package com.pkmprojects.shoppiq.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name="roles")
@NoArgsConstructor
@AllArgsConstructor
public class Role implements Serializable {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Getter
    @Setter
    @Column
    String roleName;
}
