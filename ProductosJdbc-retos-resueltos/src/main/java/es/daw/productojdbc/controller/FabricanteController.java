package es.daw.productojdbc.controller;

import es.daw.productojdbc.dto.FabricanteDto;
import es.daw.productojdbc.exception.FabricanteConProductosException;
import es.daw.productojdbc.exception.FabricanteDuplicadoException;
import es.daw.productojdbc.model.Fabricante;
import es.daw.productojdbc.service.FabricanteService;
import es.daw.productojdbc.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/fabricantes")
@RequiredArgsConstructor
public class FabricanteController {

    private final FabricanteService fabricanteService;
    // RETO 2: este controlador necesita ahora los productos -> se inyecta OTRO SERVICIO (nunca un repositorio).
    // Con @RequiredArgsConstructor basta con declarar el campo "private final".
    private final ProductoService productoService;

//    public FabricanteController(FabricanteService fabricanteService) {
//
//        this.fabricanteService = fabricanteService;
//    }

    @GetMapping
    public String listar(Model model) {

        //model.addAttribute("fabricantes", fabricanteService.listar());

        // >>> RETO 1 [CONTAR CON JOIN] · paso 5/6 · El controlador solo cambia de método del servicio.
        //     La etiqueta sigue siendo "fabricantes": la vista recibe ahora una lista de FabricanteResumen
        //     (codigo, nombre, numProductos) en vez de Fabricante, y por eso puede pintar ${f.numProductos}.
        model.addAttribute("fabricantes",fabricanteService.findAllWithNumProductos());

        return "fabricantes/lista";

    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("fabricanteDto", new FabricanteDto());
        return "fabricantes/formulario";
    }

    @GetMapping("/editar/{codigo}")
    public String editar(@PathVariable Integer codigo, Model model) {
        model.addAttribute("fabricanteDto", FabricanteDto.toDto(fabricanteService.buscarPorCodigo(codigo)));
        return "fabricantes/formulario";
    }

    // >>> RETO 2 [CONSULTA + PÁGINA NUEVA] · paso 4/6 · Ruta nueva con un dato dentro de la URL.
    //     - La clase ya tiene @RequestMapping("/fabricantes"): aquí solo va lo que cuelga -> /fabricantes/{codigo}/productos
    //     - {codigo} de la URL llega al parámetro con @PathVariable (mismo nombre).
    //     - Dos cosas al Model: el fabricante (para el título) y sus productos (para la tabla).
    //     - El return es la RUTA DEL ARCHIVO dentro de templates/, sin .html (paso 5).
    @GetMapping("/{codigo}/productos")
    public String productosDelFabricante(@PathVariable("codigo") Integer codigo, Model model) {

        model.addAttribute("fabricante",fabricanteService.buscarPorCodigo(codigo));
        model.addAttribute("productos",productoService.listarPorFabricante(codigo));

        return "fabricantes/productosDelFabricante";

    }



    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute FabricanteDto fabricanteDto,
                          BindingResult result,
                          RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "fabricantes/formulario";
        }

        // >>> RETO 3 [REGLA DE NEGOCIO: NO DUPLICADOS] · paso 6/6 · El controlador TRADUCE la excepción a un error visible.
        //     - Un solo try para alta y edición: las dos pueden lanzar la misma excepción.
        //     - rejectValue("nombre", "", e.getMessage()): 1º el CAMPO donde sale el error, 2º un código (da igual),
        //       3º el MENSAJE. Con 3 argumentos no hace falta messages.properties.
        //     - return de la VISTA (no redirect): así no se pierde lo que escribió el usuario.
        //       Sin ese return dentro del catch, el código seguiría hasta el redirect y el error no se vería nunca.
        //     - El formulario no se toca: th:errors="*{nombre}" ya pinta el mensaje.
        //     - Si el formulario tuviera un select, aquí habría que recargar su lista en el Model (como en ProductoController).
        try {
            if (fabricanteDto.getCodigo() == null) {
                Fabricante fabricante = fabricanteService.crear(FabricanteDto.toModelEntity(fabricanteDto));
                redirectAttributes.addFlashAttribute("mensaje",
                        "Fabricante creado con código " + fabricante.getCodigo());
            } else {
                Fabricante fabricante = fabricanteService.actualizar(FabricanteDto.toModelEntity(fabricanteDto));
                redirectAttributes.addFlashAttribute("mensaje",
                        "Fabricante " + fabricante.getCodigo() + " actualizado");
            }
        }
        catch (FabricanteDuplicadoException e) {
            result.rejectValue("nombre", "", e.getMessage());
            return "fabricantes/formulario";
        }
        return "redirect:/fabricantes";
    }

    @PostMapping("/borrar/{codigo}")
    public String borrar(@PathVariable("codigo") Integer codigo,
                         RedirectAttributes redirectAttributes) {
        try {
            fabricanteService.borrar(codigo);
            redirectAttributes.addFlashAttribute("mensaje", "Fabricante " + codigo + " borrado");
        } catch (FabricanteConProductosException e) {
            // Regla de negocio incumplida: avisamos en el listado, no es una página de error
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/fabricantes";
    }


}
