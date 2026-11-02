package com.devsv.pixelworks_api.repository;

import com.devsv.pixelworks_api.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {


    // Lo usará CustomUserDetailsService para el Login
    Optional<Usuario> findByCorreo(String correo);

    // Lo usará AuthService para evitar registros duplicados
    boolean existsByCorreo(String correo);

}
