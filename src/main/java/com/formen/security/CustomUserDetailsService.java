package com.formen.security;

import com.formen.entity.Usuario;
import com.formen.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;
    public CustomUserDetailsService(UsuarioRepository usuarioRepository) { this.usuarioRepository = usuarioRepository; }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario u = usuarioRepository.findByCorreo(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return User.withUsername(u.getCorreo())
                .password(u.getPassword())
                .roles(u.getRol().name())
                .disabled(!u.isActivo())
                .build();
    }
}
