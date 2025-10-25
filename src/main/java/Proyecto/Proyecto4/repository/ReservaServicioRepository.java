package Proyecto.Proyecto4.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import Proyecto.Proyecto4.models.ReservaServicio;
import Proyecto.Proyecto4.models.ReservaServicio.EstadoReserva;
import Proyecto.Proyecto4.models.ReservaServicio.TipoServicio;
import Proyecto.Proyecto4.models.Usuario;

@Repository
public interface ReservaServicioRepository extends JpaRepository<ReservaServicio, Long> {
    
    // Buscar reserva por código
    Optional<ReservaServicio> findByCodigoReserva(String codigoReserva);
    
    // Buscar reservas por usuario
    List<ReservaServicio> findByUsuarioOrderByFechaReservaDesc(Usuario usuario);
    
    // Buscar reservas por estado
    List<ReservaServicio> findByEstado(EstadoReserva estado);
    
    // Buscar reservas por tipo de servicio
    List<ReservaServicio> findByTipoServicio(TipoServicio tipoServicio);
    
    // Buscar reservas por tipo de servicio y estado
    List<ReservaServicio> findByTipoServicioAndEstado(TipoServicio tipoServicio, EstadoReserva estado);
    
    // Obtener todas las reservas con detalles (JOIN FETCH para evitar LazyInitializationException)
    @Query("SELECT DISTINCT rs FROM ReservaServicio rs " +
           "LEFT JOIN FETCH rs.usuario u " +
           "LEFT JOIN FETCH u.detallesPersona " +
           "ORDER BY rs.fechaReserva DESC")
    List<ReservaServicio> findAllWithDetails();
    
    // Obtener reservas por estado con detalles
    @Query("SELECT DISTINCT rs FROM ReservaServicio rs " +
           "LEFT JOIN FETCH rs.usuario u " +
           "LEFT JOIN FETCH u.detallesPersona " +
           "WHERE rs.estado = :estado " +
           "ORDER BY rs.fechaReserva DESC")
    List<ReservaServicio> findByEstadoWithDetails(@Param("estado") EstadoReserva estado);
    
    // Obtener reservas por tipo de servicio con detalles
    @Query("SELECT DISTINCT rs FROM ReservaServicio rs " +
           "LEFT JOIN FETCH rs.usuario u " +
           "LEFT JOIN FETCH u.detallesPersona " +
           "WHERE rs.tipoServicio = :tipoServicio " +
           "ORDER BY rs.fechaReserva DESC")
    List<ReservaServicio> findByTipoServicioWithDetails(@Param("tipoServicio") TipoServicio tipoServicio);
    
    // Buscar reservas en un rango de fechas
    @Query("SELECT rs FROM ReservaServicio rs WHERE rs.fechaServicio BETWEEN :fechaInicio AND :fechaFin")
    List<ReservaServicio> findReservasPorRangoFechas(
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin
    );
    
    // Contar reservas por tipo de servicio y estado
    Long countByTipoServicioAndEstado(TipoServicio tipoServicio, EstadoReserva estado);
    
    // Calcular ingresos por tipo de servicio en un periodo
    @Query("SELECT COALESCE(SUM(rs.montoTotal), 0.0) FROM ReservaServicio rs " +
           "WHERE rs.tipoServicio = :tipoServicio " +
           "AND rs.fechaServicio BETWEEN :fechaInicio AND :fechaFin " +
           "AND rs.estado IN ('CONFIRMADA', 'COMPLETADA')")
    Double calcularIngresosPorServicioEnPeriodo(
        @Param("tipoServicio") TipoServicio tipoServicio,
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin
    );
}
