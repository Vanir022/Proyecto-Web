package Proyecto.Proyecto4.controller;

import Proyecto.Proyecto4.dto.RegistroUsuarioDTO;
import Proyecto.Proyecto4.models.*;
import Proyecto.Proyecto4.services.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody RegistroUsuarioDTO registroDTO) {
        try {
            // Validar que las contraseñas coincidan
            if (!registroDTO.getPassword().equals(registroDTO.getConfirmPassword())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Las contraseñas no coinciden");
                return ResponseEntity.badRequest().body(error);
            }

            // Validar que se acepten los términos
            if (!registroDTO.getAcceptTerms()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Debe aceptar los términos y condiciones");
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
}