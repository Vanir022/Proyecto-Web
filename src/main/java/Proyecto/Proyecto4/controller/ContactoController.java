package Proyecto.Proyecto4.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import Proyecto.Proyecto4.models.Administrador;
import Proyecto.Proyecto4.models.Contacto;
import Proyecto.Proyecto4.services.AdministradorService;
import Proyecto.Proyecto4.services.ContactoService;
import jakarta.validation.Valid;

@Controller
public class ContactoController {
    
    @Autowired
    private ContactoService contactoService;
    
    @Autowired
    private AdministradorService administradorService;
    
    // Mostrar página de contacto
    @GetMapping("/contactos")
    public String mostrarContactos() {
        return "html/Contactos";
    }
    
    // Procesar formulario de contacto
    @PostMapping("/contactos/enviar")
    public String enviarMensaje(
            @Valid @ModelAttribute Contacto contacto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Validar errores de validación
            if (bindingResult.hasErrors()) {
                String errorMsg = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .findFirst()
                    .orElse("Error de validación en el formulario");
                redirectAttributes.addFlashAttribute("error", errorMsg);
                return "redirect:/contactos";
            }
            
            // Guardar contacto
            Contacto contactoGuardado = contactoService.guardarContacto(contacto);
            
            if (contactoGuardado != null) {
                redirectAttributes.addFlashAttribute("success", 
                    "¡Gracias por contactarnos! Hemos recibido tu mensaje y te responderemos pronto.");
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Hubo un error al enviar tu mensaje. Por favor, inténtalo de nuevo.");
            }
            
        } catch (Exception e) {
            System.err.println("Error al guardar contacto: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", 
                "Error técnico: " + e.getMessage());
        }
        
        return "redirect:/contactos";
    }
    
    // Panel de administración para ver mensajes (opcional)
    @GetMapping("/admin/contactos")
    public String panelAdmin(Authentication authentication, Model model) {
        try {
            // Obtener administrador autenticado
            String email = authentication.getName();
            Optional<Administrador> adminOpt = administradorService.buscarPorEmail(email);
            
            if (adminOpt.isPresent()) {
                Administrador admin = adminOpt.get();
                model.addAttribute("admin", admin);
            }
            
            // Agregar datos de contactos
            model.addAttribute("contactos", contactoService.obtenerTodosLosContactos());
            model.addAttribute("nuevos", contactoService.contarContactosPorEstado("NUEVO"));
            model.addAttribute("leidos", contactoService.contarContactosPorEstado("LEIDO"));
            model.addAttribute("respondidos", contactoService.contarContactosPorEstado("RESPONDIDO"));
            
            return "html/admin/contactos";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar los mensajes: " + e.getMessage());
            return "html/admin/contactos";
        }
    }
    
    // Marcar mensaje como leído
    @PostMapping("/admin/contactos/{id}/marcar-leido")
    @ResponseBody
    public String marcarLeido(@PathVariable Long id) {
        try {
            Contacto contacto = contactoService.marcarComoLeido(id);
            if (contacto != null) {
                return "success";
            }
            return "error";
        } catch (Exception e) {
            System.err.println("Error al marcar como leído: " + e.getMessage());
            return "error";
        }
    }
    
    // Marcar mensaje como respondido
    @PostMapping("/admin/contactos/{id}/marcar-respondido")
    @ResponseBody
    public String marcarRespondido(@PathVariable Long id) {
        try {
            Contacto contacto = contactoService.marcarComoRespondido(id);
            if (contacto != null) {
                return "success";
            }
            return "error";
        } catch (Exception e) {
            System.err.println("Error al marcar como respondido: " + e.getMessage());
            return "error";
        }
    }
    
    // Eliminar mensaje
    @DeleteMapping("/admin/contactos/{id}/eliminar")
    @ResponseBody
    public String eliminarContacto(@PathVariable Long id) {
        try {
            boolean eliminado = contactoService.eliminarContacto(id);
            return eliminado ? "success" : "error";
        } catch (Exception e) {
            System.err.println("Error al eliminar contacto: " + e.getMessage());
            return "error";
        }
    }
}