package projeto.OButecoBack_End.controller.usuario;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projeto.OButecoBack_End.controller.usuario.dto.UsuariosRequest;
import projeto.OButecoBack_End.controller.usuario.dto.UsuariosResponse;
import projeto.OButecoBack_End.model.entity.usuario.UsuariosEntity;
import projeto.OButecoBack_End.model.service.usuario.UsuariosService;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuariosController {
    private final UsuariosService usuariosService;

    @PostMapping()
    public ResponseEntity<UsuariosResponse> salvarUsuario(@Valid @RequestBody UsuariosRequest usuariosRequest) {
        UsuariosEntity usuariosEntity = this.usuariosService.salvarUsuario(usuariosRequest);
        return new ResponseEntity<>(UsuariosResponse.de(usuariosEntity), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuariosResponse> atualizarUsuario(@PathVariable Long id,
                                                             @Valid @RequestBody UsuariosRequest usuariosRequest) {
            UsuariosEntity usuariosEntity = this.usuariosService.atualizarUsuario(id, usuariosRequest);
            return new ResponseEntity<>(UsuariosResponse.de(usuariosEntity), HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UsuariosResponse> atualizarUsuarioParcial(@PathVariable Long id,
                                                                    @Valid @RequestBody UsuariosRequest usuariosRequest) {
            UsuariosEntity usuariosEntity = this.usuariosService.atualizarUsuarioParcial(id, usuariosRequest);
            return new ResponseEntity<>(UsuariosResponse.de(usuariosEntity), HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<UsuariosResponse>> listarUsuarios () {
        List<UsuariosResponse> usuarios = this.usuariosService.listarUsuarios()
                .stream()
                .map(UsuariosResponse::de)
                .toList();
        return new ResponseEntity<>(usuarios, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuariosResponse> buscarPorId (@PathVariable Long id) {
        UsuariosEntity usuariosEntity = this.usuariosService.buscarUsuarioPorId(id);
        return new ResponseEntity<>(UsuariosResponse.de(usuariosEntity), HttpStatus.OK);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<UsuariosResponse>> buscarUsuarios (@RequestParam String nome) {
        List<UsuariosResponse> usuarios = this.usuariosService.buscarPorNome(nome)
                .stream()
                .map(UsuariosResponse::de)
                .toList();
        return new ResponseEntity<>(usuarios, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UsuariosResponse> deletarUsuario (@PathVariable Long id) {
        this.usuariosService.deletarUsuarioPorId(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }
}
