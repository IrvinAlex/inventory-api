package com.inventory.inventario.service;

import com.inventory.inventario.model.Usuario;
import com.inventory.inventario.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(correo);
        
        if (usuario == null || usuario.getEstatus() != 1) {
            System.out.println("Usuario no encontrado o inactivo: " + correo);
            throw new UsernameNotFoundException("Usuario no encontrado o inactivo: " + correo);
        }
        
        String roleName = "ROLE_USUARIO";
        if (usuario.getRol() != null && usuario.getRol().getNombre() != null) {
            roleName = "ROLE_" + usuario.getRol().getNombre().toUpperCase();
        }
        
        System.out.println("Usuario encontrado: " + correo + " con rol: " + roleName);
        
        // Usar la contraseña tal como está almacenada en la BD
        return User.builder()
            .username(usuario.getCorreo())
            .password("{noop}" + usuario.getContrasena())
            .authorities(Collections.singletonList(new SimpleGrantedAuthority(roleName)))
            .build();
    }
}
