package projeto.OButecoBack_End.controller.usuario.Cargos;

import jakarta.validation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projeto.OButecoBack_End.controller.usuario.Cargos.dto.CargosRequest;
import projeto.OButecoBack_End.controller.usuario.Cargos.dto.CargosResponse;
import projeto.OButecoBack_End.model.entity.usuario.CargosEntity;
import projeto.OButecoBack_End.model.service.usuario.CargosService;

import java.util.List;

@RestController
@RequestMapping("/usuarios/cargos")
@RequiredArgsConstructor
public class CargosController {
    private final CargosService cargosService;


    @PostMapping()
    public ResponseEntity<CargosResponse> salvarCargo (@Valid @RequestBody CargosRequest cargosRequest) {
        CargosEntity cargosEntity = cargosService.salvarCargo(cargosRequest);
        return new ResponseEntity<>(CargosResponse.de(cargosEntity), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CargosResponse> atualizarCargo (@PathVariable Long id,
                                                          @Valid @RequestBody CargosRequest cargosRequest){
        CargosEntity cargosEntity = cargosService.atualizarCargo(id, cargosRequest);
        return new ResponseEntity<>(CargosResponse.de(cargosEntity), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<CargosResponse>> listarCargos (){
        List<CargosResponse> cargos = this.cargosService.listarCargos()
                .stream()
                .map(CargosResponse::de)
                .toList();
        return new ResponseEntity<>(cargos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CargosResponse> buscarCargoPorId (@PathVariable Long id){
        CargosEntity cargosEntity = this.cargosService.buscarCargoPorId(id);
        return new ResponseEntity<>(CargosResponse.de(cargosEntity), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CargosResponse> deletarCargo (@PathVariable Long id){
            this.cargosService.deletarCargo(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
