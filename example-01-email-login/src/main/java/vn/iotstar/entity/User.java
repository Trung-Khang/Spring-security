package vn.iotstar.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name="users")
public class User {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true, length=120) private String email;
    @Column(nullable=false, length=150) private String password;
    @Column(nullable=false, length=120) private String fullName;
    @Column(nullable=false) private boolean enabled=true;
    @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="role_id") private Role role;
    protected User() {}
    public User(String email,String password,String fullName,Role role){this.email=email;this.password=password;this.fullName=fullName;this.role=role;}
    public Long getId(){return id;} public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getPassword(){return password;} public void setPassword(String v){password=v;} public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
    public boolean isEnabled(){return enabled;} public void setEnabled(boolean v){enabled=v;} public Role getRole(){return role;} public void setRole(Role v){role=v;} public LocalDateTime getCreatedAt(){return createdAt;}
}
