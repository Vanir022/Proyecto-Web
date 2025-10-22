package Proyecto.Proyecto4.controller;

import Proyecto.Proyecto4.dto.RegistroUsuarioDTO;
import Proyecto.Proyecto4.models.*;
import Proyecto.Proyecto4.services.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            // Validar errores de validación
            if (bindingResult.hasErrors()) {
                Map<String, String> errores = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Error de validación",
                        (existing, replacement) -> existing
                    ));
                return ResponseEntity.badRequest().body(errores);
            }
            
            // Validar que las contraseñas coincidan
            if (!registroDTO.getPassword().equals(registroDTO.getConfirmPassword())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Las contraseñas no coinciden");
                return ResponseEntity.badRequest().body(error);
            }

            Usuario usuario = usuarioService.registrarConDetalles(registroDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Usuario registrado exitosamente");
            response.put("email", usuario.getEmail());
            response.put("nombre", usuario.getNombre());
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error interno del servidor");
            return ResponseEntity.internalServerError().body(error);
        }
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
}
