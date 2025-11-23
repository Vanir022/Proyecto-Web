package Proyecto.Proyecto4.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import Proyecto.Proyecto4.models.Administrador;
import Proyecto.Proyecto4.models.Usuario;
import Proyecto.Proyecto4.repository.AdministradorRepository;
import Proyecto.Proyecto4.repository.UsuarioRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Buscar primero en usuarios regulares
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();

            if (!usuario.getAccountNonLocked()) {
                throw new RuntimeException("Usuario bloqueado por múltiples intentos fallidos. Espere o contacte al administrador.");
            }

            return User.builder()
                    .username(usuario.getEmail())
                    .password(usuario.getPassword())
                    .authorities(usuario.getRol())
                    .accountLocked(!usuario.getAccountNonLocked())
                    .build();
        }

        // Si no se encuentra como usuario, buscar como administrador
        Optional<Administrador> adminOpt = administradorRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            Administrador admin = adminOpt.get();

            // Verificar que esté activo
            if (!admin.getActivo()) {
                throw new UsernameNotFoundException("Administrador desactivado: " + email);
            }

            return User.builder()
                    .username(admin.getEmail())
                    .password(admin.getPassword())
                    .authorities("ROLE_ADMIN") // Todos los administradores tienen ROLE_ADMIN
                    .build();
        }

        throw new UsernameNotFoundException("Usuario/Administrador no encontrado con email: " + email);
    }
}