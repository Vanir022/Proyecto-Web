package Proyecto.Proyecto4.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import Proyecto.Proyecto4.models.Administrador;
import Proyecto.Proyecto4.models.Habitacion;
import Proyecto.Proyecto4.models.Reserva;
import Proyecto.Proyecto4.services.AdministradorService;
import Proyecto.Proyecto4.services.HabitacionService;
import Proyecto.Proyecto4.services.ReservaService;
import Proyecto.Proyecto4.services.UsuarioService;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {
    
    @Autowired
    private AdministradorService administradorService;
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private HabitacionService habitacionService;
    
    @Autowired
    private ReservaService reservaService;
    
    @GetMapping("/dashboard")
    public String dashboardAdmin(Authentication authentication, Model model) {
        String email = authentication.getName();
        
        // Buscar el administrador actual
        Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);
        if (adminOpt.isPresent()) {
            Administrador admin = adminOpt.get();
            model.addAttribute("admin", admin);
            
            // Estadísticas generales
            model.addAttribute("totalUsuarios", usuarioService.listar().size());
            model.addAttribute("totalHabitaciones", habitacionService.obtenerTodasLasHabitaciones().size());
            model.addAttribute("habitacionesDisponibles", habitacionService.obtenerHabitacionesDisponibles().size());
            
            // Reservas
            List<Reserva> reservasPendientes = reservaService.obtenerReservasPorEstado(Reserva.EstadoReserva.PENDIENTE);
            List<Reserva> reservasConfirmadas = reservaService.obtenerReservasPorEstado(Reserva.EstadoReserva.CONFIRMADA);
            
            model.addAttribute("reservasPendientes", reservasPendientes);
            model.addAttribute("totalReservasPendientes", reservasPendientes.size());
            model.addAttribute("totalReservasConfirmadas", reservasConfirmadas.size());
            
            // Si es admin de hotel específico, filtrar por su hotel
            if (admin.getHotel() != null) {
                model.addAttribute("hotelEspecifico", admin.getHotel());
                model.addAttribute("habitacionesHotel", 
                    habitacionService.obtenerHabitacionesPorHotel(admin.getHotel()).size());
            }
            
            // Lista de hoteles disponibles
            model.addAttribute("hoteles", habitacionService.obtenerHotelesDisponibles());
            
        }
        
        return "html/admin/dashboard";
    }
    
    @GetMapping("/usuarios")
    public String gestionUsuarios(Authentication authentication, Model model) {
        String email = authentication.getName();
        Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);
        
        if (adminOpt.isPresent()) {
            model.addAttribute("admin", adminOpt.get());
            model.addAttribute("usuarios", usuarioService.listar());
        }
        
        return "html/admin/usuarios";
    }
    
    @GetMapping("/habitaciones")
    public String gestionHabitaciones(Authentication authentication, Model model,
                                    @RequestParam(required = false) String hotel,
                                    @RequestParam(required = false) String tipo,
                                    @RequestParam(required = false) String estado) {
        String email = authentication.getName();
        Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);
        
        if (adminOpt.isPresent()) {
            Administrador admin = adminOpt.get();
            model.addAttribute("admin", admin);
            
            List<Habitacion> habitaciones;
            
            if (admin.getHotel() != null) {
                // Admin de hotel específico - solo sus habitaciones
                habitaciones = habitacionService.obtenerHabitacionesPorHotel(admin.getHotel());
            } else {
                // Super admin - todas las habitaciones
                habitaciones = habitacionService.obtenerTodasLasHabitaciones();
            }
            
            // Aplicar filtros
            if (hotel != null && !hotel.isEmpty()) {
                habitaciones = habitaciones.stream()
                    .filter(h -> h.getHotel().toLowerCase().contains(hotel.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            if (tipo != null && !tipo.isEmpty()) {
                habitaciones = habitaciones.stream()
                    .filter(h -> h.getTipo().toLowerCase().contains(tipo.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            if (estado != null && !estado.isEmpty()) {
                habitaciones = habitaciones.stream()
                    .filter(h -> h.getEstadoHabitacion().name().equals(estado))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            model.addAttribute("habitaciones", habitaciones);
            model.addAttribute("hoteles", habitacionService.obtenerHotelesDisponibles());
            
            // Mantener los valores de filtro
            model.addAttribute("filtroHotel", hotel);
            model.addAttribute("filtroTipo", tipo);
            model.addAttribute("filtroEstado", estado);
        }
        
        return "html/admin/habitaciones";
    }
    
    @GetMapping("/reservas")
    public String gestionReservas(Authentication authentication, Model model) {
        String email = authentication.getName();
        Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);
        
        if (adminOpt.isPresent()) {
            model.addAttribute("admin", adminOpt.get());
            model.addAttribute("reservas", reservaService.obtenerTodasLasReservas());
        }
        
        return "html/admin/reservas";
    }
    
    // ===== ENDPOINTS REST PARA HABITACIONES =====
    
    @PostMapping("/habitaciones/crear")
    @ResponseBody
    public ResponseEntity<?> crearHabitacion(@RequestBody Map<String, Object> datos, Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);
            
            if (!adminOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Administrador no encontrado"));
            }
            
            Administrador admin = adminOpt.get();
            
            // Crear nueva habitación
            Habitacion habitacion = new Habitacion();
            habitacion.setNumero((String) datos.get("numero"));
            habitacion.setTipo((String) datos.get("tipo"));
            habitacion.setCapacidad(Integer.parseInt(datos.get("capacidad").toString()));
            habitacion.setPrecio(new BigDecimal(datos.get("precio").toString()));
            habitacion.setHotel((String) datos.get("hotel"));
            habitacion.setDisponible(Boolean.parseBoolean(datos.get("disponible").toString()));
            habitacion.setAmenidades((String) datos.get("amenidades"));
            habitacion.setVista((String) datos.get("vista"));
            habitacion.setCama((String) datos.get("cama"));
            habitacion.setMetrosCuadrados(Integer.parseInt(datos.get("metrosCuadrados").toString()));
            habitacion.setDescripcion((String) datos.get("descripcion"));
            
            // Verificar permisos de hotel si es admin específico
            if (admin.getHotel() != null && !admin.getHotel().equals(habitacion.getHotel())) {
                return ResponseEntity.badRequest().body(Map.of("error", "No tiene permisos para crear habitaciones en este hotel"));
            }
            
            Habitacion nuevaHabitacion = habitacionService.guardarHabitacion(habitacion);
            
            return ResponseEntity.ok().body(Map.of(
                "message", "Habitación creada exitosamente",
                "id", nuevaHabitacion.getId(),
                "numero", nuevaHabitacion.getNumero(),
                "hotel", nuevaHabitacion.getHotel()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error al crear habitación: " + e.getMessage()));
        }
    }
    
    @PostMapping("/habitaciones/{id}/editar")
    @ResponseBody
    public ResponseEntity<?> editarHabitacion(@PathVariable Long id, @RequestBody Map<String, Object> datos, Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);
            
            if (!adminOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Administrador no encontrado"));
            }
            
            Administrador admin = adminOpt.get();
            Optional<Habitacion> habitacionOpt = habitacionService.obtenerHabitacionPorId(id);
            
            if (!habitacionOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Habitación no encontrada"));
            }
            
            Habitacion habitacion = habitacionOpt.get();
            
            // Verificar permisos de hotel si es admin específico
            if (admin.getHotel() != null && !admin.getHotel().equals(habitacion.getHotel())) {
                return ResponseEntity.badRequest().body(Map.of("error", "No tiene permisos para editar esta habitación"));
            }
            
            // Actualizar datos
            habitacion.setNumero((String) datos.get("numero"));
            habitacion.setTipo((String) datos.get("tipo"));
            habitacion.setCapacidad(Integer.parseInt(datos.get("capacidad").toString()));
            habitacion.setPrecio(new BigDecimal(datos.get("precio").toString()));
            habitacion.setHotel((String) datos.get("hotel"));
            habitacion.setDisponible(Boolean.parseBoolean(datos.get("disponible").toString()));
            habitacion.setAmenidades((String) datos.get("amenidades"));
            habitacion.setVista((String) datos.get("vista"));
            habitacion.setCama((String) datos.get("cama"));
            habitacion.setMetrosCuadrados(Integer.parseInt(datos.get("metrosCuadrados").toString()));
            habitacion.setDescripcion((String) datos.get("descripcion"));
            
            Habitacion habitacionActualizada = habitacionService.guardarHabitacion(habitacion);
            
            return ResponseEntity.ok().body(Map.of(
                "message", "Habitación actualizada exitosamente",
                "id", habitacionActualizada.getId()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error al actualizar habitación: " + e.getMessage()));
        }
    }
    
    @PostMapping("/habitaciones/{id}/cambiar-disponibilidad")
    @ResponseBody
    public ResponseEntity<?> cambiarDisponibilidadHabitacion(@PathVariable Long id, Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);
            
            if (!adminOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Administrador no encontrado"));
            }
            
            Administrador admin = adminOpt.get();
            Optional<Habitacion> habitacionOpt = habitacionService.obtenerHabitacionPorId(id);
            
            if (!habitacionOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Habitación no encontrada"));
            }
            
            Habitacion habitacion = habitacionOpt.get();
            
            // Verificar permisos de hotel si es admin específico
            if (admin.getHotel() != null && !admin.getHotel().equals(habitacion.getHotel())) {
                return ResponseEntity.badRequest().body(Map.of("error", "No tiene permisos para modificar esta habitación"));
            }
            
            // Cambiar disponibilidad
            habitacion.setDisponible(!habitacion.getDisponible());
            habitacionService.guardarHabitacion(habitacion);
            
            String estado = habitacion.getDisponible() ? "disponible" : "bloqueada";
            
            return ResponseEntity.ok().body(Map.of(
                "message", "Habitación " + estado + " exitosamente",
                "disponible", habitacion.getDisponible()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error al cambiar disponibilidad: " + e.getMessage()));
        }
    }
    
    @PostMapping("/habitaciones/{id}/estado")
    @ResponseBody
    public ResponseEntity<?> cambiarEstadoHabitacion(@PathVariable Long id, @RequestBody Map<String, String> request, Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);
            
            if (!adminOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Administrador no encontrado"));
            }
            
            Administrador admin = adminOpt.get();
            Optional<Habitacion> habitacionOpt = habitacionService.obtenerHabitacionPorId(id);
            
            if (!habitacionOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Habitación no encontrada"));
            }
            
            Habitacion habitacion = habitacionOpt.get();
            
            // Verificar permisos de hotel si es admin específico
            if (admin.getHotel() != null && !admin.getHotel().equals(habitacion.getHotel())) {
                return ResponseEntity.badRequest().body(Map.of("error", "No tiene permisos para modificar esta habitación"));
            }
            
            String nuevoEstado = request.get("estado");
            if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Estado no especificado"));
            }
            
            // Validar estado
            try {
                Habitacion.EstadoHabitacion estadoEnum = Habitacion.EstadoHabitacion.valueOf(nuevoEstado.toUpperCase());
                habitacion.setEstadoHabitacion(estadoEnum);
                
                // Actualizar disponibilidad basada en el estado
                if (estadoEnum == Habitacion.EstadoHabitacion.LIBRE) {
                    habitacion.setDisponible(true);
                } else {
                    habitacion.setDisponible(false);
                }
                
                habitacionService.guardarHabitacion(habitacion);
                
                String estadoTexto = estadoEnum.name().equals("LIBRE") ? "Libre" : 
                                   estadoEnum.name().equals("OCUPADA") ? "Ocupada" : 
                                   estadoEnum.name().equals("MANTENIMIENTO") ? "En Mantenimiento" : "Bloqueada";
                
                return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "Estado de habitación cambiado a " + estadoTexto + " exitosamente",
                    "estado", estadoEnum.name(),
                    "disponible", habitacion.getDisponible()
                ));
                
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Estado inválido: " + nuevoEstado));
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error al cambiar estado: " + e.getMessage()));
        }
    }
    
    @GetMapping("/habitaciones/{id}/detalle")
    @ResponseBody
    public ResponseEntity<?> obtenerDetalleHabitacion(@PathVariable Long id, Authentication authentication) {
        try {
            Optional<Habitacion> habitacionOpt = habitacionService.obtenerHabitacionPorId(id);
            
            if (!habitacionOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Habitación no encontrada"));
            }
            
            Habitacion habitacion = habitacionOpt.get();
            
            Map<String, Object> response = Map.of(
                "id", habitacion.getId(),
                "numero", habitacion.getNumero(),
                "tipo", habitacion.getTipo(),
                "capacidad", habitacion.getCapacidad(),
                "precio", habitacion.getPrecio(),
                "hotel", habitacion.getHotel(),
                "disponible", habitacion.getDisponible(),
                "amenidades", habitacion.getAmenidades(),
                "vista", habitacion.getVista(),
                "cama", habitacion.getCama()
            );
            
            return ResponseEntity.ok().body(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error al obtener detalle: " + e.getMessage()));
        }
    }
    
    // ===== ENDPOINTS REST PARA GESTIÓN DE RESERVAS =====
    
    @PostMapping("/reservas/{id}/aprobar")
    @ResponseBody
    public ResponseEntity<?> aprobarReserva(@PathVariable Long id, Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);
            
            if (!adminOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Administrador no encontrado"));
            }
            
            Administrador admin = adminOpt.get();
            Optional<Reserva> reservaOpt = reservaService.obtenerReservaPorId(id);
            
            if (!reservaOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Reserva no encontrada"));
            }
            
            Reserva reserva = reservaOpt.get();
            
            // Verificar permisos de hotel si es admin específico
            if (admin.getHotel() != null && !admin.getHotel().equals(reserva.getHabitacion().getHotel())) {
                return ResponseEntity.badRequest().body(Map.of("error", "No tiene permisos para aprobar reservas de este hotel"));
            }
            
            // Verificar que la reserva esté pendiente
            if (!reserva.getEstado().equals(Reserva.EstadoReserva.PENDIENTE)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Solo se pueden aprobar reservas pendientes"));
            }
            
            // Aprobar la reserva
            reserva = reservaService.actualizarEstadoReserva(id, Reserva.EstadoReserva.CONFIRMADA);
            
            return ResponseEntity.ok().body(Map.of(
                "message", "Reserva aprobada exitosamente",
                "codigo", reserva.getCodigoReserva(),
                "estado", "CONFIRMADA"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error al aprobar reserva: " + e.getMessage()));
        }
    }
    
    @PostMapping("/reservas/{id}/rechazar")
    @ResponseBody
    public ResponseEntity<?> rechazarReserva(@PathVariable Long id, Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);
            
            if (!adminOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Administrador no encontrado"));
            }
            
            Administrador admin = adminOpt.get();
            Optional<Reserva> reservaOpt = reservaService.obtenerReservaPorId(id);
            
            if (!reservaOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Reserva no encontrada"));
            }
            
            Reserva reserva = reservaOpt.get();
            
            // Verificar permisos de hotel si es admin específico
            if (admin.getHotel() != null && !admin.getHotel().equals(reserva.getHabitacion().getHotel())) {
                return ResponseEntity.badRequest().body(Map.of("error", "No tiene permisos para rechazar reservas de este hotel"));
            }
            
            // Verificar que la reserva esté pendiente
            if (!reserva.getEstado().equals(Reserva.EstadoReserva.PENDIENTE)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Solo se pueden rechazar reservas pendientes"));
            }
            
            // Rechazar la reserva
            reserva = reservaService.actualizarEstadoReserva(id, Reserva.EstadoReserva.CANCELADA);
            
            return ResponseEntity.ok().body(Map.of(
                "message", "Reserva rechazada exitosamente",
                "codigo", reserva.getCodigoReserva(),
                "estado", "CANCELADA"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error al rechazar reserva: " + e.getMessage()));
        }
    }
    
    @PostMapping("/reservas/{id}/completar")
    @ResponseBody
    public ResponseEntity<?> completarReserva(@PathVariable Long id, Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);
            
            if (!adminOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Administrador no encontrado"));
            }
            
            Administrador admin = adminOpt.get();
            Optional<Reserva> reservaOpt = reservaService.obtenerReservaPorId(id);
            
            if (!reservaOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Reserva no encontrada"));
            }
            
            Reserva reserva = reservaOpt.get();
            
            // Verificar permisos de hotel si es admin específico
            if (admin.getHotel() != null && !admin.getHotel().equals(reserva.getHabitacion().getHotel())) {
                return ResponseEntity.badRequest().body(Map.of("error", "No tiene permisos para completar reservas de este hotel"));
            }
            
            // Verificar que la reserva esté confirmada
            if (!reserva.getEstado().equals(Reserva.EstadoReserva.CONFIRMADA)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Solo se pueden completar reservas confirmadas"));
            }
            
            // Completar la reserva
            reserva = reservaService.actualizarEstadoReserva(id, Reserva.EstadoReserva.COMPLETADA);
            
            return ResponseEntity.ok().body(Map.of(
                "message", "Reserva completada exitosamente",
                "codigo", reserva.getCodigoReserva(),
                "estado", "COMPLETADA"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error al completar reserva: " + e.getMessage()));
        }
    }
    
    @GetMapping("/estadisticas/habitaciones")
    @ResponseBody
    public ResponseEntity<?> obtenerEstadisticasHabitaciones(Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);
            
            if (!adminOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Administrador no encontrado"));
            }
            
            Administrador admin = adminOpt.get();
            
            Map<String, Integer> estadisticas = Map.of(
                "libres", habitacionService.obtenerHabitacionesLibres().size(),
                "ocupadas", habitacionService.obtenerHabitacionesOcupadas().size(), 
                "mantenimiento", habitacionService.obtenerHabitacionesPorEstado(Habitacion.EstadoHabitacion.MANTENIMIENTO).size(),
                "bloqueadas", habitacionService.obtenerHabitacionesPorEstado(Habitacion.EstadoHabitacion.BLOQUEADA).size()
            );
            
            return ResponseEntity.ok().body(estadisticas);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error al obtener estadísticas: " + e.getMessage()));
        }
    }
}