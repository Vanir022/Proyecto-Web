package Proyecto.Proyecto4.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import Proyecto.Proyecto4.models.Habitacion;
import Proyecto.Proyecto4.repository.HabitacionRepository;

@Service
public class HabitacionService {

    @Autowired
    private HabitacionRepository habitacionRepository;

    public List<Habitacion> obtenerTodasLasHabitaciones() {
        return habitacionRepository.findAll();
    }

    public List<Habitacion> obtenerHabitacionesDisponibles() {
        return habitacionRepository.findByDisponible(true);
    }

    public List<Habitacion> obtenerHabitacionesPorHotel(String hotel) {
        return habitacionRepository.findByHotel(hotel);
    }

    public List<Habitacion> obtenerHabitacionesPorTipo(String tipo) {
        return habitacionRepository.findByTipoAndDisponible(tipo, true);
    }

    public List<Habitacion> obtenerHabitacionesPorCapacidad(Integer capacidad) {
        return habitacionRepository.findByCapacidadGreaterThanEqualAndDisponible(capacidad);
    }

    public List<Habitacion> obtenerHabitacionesPorRangoPrecio(Double precioMin, Double precioMax) {
        return habitacionRepository.findByPrecioBetweenAndDisponible(precioMin, precioMax);
    }

    public Optional<Habitacion> obtenerHabitacionPorId(Long id) {
        return habitacionRepository.findById(id);
    }

    public Habitacion guardarHabitacion(Habitacion habitacion) {
        return habitacionRepository.save(habitacion);
    }

    public void eliminarHabitacion(Long id) {
        habitacionRepository.deleteById(id);
    }

    public List<String> obtenerHotelesDisponibles() {
        return habitacionRepository.findDistinctHoteles();
    }

    public List<String> obtenerTiposPorHotel(String hotel) {
        return habitacionRepository.findDistinctTiposByHotel(hotel);
    }

    public void cambiarDisponibilidad(Long id, Boolean disponible) {
        Optional<Habitacion> habitacionOpt = habitacionRepository.findById(id);
        if (habitacionOpt.isPresent()) {
            Habitacion habitacion = habitacionOpt.get();
            habitacion.setDisponible(disponible);
            habitacionRepository.save(habitacion);
        }
    }

    public List<Habitacion> buscarHabitaciones(String hotel, String tipo, Integer capacidad, Double precioMax) {
        // Obtener solo habitaciones en estado LIBRE (disponibles para reserva)
        List<Habitacion> habitaciones = habitacionRepository.findByEstadoHabitacion(Habitacion.EstadoHabitacion.LIBRE);

        return habitaciones.stream()
                .filter(h -> hotel == null || hotel.isEmpty() || h.getHotel().equalsIgnoreCase(hotel))
                .filter(h -> tipo == null || tipo.isEmpty() || h.getTipo().equalsIgnoreCase(tipo))
                .filter(h -> capacidad == null || h.getCapacidad() >= capacidad)
                .filter(h -> precioMax == null || h.getPrecio().doubleValue() <= precioMax)
                .toList();
    }

    // ===== MÉTODOS PARA MANEJO DE ESTADO DE HABITACIONES =====

    public void cambiarEstadoHabitacion(Long habitacionId, Habitacion.EstadoHabitacion nuevoEstado) {
        Optional<Habitacion> habitacionOpt = habitacionRepository.findById(habitacionId);
        if (habitacionOpt.isPresent()) {
            Habitacion habitacion = habitacionOpt.get();
            habitacion.setEstadoHabitacion(nuevoEstado);
            habitacionRepository.save(habitacion);
        } else {
            throw new RuntimeException("Habitación no encontrada con ID: " + habitacionId);
        }
    }

    public void marcarHabitacionComoOcupada(Long habitacionId) {
        cambiarEstadoHabitacion(habitacionId, Habitacion.EstadoHabitacion.OCUPADA);
    }

    public void marcarHabitacionComoLibre(Long habitacionId) {
        cambiarEstadoHabitacion(habitacionId, Habitacion.EstadoHabitacion.LIBRE);
    }

    public void marcarHabitacionEnMantenimiento(Long habitacionId) {
        cambiarEstadoHabitacion(habitacionId, Habitacion.EstadoHabitacion.MANTENIMIENTO);
    }

    public void bloquearHabitacion(Long habitacionId) {
        cambiarEstadoHabitacion(habitacionId, Habitacion.EstadoHabitacion.BLOQUEADA);
    }

    public List<Habitacion> obtenerHabitacionesPorEstado(Habitacion.EstadoHabitacion estado) {
        return habitacionRepository.findByEstadoHabitacion(estado);
    }

    public List<Habitacion> obtenerHabitacionesLibres() {
        return obtenerHabitacionesPorEstado(Habitacion.EstadoHabitacion.LIBRE);
    }

    public List<Habitacion> obtenerHabitacionesOcupadas() {
        return obtenerHabitacionesPorEstado(Habitacion.EstadoHabitacion.OCUPADA);
    }

    public boolean esHabitacionLibre(Long habitacionId) {
        Optional<Habitacion> habitacionOpt = habitacionRepository.findById(habitacionId);
        return habitacionOpt.isPresent() &&
                habitacionOpt.get().getEstadoHabitacion() == Habitacion.EstadoHabitacion.LIBRE;
    }
}