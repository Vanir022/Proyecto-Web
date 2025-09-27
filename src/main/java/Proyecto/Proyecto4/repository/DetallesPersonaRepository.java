package Proyecto.Proyecto4.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Proyecto.Proyecto4.models.DetallesPersona;

@Repository
public interface DetallesPersonaRepository extends JpaRepository<DetallesPersona, Long> {
    
    // Método para buscar detalles por el teléfono (si se necesita)
    DetallesPersona findByTelefono(String telefono);
    
    // Método para verificar si existe un DNI
    boolean existsByDni(String dni);
    
    // Método para buscar por DNI
    DetallesPersona findByDni(String dni);
}