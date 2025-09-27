package Proyecto.Proyecto4.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import Proyecto.Proyecto4.models.Administrador;
import Proyecto.Proyecto4.models.Administrador.RolAdmin;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
    
    Optional<Administrador> findByEmail(String email);
    
    List<Administrador> findByActivo(Boolean activo);
    
    List<Administrador> findByRol(RolAdmin rol);
    
    List<Administrador> findByHotel(String hotel);
    
    List<Administrador> findByHotelAndActivo(String hotel, Boolean activo);
    
    @Query("SELECT a FROM Administrador a WHERE a.hotel = :hotel AND a.rol IN :roles")
    List<Administrador> findByHotelAndRolIn(@Param("hotel") String hotel, @Param("roles") List<RolAdmin> roles);
    
    @Query("SELECT COUNT(a) FROM Administrador a WHERE a.activo = true AND a.rol = :rol")
    Long contarAdministradoresPorRol(@Param("rol") RolAdmin rol);
    
    Boolean existsByEmail(String email);
}