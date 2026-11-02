package com.devsv.pixelworks_api.repository;

import com.devsv.pixelworks_api.entities.DetalleCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleCompraRepository extends JpaRepository<DetalleCompra, Integer> {

    List<DetalleCompra> findByCompraId(Integer compraId);
}