package projeto.OButecoBack_End.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import projeto.OButecoBack_End.Controller.Usuarios.dto.UsuariosRequest;
import projeto.OButecoBack_End.Entity.CargosEntity;
import projeto.OButecoBack_End.Entity.UsuariosEntity;
import projeto.OButecoBack_End.Repository.CargosRepository;
import projeto.OButecoBack_End.Repository.UsuariosRepository;

import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuariosService {
    private final UsuariosRepository usuariosRepository;
    private final CargosRepository cargosRepository;

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

        CargosEntity cargo = cargosRepository.findById(usuariosRequest.cargoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cargo nao encontrado"));
        usuariosEntity.setCargosEntity(cargo);

        return this.usuariosRepository.save(usuariosEntity);
    }

    public UsuariosEntity atualizarUsuario(Long id, UsuariosRequest usuariosRequest) {
        UsuariosEntity usuariosEntity = buscarUsuarioPorId(id);

        if (!usuariosEntity.getUsuario().equals(usuariosRequest.usuario())) {
            if (usuariosRepository.findByUsuarioAndDeletedAtIsNull(usuariosRequest.usuario()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Usuario ja existe");
            }
        } //valida se atributo usuario ja existe caso esteja sendo alterado

        usuariosEntity.setNome(usuariosRequest.nome());
        usuariosEntity.setUsuario(usuariosRequest.usuario());
        usuariosEntity.setSenha(usuariosRequest.senha());

        if (usuariosRequest.cargoId() != null) {
            CargosEntity cargo = cargosRepository.findById(usuariosRequest.cargoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cargo nao encontrado"));
            usuariosEntity.setCargosEntity(cargo);
        }

        return this.usuariosRepository.save(usuariosEntity);
    }

    public UsuariosEntity atualizarUsuarioParcial(Long id, UsuariosRequest usuariosRequest) {
        UsuariosEntity usuariosEntity = buscarUsuarioPorId(id);

        if (usuariosRequest.usuario() != null && !usuariosEntity.getUsuario().equals(usuariosRequest.usuario())) {
            if (usuariosRepository.findByUsuarioAndDeletedAtIsNull(usuariosRequest.usuario()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Usuário já existe");
            }
            usuariosEntity.setUsuario(usuariosRequest.usuario());
        } //valida se atributo usuario ja existe caso esteja sendo alterado

        if (usuariosRequest.nome() != null) usuariosEntity.setNome(usuariosRequest.nome());
        if (usuariosRequest.senha() != null) usuariosEntity.setSenha(usuariosRequest.senha());

        if (usuariosRequest.cargoId() != null) {
            CargosEntity cargo = cargosRepository.findById(usuariosRequest.cargoId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cargo não encontrado"));
            usuariosEntity.setCargosEntity(cargo);
        }

        return this.usuariosRepository.save(usuariosEntity);
    }

    public void deletarUsuarioPorId(Long id){
        UsuariosEntity usuariosEntity = this.buscarUsuarioPorId(id);

        usuariosEntity.setDeleted_at(new Timestamp(System.currentTimeMillis()));
        this.usuariosRepository.save(usuariosEntity);  //lembrar de criar um status de ativo e inativo para usuario
    }

    public List<UsuariosEntity> listarUsuarios(){
        return this.usuariosRepository.findAllByDeletedAtIsNull();
    }


    public List<UsuariosEntity> buscarPorNome(String nome){
        return this.usuariosRepository.findByNome(nome);
    }
}
