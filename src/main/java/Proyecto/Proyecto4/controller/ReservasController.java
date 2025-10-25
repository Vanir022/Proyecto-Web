package Proyecto.Proyecto4.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import Proyecto.Proyecto4.models.Habitacion;
import Proyecto.Proyecto4.models.Reserva;
import Proyecto.Proyecto4.models.Usuario;
import Proyecto.Proyecto4.repository.UsuarioRepository;
import Proyecto.Proyecto4.services.HabitacionService;
import Proyecto.Proyecto4.services.ReservaService;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Controller
@RequestMapping("/reservas")
@Validated
public class ReservasController {
    
    @Autowired
    private HabitacionService habitacionService;
    
    @Autowired
    private ReservaService reservaService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @GetMapping
    public String mostrarReservas(Model model, Authentication authentication) {
        // Obtener hoteles disponibles
        List<String> hoteles = habitacionService.obtenerHotelesDisponibles();
        model.addAttribute("hoteles", hoteles);
        
        // Si el usuario está autenticado, obtener sus reservas
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
            
            if (usuarioOpt.isPresent()) {
                List<Reserva> misReservas = reservaService.obtenerReservasPorUsuario(usuarioOpt.get());
                model.addAttribute("misReservas", misReservas);
            }
        }
        
        return "html/cliente-reservas";
    }
    
    @GetMapping("/buscar")
    public String buscarHabitaciones(
            @RequestParam(required = false) String hotel,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Integer capacidad,
            @RequestParam(required = false) Double precioMax,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEntrada,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaSalida,
            Model model) {
        
        List<Habitacion> habitaciones = habitacionService.buscarHabitaciones(hotel, tipo, capacidad, precioMax);
        
        // Filtrar por disponibilidad de fechas si se especifican
        if (fechaEntrada != null && fechaSalida != null) {
            habitaciones = habitaciones.stream()
                    .filter(h -> reservaService.verificarDisponibilidad(h, fechaEntrada, fechaSalida))
                    .toList();
        }
        
        model.addAttribute("habitaciones", habitaciones);
        model.addAttribute("hotel", hotel);
        model.addAttribute("tipo", tipo);
        model.addAttribute("capacidad", capacidad);
        model.addAttribute("precioMax", precioMax);
        model.addAttribute("fechaEntrada", fechaEntrada);
        model.addAttribute("fechaSalida", fechaSalida);
        
        // Obtener datos para filtros
        List<String> hoteles = habitacionService.obtenerHotelesDisponibles();
        model.addAttribute("hoteles", hoteles);
        
        return "html/buscar-habitaciones";
    }
    
    @GetMapping("/habitacion/{id}")
    public String verDetalleHabitacion(@PathVariable Long id, Model model) {
        Optional<Habitacion> habitacionOpt = habitacionService.obtenerHabitacionPorId(id);
        
        if (habitacionOpt.isPresent()) {
            model.addAttribute("habitacion", habitacionOpt.get());
            return "html/detalle-habitacion";
        }
        
        return "redirect:/reservas";
    }
    
    @PostMapping("/crear")
    public String crearReserva(
            @NotNull(message = "El ID de la habitación es obligatorio")
            @RequestParam Long habitacionId,
            @NotNull(message = "La fecha de entrada es obligatoria")
            @FutureOrPresent(message = "La fecha de entrada debe ser hoy o en el futuro")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEntrada,
            @NotNull(message = "La fecha de salida es obligatoria")
            @FutureOrPresent(message = "La fecha de salida debe ser hoy o en el futuro")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaSalida,
            @NotNull(message = "El número de huéspedes es obligatorio")
            @Min(value = 1, message = "Debe haber al menos 1 huésped")
            @RequestParam Integer numeroHuespedes,
            @RequestParam(required = false) String comentarios,
            @RequestParam(required = false) String solicitudesEspeciales,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para hacer una reserva");
                return "redirect:/login";
            }
            
            String email = authentication.getName();
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
            Optional<Habitacion> habitacionOpt = habitacionService.obtenerHabitacionPorId(habitacionId);
            
            if (usuarioOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
                return "redirect:/reservas";
            }
            
            if (habitacionOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Habitación no encontrada");
                return "redirect:/reservas";
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // Obtener DNI y teléfono del perfil del usuario
            String dniCliente = null;
            String telefonoContacto = null;
            
            if (usuario.getDetallesPersona() != null) {
                dniCliente = usuario.getDetallesPersona().getDni();
                telefonoContacto = usuario.getDetallesPersona().getTelefono();
            }
            
            // Validar que el usuario tenga DNI registrado
            if (dniCliente == null || dniCliente.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", 
                    "Debes completar tu perfil con tu DNI antes de hacer una reserva. " +
                    "Ve a 'Mi Perfil' para actualizarlo.");
                return "redirect:/perfil";
            }
            
            Reserva reserva = reservaService.crearReserva(
                usuario,
                habitacionOpt.get(),
                fechaEntrada,
                fechaSalida,
                numeroHuespedes,
                comentarios,
                telefonoContacto,
                solicitudesEspeciales,
                dniCliente
            );
            
            redirectAttributes.addFlashAttribute("success", 
                "Reserva creada exitosamente. Código de reserva: " + reserva.getCodigoReserva());
            
            return "redirect:/reservas";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al crear la reserva: " + e.getMessage());
            return "redirect:/reservas";
        }
    }
    
    @PostMapping("/cancelar/{id}")
    public String cancelarReserva(@PathVariable Long id, Authentication authentication, 
                                RedirectAttributes redirectAttributes) {
        
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión");
                return "redirect:/login";
            }
            
            String email = authentication.getName();
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
            Optional<Reserva> reservaOpt = reservaService.obtenerReservaPorId(id);
            
            if (usuarioOpt.isEmpty() || reservaOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Reserva no encontrada");
                return "redirect:/reservas";
            }
            
            Reserva reserva = reservaOpt.get();
            
            // Verificar que la reserva pertenece al usuario
            if (!reserva.getUsuario().getId().equals(usuarioOpt.get().getId())) {
                redirectAttributes.addFlashAttribute("error", "No tienes permisos para cancelar esta reserva");
                return "redirect:/reservas";
            }
            
            reservaService.cancelarReserva(id);
            redirectAttributes.addFlashAttribute("success", "Reserva cancelada exitosamente");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cancelar la reserva: " + e.getMessage());
        }
        
        return "redirect:/reservas";
    }
}