package Proyecto.Proyecto4.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Proyecto.Proyecto4.dto.RegistroUsuarioDTO;
import Proyecto.Proyecto4.models.Usuario;
import Proyecto.Proyecto4.services.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UsuarioService usuarioService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@Valid @RequestBody RegistroUsuarioDTO registroDTO, BindingResult bindingResult) {
        try {
            logger.info("Iniciando registro de usuario con email: {}", registroDTO.getEmail());
            
            // Validar errores de validación
            if (bindingResult.hasErrors()) {
                Map<String, String> errores = new HashMap<>();
                for (FieldError error : bindingResult.getFieldErrors()) {
                    errores.put(error.getField(), error.getDefaultMessage() != null ? error.getDefaultMessage() : "Error de validación");
                }
                logger.warn("Errores de validación: {}", errores);
                return ResponseEntity.badRequest().body(errores);
            }
            
            // Validar que las contraseñas coincidan
            if (!registroDTO.getPassword().equals(registroDTO.getConfirmPassword())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Las contraseñas no coinciden");
                logger.warn("Las contraseñas no coinciden para el email: {}", registroDTO.getEmail());
                return ResponseEntity.badRequest().body(error);
            }

            // Validar edad mínima (18 años) en el backend también
            if (registroDTO.getBirthDate() != null) {
                var today = java.time.LocalDate.now();
                var age = today.minusYears(18);
                if (registroDTO.getBirthDate().isAfter(age)) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Debes ser mayor de 18 años para registrarte");
                    logger.warn("Usuario menor de edad intentando registrarse: {}", registroDTO.getEmail());
                    return ResponseEntity.badRequest().body(error);
                }
            }

            Usuario usuario = usuarioService.registrarConDetalles(registroDTO);
            logger.info("Usuario registrado exitosamente: {}", usuario.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Usuario registrado exitosamente");
            response.put("email", usuario.getEmail());
            response.put("nombre", usuario.getNombre());
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Error durante el registro: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("Error interno durante el registro", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno del servidor. Por favor, inténtelo de nuevo.");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // Endpoint de prueba para verificar conectividad
    @PostMapping("/test-connection")
    public ResponseEntity<?> testConnection() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Conexión funcionando correctamente");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    // Mantener el método anterior para compatibilidad
    @PostMapping("/registro-simple")
    public Usuario registrarSimple(@RequestBody Usuario usuario) {
        return usuarioService.registrar(usuario);
    }

    @GetMapping("/welcome")
    public ResponseEntity<?> welcome(HttpServletRequest request) {
        logger.info("Request received: {} {}", request.getMethod(), request.getRequestURI());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Welcome to the Resort Eden API!");
        return ResponseEntity.ok(response);
    }

    // Endpoints para validaciones individuales en tiempo real
    @PostMapping("/validar-email")
    public ResponseEntity<?> validarEmail(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email requerido"));
            }
            
            boolean existe = usuarioService.emailExiste(email.trim());
            return ResponseEntity.ok(Map.of("existe", existe));
            
        } catch (Exception e) {
            logger.error("Error al validar email", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Error del servidor"));
        }
    }

    @PostMapping("/validar-telefono")
    public ResponseEntity<?> validarTelefono(@RequestBody Map<String, String> request) {
        try {
            String telefono = request.get("telefono");
            if (telefono == null || telefono.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Teléfono requerido"));
            }
            
            boolean existe = usuarioService.telefonoExiste(telefono.trim());
            return ResponseEntity.ok(Map.of("existe", existe));
            
        } catch (Exception e) {
            logger.error("Error al validar teléfono", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Error del servidor"));
        }
    }

    @PostMapping("/validar-dni")
    public ResponseEntity<?> validarDni(@RequestBody Map<String, String> request) {
        try {
            String dni = request.get("dni");
            if (dni == null || dni.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "DNI requerido"));
            }
            
            boolean existe = usuarioService.dniExiste(dni.trim());
            return ResponseEntity.ok(Map.of("existe", existe));
            
        } catch (Exception e) {
            logger.error("Error al validar DNI", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Error del servidor"));
        }
    }
}
