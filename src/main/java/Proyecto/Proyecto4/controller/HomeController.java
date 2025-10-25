package Proyecto.Proyecto4.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import Proyecto.Proyecto4.models.Reserva;
import Proyecto.Proyecto4.models.ReservaServicio;
import Proyecto.Proyecto4.models.Usuario;
import Proyecto.Proyecto4.services.ReservaService;
import Proyecto.Proyecto4.services.ReservaServicioService;
import Proyecto.Proyecto4.services.UsuarioService;

@Controller
public class HomeController {

    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private ReservaService reservaService;
    
    @Autowired
    private ReservaServicioService reservaServicioService;


    // Mapeo para el header
    @GetMapping("/")
    public String index() {
        return "index"; // busca templates/index.html
    }

    @GetMapping("/nosotros")
    public String nosotros() {
        return "html/Nosotros"; // Thymeleaf buscará templates/Nosotros.html
    }
    
    // Mantener la ruta original de contactos para evitar que suceda algo
    /*@GetMapping("/contactos")
    public String contactos() {
        return "redirect:/contactos";
    }*/
    
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                       @RequestParam(value = "logout", required = false) String logout,
                       Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Email o contraseña incorrectos. Por favor, inténtelo de nuevo.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Ha cerrado sesión exitosamente.");
        }
        return "html/login"; // Thymeleaf buscará templates/login.html
    }

    @GetMapping("/register")
    public String register() {
        return "html/Register"; // Thymeleaf buscará templates/register.html
        
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        model.addAttribute("email", email);
        
        // Buscar el usuario completo con sus detalles
        Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(email);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            model.addAttribute("usuario", usuario);
            
            if (usuario.getDetallesPersona() != null) {
                model.addAttribute("detalles", usuario.getDetallesPersona());
            }
            
            // ========== OBTENER RESERVAS DE HABITACIONES ==========
            List<Reserva> reservasHabitaciones = reservaService.obtenerReservasPorUsuario(usuario);
            model.addAttribute("reservasHabitaciones", reservasHabitaciones);
            
            // Contar reservas de habitaciones por estado
            long habitacionesPendientes = reservasHabitaciones.stream()
                .filter(r -> r.getEstado() == Reserva.EstadoReserva.PENDIENTE)
                .count();
            long habitacionesConfirmadas = reservasHabitaciones.stream()
                .filter(r -> r.getEstado() == Reserva.EstadoReserva.CONFIRMADA)
                .count();
            long habitacionesCompletadas = reservasHabitaciones.stream()
                .filter(r -> r.getEstado() == Reserva.EstadoReserva.COMPLETADA)
                .count();
            
            // ========== OBTENER RESERVAS DE SERVICIOS ==========
            List<ReservaServicio> reservasServicios = reservaServicioService.obtenerReservasPorUsuario(usuario);
            model.addAttribute("reservasServicios", reservasServicios);
            
            // Contar reservas de servicios por estado
            long serviciosPendientes = reservasServicios.stream()
                .filter(r -> r.getEstado() == ReservaServicio.EstadoReserva.PENDIENTE)
                .count();
            long serviciosConfirmadas = reservasServicios.stream()
                .filter(r -> r.getEstado() == ReservaServicio.EstadoReserva.CONFIRMADA)
                .count();
            long serviciosCompletadas = reservasServicios.stream()
                .filter(r -> r.getEstado() == ReservaServicio.EstadoReserva.COMPLETADA)
                .count();
            
            // ========== TOTALES COMBINADOS ==========
            long totalPendientes = habitacionesPendientes + serviciosPendientes;
            long totalConfirmadas = habitacionesConfirmadas + serviciosConfirmadas;
            long totalCompletadas = habitacionesCompletadas + serviciosCompletadas;
            long totalReservas = reservasHabitaciones.size() + reservasServicios.size();
            
            model.addAttribute("reservasPendientes", totalPendientes);
            model.addAttribute("reservasConfirmadas", totalConfirmadas);
            model.addAttribute("reservasCompletadas", totalCompletadas);
            model.addAttribute("totalReservas", totalReservas);
        }
        
        return "html/dashboard"; // Thymeleaf buscará templates/dashboard.html
    }

    //Mapeo para el footer
    @GetMapping("/acercade")
    public String acercade() {
        return "html/Acercade"; // Thymeleaf buscará templates/AcercaDe.html
    }

    @GetMapping("/eventos")
    public String eventos() {
        return "html/Servicios/Eventos"; // Thymeleaf buscará templates/Eventos.html
    }

    @GetMapping("/spa")
    public String spa() {
        return "html/Servicios/Spa"; // Thymeleaf buscará templates/Spa.html
    }
    @GetMapping("/bodas")
    public String bodas() {
        return "html/Servicios/Bodas"; // Thymeleaf buscará templates/Bodas.html
    }
}