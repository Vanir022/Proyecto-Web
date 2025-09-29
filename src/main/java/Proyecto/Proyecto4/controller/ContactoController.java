package Proyecto.Proyecto4.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import Proyecto.Proyecto4.models.Contacto;
import Proyecto.Proyecto4.services.ContactoService;

@Controller
public class ContactoController {
    
    @Autowired
    private ContactoService contactoService;
    
    // Mostrar página de contacto
    @GetMapping("/contactos")
    public String mostrarContactos() {
        return "html/Contactos";
    }
    
    // Procesar formulario de contacto
    @PostMapping("/contactos/enviar")
    public String enviarMensaje(
            @RequestParam("nombre") String nombre,
            @RequestParam("email") String email,
            @RequestParam(value = "telefono", required = false) String telefono,
            @RequestParam(value = "hotel", required = false) String hotel,
            @RequestParam("mensaje") String mensaje,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Validaciones básicas
            if (nombre == null || nombre.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El nombre es obligatorio");
                return "redirect:/contactos";
            }
            
            if (email == null || email.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El email es obligatorio");
                return "redirect:/contactos";
            }
            
            if (mensaje == null || mensaje.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El mensaje es obligatorio");
                return "redirect:/contactos";
            }
            
            // Limpiar datos
            nombre = nombre.trim();
            email = email.trim().toLowerCase();
            telefono = (telefono != null && !telefono.trim().isEmpty()) ? telefono.trim() : null;
            hotel = (hotel != null && !hotel.trim().isEmpty()) ? hotel.trim() : null;
            mensaje = mensaje.trim();
            
            // Crear y guardar contacto
            Contacto contacto = new Contacto(nombre, email, telefono, hotel, mensaje);
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
    public String panelAdmin(Model model) {
        try {
            model.addAttribute("contactos", contactoService.obtenerTodosLosContactos());
            model.addAttribute("nuevos", contactoService.contarContactosPorEstado("NUEVO"));
            model.addAttribute("leidos", contactoService.contarContactosPorEstado("LEIDO"));
            model.addAttribute("respondidos", contactoService.contarContactosPorEstado("RESPONDIDO"));
            return "admin/contactos";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar los mensajes: " + e.getMessage());
            return "admin/contactos";
        }
    }
}