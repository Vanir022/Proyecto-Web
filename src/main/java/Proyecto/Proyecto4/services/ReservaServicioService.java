package Proyecto.Proyecto4.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import Proyecto.Proyecto4.models.ReservaServicio;
import Proyecto.Proyecto4.models.ReservaServicio.EstadoReserva;
import Proyecto.Proyecto4.models.ReservaServicio.TipoServicio;
import Proyecto.Proyecto4.models.Usuario;
import Proyecto.Proyecto4.repository.ReservaServicioRepository;

@Service
public class ReservaServicioService {

    @Autowired
    private ReservaServicioRepository reservaServicioRepository;

    public List<ReservaServicio> obtenerReservasPorUsuario(Usuario usuario) {
        return reservaServicioRepository.findByUsuarioOrderByFechaReservaDesc(usuario);
    }

    public List<ReservaServicio> obtenerTodasLasReservas() {
        return reservaServicioRepository.findAllWithDetails();
    }

    public List<ReservaServicio> obtenerReservasPorEstado(EstadoReserva estado) {
        return reservaServicioRepository.findByEstadoWithDetails(estado);
    }

    public List<ReservaServicio> obtenerReservasPorTipoServicio(TipoServicio tipoServicio) {
        return reservaServicioRepository.findByTipoServicioWithDetails(tipoServicio);
    }

    public Optional<ReservaServicio> obtenerReservaPorCodigo(String codigoReserva) {
        return reservaServicioRepository.findByCodigoReserva(codigoReserva);
    }

    public Optional<ReservaServicio> obtenerReservaPorId(Long id) {
        return reservaServicioRepository.findById(id);
    }

    public ReservaServicio crearReserva(ReservaServicio reserva) {
        // Generar código de reserva único
        if (reserva.getCodigoReserva() == null || reserva.getCodigoReserva().isEmpty()) {
            reserva.setCodigoReserva(generarCodigoReserva(reserva.getTipoServicio()));
        }
        
        // Establecer estado inicial si no está definido
        if (reserva.getEstado() == null) {
            reserva.setEstado(EstadoReserva.PENDIENTE);
        }
        
        return reservaServicioRepository.save(reserva);
    }

    public ReservaServicio actualizarEstadoReserva(Long reservaId, EstadoReserva nuevoEstado) {
        Optional<ReservaServicio> reservaOpt = reservaServicioRepository.findById(reservaId);
        if (reservaOpt.isPresent()) {
            ReservaServicio reserva = reservaOpt.get();
            reserva.setEstado(nuevoEstado);
            return reservaServicioRepository.save(reserva);
        }
        throw new RuntimeException("Reserva de servicio no encontrada");
    }

    public void cancelarReserva(Long reservaId) {
        actualizarEstadoReserva(reservaId, EstadoReserva.CANCELADA);
    }

    public void confirmarReserva(Long reservaId) {
        actualizarEstadoReserva(reservaId, EstadoReserva.CONFIRMADA);
    }

    public void completarReserva(Long reservaId) {
        actualizarEstadoReserva(reservaId, EstadoReserva.COMPLETADA);
    }

    private String generarCodigoReserva(TipoServicio tipoServicio) {
        String prefijo = "";
        switch (tipoServicio) {
            case SPA:
                prefijo = "SPA-";
                break;
            case BODA:
                prefijo = "BODA-";
                break;
            case EVENTO:
                prefijo = "EVT-";
                break;
        }
        return prefijo + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public List<ReservaServicio> obtenerReservasEnRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        return reservaServicioRepository.findReservasPorRangoFechas(fechaInicio, fechaFin);
    }

    public Long contarReservasPorServicioYEstado(TipoServicio tipoServicio, EstadoReserva estado) {
        return reservaServicioRepository.countByTipoServicioAndEstado(tipoServicio, estado);
    }

    public Double calcularIngresosPorServicioEnPeriodo(TipoServicio tipoServicio, LocalDate fechaInicio, LocalDate fechaFin) {
        Double ingresos = reservaServicioRepository.calcularIngresosPorServicioEnPeriodo(tipoServicio, fechaInicio, fechaFin);
        return ingresos != null ? ingresos : 0.0;
    }
}
