package es.daw.productojdbc.repository;

import es.daw.productojdbc.dto.FabricanteResumen;
import es.daw.productojdbc.model.Fabricante;

import java.util.List;

public interface FabricanteRepository extends GenericRepository<Fabricante, Integer> {

    // >>> RETO 3 [REGLA DE NEGOCIO: NO DUPLICADOS] · paso 2/6 · Declarar las DOS comprobaciones (sin cuerpo):
    //     una para el alta y otra para la edición (que excluye al propio registro).
    //     "public abstract" es opcional en una interfaz: las dos líneas de abajo son equivalentes en forma.
    public abstract boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndCodigoNot(String nombre, Integer codigo);

    // -----------------------------------------

    // >>> RETO 1 [CONTAR CON JOIN] · paso 2/6 · Declarar la consulta. Devuelve FabricanteResumen (el DTO del paso 1),
    //     no Fabricante, porque Fabricante no tiene dónde guardar el número de productos.
    List<FabricanteResumen> findAllWithNumProductos();


}
