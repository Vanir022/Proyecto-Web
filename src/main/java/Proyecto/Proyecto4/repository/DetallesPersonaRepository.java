package Proyecto.Proyecto4.repository;

import Proyecto.Proyecto4.models.DetallesPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetallesPersonaRepository extends JpaRepository<DetallesPersona, Long> {
    
    // Método para buscar detalles por el teléfono (si se necesita)
    DetallesPersona findByTelefono(String telefono);
}