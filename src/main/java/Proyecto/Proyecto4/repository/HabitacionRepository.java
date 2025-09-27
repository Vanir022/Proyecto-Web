package Proyecto.Proyecto4.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import Proyecto.Proyecto4.models.Habitacion;

@Repository
public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {
    
    List<Habitacion> findByDisponible(Boolean disponible);
    
    List<Habitacion> findByHotelAndDisponible(String hotel, Boolean disponible);
    
    List<Habitacion> findByTipoAndDisponible(String tipo, Boolean disponible);
    
    @Query("SELECT h FROM Habitacion h WHERE h.capacidad >= :capacidad AND h.disponible = true")
    List<Habitacion> findByCapacidadGreaterThanEqualAndDisponible(@Param("capacidad") Integer capacidad);
    
    @Query("SELECT h FROM Habitacion h WHERE h.precio BETWEEN :precioMin AND :precioMax AND h.disponible = true")
    List<Habitacion> findByPrecioBetweenAndDisponible(@Param("precioMin") Double precioMin, @Param("precioMax") Double precioMax);
    
    List<Habitacion> findByHotel(String hotel);
    
    @Query("SELECT DISTINCT h.hotel FROM Habitacion h")
    List<String> findDistinctHoteles();
    
    @Query("SELECT DISTINCT h.tipo FROM Habitacion h WHERE h.hotel = :hotel")
    List<String> findDistinctTiposByHotel(@Param("hotel") String hotel);
    
    // Métodos para manejo de estado de habitaciones
    List<Habitacion> findByEstadoHabitacion(Habitacion.EstadoHabitacion estado);
    
    @Query("SELECT h FROM Habitacion h WHERE h.hotel = :hotel AND h.estadoHabitacion = :estado")
    List<Habitacion> findByHotelAndEstadoHabitacion(@Param("hotel") String hotel, @Param("estado") Habitacion.EstadoHabitacion estado);
    
    @Query("SELECT COUNT(h) FROM Habitacion h WHERE h.hotel = :hotel AND h.estadoHabitacion = :estado")
    Long contarHabitacionesPorHotelYEstado(@Param("hotel") String hotel, @Param("estado") Habitacion.EstadoHabitacion estado);
}