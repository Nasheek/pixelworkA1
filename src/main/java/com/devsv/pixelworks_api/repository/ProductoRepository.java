package com.devsv.pixelworks_api.repository;

import com.devsv.pixelworks_api.entities.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, Integer id);

    List<Producto> findByActivoTrue(); //Linea agregada por error al cargar juego

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND NOT EXISTS (SELECT c FROM ClaveActivacion c WHERE c.producto = p AND c.estado = 'DISPONIBLE')")
    java.util.List<Producto> findProductosSinStock();
}
