package Proyecto.Proyecto4.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import Proyecto.Proyecto4.models.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
}