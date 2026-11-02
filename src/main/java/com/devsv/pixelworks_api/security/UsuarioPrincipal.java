package com.devsv.pixelworks_api.security;

import com.devsv.pixelworks_api.entities.Usuario;
import com.devsv.pixelworks_api.enums.EstadoUsuario;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class UsuarioPrincipal implements UserDetails {

    private final Integer id;
    private final String correo;
    private final String password;
    private final EstadoUsuario estado;
    private final String nombre;
    private final String rol;

    public UsuarioPrincipal(Usuario usuario) {
        this.id = usuario.getId();
        this.correo = usuario.getCorreo();
        this.password = usuario.getPassword();
        this.estado = usuario.getEstado();
        this.nombre = usuario.getNombre();
        // Accedemos directamente a la relación porque ya no hay tabla intermedia UsuarioRole
        this.rol = usuario.getRol().getNombre();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    @Override
    public String getUsername() { return correo; } // TRUCO: Le decimos a Spring que el username es el correo

    @Override
    public String getPassword() { return password; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return estado == EstadoUsuario.ACTIVO; }
}