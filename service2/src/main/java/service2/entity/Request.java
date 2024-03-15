package service2.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "request")
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String status;

    @Column
    private String songId;

    @Column
    private String songS3URI;
}

