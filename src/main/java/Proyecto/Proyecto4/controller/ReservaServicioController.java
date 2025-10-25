package Proyecto.Proyecto4.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import Proyecto.Proyecto4.models.ReservaServicio;
import Proyecto.Proyecto4.models.Usuario;
import Proyecto.Proyecto4.services.ReservaServicioService;
import Proyecto.Proyecto4.services.UsuarioService;

@Controller
@RequestMapping("/reservas-servicios")
public class ReservaServicioController {

    private static final Logger logger = LoggerFactory.getLogger(ReservaServicioController.class);

    @Autowired
    private ReservaServicioService reservaServicioService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Mostrar el formulario de reserva de servicios
     */
    @GetMapping("/nueva")
    public String mostrarFormularioReserva(@RequestParam(required = false) String tipo, Model model) {
        model.addAttribute("tipoServicio", tipo);
        return "html/Servicios/formulario-reserva";
    }

    /**
     * Procesar reserva de SPA
     */
    @PostMapping("/spa")
    @ResponseBody
    public ResponseEntity<?> crearReservaSpa(@RequestBody Map<String, Object> datos) {
        try {
            logger.info("📌 Recibiendo solicitud de reserva de SPA");
            
            // Obtener usuario autenticado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            Usuario usuario = usuarioService.buscarPorEmail(email).orElse(null);

            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Debe iniciar sesión para hacer una reserva"));
            }

            // Crear la reserva
            ReservaServicio reserva = new ReservaServicio();
            reserva.setUsuario(usuario);
            reserva.setTipoServicio(ReservaServicio.TipoServicio.SPA);
            
            // Nombre del paquete/tratamiento
            String tratamiento = (String) datos.get("tratamiento");
            reserva.setNombrePaquete(tratamiento);
            
            // Fecha y hora
            LocalDate fechaServicio = LocalDate.parse((String) datos.get("fecha"));
            LocalTime horaServicio = LocalTime.parse((String) datos.get("hora"));
            reserva.setFechaServicio(fechaServicio);
            reserva.setHoraServicio(horaServicio);
            
            // Número de personas
            Integer personas = Integer.parseInt(datos.get("personas").toString());
            reserva.setNumeroPersonas(personas);
            
            // Monto (extraer del string del tratamiento)
            BigDecimal monto = extraerMonto((String) datos.get("tratamientoTexto"));
            reserva.setMontoTotal(monto);
            
            // Datos de contacto del usuario autenticado
            reserva.setDniCliente(usuario.getDetallesPersona() != null ? 
                usuario.getDetallesPersona().getDni() : "");
            reserva.setTelefonoContacto(usuario.getDetallesPersona() != null ? 
                usuario.getDetallesPersona().getTelefono() : "");
            reserva.setEmailContacto(usuario.getEmail());
            
            // Solicitudes especiales
            reserva.setSolicitudesEspeciales((String) datos.get("mensaje"));
            
            // Estado inicial
            reserva.setEstado(ReservaServicio.EstadoReserva.PENDIENTE);
            reserva.setFechaReserva(LocalDateTime.now());

            // Guardar la reserva
            ReservaServicio reservaGuardada = reservaServicioService.crearReserva(reserva);
            
            logger.info("✅ Reserva de SPA creada exitosamente: {}", reservaGuardada.getCodigoReserva());

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("codigo", reservaGuardada.getCodigoReserva());
            respuesta.put("mensaje", "Reserva creada exitosamente");
            
            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            logger.error("❌ Error al crear reserva de SPA: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al procesar la reserva: " + e.getMessage()));
        }
    }

    /**
     * Procesar reserva de BODA
     */
    @PostMapping("/boda")
    @ResponseBody
    public ResponseEntity<?> crearReservaBoda(@RequestBody Map<String, Object> datos) {
        try {
            logger.info("📌 Recibiendo solicitud de reserva de BODA");
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            Usuario usuario = usuarioService.buscarPorEmail(email).orElse(null);

            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Debe iniciar sesión para hacer una reserva"));
            }

            ReservaServicio reserva = new ReservaServicio();
            reserva.setUsuario(usuario);
            reserva.setTipoServicio(ReservaServicio.TipoServicio.BODA);
            
            // Paquete seleccionado
            String paquete = (String) datos.get("paqueteTexto");
            reserva.setNombrePaquete(paquete);
            
            // Fecha de la boda
            LocalDate fechaBoda = LocalDate.parse((String) datos.get("fecha"));
            reserva.setFechaServicio(fechaBoda);
            reserva.setHoraServicio(LocalTime.of(16, 0)); // Hora default 4 PM
            
            // Número de invitados (convertir rango a número)
            String invitadosRango = (String) datos.get("invitados");
            Integer numeroInvitados = extraerNumeroInvitados(invitadosRango);
            reserva.setNumeroPersonas(numeroInvitados);
            
            // Monto del paquete
            BigDecimal monto = extraerMonto((String) datos.get("paqueteTexto"));
            reserva.setMontoTotal(monto);
            
            // Destino/Hotel
            String destino = (String) datos.get("destino");
            reserva.setHotel(convertirDestinoAHotel(destino));
            
            // Datos de contacto del usuario autenticado
            reserva.setTelefonoContacto(usuario.getDetallesPersona() != null ? 
                usuario.getDetallesPersona().getTelefono() : "");
            reserva.setEmailContacto(usuario.getEmail());
            reserva.setDniCliente(usuario.getDetallesPersona() != null ? 
                usuario.getDetallesPersona().getDni() : "");
            
            // Comentarios
            String comentarios = String.format("Novios: %s y %s\nPresupuesto: %s\nServicios adicionales: %s",
                datos.get("novia"), datos.get("novio"), datos.get("presupuesto"), datos.get("serviciosAdicionales"));
            reserva.setComentarios(comentarios);
            reserva.setSolicitudesEspeciales((String) datos.get("mensaje"));
            
            reserva.setEstado(ReservaServicio.EstadoReserva.PENDIENTE);
            reserva.setFechaReserva(LocalDateTime.now());

            ReservaServicio reservaGuardada = reservaServicioService.crearReserva(reserva);
            
            logger.info("✅ Reserva de BODA creada exitosamente: {}", reservaGuardada.getCodigoReserva());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "codigo", reservaGuardada.getCodigoReserva(),
                "mensaje", "Solicitud de cotización enviada exitosamente"
            ));

        } catch (Exception e) {
            logger.error("❌ Error al crear reserva de BODA: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al procesar la solicitud: " + e.getMessage()));
        }
    }

    /**
     * Procesar reserva de EVENTO
     */
    @PostMapping("/evento")
    @ResponseBody
    public ResponseEntity<?> crearReservaEvento(@RequestBody Map<String, Object> datos) {
        try {
            logger.info("📌 Recibiendo solicitud de reserva de EVENTO");
            
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            Usuario usuario = usuarioService.buscarPorEmail(email).orElse(null);

            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Debe iniciar sesión para hacer una reserva"));
            }

            ReservaServicio reserva = new ReservaServicio();
            reserva.setUsuario(usuario);
            reserva.setTipoServicio(ReservaServicio.TipoServicio.EVENTO);
            
            // Tipo de evento
            String tipoEvento = (String) datos.get("tipoEvento");
            reserva.setNombrePaquete(tipoEvento);
            
            // Fecha y hora del evento
            LocalDate fechaEvento = LocalDate.parse((String) datos.get("fechaEvento"));
            reserva.setFechaServicio(fechaEvento);
            
            String horaStr = (String) datos.get("horaEvento");
            if (horaStr != null && !horaStr.isEmpty()) {
                reserva.setHoraServicio(LocalTime.parse(horaStr));
            } else {
                reserva.setHoraServicio(LocalTime.of(9, 0)); // Default 9 AM
            }
            
            // Número de invitados
            Integer invitados = Integer.parseInt(datos.get("invitados").toString());
            reserva.setNumeroPersonas(invitados);
            
            // Calcular monto aproximado según el tipo de salón
            BigDecimal monto = calcularMontoEvento(invitados);
            reserva.setMontoTotal(monto);
            
            // Datos de contacto del usuario autenticado
            reserva.setDniCliente(usuario.getDetallesPersona() != null ? 
                usuario.getDetallesPersona().getDni() : "");
            reserva.setTelefonoContacto(usuario.getDetallesPersona() != null ? 
                usuario.getDetallesPersona().getTelefono() : "");
            reserva.setEmailContacto(usuario.getEmail());
            
            // Detalles del evento
            reserva.setSolicitudesEspeciales((String) datos.get("mensaje"));
            
            reserva.setEstado(ReservaServicio.EstadoReserva.PENDIENTE);
            reserva.setFechaReserva(LocalDateTime.now());

            ReservaServicio reservaGuardada = reservaServicioService.crearReserva(reserva);
            
            logger.info("✅ Reserva de EVENTO creada exitosamente: {}", reservaGuardada.getCodigoReserva());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "codigo", reservaGuardada.getCodigoReserva(),
                "mensaje", "Solicitud de evento enviada exitosamente"
            ));

        } catch (Exception e) {
            logger.error("❌ Error al crear reserva de EVENTO: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al procesar la solicitud: " + e.getMessage()));
        }
    }

    /**
     * Ver mis reservas de servicios
     */
    @GetMapping("/mis-reservas")
    public String verMisReservas(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            Usuario usuario = usuarioService.buscarPorEmail(email).orElse(null);

            if (usuario != null) {
                List<ReservaServicio> reservas = reservaServicioService.obtenerReservasPorUsuario(usuario);
                model.addAttribute("reservas", reservas);
                logger.info("📋 Usuario {} tiene {} reservas de servicios", email, reservas.size());
            }

            return "html/cliente-reservas-servicios";
        } catch (Exception e) {
            logger.error("❌ Error al cargar reservas: ", e);
            return "redirect:/login";
        }
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Extraer monto de un string como "Masaje Relajante - S/180"
     */
    private BigDecimal extraerMonto(String texto) {
        try {
            if (texto == null || !texto.contains("S/")) {
                return BigDecimal.ZERO;
            }
            
            String montoStr = texto.substring(texto.indexOf("S/") + 2).trim();
            // Remover comas y otros caracteres
            montoStr = montoStr.replaceAll("[^0-9.]", "");
            
            return new BigDecimal(montoStr);
        } catch (Exception e) {
            logger.warn("⚠️ No se pudo extraer monto de: {}", texto);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Extraer número de invitados de un rango como "31-80"
     */
    private Integer extraerNumeroInvitados(String rango) {
        try {
            if (rango == null) return 50;
            
            if (rango.contains("-")) {
                String[] partes = rango.split("-");
                int min = Integer.parseInt(partes[0].trim());
                int max = Integer.parseInt(partes[1].trim());
                return (min + max) / 2; // Promedio
            } else if (rango.contains("+")) {
                return 200; // Más de 150 = 200
            }
            
            return Integer.parseInt(rango.trim());
        } catch (Exception e) {
            return 50; // Default
        }
    }

    /**
     * Convertir código de destino a nombre de hotel
     */
    private String convertirDestinoAHotel(String destino) {
        if (destino == null) return "Por definir";
        
        switch (destino.toLowerCase()) {
            case "cusco":
                return "Eden Cusco";
            case "paracas":
                return "Eden Paracas";
            case "valle-sagrado":
                return "Eden Valle Sagrado";
            default:
                return "Por definir";
        }
    }

    /**
     * Calcular monto aproximado según número de invitados
     */
    private BigDecimal calcularMontoEvento(Integer invitados) {
        if (invitados <= 40) {
            return new BigDecimal("800");
        } else if (invitados <= 80) {
            return new BigDecimal("1200");
        } else if (invitados <= 150) {
            return new BigDecimal("1800");
        } else {
            return new BigDecimal("2500");
        }
    }
}
