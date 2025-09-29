package Proyecto.Proyecto4.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import Proyecto.Proyecto4.models.Contacto;

@Repository
public interface ContactoRepository extends JpaRepository<Contacto, Long> {
    
    // Buscar por estado
    List<Contacto> findByEstadoOrderByFechaEnvioDesc(String estado);
    
    // Buscar todos ordenados por fecha
    List<Contacto> findAllByOrderByFechaEnvioDesc();
    
    // Buscar por hotel
    List<Contacto> findByHotelOrderByFechaEnvioDesc(String hotel);
    
    // Contar por estado
    @Query("SELECT COUNT(c) FROM Contacto c WHERE c.estado = ?1")
    Long countByEstado(String estado);
    
    // Buscar por email
    List<Contacto> findByEmailOrderByFechaEnvioDesc(String email);
}