package Proyecto.Proyecto4.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import Proyecto.Proyecto4.models.Usuario;
import Proyecto.Proyecto4.services.UsuarioService;

@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    @Autowired
    private UsuarioService usuarioService;

    private final String UPLOAD_DIR = "src/main/resources/static/uploads/fotos/";

    @PostMapping("/subir-foto")
    public ResponseEntity<?> subirFoto(@RequestParam("foto") MultipartFile file,
                                      @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("No se seleccionó ningún archivo");
            }

            // Validar tipo de archivo
            String contentType = file.getContentType();
            if (!isImageFile(contentType)) {
                return ResponseEntity.badRequest().body("Solo se permiten archivos de imagen (JPG, PNG, GIF)");
            }

            // Buscar usuario
            Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(userDetails.getUsername());
            if (!usuarioOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Usuario no encontrado");
            }

            Usuario usuario = usuarioOpt.get();
            
            // Crear directorio si no existe
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Generar nombre único para el archivo
            String fileName = "perfil_" + usuario.getId() + "_" + System.currentTimeMillis() + 
                            getFileExtension(file.getOriginalFilename());

            // Guardar archivo
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.copy(file.getInputStream(), filePath);

            // Actualizar base de datos
            if (usuario.getDetallesPersona() != null) {
                usuario.getDetallesPersona().setFotoPerfil(fileName);
                usuarioService.guardar(usuario);
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Foto subida exitosamente");
            response.put("fileName", fileName);
            response.put("url", "/uploads/fotos/" + fileName);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error al subir la foto: " + e.getMessage());
        }
    }

    @PostMapping("/cambiar-password")
    public ResponseEntity<?> cambiarPassword(@RequestBody Map<String, String> passwords,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String passwordActual = passwords.get("passwordActual");
            String passwordNueva = passwords.get("passwordNueva");
            String confirmarPassword = passwords.get("confirmarPassword");

            if (!passwordNueva.equals(confirmarPassword)) {
                return ResponseEntity.badRequest().body("Las contraseñas nuevas no coinciden");
            }

            Optional<Usuario> usuarioOpt = usuarioService.buscarPorEmail(userDetails.getUsername());
            if (!usuarioOpt.isPresent()) {
                return ResponseEntity.badRequest().body("Usuario no encontrado");
            }

            Usuario usuario = usuarioOpt.get();

            // Validar password actual
            if (!usuarioService.validarPassword(passwordActual, usuario.getPassword())) {
                return ResponseEntity.badRequest().body("La contraseña actual es incorrecta");
            }

            // Cambiar password
            usuarioService.cambiarPassword(usuario, passwordNueva);

            return ResponseEntity.ok("Contraseña cambiada exitosamente");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al cambiar contraseña: " + e.getMessage());
        }
    }

    private boolean isImageFile(String contentType) {
        return contentType != null && (
            contentType.equals("image/jpeg") ||
            contentType.equals("image/jpg") ||
            contentType.equals("image/png") ||
            contentType.equals("image/gif")
        );
    }

    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.lastIndexOf(".") > 0) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return ".jpg"; // extensión por defecto
    }
}