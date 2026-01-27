package Proyecto.Proyecto4.controller;

import Proyecto.Proyecto4.models.*;
import Proyecto.Proyecto4.services.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.listar();
    }

    @PostMapping("/desbloquear/{id}")
    public ResponseEntity<String> desbloquearUsuario(@PathVariable("id") Long id) {
        try {
            boolean ok = usuarioService.desbloquearUsuario(id);
            if (ok) {
                return ResponseEntity.ok("Usuario desbloqueado exitosamente");
            } else {
                return ResponseEntity.status(404).body("Usuario no encontrado");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error desbloqueando usuario: " + e.getMessage());
        }
    }
}
