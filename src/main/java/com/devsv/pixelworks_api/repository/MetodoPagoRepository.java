package com.devsv.pixelworks_api.repository;

import com.devsv.pixelworks_api.entities.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Integer> {
    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, Integer id);
}
