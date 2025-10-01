package Proyecto.Proyecto4.services;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import Proyecto.Proyecto4.models.Habitacion;
import Proyecto.Proyecto4.models.Reserva;
import Proyecto.Proyecto4.models.Reserva.EstadoReserva;
import Proyecto.Proyecto4.models.Usuario;
import Proyecto.Proyecto4.repository.ReservaRepository;

@Service
public class ReservaService {
    
    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private HabitacionService habitacionService;
    
    public List<Reserva> obtenerReservasPorUsuario(Usuario usuario) {
        return reservaRepository.findByUsuarioOrderByFechaReservaDesc(usuario);
    }
    
    public List<Reserva> obtenerTodasLasReservas() {
        List<Reserva> reservas = reservaRepository.findAll();
        
        // Forzar la carga de entidades relacionadas para evitar lazy loading issues
        reservas.forEach(reserva -> {
            if (reserva.getUsuario() != null) {
                reserva.getUsuario().getNombre(); // Forzar carga
                if (reserva.getUsuario().getDetallesPersona() != null) {
                    reserva.getUsuario().getDetallesPersona().getNombres(); // Forzar carga
                }
            }
            if (reserva.getHabitacion() != null) {
                reserva.getHabitacion().getNumero(); // Forzar carga
                reserva.getHabitacion().getHotel(); // Forzar carga
            }
        });
        
        return reservas;
    }
    
    public List<Reserva> obtenerReservasPorEstado(EstadoReserva estado) {
        return reservaRepository.findByEstado(estado);
    }
    
    public Optional<Reserva> obtenerReservaPorCodigo(String codigoReserva) {
        return reservaRepository.findByCodigoReserva(codigoReserva);
    }
    
    public Optional<Reserva> obtenerReservaPorId(Long id) {
        return reservaRepository.findById(id);
    }
    
    public boolean verificarDisponibilidad(Habitacion habitacion, LocalDate fechaEntrada, LocalDate fechaSalida) {
        List<Reserva> reservasConflicto = reservaRepository.findReservasConflictoFechas(
            habitacion, fechaEntrada, fechaSalida);
        return reservasConflicto.isEmpty();
    }
    
    public Reserva crearReserva(Usuario usuario, Habitacion habitacion, LocalDate fechaEntrada, 
                               LocalDate fechaSalida, Integer numeroHuespedes, String comentarios,
                               String telefonoContacto, String solicitudesEspeciales, String dniCliente) {
        
        // Verificar disponibilidad
        if (!verificarDisponibilidad(habitacion, fechaEntrada, fechaSalida)) {
            throw new RuntimeException("La habitación no está disponible en las fechas seleccionadas");
        }
        
        // Validar DNI
        if (dniCliente == null || dniCliente.trim().isEmpty()) {
            throw new RuntimeException("El DNI del cliente es obligatorio");
        }
        
        if (!dniCliente.matches("\\d{8}")) {
            throw new RuntimeException("El DNI debe tener exactamente 8 dígitos");
        }
        
        // Calcular el monto total
        long dias = ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
        if (dias <= 0) {
            throw new RuntimeException("Las fechas de entrada y salida no son válidas");
        }
        
        double montoTotal = habitacion.getPrecio().doubleValue() * dias;
        
        // Crear la reserva
        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setHabitacion(habitacion);
        reserva.setFechaEntrada(fechaEntrada);
        reserva.setFechaSalida(fechaSalida);
        reserva.setNumeroHuespedes(numeroHuespedes);
        reserva.setMontoTotal(java.math.BigDecimal.valueOf(montoTotal));
        reserva.setEstado(EstadoReserva.PENDIENTE);
        reserva.setComentarios(comentarios);
        reserva.setTelefonoContacto(telefonoContacto);
        reserva.setSolicitudesEspeciales(solicitudesEspeciales);
        reserva.setCodigoReserva(generarCodigoReserva());
        reserva.setDniCliente(dniCliente);
        
        return reservaRepository.save(reserva);
    }
    
    public Reserva actualizarEstadoReserva(Long reservaId, EstadoReserva nuevoEstado) {
        Optional<Reserva> reservaOpt = reservaRepository.findById(reservaId);
        if (reservaOpt.isPresent()) {
            Reserva reserva = reservaOpt.get();
            EstadoReserva estadoAnterior = reserva.getEstado();
            reserva.setEstado(nuevoEstado);
            
            // Actualizar el estado de la habitación según el estado de la reserva
            Long habitacionId = reserva.getHabitacion().getId();
            
            switch (nuevoEstado) {
                case CONFIRMADA:
                    // Cuando se confirma una reserva, la habitación pasa a estar ocupada
                    habitacionService.marcarHabitacionComoOcupada(habitacionId);
                    break;
                case COMPLETADA:
                    // Cuando se completa una reserva, la habitación queda libre
                    habitacionService.marcarHabitacionComoLibre(habitacionId);
                    break;
                case CANCELADA:
                    // Cuando se cancela una reserva, liberar la habitación si estaba ocupada
                    if (estadoAnterior == EstadoReserva.CONFIRMADA) {
                        habitacionService.marcarHabitacionComoLibre(habitacionId);
                    }
                    break;
                case NO_SHOW:
                    // Si el cliente no se presenta, liberar la habitación
                    if (estadoAnterior == EstadoReserva.CONFIRMADA) {
                        habitacionService.marcarHabitacionComoLibre(habitacionId);
                    }
                    break;
                default:
                    // Para estado PENDIENTE u otros, no hacer cambios en el estado de la habitación
                    break;
            }
            
            return reservaRepository.save(reserva);
        }
        throw new RuntimeException("Reserva no encontrada");
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
    
    private String generarCodigoReserva() {
        return "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    public List<Reserva> obtenerReservasEnRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        return reservaRepository.findReservasPorRangoFechas(fechaInicio, fechaFin);
    }
    
    public Long contarReservasPorHotelYEstado(String hotel, EstadoReserva estado) {
        return reservaRepository.contarReservasPorHotelYEstado(hotel, estado);
    }
    
    public Double calcularIngresosEnPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        Double ingresos = reservaRepository.calcularIngresosEnPeriodo(fechaInicio, fechaFin);
        return ingresos != null ? ingresos : 0.0;
    }
}