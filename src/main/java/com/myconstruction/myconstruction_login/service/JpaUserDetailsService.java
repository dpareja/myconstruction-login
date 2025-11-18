package com.myconstruction.myconstruction_login.service;

import jakarta.persistence.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
public class JpaUserDetailsService implements UserDetailsService {

    @PersistenceContext
    private EntityManager em;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("PASO 1: Buscando usuario en DB: " + username);

        User user = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getSingleResult();

        System.out.println("PASO 2: Usuario encontrado: " + user.getUsername());
        System.out.println("PASO 4: Enabled: " + user.isEnabled());

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .disabled(!user.isEnabled())
                .build();

        System.out.println("PASO 5: UserDetails creado → LOGIN VA A COMPARAR");
        return userDetails;
    }
}