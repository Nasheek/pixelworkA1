package com.devsv.pixelworks_api.repository;

import com.devsv.pixelworks_api.entities.OfertaProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfertaProductoRepository extends JpaRepository<OfertaProducto, Integer> {

    boolean existsByOfertaIdAndProductoId(
            Integer ofertaId,
            Integer productoId
    );

    List<OfertaProducto> findByOfertaId(
            Integer ofertaId
    );

    @Query("""
        SELECT op
        FROM OfertaProducto op
        JOIN FETCH op.oferta o
        WHERE op.producto.id IN :productoIds
          AND o.fechaInicio <= CURRENT_DATE
          AND o.fechaFin >= CURRENT_DATE
        ORDER BY o.fechaInicio DESC, o.id DESC
        """)
    List<OfertaProducto> obtenerOfertasActivasPorProductos(
            @Param("productoIds") List<Integer> productoIds
    );
}