package com.example.app.Models;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false, unique = true, name = "serial_number")
    private String serialNumber; // Thêm trường serial_number
    @Column(nullable = false, unique = true, name = "gas_threshold")
    private Integer  gasThreshold;
    @Column(nullable = false, unique = true, name = "CO_threshold")
    private Integer  COThreshold;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<DeviceInformation> deviceInformations;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id" , referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id" , referencedColumnName = "id")
    )
    private Set<Role> roles;


}
