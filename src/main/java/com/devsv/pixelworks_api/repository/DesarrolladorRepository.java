package com.devsv.pixelworks_api.repository;

import com.devsv.pixelworks_api.entities.Desarrollador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DesarrolladorRepository extends JpaRepository<Desarrollador, Integer> {

    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, Integer id);
}
