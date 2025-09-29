package Proyecto.Proyecto4.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import Proyecto.Proyecto4.models.Contacto;
import Proyecto.Proyecto4.repository.ContactoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ContactoService {
    
    @Autowired
    private ContactoRepository contactoRepository;
    
    // Guardar un nuevo contacto
    public Contacto guardarContacto(Contacto contacto) {
        return contactoRepository.save(contacto);
    }
    
    // Guardar contacto con parámetros
    public Contacto guardarContacto(String nombre, String email, String telefono, String hotel, String mensaje) {
        Contacto contacto = new Contacto(nombre, email, telefono, hotel, mensaje);
        return contactoRepository.save(contacto);
    }
    
    // Obtener todos los contactos
    public List<Contacto> obtenerTodosLosContactos() {
        return contactoRepository.findAllByOrderByFechaEnvioDesc();
    }
    
    // Obtener contacto por ID
    public Optional<Contacto> obtenerContactoPorId(Long id) {
        return contactoRepository.findById(id);
    }
    
    // Obtener contactos por estado
    public List<Contacto> obtenerContactosPorEstado(String estado) {
        return contactoRepository.findByEstadoOrderByFechaEnvioDesc(estado);
    }
    
    // Obtener contactos por hotel
    public List<Contacto> obtenerContactosPorHotel(String hotel) {
        return contactoRepository.findByHotelOrderByFechaEnvioDesc(hotel);
    }
    
    // Contar contactos por estado
    public Long contarContactosPorEstado(String estado) {
        return contactoRepository.countByEstado(estado);
    }
    
    // Cambiar estado del contacto
    public Contacto cambiarEstado(Long id, String nuevoEstado) {
        Optional<Contacto> contactoOpt = contactoRepository.findById(id);
        if (contactoOpt.isPresent()) {
            Contacto contacto = contactoOpt.get();
            contacto.setEstado(nuevoEstado);
            return contactoRepository.save(contacto);
        }
        return null;
    }
    
    // Eliminar contacto
    public boolean eliminarContacto(Long id) {
        try {
            contactoRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // Obtener contactos nuevos (no leídos)
    public List<Contacto> obtenerContactosNuevos() {
        return obtenerContactosPorEstado("NUEVO");
    }
    
    // Marcar como leído
    public Contacto marcarComoLeido(Long id) {
        return cambiarEstado(id, "LEIDO");
    }
    
    // Marcar como respondido
    public Contacto marcarComoRespondido(Long id) {
        return cambiarEstado(id, "RESPONDIDO");
    }
}