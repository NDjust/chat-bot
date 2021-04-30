package me.nathan.oauthclient.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import me.nathan.oauthclient.domain.type.ProviderType;
import me.nathan.oauthclient.domain.type.Role;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "user")
public class User extends DateTimeEntity {

    private static final long serialVersionUID = 2127198281192056516L;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(name = "gender")
    private String gender;

    @Column(name = "profile_image")
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type")
    private ProviderType providerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    protected User() {
    }

    @Builder
    public User(Long id, String username, String name, String email, String profileImage, String phone, Role role, ProviderType providerType) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.profileImage = profileImage;
        this.name = name;
        this.role = role;
        this.providerType = providerType;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public User update(String id, String email, String name, String phone) {
        this.username = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        return this;
    }

}
