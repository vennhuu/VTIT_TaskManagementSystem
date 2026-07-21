package com.vennhuu.TaskManagementSystem.Entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="roles")
@Getter
@Setter
public class Role {
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id ;

    private String name ;

    @Column(columnDefinition = "TEXT")
    private String description ;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private List<User> users ;
}
