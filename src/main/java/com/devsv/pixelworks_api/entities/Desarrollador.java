package com.devsv.pixelworks_api.entities;

import com.devsv.pixelworks_api.enums.TipoDesarrollador;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "desarrolladores", schema = "public")
public class Desarrollador implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "descripcion", length = 100)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 1)
    private TipoDesarrollador tipo;

    @Column(name = "pais", length = 50)
    private String pais;
}