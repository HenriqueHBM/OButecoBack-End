package projeto.OButecoBack_End.controller.usuario;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projeto.OButecoBack_End.controller.endereco.EnderecoResponse;
import projeto.OButecoBack_End.controller.usuario.dto.UsuariosRequest;
import projeto.OButecoBack_End.controller.usuario.dto.UsuariosResponse;
import projeto.OButecoBack_End.model.entity.usuario.UsuariosEntity;
import projeto.OButecoBack_End.model.service.endereco.EnderecoService;
import projeto.OButecoBack_End.model.service.usuario.UsuariosService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin("*") //pode-se passar um ip para apenas aquele servidor fazer as requisicoes ex. http://localhost:5432
public class UsuariosController {
    private static final Logger log = LoggerFactory.getLogger(UsuariosController.class);

    public UsuariosController(UsuariosService usuariosService, EnderecoService enderecoService) {
        this.usuariosService = usuariosService;
        this.enderecoService = enderecoService;
    }

    private final UsuariosService usuariosService;
    private final EnderecoService enderecoService;

    @PostMapping()
    public ResponseEntity<UsuariosResponse> salvarUsuario(@Valid @RequestBody UsuariosRequest usuariosRequest) {
        log.info("Solicitação de cadastro de usuário. Usuário: {}", usuariosRequest.usuario());

        UsuariosEntity usuariosEntity = this.usuariosService.salvarUsuario(usuariosRequest);

        log.info("Usuário cadastrado com sucesso. ID: {}, Usuário: {}",
                usuariosEntity.getId(),
                usuariosEntity.getUsuario());

        return new ResponseEntity<>(UsuariosResponse.de(usuariosEntity), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuariosResponse> atualizarUsuario(@PathVariable Long id,
                                                             @Valid @RequestBody UsuariosRequest usuariosRequest) {
        log.info("Solicitação de atualização de usuário. ID: {}", id);

        UsuariosEntity usuariosEntity = this.usuariosService.atualizarUsuario(id, usuariosRequest);

        log.info("Usuário atualizado com sucesso. ID: {}", id);

        return new ResponseEntity<>(UsuariosResponse.de(usuariosEntity), HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UsuariosResponse> atualizarUsuarioParcial(@PathVariable Long id,
                                                                    @Valid @RequestBody UsuariosRequest usuariosRequest) {
        log.info("Solicitação de atualização parcial de usuário. ID: {}", id);

        UsuariosEntity usuariosEntity = this.usuariosService.atualizarUsuarioParcial(id, usuariosRequest);

        log.info("Atualização parcial concluída. ID: {}", id);

        return new ResponseEntity<>(UsuariosResponse.de(usuariosEntity), HttpStatus.OK);
    }

    @PatchMapping("change_status/{id}")
    public ResponseEntity<UsuariosResponse> atualizarStatusUsuario(
            @PathVariable Long id
    ){
        log.info("Solicitação de alteração de status do usuário. ID: {}", id);

        UsuariosEntity usuariosEntity = this.usuariosService.atualizarStatusUsuario(id);

        log.info("Status do usuário alterado com sucesso. ID: {}", id);

        return new ResponseEntity<>(UsuariosResponse.de(usuariosEntity), HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<UsuariosResponse>> listarUsuarios () {
        log.info("Solicitação de consulta de usuários");

        List<UsuariosResponse> usuarios = this.usuariosService.listarUsuarios()
                .stream()
                .map(UsuariosResponse::de)
                .toList();

        log.info("Usuários consultados com sucesso. Total: {}", usuarios.size());

        return new ResponseEntity<>(usuarios, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuariosResponse> buscarPorId (@PathVariable Long id) {
        log.info("Solicitação de busca de usuário. ID: {}", id);

        UsuariosEntity usuariosEntity = this.usuariosService.buscarUsuarioPorId(id);

        log.info("Busca por usuários concluída. Nome: {}",
                usuariosEntity.getNome());

        return new ResponseEntity<>(UsuariosResponse.de(usuariosEntity), HttpStatus.OK);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<UsuariosResponse>> buscarUsuarios (@RequestParam String nome) {
        log.info("Solicitação de busca de usuários por nome. Nome: {}", nome);

        List<UsuariosResponse> usuarios = this.usuariosService.buscarPorNome(nome)
                .stream()
                .map(UsuariosResponse::de)
                .toList();

        log.info("Busca por nome concluída. Nome: {}, Total: {}", nome, usuarios.size());

        return new ResponseEntity<>(usuarios, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UsuariosResponse> deletarUsuario (@PathVariable Long id) {
        log.info("Solicitação de exclusão de usuário. ID: {}", id);

        this.usuariosService.deletarUsuarioPorId(id);

        log.info("Usuário excluído com sucesso. ID: {}", id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    @GetMapping("/buscarCep/{cep}")
    public ResponseEntity<EnderecoResponse> buscarCep(@PathVariable String cep){
        log.info("Solicitação de busca de endereço por CEP: {}", cep);

        return ResponseEntity.ok(this.enderecoService.buscarPorCep(cep));
    }
}
