package projeto.OButecoBack_End.model.service.usuario;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import projeto.OButecoBack_End.controller.usuario.dto.UsuariosRequest;
import projeto.OButecoBack_End.model.Enum.EStatus;
import projeto.OButecoBack_End.model.entity.usuario.UsuariosEntity;
import projeto.OButecoBack_End.model.repository.usuario.UsuariosRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projeto.OButecoBack_End.model.service.estoque.EstoquesService;

import java.sql.Timestamp;
import java.util.List;

@Service
public class UsuariosService {
    private static final Logger log = LoggerFactory.getLogger(UsuariosService.class);

    public UsuariosService(UsuariosRepository usuariosRepository) {
        this.usuariosRepository = usuariosRepository;
    }

    private final UsuariosRepository usuariosRepository;

    public UsuariosEntity buscarUsuarioPorId(Long id){
        log.info("Buscando usuário pelo ID: {}", id);
        return this.usuariosRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(
                        () -> {
                            log.warn("Usuário não encontrado. ID: {}", id);

                            return new ResponseStatusException(HttpStatus.NOT_FOUND,
                                    "Usuario nao encontrado com id");
                        } );
    }

    public UsuariosEntity salvarUsuario(UsuariosRequest usuariosRequest) {
        log.info("Criando usuário. Usuário: {}", usuariosRequest.usuario());

        if (usuariosRepository.findByUsuarioAndDeletedAtIsNull(usuariosRequest.usuario()).isPresent()) {
            log.warn("Tentativa de cadastro de usuário já existente. Usuário: {}",
                    usuariosRequest.usuario());

            throw new ResponseStatusException(HttpStatus.CONFLICT, "Usuário já existe");
        } //valida se atributo usuario ja existe antes de salvar

        UsuariosEntity usuariosEntity = new UsuariosEntity();

        usuariosEntity.setNome(usuariosRequest.nome());
        usuariosEntity.setUsuario(usuariosRequest.usuario());
        usuariosEntity.setSenha(usuariosRequest.senha());
        usuariosEntity.setCargo(usuariosRequest.cargoEnum());
        usuariosEntity.setStatus(EStatus.ATIVO);

        UsuariosEntity usuarioSalvo = this.usuariosRepository.save(usuariosEntity);

        log.info("Usuário criado com sucesso. ID: {}, Usuário: {}",
                usuarioSalvo.getId(),
                usuarioSalvo.getUsuario());

        return usuarioSalvo;
    }

    public UsuariosEntity atualizarUsuario(Long id, UsuariosRequest usuariosRequest) {
        log.info("Atualizando usuário. ID: {}", id);

        UsuariosEntity usuariosEntity = this.buscarUsuarioPorId(id);

        if (!usuariosEntity.getUsuario().equals(usuariosRequest.usuario())) {
            if (usuariosRepository.findByUsuarioAndDeletedAtIsNull(usuariosRequest.usuario()).isPresent()) {
                log.warn("Tentativa de alterar usuário para nome de usuário já existente. ID: {}, Usuário: {}", id,
                        usuariosRequest.usuario());
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Usuario ja existe");
            }
        } //valida se atributo usuario ja existe caso esteja sendo alterado

        usuariosEntity.setNome(usuariosRequest.nome());
        usuariosEntity.setUsuario(usuariosRequest.usuario());
        usuariosEntity.setSenha(usuariosRequest.senha());
        usuariosEntity.setCargo(usuariosRequest.cargoEnum());

        UsuariosEntity usuarioAtualizado = this.usuariosRepository.save(usuariosEntity);

        log.info("Usuário atualizado com sucesso. ID: {}, Usuário: {}",
                usuarioAtualizado.getId(),
                usuarioAtualizado.getUsuario());

        return usuarioAtualizado;
    }

    public UsuariosEntity atualizarStatusUsuario(Long id){
        UsuariosEntity usuario = this.buscarUsuarioPorId(id);

        log.info("Alterando status do usuário. ID: {}, Status atual: {}",
                id,
                usuario.getStatus());

        usuario.setStatus(usuario.getStatus().equals(EStatus.ATIVO) ? EStatus.INATIVO: EStatus.ATIVO);

        UsuariosEntity usuarioAtualizado = this.usuariosRepository.save(usuario);

        log.info("Status do usuário alterado. ID: {}, Novo status: {}",
                id,
                usuarioAtualizado.getStatus());

        return usuarioAtualizado;
    }
    public UsuariosEntity atualizarUsuarioParcial(Long id, UsuariosRequest usuariosRequest) {
        log.info("Iniciando atualização parcial do usuário. ID: {}", id);

        UsuariosEntity usuariosEntity = buscarUsuarioPorId(id);

        //verifica se nao esta vazio o usuario no request e se existe/presente tal usuario
        if (usuariosRequest.usuario() != null && !usuariosEntity.getUsuario().equals(usuariosRequest.usuario())) {
            //verifica se nao existe um outro usuario com o mesmo nome do passsado para atualizar
            if (usuariosRepository.findByUsuarioAndDeletedAtIsNull(usuariosRequest.usuario()).isPresent()) {
                log.warn("Tentativa de alterar usuário para nome de usuário já existente. ID: {}, Usuário: {}", id,
                        usuariosRequest.usuario());
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Usuário já existe");
            }
            usuariosEntity.setUsuario(usuariosRequest.usuario());
        } //valida se atributo usuario ja existe caso esteja sendo alterado

        if (usuariosRequest.nome() != null) usuariosEntity.setNome(usuariosRequest.nome());
        if (usuariosRequest.senha() != null) usuariosEntity.setSenha(usuariosRequest.senha());

        if (usuariosRequest.cargoEnum() != null) {
            //procura o cargo antes de atualizar se nao lanca exeption
            usuariosEntity.setCargo(usuariosRequest.cargoEnum());
        }

        UsuariosEntity usuarioAtualizado = this.usuariosRepository.save(usuariosEntity);

        log.info("Atualização parcial concluída. ID: {}, Usuário: {}",
                usuarioAtualizado.getId(),
                usuarioAtualizado.getUsuario());

        return usuarioAtualizado;
    }

    public void deletarUsuarioPorId(Long id){
        UsuariosEntity usuariosEntity = this.buscarUsuarioPorId(id);

        log.info("Excluindo usuário. ID: {}, Usuário: {}",
                id,
                usuariosEntity.getUsuario());

        //nao é deletado de fato o usuario, apenas preenche um campo de deleted_at e nao
        // ignora em outras consultas caso esteja preenchido esse campo
        usuariosEntity.setDeletedAt(new Timestamp(System.currentTimeMillis()));
        usuariosEntity.setStatus(EStatus.INATIVO);

        this.usuariosRepository.save(usuariosEntity);

        log.info("Usuário excluído com sucesso. ID: {}", id);
    }

    public List<UsuariosEntity> listarUsuarios(){
        return this.usuariosRepository.findAllByDeletedAtIsNull();
    }


    public List<UsuariosEntity> buscarPorNome(String nome){
        return this.usuariosRepository.findByNome(nome);
    }
}
