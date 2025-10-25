package Proyecto.Proyecto4.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import Proyecto.Proyecto4.models.Habitacion;
import Proyecto.Proyecto4.models.Reserva;
import Proyecto.Proyecto4.models.Reserva.EstadoReserva;
import Proyecto.Proyecto4.models.Usuario;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

       List<Reserva> findByUsuario(Usuario usuario);

       List<Reserva> findByUsuarioOrderByFechaReservaDesc(Usuario usuario);

       List<Reserva> findByEstado(EstadoReserva estado);
       
       // Consulta optimizada para buscar por estado con JOIN FETCH
       @Query("SELECT DISTINCT r FROM Reserva r " +
              "LEFT JOIN FETCH r.usuario u " +
              "LEFT JOIN FETCH u.detallesPersona " +
              "LEFT JOIN FETCH r.habitacion h " +
              "WHERE r.estado = :estado " +
              "ORDER BY r.fechaReserva DESC")
       List<Reserva> findByEstadoWithDetails(@Param("estado") EstadoReserva estado);

       Optional<Reserva> findByCodigoReserva(String codigoReserva);

       @Query("SELECT r FROM Reserva r WHERE r.habitacion = :habitacion AND " +
                     "((r.fechaEntrada <= :fechaSalida AND r.fechaSalida >= :fechaEntrada) AND " +
                     "(r.estado = 'CONFIRMADA' OR r.estado = 'PENDIENTE'))")
       List<Reserva> findReservasConflictoFechas(@Param("habitacion") Habitacion habitacion,
                     @Param("fechaEntrada") LocalDate fechaEntrada,
                     @Param("fechaSalida") LocalDate fechaSalida);

       @Query("SELECT r FROM Reserva r WHERE r.fechaEntrada BETWEEN :fechaInicio AND :fechaFin")
       List<Reserva> findReservasPorRangoFechas(@Param("fechaInicio") LocalDate fechaInicio,
                     @Param("fechaFin") LocalDate fechaFin);

       @Query("SELECT COUNT(r) FROM Reserva r WHERE r.habitacion.hotel = :hotel AND r.estado = :estado")
       Long contarReservasPorHotelYEstado(@Param("hotel") String hotel, @Param("estado") EstadoReserva estado);

       @Query("SELECT SUM(r.montoTotal) FROM Reserva r WHERE r.estado = 'COMPLETADA' AND " +
                     "r.fechaEntrada BETWEEN :fechaInicio AND :fechaFin")
       Double calcularIngresosEnPeriodo(@Param("fechaInicio") LocalDate fechaInicio,
                     @Param("fechaFin") LocalDate fechaFin);

       List<Reserva> findByHabitacion(Habitacion habitacion);
       
       // Consulta optimizada con JOIN FETCH para evitar LazyInitializationException
       @Query("SELECT DISTINCT r FROM Reserva r " +
              "LEFT JOIN FETCH r.usuario u " +
              "LEFT JOIN FETCH u.detallesPersona " +
              "LEFT JOIN FETCH r.habitacion h " +
              "ORDER BY r.fechaReserva DESC")
       List<Reserva> findAllWithDetails();
}