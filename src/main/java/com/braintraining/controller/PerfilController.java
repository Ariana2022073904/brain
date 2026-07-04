/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.braintraining.controller;

import com.braintraining.model.ExamenPerfil;
import com.braintraining.model.Usuario;
import com.braintraining.repository.ExamenPerfilRepository;
import com.braintraining.repository.UsuarioRepository;
import com.braintraining.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 *
 * @author FAMILIA
 */
@Controller
public class PerfilController {

    /** Tamaño máximo (ancho o alto) que tendrá la foto guardada, en píxeles. */
    private static final int FOTO_MAX_DIMENSION = 400;
    /** Calidad de compresión JPEG (0.0 - 1.0). */
    private static final float FOTO_CALIDAD_JPEG = 0.82f;

    @Autowired private UsuarioService usuarioService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ExamenPerfilRepository examenRepo;

    @GetMapping("/perfil")
    public String perfil(Authentication auth, Model model) {
        Usuario usuario = usuarioService.findByUsername(auth.getName()).orElseThrow();
        ExamenPerfil perfil = examenRepo.findByUsuario(usuario).orElse(null);

        model.addAttribute("usuario", usuario);
        model.addAttribute("perfil", perfil);
        model.addAttribute("activePage", "perfil");
        return "perfil";
    }

    @PostMapping("/perfil")
    public String guardarPerfil(Authentication auth,
                                 @RequestParam String username,
                                 @RequestParam(required = false) String password,
                                 @RequestParam(required = false) String descripcion,
                                 @RequestParam(required = false) MultipartFile foto,
                                 Model model) {

        Usuario usuario = usuarioService.findByUsername(auth.getName()).orElseThrow();

        try {
            byte[] fotoBytes = null;
            String fotoTipo = null;
            if (foto != null && !foto.isEmpty()) {
                fotoBytes = redimensionarYComprimir(foto.getBytes());
                fotoTipo = "image/jpeg";
            }

            String nuevoUsername = usuarioService.actualizarPerfil(
                    usuario, username, password, fotoBytes, fotoTipo, descripcion
            );

            // Si el username cambió, actualizamos el contexto de seguridad
            // para que la sesión actual siga siendo válida sin re-login.
            if (!nuevoUsername.equals(auth.getName())) {
                Authentication nuevaAuth = new UsernamePasswordAuthenticationToken(
                        nuevoUsername, auth.getCredentials(), auth.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(nuevaAuth);
            }

        } catch (IllegalArgumentException e) {
            return conError(usuario, model, e.getMessage());
        } catch (IOException e) {
            return conError(usuario, model, "No se pudo procesar la imagen. Intenta con otra foto (jpg, png o webp).");
        }

        return "redirect:/menu?perfilOk";
    }

    private String conError(Usuario usuario, Model model, String mensaje) {
        ExamenPerfil perfil = examenRepo.findByUsuario(usuario).orElse(null);
        model.addAttribute("usuario", usuario);
        model.addAttribute("perfil", perfil);
        model.addAttribute("activePage", "perfil");
        model.addAttribute("error", mensaje);
        return "perfil";
    }

    /**
     * Redimensiona la imagen (máx. 400x400) y la re-comprime como JPEG
     * para que nunca ocupe demasiado espacio en la base de datos
     * (evita el error "Packet for query is too large" de MySQL).
     */
    private byte[] redimensionarYComprimir(byte[] original) throws IOException {

        BufferedImage imagenOriginal = ImageIO.read(new ByteArrayInputStream(original));

        if (imagenOriginal == null) {
            throw new IOException("Formato de imagen no reconocido.");
        }

        int anchoOriginal = imagenOriginal.getWidth();
        int altoOriginal = imagenOriginal.getHeight();

        double escala = Math.min(
                1.0,
                (double) FOTO_MAX_DIMENSION / Math.max(anchoOriginal, altoOriginal)
        );

        int nuevoAncho = Math.max(1, (int) Math.round(anchoOriginal * escala));
        int nuevoAlto = Math.max(1, (int) Math.round(altoOriginal * escala));

        BufferedImage imagenRedimensionada = new BufferedImage(
                nuevoAncho, nuevoAlto, BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g2d = imagenRedimensionada.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        // fondo blanco por si la imagen original tenía transparencia (PNG)
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, nuevoAncho, nuevoAlto);
        g2d.drawImage(imagenOriginal, 0, 0, nuevoAncho, nuevoAlto, null);
        g2d.dispose();

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IOException("No hay un codificador JPEG disponible.");
        }
        ImageWriter writer = writers.next();

        ImageWriteParam parametros = writer.getDefaultWriteParam();
        parametros.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        parametros.setCompressionQuality(FOTO_CALIDAD_JPEG);

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(salida)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(imagenRedimensionada, null, null), parametros);
        } finally {
            writer.dispose();
        }

        return salida.toByteArray();
    }

    @GetMapping("/perfil/foto")
    public ResponseEntity<byte[]> fotoPropia(Authentication auth) {
        Usuario usuario = usuarioService.findByUsername(auth.getName()).orElseThrow();
        return servirFoto(usuario);
    }

    @GetMapping("/perfil/foto/{username}")
    public ResponseEntity<byte[]> fotoDe(@PathVariable String username) {
        Usuario usuario = usuarioService.findByUsername(username).orElseThrow();
        return servirFoto(usuario);
    }

    private ResponseEntity<byte[]> servirFoto(Usuario usuario) {
        if (!usuario.isTieneFoto()) {
            return ResponseEntity.notFound().build();
        }
        MediaType tipo;
        try {
            tipo = MediaType.parseMediaType(
                    usuario.getFotoTipo() != null ? usuario.getFotoTipo() : "image/jpeg"
            );
        } catch (Exception e) {
            tipo = MediaType.IMAGE_JPEG;
        }
        return ResponseEntity.ok()
                .contentType(tipo)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .body(usuario.getFoto());
    }
}