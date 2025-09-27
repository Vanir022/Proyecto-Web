package Proyecto.Proyecto4.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import Proyecto.Proyecto4.models.Usuario;
import Proyecto.Proyecto4.repository.UsuarioRepository;

@ControllerAdvice
public class GlobalModelController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @ModelAttribute
    public void addAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            String email = auth.getName();
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
            
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                model.addAttribute("usuario", usuario);
                
                if (usuario.getDetallesPersona() != null) {
                    model.addAttribute("detalles", usuario.getDetallesPersona());
                }
            }
        }
    }
}