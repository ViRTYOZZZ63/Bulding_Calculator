package backend.back.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import backend.back.entity.enums.Role;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String login;

    @Column(nullable = false)
    private String password;         // bcrypt

    private String firstName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    private Role role;               // MANAGER, ADMIN

    private LocalDateTime createdAt;
}