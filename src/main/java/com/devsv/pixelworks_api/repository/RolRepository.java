package com.devsv.pixelworks_api.repository;

import com.devsv.pixelworks_api.entities.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    Optional<Rol> findByNombre(String nombre);

    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, Integer id);
}
