package com.inventory.inventario.controller;

import com.inventory.inventario.model.AuthRequest;
import com.inventory.inventario.model.Rol;
import com.inventory.inventario.model.Usuario;
import com.inventory.inventario.repository.RolRepository;
import com.inventory.inventario.repository.UsuarioRepository;
import com.inventory.inventario.security.JwtUtil;
import com.inventory.inventario.security.TokenBlacklist;
import com.inventory.inventario.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class AuthController {


    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtTokenUtil;
    
    @Autowired
    private TokenBlacklist tokenBlacklist;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private RolRepository rolRepository;

    // Endpoint para verificar usuario (solo para depuración)
    @GetMapping("/check/{correo}")
    public ResponseEntity<?> checkUser(@PathVariable String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo);
        Map<String, Object> response = new HashMap<>();
        
        if (usuario != null) {
            response.put("existe", true);
            response.put("nombre", usuario.getNombre());
            response.put("correo", usuario.getCorreo());
            response.put("estatus", usuario.getEstatus());
            if (usuario.getRol() != null) {
                response.put("rol", usuario.getRol().getNombre());
                response.put("idRol", usuario.getRol().getId());
            } else {
                response.put("rol", "Sin rol asignado");
            }
            return ResponseEntity.ok(response);
        } else {
            response.put("existe", false);
            response.put("mensaje", "Usuario no encontrado");
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequest authRequest) {
        try {
            // Debug: imprimir información del intento de autenticación
            System.out.println("Intento de login con correo: " + authRequest.getCorreo());

            // Verificamos si el usuario existe
            Usuario usuario = usuarioRepository.findByCorreo(authRequest.getCorreo());
            if (usuario == null) {
                System.out.println("Usuario no encontrado: " + authRequest.getCorreo());
                return ResponseEntity.status(401).body(Map.of("mensaje", "Usuario no encontrado"));
            }

            // Verificamos si la contraseña coincide
            if (!authRequest.getContrasena().equals(usuario.getContrasena())) {
                System.out.println("Contraseña incorrecta para: " + authRequest.getCorreo());
                return ResponseEntity.status(401).body(Map.of("mensaje", "Contraseña incorrecta"));
            }

            // Generamos el token
            final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getCorreo());
            final String jwt = jwtTokenUtil.generateToken(userDetails);

            System.out.println("Autenticación exitosa para: " + authRequest.getCorreo());
            System.out.println("Token generado: " + jwt.substring(0, 10) + "...");

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("token", jwt);
            responseBody.put("usuario", Map.of(
                    "id", usuario.getId(),
                    "nombre", usuario.getNombre(),
                    "correo", usuario.getCorreo(),
                    "rol", usuario.getRol() != null ? usuario.getRol().getNombre() : null));

            return ResponseEntity.ok(responseBody);

        } catch (Exception e) {
            System.out.println("Error inesperado en autenticación: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("mensaje", "Error en autenticación: " + e.getMessage()));
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || authHeader.isEmpty() || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensaje", "Token no proporcionado o inválido"));
            }

            // Extraer el token
            String token = authHeader.substring(7);
            
            // Validar el token
            if (tokenBlacklist.isBlacklisted(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensaje", "Token en lista negra, sesión expirada"));
            }

            String correo = jwtTokenUtil.extractUsername(token);
            Usuario usuario = usuarioRepository.findByCorreo(correo);
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensaje", "Usuario no encontrado"));
            }

            return ResponseEntity.ok(Map.of(
                "mensaje", "Token válido",
                "usuario", Map.of(
                    "id", usuario.getId(),
                    "nombre", usuario.getNombre(),
                    "correo", usuario.getCorreo(),
                    "rol", usuario.getRol() != null ? usuario.getRol().getNombre() : null
                )
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensaje", "Token inválido o expirado"));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Debug the header
            System.out.println("Received logout request with Authorization header: " + authHeader);
            
            // Check if header is null or empty
            if (authHeader == null || authHeader.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensaje", "Token no proporcionado"));
            }
            
            // Extract token (handle both "Bearer token" and raw token formats)
            String token;
            if (authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            } else {
                token = authHeader; // Assume raw token was provided
            }
            
            // Validate token is not empty
            if (token.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("mensaje", "Token vacío"));
            }
            
            // Add to blacklist
            tokenBlacklist.blacklist(token);
            System.out.println("Token successfully blacklisted: " + token.substring(0, Math.min(10, token.length())) + "...");
            
            return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada correctamente"));
        } catch (Exception e) {
            System.out.println("Error during logout: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensaje", "Error al cerrar sesión: " + e.getMessage()));
        }
    }
    
    // Endpoint de ayuda para crear un usuario de prueba
    @PostMapping("/registro-prueba")
    public ResponseEntity<?> crearUsuarioPrueba() {
        // Buscar el rol administrador (asumiendo que existe con ID 1)
        Optional<Rol> rolOpt = rolRepository.findById(1);
        if (!rolOpt.isPresent()) {
            // Si no existe el rol, lo creamos
            Rol rolAdmin = new Rol();
            rolAdmin.setNombre("ADMIN");
            rolAdmin = rolRepository.save(rolAdmin);
            
            // Crear usuario de prueba
            Usuario usuario = new Usuario();
            usuario.setNombre("Administrador");
            usuario.setCorreo("admin@test.com");
            usuario.setContrasena("12345");
            usuario.setRol(rolAdmin);
            usuario.setEstatus(1);
            
            usuarioRepository.save(usuario);
            return ResponseEntity.ok("Usuario de prueba creado: admin@test.com / 12345");
        } else {
            // Si ya existe el rol, verificamos si ya existe el usuario
            Usuario usuarioExistente = usuarioRepository.findByCorreo("admin@test.com");
            if (usuarioExistente == null) {
                Usuario usuario = new Usuario();
                usuario.setNombre("Administrador");
                usuario.setCorreo("admin@test.com");
                usuario.setContrasena("12345");
                usuario.setRol(rolOpt.get());
                usuario.setEstatus(1);
                
                usuarioRepository.save(usuario);
                return ResponseEntity.ok("Usuario de prueba creado: admin@test.com / 12345");
            } else {
                return ResponseEntity.ok("Usuario de prueba ya existe: admin@test.com / 12345");
            }
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> checkAuthStatus() {
        return ResponseEntity.ok(Map.of(
            "status", "running", 
            "message", "Authentication service is working correctly"
        ));
    }
}
