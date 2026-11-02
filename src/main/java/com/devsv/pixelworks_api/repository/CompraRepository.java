package com.devsv.pixelworks_api.repository;

import com.devsv.pixelworks_api.entities.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Integer> {

    List<Compra> findByUsuarioId(Integer usuarioId);

    @Query("SELECT p.nombre, SUM(d.cantidad), SUM(d.subTotal) " +
            "FROM DetalleCompra d JOIN d.producto p JOIN d.compra c " +
            "WHERE c.fechaVenta BETWEEN :inicio AND :fin " +
            "AND (:hasProductos = false OR p.id IN :productoIds) " +
            "GROUP BY p.id, p.nombre")
    List<Object[]> obtenerDesgloseVentas(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("productoIds") List<Integer> productoIds,
            @Param("hasProductos") boolean hasProductos
    );
}