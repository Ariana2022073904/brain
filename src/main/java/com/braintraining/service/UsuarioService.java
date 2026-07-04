package com.braintraining.service;

import com.braintraining.model.Usuario;
import com.braintraining.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean existeUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public void registrar(String username, String password, String email, int edad) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(password));
        u.setEmail(email);
        u.setEdad(edad);
        u.setRol(Usuario.Rol.usuario);
        usuarioRepository.save(u);
    }

    public Optional<Usuario> findByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
    }

    public long contarUsuarios() {
        return usuarioRepository.count();
    }

    /**
     * Actualiza username, password, foto y descripción del usuario.
     * @param nuevoUsername  nuevo username (puede ser igual al actual)
     * @param nuevaPassword  nueva contraseña en texto plano, o null/vacío para no cambiarla
     * @param fotoBytes      bytes de la nueva foto, o null para no cambiarla
     * @param fotoTipo       content-type de la foto (image/png, image/jpeg, etc.)
     * @param descripcion    descripción/bio del usuario, puede ser null o vacía
     * @return el nombre de usuario final (puede haber cambiado)
     * @throws IllegalArgumentException si el nuevo username ya está en uso por otro usuario
     */
    public String actualizarPerfil(Usuario usuario, String nuevoUsername, String nuevaPassword,
                                    byte[] fotoBytes, String fotoTipo, String descripcion) {

        if (nuevoUsername != null && !nuevoUsername.isBlank() && !nuevoUsername.equals(usuario.getUsername())) {
            if (usuarioRepository.existsByUsername(nuevoUsername)) {
                throw new IllegalArgumentException("Ese nombre de usuario ya está en uso.");
            }
            usuario.setUsername(nuevoUsername);
        }

        if (nuevaPassword != null && !nuevaPassword.isBlank()) {
            usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        }

        if (fotoBytes != null && fotoBytes.length > 0) {
            usuario.setFoto(fotoBytes);
            usuario.setFotoTipo(fotoTipo);
        }

        usuario.setDescripcion(descripcion);

        usuarioRepository.save(usuario);
        return usuario.getUsername();
    }
}