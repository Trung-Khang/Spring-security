package vn.iotstar.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name="roles")
public class Role {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true, length=30) private String name;
    @OneToMany(mappedBy="role") private List<User> users = new ArrayList<>();
    protected Role() {}
    public Role(String name) { this.name=name; }
    public Long getId(){return id;} public String getName(){return name;} public void setName(String n){name=n;}
}
