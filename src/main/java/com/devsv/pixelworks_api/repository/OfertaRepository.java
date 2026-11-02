package com.devsv.pixelworks_api.repository;

import com.devsv.pixelworks_api.entities.Oferta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfertaRepository extends JpaRepository<Oferta, Integer> {
    boolean existsByNombre(String nombre);
}