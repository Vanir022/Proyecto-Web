package Proyecto.Proyecto4.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {


    // Mapeo para el header
    @GetMapping("/")
    public String index() {
        return "index"; // busca templates/index.html
    }

    @GetMapping("/nosotros")
    public String nosotros() {
        return "html/Nosotros"; // Thymeleaf buscará templates/Nosotros.html
    }
    
    @GetMapping("/contactos")
    public String contactos() {
        return "html/Contactos"; // Thymeleaf buscará templates/Contactos.html
    }
    @GetMapping("/login")
    public String login() {
        return "html/login"; // Thymeleaf buscará templates/login.html
    }

    @GetMapping("/register")
    public String register() {
        return "html/Register"; // Thymeleaf buscará templates/register.html
        
    }

    //Mapeo para el footer
    @GetMapping("/acercade")
    public String acercade() {
        return "html/Acercade"; // Thymeleaf buscará templates/AcercaDe.html
    }

    @GetMapping("/eventos")
    public String eventos() {
        return "html/Servicios/Eventos"; // Thymeleaf buscará templates/Eventos.html
    }

    @GetMapping("/spa")
    public String spa() {
        return "html/Servicios/Spa"; // Thymeleaf buscará templates/Spa.html
    }
    @GetMapping("/bodas")
    public String bodas() {
        return "html/Servicios/Bodas"; // Thymeleaf buscará templates/Bodas.html
    }
}