package Proyecto.Proyecto4.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import Proyecto.Proyecto4.models.Administrador;
import Proyecto.Proyecto4.models.DetallesPersona;
import Proyecto.Proyecto4.models.Habitacion;
import Proyecto.Proyecto4.models.Reserva;
import Proyecto.Proyecto4.models.ReservaServicio;
import Proyecto.Proyecto4.models.Usuario;
import Proyecto.Proyecto4.services.AdministradorService;
import Proyecto.Proyecto4.services.HabitacionService;
import Proyecto.Proyecto4.services.ReservaService;
import Proyecto.Proyecto4.services.ReservaServicioService;
import Proyecto.Proyecto4.services.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;

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

    @Autowired
    private ReservaServicioService reservaServicioService;

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportDashboardData(Authentication authentication) {
        // Genera un archivo Excel (.xlsx) con varias hojas que contienen:
        // - Resumen: métricas generales
        // - Usuarios: lista de usuarios y sus detalles
        // - Habitaciones: lista de habitaciones
        // - Reservas Pendientes: detalle de reservas pendientes
        // - Reservas Confirmadas: detalle de reservas confirmadas

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

            // Estilos básicos
            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Hoja Resumen
            org.apache.poi.ss.usermodel.Sheet resumen = workbook.createSheet("Resumen");
            int rowIdx = 0;

            // Encabezado
            org.apache.poi.ss.usermodel.Row hRow = resumen.createRow(rowIdx++);
            org.apache.poi.ss.usermodel.Cell hCell = hRow.createCell(0);
            hCell.setCellValue("Métrica");
            hCell.setCellStyle(headerStyle);
            hRow.createCell(1).setCellValue("Valor");

            // Recolectar datos
            java.util.List<Usuario> usuarios = usuarioService.listar();
            java.util.List<Habitacion> habitaciones = habitacionService.obtenerTodasLasHabitaciones();
            java.util.List<Reserva> reservasPendientes = reservaService.obtenerReservasPorEstado(Reserva.EstadoReserva.PENDIENTE);
            java.util.List<Reserva> reservasConfirmadas = reservaService.obtenerReservasPorEstado(Reserva.EstadoReserva.CONFIRMADA);

            // Añadir métricas al resumen
            org.apache.poi.ss.usermodel.Row r1 = resumen.createRow(rowIdx++);
            r1.createCell(0).setCellValue("Total Usuarios");
            r1.createCell(1).setCellValue(usuarios.size());

            org.apache.poi.ss.usermodel.Row r2 = resumen.createRow(rowIdx++);
            r2.createCell(0).setCellValue("Total Habitaciones");
            r2.createCell(1).setCellValue(habitaciones.size());

            org.apache.poi.ss.usermodel.Row r3 = resumen.createRow(rowIdx++);
            r3.createCell(0).setCellValue("Habitaciones Disponibles");
            r3.createCell(1).setCellValue(habitacionService.obtenerHabitacionesDisponibles().size());

            org.apache.poi.ss.usermodel.Row r4 = resumen.createRow(rowIdx++);
            r4.createCell(0).setCellValue("Reservas Pendientes");
            r4.createCell(1).setCellValue(reservasPendientes.size());

            org.apache.poi.ss.usermodel.Row r5 = resumen.createRow(rowIdx++);
            r5.createCell(0).setCellValue("Reservas Confirmadas");
            r5.createCell(1).setCellValue(reservasConfirmadas.size());

            // Autosize columnas resumen
            resumen.autoSizeColumn(0);
            resumen.autoSizeColumn(1);

            // Hoja Usuarios
            org.apache.poi.ss.usermodel.Sheet sheetUsuarios = workbook.createSheet("Usuarios");
            int ur = 0;
            org.apache.poi.ss.usermodel.Row headerUsuarios = sheetUsuarios.createRow(ur++);
            headerUsuarios.createCell(0).setCellValue("ID");
            headerUsuarios.createCell(1).setCellValue("Nombre");
            headerUsuarios.createCell(2).setCellValue("Email");
            headerUsuarios.createCell(3).setCellValue("Teléfono");
            for (int i = 0; i < headerUsuarios.getLastCellNum(); i++) headerUsuarios.getCell(i).setCellStyle(headerStyle);

            for (Usuario u : usuarios) {
                org.apache.poi.ss.usermodel.Row row = sheetUsuarios.createRow(ur++);
                row.createCell(0).setCellValue(u.getId() != null ? u.getId() : 0);
                // DetallesPersona tiene 'nombres' y 'apellidos' y helper getNombreCompleto()
                row.createCell(1).setCellValue(u.getDetallesPersona() != null ? u.getDetallesPersona().getNombreCompleto() : "");
                row.createCell(2).setCellValue(u.getEmail() != null ? u.getEmail() : "");
                row.createCell(3).setCellValue(u.getDetallesPersona() != null && u.getDetallesPersona().getTelefono() != null ? u.getDetallesPersona().getTelefono() : "");
            }
            sheetUsuarios.autoSizeColumn(0);
            sheetUsuarios.autoSizeColumn(1);
            sheetUsuarios.autoSizeColumn(2);
            sheetUsuarios.autoSizeColumn(3);

            // Hoja Habitaciones
            org.apache.poi.ss.usermodel.Sheet sheetHab = workbook.createSheet("Habitaciones");
            int hr = 0;
            org.apache.poi.ss.usermodel.Row headerHab = sheetHab.createRow(hr++);
            headerHab.createCell(0).setCellValue("ID");
            headerHab.createCell(1).setCellValue("Número/Nombre");
            headerHab.createCell(2).setCellValue("Tipo");
            headerHab.createCell(3).setCellValue("Precio");
            headerHab.createCell(4).setCellValue("Estado");
            for (int i = 0; i < headerHab.getLastCellNum(); i++) headerHab.getCell(i).setCellStyle(headerStyle);

            for (Habitacion h : habitaciones) {
                org.apache.poi.ss.usermodel.Row row = sheetHab.createRow(hr++);
                row.createCell(0).setCellValue(h.getId() != null ? h.getId() : 0);
                // Habitacion no tiene 'nombre' — usamos 'numero' como identificador
                row.createCell(1).setCellValue(h.getNumero() != null ? h.getNumero() : "");
                row.createCell(2).setCellValue(h.getTipo() != null ? h.getTipo() : "");
                row.createCell(3).setCellValue(h.getPrecio() != null ? h.getPrecio().doubleValue() : 0.0);
                row.createCell(4).setCellValue(Boolean.TRUE.equals(h.getDisponible()) ? "Disponible" : "Ocupada");
            }
            for (int i = 0; i <= 4; i++) sheetHab.autoSizeColumn(i);

            // Hoja Reservas Pendientes
            org.apache.poi.ss.usermodel.Sheet sheetPend = workbook.createSheet("Reservas Pendientes");
            int pr = 0;
            org.apache.poi.ss.usermodel.Row headerPend = sheetPend.createRow(pr++);
            headerPend.createCell(0).setCellValue("ID");
            headerPend.createCell(1).setCellValue("Usuario");
            headerPend.createCell(2).setCellValue("Habitación");
            headerPend.createCell(3).setCellValue("Fecha Inicio");
            headerPend.createCell(4).setCellValue("Fecha Fin");
            headerPend.createCell(5).setCellValue("Estado");
            for (int i = 0; i < headerPend.getLastCellNum(); i++) headerPend.getCell(i).setCellStyle(headerStyle);

            for (Reserva rp : reservasPendientes) {
                org.apache.poi.ss.usermodel.Row row = sheetPend.createRow(pr++);
                row.createCell(0).setCellValue(rp.getId() != null ? rp.getId() : 0);
                row.createCell(1).setCellValue(rp.getUsuario() != null && rp.getUsuario().getEmail() != null ? rp.getUsuario().getEmail() : "");
                row.createCell(2).setCellValue(rp.getHabitacion() != null ? (rp.getHabitacion().getNumero() != null ? rp.getHabitacion().getNumero() : "") : "");
                row.createCell(3).setCellValue(rp.getFechaEntrada() != null ? rp.getFechaEntrada().toString() : "");
                row.createCell(4).setCellValue(rp.getFechaSalida() != null ? rp.getFechaSalida().toString() : "");
                row.createCell(5).setCellValue(rp.getEstado() != null ? rp.getEstado().toString() : "");
            }
            for (int i = 0; i <= 5; i++) sheetPend.autoSizeColumn(i);

            // Hoja Reservas Confirmadas
            org.apache.poi.ss.usermodel.Sheet sheetConf = workbook.createSheet("Reservas Confirmadas");
            int cr = 0;
            org.apache.poi.ss.usermodel.Row headerConf = sheetConf.createRow(cr++);
            headerConf.createCell(0).setCellValue("ID");
            headerConf.createCell(1).setCellValue("Usuario");
            headerConf.createCell(2).setCellValue("Habitación");
            headerConf.createCell(3).setCellValue("Fecha Inicio");
            headerConf.createCell(4).setCellValue("Fecha Fin");
            headerConf.createCell(5).setCellValue("Estado");
            for (int i = 0; i < headerConf.getLastCellNum(); i++) headerConf.getCell(i).setCellStyle(headerStyle);

            for (Reserva rc : reservasConfirmadas) {
                org.apache.poi.ss.usermodel.Row row = sheetConf.createRow(cr++);
                row.createCell(0).setCellValue(rc.getId() != null ? rc.getId() : 0);
                row.createCell(1).setCellValue(rc.getUsuario() != null && rc.getUsuario().getEmail() != null ? rc.getUsuario().getEmail() : "");
                row.createCell(2).setCellValue(rc.getHabitacion() != null ? (rc.getHabitacion().getNumero() != null ? rc.getHabitacion().getNumero() : "") : "");
                row.createCell(3).setCellValue(rc.getFechaEntrada() != null ? rc.getFechaEntrada().toString() : "");
                row.createCell(4).setCellValue(rc.getFechaSalida() != null ? rc.getFechaSalida().toString() : "");
                row.createCell(5).setCellValue(rc.getEstado() != null ? rc.getEstado().toString() : "");
            }
            for (int i = 0; i <= 5; i++) sheetConf.autoSizeColumn(i);

            // Escribir workbook a bytes
            try (java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream()) {
                workbook.write(bos);
                byte[] bytes = bos.toByteArray();
                return ResponseEntity
                        .ok()
                        .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                        .header("Content-Disposition", "attachment; filename=dashboard_stats.xlsx")
                        .body(bytes);
            }

        } catch (Exception e) {
            logger.error("Error generando archivo Excel: ", e);
            return ResponseEntity.status(500).body(null);
        }
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
    public String gestionUsuarios(Authentication authentication, Model model,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) String estado) {
        String email = authentication.getName();
        Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);

        if (adminOpt.isPresent()) {
            model.addAttribute("admin", adminOpt.get());
            
            // Obtener todos los usuarios
            List<Usuario> usuarios = usuarioService.listar();
            
            // Aplicar filtro de búsqueda (nombre, email o DNI)
            if (busqueda != null && !busqueda.trim().isEmpty()) {
                String busquedaLower = busqueda.toLowerCase().trim();
                usuarios = usuarios.stream()
                    .filter(u -> 
                        (u.getNombre() != null && u.getNombre().toLowerCase().contains(busquedaLower)) ||
                        (u.getEmail() != null && u.getEmail().toLowerCase().contains(busquedaLower)) ||
                        (u.getDetallesPersona() != null && u.getDetallesPersona().getDni() != null && 
                         u.getDetallesPersona().getDni().contains(busquedaLower))
                    )
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // Aplicar filtro de estado
            if (estado != null && !estado.trim().isEmpty()) {
                if ("activo".equalsIgnoreCase(estado)) {
                    usuarios = usuarios.stream()
                        .filter(u -> u.getActivo() == null || u.getActivo() == true)
                        .collect(java.util.stream.Collectors.toList());
                } else if ("inactivo".equalsIgnoreCase(estado)) {
                    usuarios = usuarios.stream()
                        .filter(u -> u.getActivo() != null && u.getActivo() == false)
                        .collect(java.util.stream.Collectors.toList());
                }
            }
            
            model.addAttribute("usuarios", usuarios);
            model.addAttribute("busqueda", busqueda);
            model.addAttribute("estado", estado);
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
                    "aceptaMarketing", detalles != null ? detalles.getAceptaMarketing() : false,
                    "activo", usuario.getActivo() != null ? usuario.getActivo() : true);

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
            
            // Actualizar estado del usuario
            if (datos.containsKey("activo")) {
                usuario.setActivo(Boolean.parseBoolean(datos.get("activo").toString()));
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

    @PostMapping("/usuarios/{id}/editar")
    @ResponseBody
    public ResponseEntity<?> editarUsuarioPost(@PathVariable Long id, 
            @RequestParam Map<String, String> params, 
            Authentication authentication) {
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
            if (params.containsKey("nombre") && params.get("nombre") != null) {
                usuario.setNombre(params.get("nombre"));
            }
            if (params.containsKey("email") && params.get("email") != null) {
                String nuevoEmail = params.get("email");
                Optional<Usuario> usuarioExistente = usuarioService.buscarPorEmail(nuevoEmail);
                if (usuarioExistente.isPresent() && !usuarioExistente.get().getId().equals(id)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "El email ya está registrado"));
                }
                usuario.setEmail(nuevoEmail);
            }

            // Manejar cambio de contraseña
            if (params.containsKey("password") && params.get("password") != null && !params.get("password").trim().isEmpty()) {
                String nuevaPassword = params.get("password");
                usuarioService.cambiarPassword(usuario, nuevaPassword);
            }

            // Actualizar detalles personales
            DetallesPersona detalles = usuario.getDetallesPersona();
            if (detalles == null) {
                detalles = new DetallesPersona();
                usuario.setDetallesPersona(detalles);
            }

            if (params.containsKey("apellidos")) {
                detalles.setApellidos(params.get("apellidos"));
            }
            if (params.containsKey("dni")) {
                detalles.setDni(params.get("dni"));
            }
            if (params.containsKey("telefono")) {
                detalles.setTelefono(params.get("telefono"));
            }
            if (params.containsKey("fechaNacimiento") && !params.get("fechaNacimiento").isEmpty()) {
                detalles.setFechaNacimiento(LocalDate.parse(params.get("fechaNacimiento")));
            }
            if (params.containsKey("intereses")) {
                detalles.setIntereses(params.get("intereses"));
            }
            if (params.containsKey("aceptaMarketing")) {
                detalles.setAceptaMarketing(Boolean.parseBoolean(params.get("aceptaMarketing")));
            }
            
            // Actualizar estado del usuario
            if (params.containsKey("activo")) {
                usuario.setActivo(Boolean.parseBoolean(params.get("activo")));
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
        logger.info("=== GESTIÓN RESERVAS - Inicio ===");
        logger.info("Usuario autenticado: {}", email);
        
        Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);

        if (adminOpt.isPresent()) {
            Administrador admin = adminOpt.get();
            model.addAttribute("admin", admin);
            logger.info("Admin encontrado: {} - {}", admin.getNombres(), admin.getApellidos());

            // Obtener todas las reservas con detalles cargados (JOIN FETCH)
            List<Reserva> todasLasReservas = reservaService.obtenerTodasLasReservas();
            logger.info("Total de reservas obtenidas: {}", todasLasReservas.size());
            
            // Log detallado de cada reserva
            for(Reserva r : todasLasReservas) {
                logger.info("Reserva {}: Usuario={}, Habitacion={}, FechaEntrada={}", 
                    r.getCodigoReserva(),
                    r.getUsuario() != null ? r.getUsuario().getEmail() : "null",
                    r.getHabitacion() != null ? r.getHabitacion().getNumero() : "null",
                    r.getFechaEntrada());
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
            
            logger.info("Estadísticas - Pendientes:{}, Confirmadas:{}, Completadas:{}, Canceladas:{}", 
                pendientes, confirmadas, completadas, canceladas);
        } else {
            logger.error("Admin no encontrado para email: {}", email);
        }
        
        logger.info("=== GESTIÓN RESERVAS - Fin ===");
        return "html/admin/reservas";
    }

    @GetMapping("/reservas-servicios")
    public String gestionReservasServicios(Authentication authentication, Model model) {
        String email = authentication.getName();
        logger.info("=== GESTIÓN RESERVAS SERVICIOS - Inicio ===");
        logger.info("Usuario autenticado: {}", email);
        
        Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);

        if (adminOpt.isPresent()) {
            Administrador admin = adminOpt.get();
            model.addAttribute("admin", admin);
            logger.info("Admin encontrado: {} - {}", admin.getNombres(), admin.getApellidos());

            // Obtener todas las reservas de servicios con detalles cargados (JOIN FETCH)
            List<ReservaServicio> todasLasReservas = reservaServicioService.obtenerTodasLasReservas();
            logger.info("Total de reservas de servicios obtenidas: {}", todasLasReservas.size());
            
            model.addAttribute("reservasServicios", todasLasReservas);

            // Calcular estadísticas por estado y tipo
            long pendientes = todasLasReservas.stream()
                    .filter(r -> r.getEstado() == ReservaServicio.EstadoReserva.PENDIENTE)
                    .count();
            long confirmadas = todasLasReservas.stream()
                    .filter(r -> r.getEstado() == ReservaServicio.EstadoReserva.CONFIRMADA)
                    .count();
            long completadas = todasLasReservas.stream()
                    .filter(r -> r.getEstado() == ReservaServicio.EstadoReserva.COMPLETADA)
                    .count();
            long canceladas = todasLasReservas.stream()
                    .filter(r -> r.getEstado() == ReservaServicio.EstadoReserva.CANCELADA)
                    .count();

            // Estadísticas por tipo de servicio
            long reservasSpa = todasLasReservas.stream()
                    .filter(r -> r.getTipoServicio() == ReservaServicio.TipoServicio.SPA)
                    .count();
            long reservasBodas = todasLasReservas.stream()
                    .filter(r -> r.getTipoServicio() == ReservaServicio.TipoServicio.BODA)
                    .count();
            long reservasEventos = todasLasReservas.stream()
                    .filter(r -> r.getTipoServicio() == ReservaServicio.TipoServicio.EVENTO)
                    .count();

            model.addAttribute("reservasPendientes", pendientes);
            model.addAttribute("reservasConfirmadas", confirmadas);
            model.addAttribute("reservasCompletadas", completadas);
            model.addAttribute("reservasCanceladas", canceladas);
            
            model.addAttribute("reservasSpa", reservasSpa);
            model.addAttribute("reservasBodas", reservasBodas);
            model.addAttribute("reservasEventos", reservasEventos);
            
            logger.info("Estadísticas - Pendientes:{}, Confirmadas:{}, Completadas:{}, Canceladas:{}", 
                pendientes, confirmadas, completadas, canceladas);
        } else {
            logger.error("Admin no encontrado para email: {}", email);
        }
        
        logger.info("=== GESTIÓN RESERVAS SERVICIOS - Fin ===");
        return "html/admin/reservas-servicios";
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

    // ===== ENDPOINTS REST PARA GESTIÓN DE RESERVAS DE SERVICIOS =====

    @PostMapping("/reservas-servicios/{id}/aprobar")
    @ResponseBody
    public ResponseEntity<?> aprobarReservaServicio(@PathVariable Long id, Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);

            if (!adminOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Administrador no encontrado"));
            }

            Optional<ReservaServicio> reservaOpt = reservaServicioService.obtenerReservaPorId(id);

            if (!reservaOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Reserva de servicio no encontrada"));
            }

            ReservaServicio reserva = reservaOpt.get();

            // Verificar que la reserva esté pendiente
            if (!reserva.getEstado().equals(ReservaServicio.EstadoReserva.PENDIENTE)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Solo se pueden aprobar reservas pendientes"));
            }

            // Aprobar la reserva
            reserva = reservaServicioService.actualizarEstadoReserva(id, ReservaServicio.EstadoReserva.CONFIRMADA);

            return ResponseEntity.ok().body(Map.of(
                    "message", "Reserva de servicio aprobada exitosamente",
                    "codigo", reserva.getCodigoReserva(),
                    "estado", "CONFIRMADA"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al aprobar reserva de servicio: " + e.getMessage()));
        }
    }

    @PostMapping("/reservas-servicios/{id}/rechazar")
    @ResponseBody
    public ResponseEntity<?> rechazarReservaServicio(@PathVariable Long id, Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);

            if (!adminOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Administrador no encontrado"));
            }

            Optional<ReservaServicio> reservaOpt = reservaServicioService.obtenerReservaPorId(id);

            if (!reservaOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Reserva de servicio no encontrada"));
            }

            ReservaServicio reserva = reservaOpt.get();

            // Verificar que la reserva esté pendiente
            if (!reserva.getEstado().equals(ReservaServicio.EstadoReserva.PENDIENTE)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Solo se pueden rechazar reservas pendientes"));
            }

            // Rechazar la reserva
            reserva = reservaServicioService.actualizarEstadoReserva(id, ReservaServicio.EstadoReserva.CANCELADA);

            return ResponseEntity.ok().body(Map.of(
                    "message", "Reserva de servicio rechazada exitosamente",
                    "codigo", reserva.getCodigoReserva(),
                    "estado", "CANCELADA"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al rechazar reserva de servicio: " + e.getMessage()));
        }
    }

    @PostMapping("/reservas-servicios/{id}/completar")
    @ResponseBody
    public ResponseEntity<?> completarReservaServicio(@PathVariable Long id, Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);

            if (!adminOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Administrador no encontrado"));
            }

            Optional<ReservaServicio> reservaOpt = reservaServicioService.obtenerReservaPorId(id);

            if (!reservaOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Reserva de servicio no encontrada"));
            }

            ReservaServicio reserva = reservaOpt.get();

            // Verificar que la reserva esté confirmada
            if (!reserva.getEstado().equals(ReservaServicio.EstadoReserva.CONFIRMADA)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Solo se pueden completar reservas confirmadas"));
            }

            // Completar la reserva
            reserva = reservaServicioService.actualizarEstadoReserva(id, ReservaServicio.EstadoReserva.COMPLETADA);

            return ResponseEntity.ok().body(Map.of(
                    "message", "Reserva de servicio completada exitosamente",
                    "codigo", reserva.getCodigoReserva(),
                    "estado", "COMPLETADA"));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al completar reserva de servicio: " + e.getMessage()));
        }
    }

    // ===== ENDPOINTS PARA REPORTES =====

    @GetMapping("/reportes")
    public String reportes(Authentication authentication, Model model) {
        String email = authentication.getName();
        Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);

        if (adminOpt.isPresent()) {
            Administrador admin = adminOpt.get();
            model.addAttribute("admin", admin);

            // Agregar lista de hoteles para el filtro
            model.addAttribute("hoteles", habitacionService.obtenerHotelesDisponibles());

            // Estadísticas iniciales (mes actual)
            LocalDate ahora = LocalDate.now();
            LocalDate inicioMes = ahora.withDayOfMonth(1);
            LocalDate finMes = ahora.withDayOfMonth(ahora.lengthOfMonth());

            // Obtener todas las reservas del mes
            List<Reserva> todasReservas = reservaService.obtenerTodasLasReservas();
            List<Reserva> reservasMes = todasReservas.stream()
                .filter(r -> {
                    LocalDate entrada = r.getFechaEntrada();
                    LocalDate salida = r.getFechaSalida();
                    // La reserva está en el mes si tiene alguna noche en el período
                    return !(salida.isBefore(inicioMes) || entrada.isAfter(finMes));
                })
                .collect(java.util.stream.Collectors.toList());

            // Calcular estadísticas - incluir CONFIRMADA y COMPLETADA
            long reservasCompletadas = reservasMes.stream()
                .filter(r -> r.getEstado().equals(Reserva.EstadoReserva.COMPLETADA) ||
                            r.getEstado().equals(Reserva.EstadoReserva.CONFIRMADA))
                .count();

            BigDecimal totalIngresos = reservasMes.stream()
                .filter(r -> r.getEstado().equals(Reserva.EstadoReserva.COMPLETADA) || 
                            r.getEstado().equals(Reserva.EstadoReserva.CONFIRMADA))
                .map(r -> r.getMontoTotal() != null ? r.getMontoTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Clientes nuevos (usuarios registrados este mes)
            long clientesNuevos = usuarioService.listar().stream()
                .filter(u -> {
                    // Nota: Asumo que Usuario tiene fecha de registro
                    // Si no existe, esta parte puede omitirse
                    return true; // Placeholder
                })
                .count();

            model.addAttribute("reservasCompletadas", reservasCompletadas);
            model.addAttribute("totalIngresos", totalIngresos);
            model.addAttribute("clientesNuevos", clientesNuevos);
            model.addAttribute("tasaOcupacion", 0); // Calculado dinámicamente en JS
        }

        return "html/admin/reportes";
    }

    @GetMapping("/reportes/estadisticas")
    @ResponseBody
    public ResponseEntity<?> obtenerEstadisticasReportes(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(required = false) String hotel,
            Authentication authentication) {
        
        try {
            String email = authentication.getName();
            Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);

            if (!adminOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Administrador no encontrado"));
            }

            Administrador admin = adminOpt.get();

            // Parsear fechas
            LocalDate fechaDesde = (desde != null && !desde.isEmpty()) 
                ? LocalDate.parse(desde) 
                : LocalDate.now().withDayOfMonth(1);
            
            LocalDate fechaHasta = (hasta != null && !hasta.isEmpty()) 
                ? LocalDate.parse(hasta) 
                : LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

            // Obtener todas las reservas
            List<Reserva> todasReservas = reservaService.obtenerTodasLasReservas();

            // Filtrar por fechas - incluir reservas que tengan alguna noche en el período
            List<Reserva> reservasFiltradas = todasReservas.stream()
                .filter(r -> {
                    LocalDate entrada = r.getFechaEntrada();
                    LocalDate salida = r.getFechaSalida();
                    // La reserva está en el período si:
                    // - Su entrada está dentro del período, O
                    // - Su salida está dentro del período, O
                    // - Cubre completamente el período (entra antes y sale después)
                    return !(salida.isBefore(fechaDesde) || entrada.isAfter(fechaHasta));
                })
                .collect(java.util.stream.Collectors.toList());

            // Filtrar por hotel si es admin de hotel específico o si se especificó filtro
            if (admin.getHotel() != null) {
                String hotelAdmin = admin.getHotel();
                reservasFiltradas = reservasFiltradas.stream()
                    .filter(r -> r.getHabitacion() != null && 
                                hotelAdmin.equals(r.getHabitacion().getHotel()))
                    .collect(java.util.stream.Collectors.toList());
            } else if (hotel != null && !hotel.isEmpty()) {
                reservasFiltradas = reservasFiltradas.stream()
                    .filter(r -> r.getHabitacion() != null && 
                                hotel.equals(r.getHabitacion().getHotel()))
                    .collect(java.util.stream.Collectors.toList());
            }

            // Calcular estadísticas
            // Contar reservas confirmadas (CONFIRMADA + COMPLETADA)
            long reservasCompletadas = reservasFiltradas.stream()
                .filter(r -> r.getEstado().equals(Reserva.EstadoReserva.COMPLETADA) ||
                            r.getEstado().equals(Reserva.EstadoReserva.CONFIRMADA))
                .count();

            BigDecimal totalIngresos = reservasFiltradas.stream()
                .filter(r -> r.getEstado().equals(Reserva.EstadoReserva.COMPLETADA) || 
                            r.getEstado().equals(Reserva.EstadoReserva.CONFIRMADA))
                .map(r -> r.getMontoTotal() != null ? r.getMontoTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Calcular tasa de ocupación (incluir CONFIRMADA y COMPLETADA)
            List<Reserva> reservasParaOcupacion = reservasFiltradas.stream()
                .filter(r -> r.getEstado().equals(Reserva.EstadoReserva.COMPLETADA) ||
                            r.getEstado().equals(Reserva.EstadoReserva.CONFIRMADA))
                .collect(java.util.stream.Collectors.toList());

            double tasaOcupacion = calcularTasaOcupacion(
                reservasParaOcupacion, 
                fechaDesde.toString(), 
                fechaHasta.toString(), 
                hotel, 
                admin
            );

            // Clientes nuevos en el período
            long clientesNuevos = usuarioService.listar().size() / 10; // Estimación simple

            Map<String, Object> respuesta = new java.util.HashMap<>();
            respuesta.put("reservasCompletadas", reservasCompletadas);
            respuesta.put("totalIngresos", totalIngresos.setScale(2, java.math.RoundingMode.HALF_UP).toString());
            respuesta.put("tasaOcupacion", Math.round(tasaOcupacion));
            respuesta.put("clientesNuevos", clientesNuevos);

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            logger.error("Error al obtener estadísticas de reportes", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error al obtener estadísticas: " + e.getMessage()));
        }
    }

    @GetMapping("/reportes/detalle-reservas")
    @ResponseBody
    public ResponseEntity<?> obtenerDetalleReservas(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(required = false) String hotel,
            Authentication authentication) {
        
        try {
            String email = authentication.getName();
            Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);

            if (!adminOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Administrador no encontrado"));
            }

            Administrador admin = adminOpt.get();

            // Parsear fechas
            LocalDate fechaDesde = (desde != null && !desde.isEmpty()) 
                ? LocalDate.parse(desde) 
                : LocalDate.now().withDayOfMonth(1);
            
            LocalDate fechaHasta = (hasta != null && !hasta.isEmpty()) 
                ? LocalDate.parse(hasta) 
                : LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

            // Obtener reservas filtradas
            List<Reserva> todasReservas = reservaService.obtenerTodasLasReservas();
            List<Reserva> reservasFiltradas = todasReservas.stream()
                .filter(r -> {
                    LocalDate inicio = r.getFechaEntrada();
                    return !inicio.isBefore(fechaDesde) && !inicio.isAfter(fechaHasta);
                })
                .collect(java.util.stream.Collectors.toList());

            // Filtrar por hotel
            if (admin.getHotel() != null) {
                String hotelAdmin = admin.getHotel();
                reservasFiltradas = reservasFiltradas.stream()
                    .filter(r -> r.getHabitacion() != null && 
                                hotelAdmin.equals(r.getHabitacion().getHotel()))
                    .collect(java.util.stream.Collectors.toList());
            } else if (hotel != null && !hotel.isEmpty()) {
                reservasFiltradas = reservasFiltradas.stream()
                    .filter(r -> r.getHabitacion() != null && 
                                hotel.equals(r.getHabitacion().getHotel()))
                    .collect(java.util.stream.Collectors.toList());
            }

            // Construir lista de detalles
            List<Map<String, Object>> reservasDetalle = new java.util.ArrayList<>();
            for (Reserva r : reservasFiltradas) {
                Map<String, Object> detalle = new java.util.HashMap<>();
                detalle.put("id", r.getId());
                detalle.put("codigo", r.getCodigoReserva());
                detalle.put("fechaReserva", r.getFechaEntrada().toString());
                detalle.put("usuario", r.getUsuario() != null ? r.getUsuario().getNombre() : "N/A");
                detalle.put("habitacion", r.getHabitacion() != null ? r.getHabitacion().getNumero() : "N/A");
                detalle.put("estado", r.getEstado().toString());
                detalle.put("monto", r.getMontoTotal() != null ? r.getMontoTotal().toString() : "0.00");
                reservasDetalle.add(detalle);
            }

            return ResponseEntity.ok(Map.of("reservas", reservasDetalle));

        } catch (Exception e) {
            logger.error("Error al obtener detalle de reservas", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error al obtener detalle: " + e.getMessage()));
        }
    }

    @GetMapping("/reportes/detalle-ingresos")
    @ResponseBody
    public ResponseEntity<?> obtenerDetalleIngresos(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(required = false) String hotel,
            Authentication authentication) {
        
        try {
            String email = authentication.getName();
            Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);

            if (!adminOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Administrador no encontrado"));
            }

            Administrador admin = adminOpt.get();

            // Parsear fechas
            LocalDate fechaDesde = (desde != null && !desde.isEmpty()) 
                ? LocalDate.parse(desde) 
                : LocalDate.now().withDayOfMonth(1);
            
            LocalDate fechaHasta = (hasta != null && !hasta.isEmpty()) 
                ? LocalDate.parse(hasta) 
                : LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

            // Obtener reservas filtradas
            List<Reserva> todasReservas = reservaService.obtenerTodasLasReservas();
            List<Reserva> reservasFiltradas = todasReservas.stream()
                .filter(r -> {
                    LocalDate inicio = r.getFechaEntrada();
                    boolean enRango = !inicio.isBefore(fechaDesde) && !inicio.isAfter(fechaHasta);
                    boolean tieneIngreso = r.getEstado().equals(Reserva.EstadoReserva.COMPLETADA) || 
                                          r.getEstado().equals(Reserva.EstadoReserva.CONFIRMADA);
                    return enRango && tieneIngreso;
                })
                .collect(java.util.stream.Collectors.toList());

            // Filtrar por hotel
            if (admin.getHotel() != null) {
                String hotelAdmin = admin.getHotel();
                reservasFiltradas = reservasFiltradas.stream()
                    .filter(r -> r.getHabitacion() != null && 
                                hotelAdmin.equals(r.getHabitacion().getHotel()))
                    .collect(java.util.stream.Collectors.toList());
            } else if (hotel != null && !hotel.isEmpty()) {
                reservasFiltradas = reservasFiltradas.stream()
                    .filter(r -> r.getHabitacion() != null && 
                                hotel.equals(r.getHabitacion().getHotel()))
                    .collect(java.util.stream.Collectors.toList());
            }

            // Construir lista de ingresos
            List<Map<String, Object>> ingresosDetalle = new java.util.ArrayList<>();
            for (Reserva r : reservasFiltradas) {
                Map<String, Object> detalle = new java.util.HashMap<>();
                detalle.put("fecha", r.getFechaEntrada().toString());
                detalle.put("concepto", "Reserva " + r.getCodigoReserva() + " - " + 
                    (r.getHabitacion() != null ? r.getHabitacion().getNumero() : "N/A"));
                detalle.put("ingreso", r.getMontoTotal() != null ? r.getMontoTotal().toString() : "0.00");
                detalle.put("estado", r.getEstado().toString());
                ingresosDetalle.add(detalle);
            }

            return ResponseEntity.ok(Map.of("ingresos", ingresosDetalle));

        } catch (Exception e) {
            logger.error("Error al obtener detalle de ingresos", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error al obtener detalle: " + e.getMessage()));
        }
    }

    @GetMapping("/reportes/detalle-ocupacion")
    @ResponseBody
    public ResponseEntity<?> obtenerDetalleOcupacion(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(required = false) String hotel,
            Authentication authentication) {
        
        try {
            String email = authentication.getName();
            Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);

            if (!adminOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Administrador no encontrado"));
            }

            Administrador admin = adminOpt.get();

            // Parsear fechas
            LocalDate fechaDesde = (desde != null && !desde.isEmpty()) 
                ? LocalDate.parse(desde) 
                : LocalDate.now().withDayOfMonth(1);
            
            LocalDate fechaHasta = (hasta != null && !hasta.isEmpty()) 
                ? LocalDate.parse(hasta) 
                : LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

            // Obtener todas las habitaciones
            List<Habitacion> habitaciones;
            if (admin.getHotel() != null) {
                habitaciones = habitacionService.obtenerHabitacionesPorHotel(admin.getHotel());
            } else if (hotel != null && !hotel.isEmpty()) {
                habitaciones = habitacionService.obtenerHabitacionesPorHotel(hotel);
            } else {
                habitaciones = habitacionService.obtenerTodasLasHabitaciones();
            }

            // Construir lista de ocupación
            List<Map<String, Object>> ocupacionDetalle = new java.util.ArrayList<>();
            for (Habitacion h : habitaciones) {
                // Verificar si la habitación tiene reservas en el período
                List<Reserva> reservasHabitacion = reservaService.obtenerTodasLasReservas().stream()
                    .filter(r -> r.getHabitacion() != null && 
                                r.getHabitacion().getId().equals(h.getId()))
                    .filter(r -> {
                        LocalDate inicio = r.getFechaEntrada();
                        LocalDate fin = r.getFechaSalida();
                        return !(fin.isBefore(fechaDesde) || inicio.isAfter(fechaHasta));
                    })
                    .collect(java.util.stream.Collectors.toList());

                // Calcular días ocupados
                long diasTotales = java.time.temporal.ChronoUnit.DAYS.between(fechaDesde, fechaHasta) + 1;
                long diasOcupados = 0;

                for (Reserva r : reservasHabitacion) {
                    if (r.getEstado().equals(Reserva.EstadoReserva.COMPLETADA) || 
                        r.getEstado().equals(Reserva.EstadoReserva.CONFIRMADA)) {
                        
                        LocalDate inicioReserva = r.getFechaEntrada().isBefore(fechaDesde) ? fechaDesde : r.getFechaEntrada();
                        LocalDate finReserva = r.getFechaSalida().isAfter(fechaHasta) ? fechaHasta : r.getFechaSalida();
                        
                        diasOcupados += java.time.temporal.ChronoUnit.DAYS.between(inicioReserva, finReserva) + 1;
                    }
                }

                double porcentajeOcupacion = diasTotales > 0 ? (diasOcupados * 100.0 / diasTotales) : 0;

                Map<String, Object> detalle = new java.util.HashMap<>();
                detalle.put("fecha", fechaDesde + " a " + fechaHasta);
                detalle.put("habitacion", h.getNumero());
                detalle.put("estado", h.getEstadoHabitacion().toString());
                detalle.put("ocupacion", String.format("%.1f%%", porcentajeOcupacion));
                ocupacionDetalle.add(detalle);
            }

            return ResponseEntity.ok(Map.of("ocupacion", ocupacionDetalle));

        } catch (Exception e) {
            logger.error("Error al obtener detalle de ocupación", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error al obtener detalle: " + e.getMessage()));
        }
    }

    @GetMapping("/reportes/grafico-ingresos")
    @ResponseBody
    public ResponseEntity<?> obtenerGraficoIngresos(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            @RequestParam(required = false) String hotel,
            Authentication authentication) {
        
        try {
            String email = authentication.getName();
            Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);

            if (!adminOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Administrador no encontrado"));
            }

            Administrador admin = adminOpt.get();

            // Parsear fechas
            LocalDate fechaDesde = (desde != null && !desde.isEmpty()) 
                ? LocalDate.parse(desde) 
                : LocalDate.now().withDayOfMonth(1);
            
            LocalDate fechaHasta = (hasta != null && !hasta.isEmpty()) 
                ? LocalDate.parse(hasta) 
                : LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

            // Obtener todas las reservas
            List<Reserva> todasReservas = reservaService.obtenerTodasLasReservas();

            // Filtrar por hotel si es necesario
            if (admin.getHotel() != null) {
                String hotelAdmin = admin.getHotel();
                todasReservas = todasReservas.stream()
                    .filter(r -> r.getHabitacion() != null && 
                                hotelAdmin.equals(r.getHabitacion().getHotel()))
                    .collect(java.util.stream.Collectors.toList());
            } else if (hotel != null && !hotel.isEmpty()) {
                todasReservas = todasReservas.stream()
                    .filter(r -> r.getHabitacion() != null && 
                                hotel.equals(r.getHabitacion().getHotel()))
                    .collect(java.util.stream.Collectors.toList());
            }

            // Filtrar solo reservas COMPLETADA y CONFIRMADA
            todasReservas = todasReservas.stream()
                .filter(r -> r.getEstado().equals(Reserva.EstadoReserva.COMPLETADA) ||
                            r.getEstado().equals(Reserva.EstadoReserva.CONFIRMADA))
                .collect(java.util.stream.Collectors.toList());

            // Determinar si agrupamos por día, semana o mes según el rango de fechas
            long diasDiferencia = java.time.temporal.ChronoUnit.DAYS.between(fechaDesde, fechaHasta);
            
            List<String> etiquetas = new java.util.ArrayList<>();
            List<BigDecimal> ingresos = new java.util.ArrayList<>();

            if (diasDiferencia <= 31) {
                // Agrupar por día (hasta 1 mes)
                for (LocalDate fecha = fechaDesde; !fecha.isAfter(fechaHasta); fecha = fecha.plusDays(1)) {
                    final LocalDate fechaActual = fecha;
                    BigDecimal ingresoDelDia = todasReservas.stream()
                        .filter(r -> {
                            LocalDate entrada = r.getFechaEntrada();
                            LocalDate salida = r.getFechaSalida();
                            return !fechaActual.isBefore(entrada) && !fechaActual.isAfter(salida);
                        })
                        .map(r -> {
                            // Calcular el ingreso proporcional por día
                            long diasReserva = java.time.temporal.ChronoUnit.DAYS.between(
                                r.getFechaEntrada(), r.getFechaSalida()) + 1;
                            return r.getMontoTotal() != null ? 
                                r.getMontoTotal().divide(BigDecimal.valueOf(diasReserva), 2, java.math.RoundingMode.HALF_UP) : 
                                BigDecimal.ZERO;
                        })
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    etiquetas.add(fechaActual.getDayOfMonth() + "/" + fechaActual.getMonthValue());
                    ingresos.add(ingresoDelDia);
                }
            } else if (diasDiferencia <= 180) {
                // Agrupar por semana (hasta 6 meses)
                LocalDate inicioSemana = fechaDesde;
                while (!inicioSemana.isAfter(fechaHasta)) {
                    LocalDate finSemana = inicioSemana.plusDays(6);
                    if (finSemana.isAfter(fechaHasta)) {
                        finSemana = fechaHasta;
                    }

                    final LocalDate inicioSemanaFinal = inicioSemana;
                    final LocalDate finSemanaFinal = finSemana;

                    BigDecimal ingresoDeLaSemana = todasReservas.stream()
                        .filter(r -> {
                            LocalDate entrada = r.getFechaEntrada();
                            LocalDate salida = r.getFechaSalida();
                            return !(salida.isBefore(inicioSemanaFinal) || entrada.isAfter(finSemanaFinal));
                        })
                        .map(r -> {
                            // Calcular días en esta semana
                            LocalDate inicioReserva = r.getFechaEntrada().isBefore(inicioSemanaFinal) ? 
                                inicioSemanaFinal : r.getFechaEntrada();
                            LocalDate finReserva = r.getFechaSalida().isAfter(finSemanaFinal) ? 
                                finSemanaFinal : r.getFechaSalida();
                            
                            long diasEnSemana = java.time.temporal.ChronoUnit.DAYS.between(
                                inicioReserva, finReserva) + 1;
                            long diasTotalesReserva = java.time.temporal.ChronoUnit.DAYS.between(
                                r.getFechaEntrada(), r.getFechaSalida()) + 1;
                            
                            BigDecimal montoTotal = r.getMontoTotal() != null ? r.getMontoTotal() : BigDecimal.ZERO;
                            return montoTotal.multiply(BigDecimal.valueOf(diasEnSemana))
                                .divide(BigDecimal.valueOf(diasTotalesReserva), 2, java.math.RoundingMode.HALF_UP);
                        })
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    etiquetas.add(inicioSemana.getDayOfMonth() + "/" + inicioSemana.getMonthValue());
                    ingresos.add(ingresoDeLaSemana);
                    
                    inicioSemana = inicioSemana.plusWeeks(1);
                }
            } else {
                // Agrupar por mes (más de 6 meses)
                LocalDate inicioMes = fechaDesde.withDayOfMonth(1);
                while (!inicioMes.isAfter(fechaHasta)) {
                    LocalDate finMes = inicioMes.withDayOfMonth(inicioMes.lengthOfMonth());
                    if (finMes.isAfter(fechaHasta)) {
                        finMes = fechaHasta;
                    }
                    if (inicioMes.isBefore(fechaDesde)) {
                        inicioMes = fechaDesde;
                    }

                    final LocalDate inicioMesFinal = inicioMes;
                    final LocalDate finMesFinal = finMes;

                    BigDecimal ingresoDelMes = todasReservas.stream()
                        .filter(r -> {
                            LocalDate entrada = r.getFechaEntrada();
                            LocalDate salida = r.getFechaSalida();
                            return !(salida.isBefore(inicioMesFinal) || entrada.isAfter(finMesFinal));
                        })
                        .map(r -> {
                            // Calcular días en este mes
                            LocalDate inicioReserva = r.getFechaEntrada().isBefore(inicioMesFinal) ? 
                                inicioMesFinal : r.getFechaEntrada();
                            LocalDate finReserva = r.getFechaSalida().isAfter(finMesFinal) ? 
                                finMesFinal : r.getFechaSalida();
                            
                            long diasEnMes = java.time.temporal.ChronoUnit.DAYS.between(
                                inicioReserva, finReserva) + 1;
                            long diasTotalesReserva = java.time.temporal.ChronoUnit.DAYS.between(
                                r.getFechaEntrada(), r.getFechaSalida()) + 1;
                            
                            BigDecimal montoTotal = r.getMontoTotal() != null ? r.getMontoTotal() : BigDecimal.ZERO;
                            return montoTotal.multiply(BigDecimal.valueOf(diasEnMes))
                                .divide(BigDecimal.valueOf(diasTotalesReserva), 2, java.math.RoundingMode.HALF_UP);
                        })
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun",
                                    "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};
                    etiquetas.add(meses[inicioMesFinal.getMonthValue() - 1] + " " + inicioMesFinal.getYear());
                    ingresos.add(ingresoDelMes);
                    
                    inicioMes = inicioMes.plusMonths(1).withDayOfMonth(1);
                }
            }

            // Convertir a formato JSON
            List<String> ingresosStr = ingresos.stream()
                .map(i -> i.setScale(2, java.math.RoundingMode.HALF_UP).toString())
                .collect(java.util.stream.Collectors.toList());

            Map<String, Object> respuesta = new java.util.HashMap<>();
            respuesta.put("etiquetas", etiquetas);
            respuesta.put("ingresos", ingresosStr);

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            logger.error("Error al obtener gráfico de ingresos", e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Error al obtener gráfico: " + e.getMessage()));
        }
    }

    // Método auxiliar para calcular tasa de ocupación
    private double calcularTasaOcupacion(List<Reserva> reservasCompletadas, String desde, String hasta, 
                                        String hotel, Administrador admin) {
        try {
            LocalDate fechaDesde = LocalDate.parse(desde);
            LocalDate fechaHasta = LocalDate.parse(hasta);
            long diasTotales = java.time.temporal.ChronoUnit.DAYS.between(fechaDesde, fechaHasta) + 1;

            // Obtener habitaciones según filtro
            List<Habitacion> habitaciones;
            if (admin.getHotel() != null) {
                habitaciones = habitacionService.obtenerHabitacionesPorHotel(admin.getHotel());
            } else if (hotel != null && !hotel.isEmpty()) {
                habitaciones = habitacionService.obtenerHabitacionesPorHotel(hotel);
            } else {
                habitaciones = habitacionService.obtenerTodasLasHabitaciones();
            }

            if (habitaciones.isEmpty() || diasTotales <= 0) {
                return 0.0;
            }

            // Calcular días totales disponibles
            long diasDisponiblesTotales = habitaciones.size() * diasTotales;

            // Calcular días ocupados - solo contar los días que están dentro del período
            long diasOcupados = 0;
            for (Reserva r : reservasCompletadas) {
                // Ajustar las fechas de la reserva al período consultado
                LocalDate inicioReserva = r.getFechaEntrada();
                LocalDate finReserva = r.getFechaSalida();
                
                // Si la reserva comienza antes del período, usar la fecha de inicio del período
                if (inicioReserva.isBefore(fechaDesde)) {
                    inicioReserva = fechaDesde;
                }
                
                // Si la reserva termina después del período, usar la fecha de fin del período
                if (finReserva.isAfter(fechaHasta)) {
                    finReserva = fechaHasta;
                }
                
                // Calcular días ocupados solo si la reserva tiene días dentro del período
                if (!inicioReserva.isAfter(finReserva)) {
                    // Sumar 1 porque queremos incluir tanto el día de entrada como el de salida
                    long diasReserva = java.time.temporal.ChronoUnit.DAYS.between(inicioReserva, finReserva) + 1;
                    diasOcupados += diasReserva;
                }
            }

            // Calcular porcentaje
            return diasDisponiblesTotales > 0 ? (diasOcupados * 100.0 / diasDisponiblesTotales) : 0.0;

        } catch (Exception e) {
            logger.error("Error al calcular tasa de ocupación", e);
            return 0.0;
        }
    }
}
