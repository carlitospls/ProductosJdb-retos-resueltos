package es.daw.productojdbc.service;

import es.daw.productojdbc.dto.FabricanteResumen;
import es.daw.productojdbc.exception.FabricanteConProductosException;
import es.daw.productojdbc.exception.FabricanteDuplicadoException;
import es.daw.productojdbc.exception.RecursoNoEncontradoException;
import es.daw.productojdbc.model.Fabricante;
import es.daw.productojdbc.repository.FabricanteRepository;
import es.daw.productojdbc.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FabricanteService {

    private final FabricanteRepository fabricanteRepository;
    private final ProductoRepository productoRepository;

//    public FabricanteService(FabricanteRepository fabricanteRepository,
//                             ProductoRepository productoRepository) {
//        this.fabricanteRepository = fabricanteRepository;
//        this.productoRepository = productoRepository;
//    }

    public List<Fabricante> listar() {
        return fabricanteRepository.findAll();
    }

    // >>> RETO 1 [CONTAR CON JOIN] · paso 4/6 · Método en el servicio: aquí solo reenvía al repositorio,
    //     pero el controlador siempre pasa por el servicio (controlador -> servicio -> repositorio).
    public List<FabricanteResumen> findAllWithNumProductos() {
        return fabricanteRepository.findAllWithNumProductos();
    }
    // ------------------

    public Fabricante buscarPorCodigo(Integer codigo) {
        return fabricanteRepository.findById(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe el fabricante con código " + codigo));
    }

    public Fabricante crear(Fabricante fabricante) {

        // eliminar espacios a izq y derecha
        fabricante.setNombre(fabricante.getNombre().strip());

        // >>> RETO 3 [REGLA DE NEGOCIO: NO DUPLICADOS] · paso 4/6 · La regla en el ALTA.
        //     - Va en el SERVICIO (ni en el controlador ni en el repositorio).
        //     - DESPUÉS de quitar espacios (strip/trim) y ANTES de save(): si falla, no se guarda nada.
        //     - El repositorio solo contesta true/false; quien decide y lanza la excepción es el servicio.
        if (fabricanteRepository.existsByNombreIgnoreCase(fabricante.getNombre())){
            // lanzar excepción propia
            throw new FabricanteDuplicadoException(fabricante.getNombre());
        }

        //----------------------------------------------
        fabricanteRepository.save(fabricante);
        return fabricante;
    }

    public Fabricante actualizar(Fabricante fabricante) {
        // REGLA DE NEGOCIO PARA BUSCAR EL CÓDIGO DE FABRICANTE. SI NO EXISTE LANZA EXCEPCIÓN...
        buscarPorCodigo(fabricante.getCodigo());
        // REGLA DE NEGOCIO PARA ELIMINAR ESPACIOS EN EL NOMBRE DEL FABRICANTE..
        fabricante.setNombre(fabricante.getNombre().strip());

        // >>> RETO 3 [REGLA DE NEGOCIO: NO DUPLICADOS] · paso 5/6 · La regla en la EDICIÓN.
        //     Se usa la versión "...AndCodigoNot": busca OTRO fabricante con ese nombre, sin contar al que se edita.
        //     Con la versión del alta, guardar sin cambiar el nombre daría un falso duplicado (chocaría consigo mismo).
        if (fabricanteRepository.existsByNombreIgnoreCaseAndCodigoNot(fabricante.getNombre(),fabricante.getCodigo())){
            // lanzar excepción propia
            throw new FabricanteDuplicadoException(fabricante.getNombre());
        }

        //----------------------------------------------

        fabricanteRepository.update(fabricante);
        return fabricante;
    }

    public void borrar(Integer codigo) {
        buscarPorCodigo(codigo);

        // Regla de negocio: comprobada aquí, antes de llegar a la BD
        if (productoRepository.existsByFabricanteCodigo(codigo)) {
            throw new FabricanteConProductosException(codigo);
        }
        fabricanteRepository.delete(codigo);
    }





}
