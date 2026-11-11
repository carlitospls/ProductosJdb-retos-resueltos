package es.daw.productojdbc.repository;

import es.daw.productojdbc.model.Producto;

import java.util.List;

/**
 * Repositorio de productos: hereda el CRUD fijando T = Producto, ID = Integer
 * y añade las consultas propias de productos.
 */
public interface ProductoRepository extends GenericRepository<Producto, Integer> {

    /** ¿Hay algún producto de este fabricante? Lo usa FabricanteService antes de borrar. */
    boolean existsByFabricanteCodigo(Integer codigoFabricante);


    // >>> RETO 2 [CONSULTA + PÁGINA NUEVA] · paso 1/6 · DECLARAR la consulta (sin cuerpo: es una promesa).
    //     En cuanto la declaras, ProductoRepositoryJdbc se pone en rojo hasta que la implementes (paso 2).
    List<Producto> findByFabricanteCodigo(Integer codigoFabricante);
}
