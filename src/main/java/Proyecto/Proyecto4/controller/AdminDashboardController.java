package Proyecto.Proyecto4.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Proyecto.Proyecto4.models.Administrador;
import Proyecto.Proyecto4.models.DetallesPersona;
import Proyecto.Proyecto4.models.Habitacion;
import Proyecto.Proyecto4.models.Reserva;
import Proyecto.Proyecto4.models.Usuario;
import Proyecto.Proyecto4.services.AdministradorService;
import Proyecto.Proyecto4.services.HabitacionService;
import Proyecto.Proyecto4.services.ReservaService;
import Proyecto.Proyecto4.services.UsuarioService;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardController.class);

    @Autowired
    private AdministradorService administradorService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private HabitacionService habitacionService;

    @Autowired
    private ReservaService reservaService;

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportDashboardData(Authentication authentication) {
        String email = authentication.getName();
        StringBuilder csvData = new StringBuilder();
        
        // Encabezados
        csvData.append("Métrica,Valor\n");
        
        // Estadísticas generales
        int totalUsuarios = usuarioService.listar().size();
        int totalHabitaciones = habitacionService.obtenerTodasLasHabitaciones().size();
        int habitacionesDisponibles = habitacionService.obtenerTodasLasHabitaciones().size();
        List<Reserva> reservasPendientes = reservaService.obtenerReservasPorEstado(Reserva.EstadoReserva.PENDIENTE);
        List<Reserva> reservasConfirmadas = reservaService.obtenerReservasPorEstado(Reserva.EstadoReserva.CONFIRMADA);
        
        // Agregar datos al CSV
        csvData.append("Total Usuarios,").append(totalUsuarios).append("\n");
        csvData.append("Total Habitaciones,").append(totalHabitaciones).append("\n");
        csvData.append("Habitaciones Disponibles,").append(habitacionesDisponibles).append("\n");
        csvData.append("Reservas Pendientes,").append(reservasPendientes.size()).append("\n");
        csvData.append("Reservas Confirmadas,").append(reservasConfirmadas.size()).append("\n");
        
        // Configurar la respuesta HTTP
        byte[] bytes = csvData.toString().getBytes();
        return ResponseEntity
            .ok()
            .header("Content-Type", "text/csv")
            .header("Content-Disposition", "attachment; filename=dashboard_stats.csv")
            .body(bytes);
    }

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
            // Mostrar total de habitaciones en el sistema (no solo las libres)
            model.addAttribute("habitacionesDisponibles", habitacionService.obtenerTodasLasHabitaciones().size());
            
            // Reservas
            List<Reserva> reservasPendientes = reservaService.obtenerReservasPorEstado(Reserva.EstadoReserva.PENDIENTE);
            List<Reserva> reservasConfirmadas = reservaService
                    .obtenerReservasPorEstado(Reserva.EstadoReserva.CONFIRMADA);

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

    @GetMapping("/usuarios/{id}/datos")
    @ResponseBody
    public ResponseEntity<?> obtenerDatosUsuario(@PathVariable Long id, Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);

            if (!adminOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Administrador no encontrado"));
            }

            Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
            if (!usuarioOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Usuario no encontrado"));
            }

            Usuario usuario = usuarioOpt.get();
            DetallesPersona detalles = usuario.getDetallesPersona();

            Map<String, Object> response = Map.of(
                    "id", usuario.getId(),
                    "nombre", usuario.getNombre(),
                    "email", usuario.getEmail(),
                    "apellidos", detalles != null ? detalles.getApellidos() : "",
                    "dni", detalles != null ? detalles.getDni() : "",
                    "telefono", detalles != null ? detalles.getTelefono() : "",
                    "fechaNacimiento", detalles != null && detalles.getFechaNacimiento() != null ? detalles.getFechaNacimiento().toString() : "",
                    "intereses", detalles != null ? detalles.getIntereses() : "",
                    "aceptaMarketing", detalles != null ? detalles.getAceptaMarketing() : false);

            return ResponseEntity.ok().body(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error al obtener datos del usuario: " + e.getMessage()));
        }
    }

    //EDITAR DATOS DEL USUARIO EN PANEL DE ADMINISTRACIÓN O DASHBOARD DE ADMIN
    @PutMapping("/usuarios/{id}")
    @ResponseBody
    public ResponseEntity<?> editarUsuario(@PathVariable Long id, @RequestBody Map<String, Object> datos, Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);

            if (!adminOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Administrador no encontrado"));
            }

            Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
            if (!usuarioOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Usuario no encontrado"));
            }

            Usuario usuario = usuarioOpt.get();

            // Actualizar datos básicos del usuario
            if (datos.containsKey("nombre") && datos.get("nombre") != null) {
                usuario.setNombre((String) datos.get("nombre"));
            }
            if (datos.containsKey("email") && datos.get("email") != null) {
                String nuevoEmail = (String) datos.get("email");
                // Verificar si el email ya existe en otro usuario
                Optional<Usuario> usuarioExistente = usuarioService.buscarPorEmail(nuevoEmail);
                if (usuarioExistente.isPresent() && !usuarioExistente.get().getId().equals(id)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "El email ya está registrado por otro usuario"));
                }
                usuario.setEmail(nuevoEmail);
            }

            // Manejar cambio de contraseña
            if (datos.containsKey("password") && datos.get("password") != null && !((String) datos.get("password")).trim().isEmpty()) {
                String nuevaPassword = (String) datos.get("password");
                String confirmarPassword = (String) datos.get("confirmarPassword");

                if (!nuevaPassword.equals(confirmarPassword)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Las contraseñas no coinciden"));
                }

                usuarioService.cambiarPassword(usuario, nuevaPassword);
            }

            // Actualizar detalles personales
            DetallesPersona detalles = usuario.getDetallesPersona();
            if (detalles == null) {
                detalles = new DetallesPersona();
                usuario.setDetallesPersona(detalles);
            }

            if (datos.containsKey("apellidos")) {
                detalles.setApellidos((String) datos.get("apellidos"));
            }
            if (datos.containsKey("dni")) {
                String nuevoDni = (String) datos.get("dni");
                if (nuevoDni != null && !nuevoDni.trim().isEmpty()) {
                    // Verificar si el DNI ya existe en otro usuario
                    // Nota: Aquí necesitarías un método en el servicio para verificar DNI único
                    detalles.setDni(nuevoDni);
                }
            }
            if (datos.containsKey("telefono")) {
                detalles.setTelefono((String) datos.get("telefono"));
            }
            // Actualizar fecha de nacimiento: Se utiliza LocalDate.parse() en lugar de java.sql.Date.valueOf()
            // para evitar problemas de compatibilidad con el formato de fecha esperado por LocalDate.
            // LocalDate.parse() espera el formato ISO-8601 (YYYY-MM-DD) que es más estándar y compatible.
            if (datos.containsKey("fechaNacimiento")) {
                String fechaStr = (String) datos.get("fechaNacimiento");
                if (fechaStr != null && !fechaStr.trim().isEmpty()) {
                    detalles.setFechaNacimiento(LocalDate.parse(fechaStr));
                }
            }
            if (datos.containsKey("intereses")) {
                detalles.setIntereses((String) datos.get("intereses"));
            }
            if (datos.containsKey("aceptaMarketing")) {
                detalles.setAceptaMarketing(Boolean.parseBoolean(datos.get("aceptaMarketing").toString()));
            }

            // Guardar cambios
            usuarioService.guardar(usuario);

            return ResponseEntity.ok().body(Map.of(
                    "message", "Usuario actualizado exitosamente",
                    "id", usuario.getId()));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error al actualizar usuario: " + e.getMessage()));
        }
    }

    @GetMapping("/usuarios/{id}/detalle")
    @ResponseBody
    public String obtenerDetalleUsuario(@PathVariable Long id, HttpServletRequest request, Authentication authentication) {
        try {
            logger.info("Request received: {} {}", request.getMethod(), request.getRequestURI());
            String email = authentication.getName();
            Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);

            if (!adminOpt.isPresent()) {
                return "<div class='alert alert-danger'>Error: Administrador no encontrado</div>";
            }

            Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
            if (!usuarioOpt.isPresent()) {
                return "<div class='alert alert-danger'>Usuario no encontrado</div>";
            }

            Usuario usuario = usuarioOpt.get();
            List<Reserva> reservas = reservaService.obtenerReservasPorUsuario(usuario);

            StringBuilder html = new StringBuilder();
            html.append("<div class='row'>");

            // Información personal del usuario
            html.append("<div class='col-md-6'>");
            html.append("<div class='card h-100'>");
            html.append("<div class='card-header bg-info text-white'>");
            html.append("<h6 class='mb-0'><i class='fas fa-user me-2'></i>Información Personal</h6>");
            html.append("</div>");
            html.append("<div class='card-body'>");

            if (usuario.getDetallesPersona() != null && usuario.getDetallesPersona().getFotoPerfil() != null) {
                html.append("<div class='text-center mb-3'>");
                html.append("<img src='/uploads/fotos/").append(usuario.getDetallesPersona().getFotoPerfil())
                        .append("' ");
                html.append("class='rounded-circle' style='width: 100px; height: 100px; object-fit: cover;'>");
                html.append("</div>");
            } else {
                html.append("<div class='text-center mb-3'>");
                html.append("<i class='fas fa-user-circle fa-5x text-secondary'></i>");
                html.append("</div>");
            }

            html.append("<table class='table table-borderless'>");
            html.append("<tr><td><strong>ID:</strong></td><td>").append(usuario.getId()).append("</td></tr>");
            html.append("<tr><td><strong>Nombre:</strong></td><td>").append(usuario.getNombre()).append("</td></tr>");
            html.append("<tr><td><strong>Email:</strong></td><td>").append(usuario.getEmail()).append("</td></tr>");
            html.append("<tr><td><strong>Rol:</strong></td><td><span class='badge bg-info'>").append(usuario.getRol())
                    .append("</span></td></tr>");

            if (usuario.getDetallesPersona() != null) {
                DetallesPersona detalles = usuario.getDetallesPersona();
                html.append("<tr><td><strong>Apellidos:</strong></td><td>")
                        .append(detalles.getApellidos() != null ? detalles.getApellidos() : "No especificado")
                        .append("</td></tr>");
                html.append("<tr><td><strong>DNI:</strong></td><td>")
                        .append(detalles.getDni() != null ? detalles.getDni() : "No especificado").append("</td></tr>");
                html.append("<tr><td><strong>Teléfono:</strong></td><td>")
                        .append(detalles.getTelefono() != null ? detalles.getTelefono() : "No especificado")
                        .append("</td></tr>");
                html.append("<tr><td><strong>Fecha Nac.:</strong></td><td>")
                        .append(detalles.getFechaNacimiento() != null ? detalles.getFechaNacimiento().toString()
                                : "No especificado")
                        .append("</td></tr>");
                html.append("<tr><td><strong>Intereses:</strong></td><td>")
                        .append(detalles.getIntereses() != null ? detalles.getIntereses() : "No especificado")
                        .append("</td></tr>");
                html.append("<tr><td><strong>Marketing:</strong></td><td>")
                        .append(detalles.getAceptaMarketing() != null && detalles.getAceptaMarketing() ? "Sí" : "No")
                        .append("</td></tr>");
            } else {
                html.append(
                        "<tr><td colspan='2'><em class='text-muted'>No hay detalles personales registrados</em></td></tr>");
            }

            html.append("</table>");
            html.append("</div></div></div>");

            // Historial de reservas
            html.append("<div class='col-md-6'>");
            html.append("<div class='card h-100'>");
            html.append("<div class='card-header bg-success text-white'>");
            html.append("<h6 class='mb-0'><i class='fas fa-calendar-check me-2'></i>Historial de Reservas (")
                    .append(reservas.size()).append(")</h6>");
            html.append("</div>");
            html.append("<div class='card-body' style='max-height: 400px; overflow-y: auto;'>");

            if (!reservas.isEmpty()) {
                for (Reserva reserva : reservas) {
                    String estadoClass = "";
                    switch (reserva.getEstado()) {
                        case CONFIRMADA -> estadoClass = "bg-success";
                        case PENDIENTE -> estadoClass = "bg-warning";
                        case CANCELADA -> estadoClass = "bg-danger";
                        case COMPLETADA -> estadoClass = "bg-info";
                        default -> estadoClass = "bg-secondary";
                    }

                    html.append("<div class='card mb-2 border-left-").append(estadoClass.replace("bg-", ""))
                            .append("'>");
                    html.append("<div class='card-body p-2'>");
                    html.append("<div class='d-flex justify-content-between align-items-center'>");
                    html.append("<h6 class='card-title mb-1'>").append(reserva.getCodigoReserva()).append("</h6>");
                    html.append("<span class='badge ").append(estadoClass).append("'>").append(reserva.getEstado())
                            .append("</span>");
                    html.append("</div>");
                    html.append("<p class='card-text mb-1'><small>");
                    html.append("<strong>Hotel:</strong> ").append(reserva.getHabitacion().getHotel()).append("<br>");
                    html.append("<strong>Habitación:</strong> ").append(reserva.getHabitacion().getNumero())
                            .append(" (").append(reserva.getHabitacion().getTipo()).append(")<br>");
                    html.append("<strong>Fechas:</strong> ").append(reserva.getFechaEntrada()).append(" - ")
                            .append(reserva.getFechaSalida()).append("<br>");
                    html.append("<strong>Huéspedes:</strong> ").append(reserva.getNumeroHuespedes()).append("<br>");
                    html.append("<strong>Total:</strong> S/ ").append(String.format("%.2f", reserva.getMontoTotal()));
                    html.append("</small></p>");
                    html.append("</div></div>");
                }
            } else {
                html.append("<div class='text-center text-muted py-4'>");
                html.append("<i class='fas fa-calendar-times fa-3x mb-3'></i>");
                html.append("<p>Este usuario no tiene reservas registradas</p>");
                html.append("</div>");
            }

            // Añadir botón de editar
            html.append("<div class='text-center mt-3'>");
            html.append("<button id='btnEditarUsuarioDetalle' class='btn btn-warning'>");
            html.append("<i class='fas fa-edit me-2'></i>Editar Usuario");
            html.append("</button>");
            html.append("</div>");
            
            html.append("</div></div></div>");
            html.append("</div>");

            return html.toString();

        } catch (Exception e) {
            return "<div class='alert alert-danger'>Error al cargar los detalles del usuario: " + e.getMessage()
                    + "</div>";
        }
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

            // Ordenar habitaciones: primero por número (mayor a menor), luego por precio
            // (mayor a menor)
            habitaciones = habitaciones.stream()
                    .sorted((h1, h2) -> {
                        // Convertir números de habitación a enteros para comparación numérica
                        int num1 = 0, num2 = 0;
                        try {
                            num1 = Integer.parseInt(h1.getNumero().replaceAll("[^0-9]", ""));
                        } catch (NumberFormatException e) {
                            num1 = 0;
                        }
                        try {
                            num2 = Integer.parseInt(h2.getNumero().replaceAll("[^0-9]", ""));
                        } catch (NumberFormatException e) {
                            num2 = 0;
                        }

                        // Ordenar por número (mayor a menor)
                        int numeroComparison = Integer.compare(num2, num1);
                        if (numeroComparison != 0) {
                            return numeroComparison;
                        }

                        // Si los números son iguales, ordenar por precio (mayor a menor)
                        return h2.getPrecio().compareTo(h1.getPrecio());
                    })
                    .collect(java.util.stream.Collectors.toList());

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
            Administrador admin = adminOpt.get();
            model.addAttribute("admin", admin);

            // Obtener todas las reservas SIN FILTRAR
            List<Reserva> todasLasReservas = reservaService.obtenerTodasLasReservas();

            // Debug: Imprimir información básica
            System.out.println("=== DEBUG RESERVAS ===");
            System.out.println("Total reservas encontradas en BD: " + todasLasReservas.size());
            System.out.println("Admin email: " + admin.getEmail());
            System.out.println("Admin rol: " + admin.getRol());
            System.out.println("Admin hotel: " + admin.getHotel());

            // TEMPORALMENTE COMENTADO - NO FILTRAR POR HOTEL PARA VER TODAS LAS RESERVAS
            /*
             * if (admin.getHotel() != null) {
             * List<Reserva> reservasOriginales = new ArrayList<>(todasLasReservas);
             * todasLasReservas = todasLasReservas.stream()
             * .filter(r -> r.getHabitacion() != null && r.getHabitacion().getHotel() !=
             * null
             * && r.getHabitacion().getHotel().equals(admin.getHotel()))
             * .collect(java.util.stream.Collectors.toList());
             * System.out.println("Reservas filtradas por hotel " + admin.getHotel() + ": "
             * + todasLasReservas.size());
             * }
             */

            // Mostrar info de todas las reservas encontradas
            for (Reserva r : todasLasReservas) {
                System.out.println("Reserva: " + r.getCodigoReserva() +
                        " | Usuario: " + (r.getUsuario() != null ? r.getUsuario().getNombre() : "NULL") +
                        " | Estado: " + r.getEstado());
            }

            model.addAttribute("reservas", todasLasReservas);

            // Calcular estadísticas por estado
            long pendientes = todasLasReservas.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.PENDIENTE)
                    .count();
            long confirmadas = todasLasReservas.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.CONFIRMADA)
                    .count();
            long completadas = todasLasReservas.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.COMPLETADA)
                    .count();
            long canceladas = todasLasReservas.stream()
                    .filter(r -> r.getEstado() == Reserva.EstadoReserva.CANCELADA)
                    .count();

            model.addAttribute("reservasPendientes", pendientes);
            model.addAttribute("reservasConfirmadas", confirmadas);
            model.addAttribute("reservasCompletadas", completadas);
            model.addAttribute("reservasCanceladas", canceladas);

            System.out.println("Estadísticas - Pendientes: " + pendientes + ", Confirmadas: " + confirmadas +
                    ", Completadas: " + completadas + ", Canceladas: " + canceladas);
            System.out.println("======================");
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
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No tiene permisos para crear habitaciones en este hotel"));
            }

            Habitacion nuevaHabitacion = habitacionService.guardarHabitacion(habitacion);

            return ResponseEntity.ok().body(Map.of(
                    "message", "Habitación creada exitosamente",
                    "id", nuevaHabitacion.getId(),
                    "numero", nuevaHabitacion.getNumero(),
                    "hotel", nuevaHabitacion.getHotel()));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error al crear habitación: " + e.getMessage()));
        }
    }

    @PostMapping("/habitaciones/{id}/editar")
    @ResponseBody
    public ResponseEntity<?> editarHabitacion(@PathVariable Long id, @RequestBody Map<String, Object> datos,
            Authentication authentication) {
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
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No tiene permisos para editar esta habitación"));
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
                    "id", habitacionActualizada.getId()));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al actualizar habitación: " + e.getMessage()));
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
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No tiene permisos para modificar esta habitación"));
            }

            // Cambiar disponibilidad
            habitacion.setDisponible(!habitacion.getDisponible());
            habitacionService.guardarHabitacion(habitacion);

            String estado = habitacion.getDisponible() ? "disponible" : "bloqueada";

            return ResponseEntity.ok().body(Map.of(
                    "message", "Habitación " + estado + " exitosamente",
                    "disponible", habitacion.getDisponible()));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al cambiar disponibilidad: " + e.getMessage()));
        }
    }

    @PostMapping("/habitaciones/{id}/estado")
    @ResponseBody
    public ResponseEntity<?> cambiarEstadoHabitacion(@PathVariable Long id, @RequestBody Map<String, String> request,
            Authentication authentication) {
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
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No tiene permisos para modificar esta habitación"));
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

                String estadoTexto = estadoEnum.name().equals("LIBRE") ? "Libre"
                        : estadoEnum.name().equals("OCUPADA") ? "Ocupada"
                                : estadoEnum.name().equals("MANTENIMIENTO") ? "En Mantenimiento" : "Bloqueada";

                return ResponseEntity.ok().body(Map.of(
                        "success", true,
                        "message", "Estado de habitación cambiado a " + estadoTexto + " exitosamente",
                        "estado", estadoEnum.name(),
                        "disponible", habitacion.getDisponible()));

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
                    "cama", habitacion.getCama());

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
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No tiene permisos para aprobar reservas de este hotel"));
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
                    "estado", "CONFIRMADA"));

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
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No tiene permisos para rechazar reservas de este hotel"));
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
                    "estado", "CANCELADA"));

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
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No tiene permisos para completar reservas de este hotel"));
            }

            // Verificar que la reserva esté confirmada
            if (!reserva.getEstado().equals(Reserva.EstadoReserva.CONFIRMADA)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Solo se pueden completar reservas confirmadas"));
            }

            // Completar la reserva
            reserva = reservaService.actualizarEstadoReserva(id, Reserva.EstadoReserva.COMPLETADA);

            return ResponseEntity.ok().body(Map.of(
                    "message", "Reserva completada exitosamente",
                    "codigo", reserva.getCodigoReserva(),
                    "estado", "COMPLETADA"));

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

            // No need to create unused admin variable
            if (!adminOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Administrador no encontrado")); 
            }

            Map<String, Integer> estadisticas = Map.of(
                    "libres", habitacionService.obtenerHabitacionesLibres().size(),
                    "ocupadas", habitacionService.obtenerHabitacionesOcupadas().size(),
                    "mantenimiento",
                    habitacionService.obtenerHabitacionesPorEstado(Habitacion.EstadoHabitacion.MANTENIMIENTO).size(),
                    "bloqueadas",
                    habitacionService.obtenerHabitacionesPorEstado(Habitacion.EstadoHabitacion.BLOQUEADA).size());

            return ResponseEntity.ok().body(estadisticas);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al obtener estadísticas: " + e.getMessage()));
        }
    }

    
    //METODO REST DELETE PARA ELIMINAR USUARIO
    @DeleteMapping("/usuarios/{id}/eliminar")
    @ResponseBody
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id, Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);

            if (!adminOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Administrador no encontrado"));
            }

            Optional<Usuario> usuarioOpt = usuarioService.buscarPorId(id);
            if (!usuarioOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Usuario no encontrado"));
            }

            Usuario usuario = usuarioOpt.get();

            // Verificar que no se pueda eliminar a sí mismo
            if (usuario.getEmail().equals(email)) {
                return ResponseEntity.badRequest().body(Map.of("error", "No puede eliminar su propia cuenta"));
            }

            // Eliminar el usuario
            usuarioService.eliminar(id);

            return ResponseEntity.ok().body(Map.of(
                    "message", "Usuario eliminado exitosamente",
                    "id", id));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error al eliminar usuario: " + e.getMessage()));
        }
    }

    @GetMapping("/diagrama-conceptual")
    public String diagramaConceptual(Authentication authentication, Model model) {
        String email = authentication.getName();
        Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);

        if (adminOpt.isPresent()) {
            model.addAttribute("admin", adminOpt.get());
        }

        return "html/admin/conceptual";
    }
}
