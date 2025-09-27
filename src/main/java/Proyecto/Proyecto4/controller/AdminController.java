package Proyecto.Proyecto4.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import Proyecto.Proyecto4.models.Administrador;
import Proyecto.Proyecto4.models.Administrador.RolAdmin;
import Proyecto.Proyecto4.services.AdministradorService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @Autowired
    private AdministradorService administradorService;
    
    @PostMapping("/crear")
    public ResponseEntity<?> crearAdministrador(@RequestBody Map<String, Object> datos) {
        try {
            String email = (String) datos.get("email");
            String password = (String) datos.get("password");
            String nombres = (String) datos.get("nombres");
            String apellidos = (String) datos.get("apellidos");
            String telefono = (String) datos.get("telefono");
            String rolStr = (String) datos.get("rol");
            String hotel = (String) datos.get("hotel");
            
            // Validar datos requeridos
            if (email == null || password == null || nombres == null || apellidos == null || rolStr == null) {
                return ResponseEntity.badRequest().body("Faltan campos requeridos");
            }
            
            RolAdmin rol = RolAdmin.valueOf(rolStr.toUpperCase());
            
            Administrador admin = administradorService.crearAdministrador(
                email, password, nombres, apellidos, telefono, rol, hotel
            );
            
            return ResponseEntity.ok().body(Map.of(
                "message", "Administrador creado exitosamente",
                "id", admin.getId(),
                "email", admin.getEmail()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @PostMapping("/crear-super-admin")
    public ResponseEntity<?> crearSuperAdminInicial() {
        try {
            administradorService.crearSuperAdminInicial();
            return ResponseEntity.ok().body("Super Admin creado o ya existe");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
    
    @GetMapping("/listar")
    public ResponseEntity<?> listarAdministradores() {
        try {
            return ResponseEntity.ok(administradorService.obtenerTodosLosAdministradores());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}