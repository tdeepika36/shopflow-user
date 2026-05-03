package com.example.shopflow_user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class User {


            @Id
            private Long id;
            private String email;
            @Column("password_hash")
            private String passwordHash;
            private Role role;
            @Column("created_at")
            private Instant createdAt;



}
