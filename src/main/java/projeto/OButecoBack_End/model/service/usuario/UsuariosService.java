package projeto.OButecoBack_End.model.service.usuario;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import projeto.OButecoBack_End.controller.usuario.dto.UsuariosRequest;
import projeto.OButecoBack_End.model.Enum.EStatus;
import projeto.OButecoBack_End.model.entity.usuario.UsuariosEntity;
import projeto.OButecoBack_End.model.repository.usuario.UsuariosRepository;

import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuariosService {
    private final UsuariosRepository usuariosRepository;

    public UsuariosEntity buscarUsuarioPorId(Long id){
        return this.usuariosRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Usuario nao encontrado com id")
                );
    }

    public UsuariosEntity salvarUsuario(UsuariosRequest usuariosRequest) {
        if (usuariosRepository.findByUsuarioAndDeletedAtIsNull(usuariosRequest.usuario()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Usuário já existe");
        } //valida se atributo usuario ja existe antes de salvar

        UsuariosEntity usuariosEntity = new UsuariosEntity();

        usuariosEntity.setNome(usuariosRequest.nome());
        usuariosEntity.setUsuario(usuariosRequest.usuario());
        usuariosEntity.setSenha(usuariosRequest.senha());
        usuariosEntity.setCargo(usuariosRequest.cargoEnum());
        usuariosEntity.setStatus(EStatus.ATIVO);
        return this.usuariosRepository.save(usuariosEntity);
    }

    public UsuariosEntity atualizarUsuario(Long id, UsuariosRequest usuariosRequest) {
        UsuariosEntity usuariosEntity = this.buscarUsuarioPorId(id);

        if (!usuariosEntity.getUsuario().equals(usuariosRequest.usuario())) {
            if (usuariosRepository.findByUsuarioAndDeletedAtIsNull(usuariosRequest.usuario()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Usuario ja existe");
            }
        } //valida se atributo usuario ja existe caso esteja sendo alterado

        usuariosEntity.setNome(usuariosRequest.nome());
        usuariosEntity.setUsuario(usuariosRequest.usuario());
        usuariosEntity.setSenha(usuariosRequest.senha());
        usuariosEntity.setCargo(usuariosRequest.cargoEnum());

        return this.usuariosRepository.save(usuariosEntity);
    }

    public UsuariosEntity atualizarStatusUsuario(Long id){
        UsuariosEntity usuario = this.buscarUsuarioPorId(id);
        usuario.setStatus(usuario.getStatus().equals(EStatus.ATIVO) ? EStatus.INATIVO: EStatus.ATIVO);
        return this.usuariosRepository.save(usuario);
    }
    public UsuariosEntity atualizarUsuarioParcial(Long id, UsuariosRequest usuariosRequest) {
        UsuariosEntity usuariosEntity = buscarUsuarioPorId(id);

        //verifica se nao esta vazio o usuario no request e se existe/presente tal usuario
        if (usuariosRequest.usuario() != null && !usuariosEntity.getUsuario().equals(usuariosRequest.usuario())) {
            //verifica se nao existe um outro usuario com o mesmo nome do passsado para atualizar
            if (usuariosRepository.findByUsuarioAndDeletedAtIsNull(usuariosRequest.usuario()).isPresent()) {
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

        return this.usuariosRepository.save(usuariosEntity);
    }

    public void deletarUsuarioPorId(Long id){
        UsuariosEntity usuariosEntity = this.buscarUsuarioPorId(id);

        //nao é deletado de fato o usuario, apenas preenche um campo de deleted_at e nao
        // ignora em outras consultas caso esteja preenchido esse campo
        usuariosEntity.setDeletedAt(new Timestamp(System.currentTimeMillis()));
        usuariosEntity.setStatus(EStatus.INATIVO);
        this.usuariosRepository.save(usuariosEntity);  //lembrar de criar um status de ativo e inativo para usuario
    }

    public List<UsuariosEntity> listarUsuarios(){
        return this.usuariosRepository.findAllByDeletedAtIsNull();
    }


    public List<UsuariosEntity> buscarPorNome(String nome){
        return this.usuariosRepository.findByNome(nome);
    }
}
