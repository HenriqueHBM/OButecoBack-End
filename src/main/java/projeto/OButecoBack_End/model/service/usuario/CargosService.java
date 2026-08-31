package projeto.OButecoBack_End.model.service.usuario;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import projeto.OButecoBack_End.controller.usuario.Cargos.dto.CargosRequest;
import projeto.OButecoBack_End.model.entity.usuario.CargosEntity;
import projeto.OButecoBack_End.model.repository.usuario.CargosRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CargosService {

    private final CargosRepository cargosRepository;

    public CargosEntity buscarCargoPorId(Long id){
        return this.cargosRepository.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "Cargo nao encontrado com id")
                );
    }

    public CargosEntity salvarCargo(CargosRequest cargosRequest) {
        if (cargosRepository.findByCargo(cargosRequest.cargo()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cargo já existe");
        } //valida se cargo ja existe

        CargosEntity cargosEntity = new CargosEntity();

        cargosEntity.setCargo(cargosRequest.cargo());

        return this.cargosRepository.save(cargosEntity);
    }

    public CargosEntity atualizarCargo(Long id, CargosRequest cargosRequest) {
        CargosEntity cargosEntity = buscarCargoPorId(id);

        if (!cargosEntity.getCargo().equals(cargosRequest.cargo())) {
            if (cargosRepository.findByCargo(cargosRequest.cargo()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Cargo já existe");
            }
        } //valida se cargo ja existe caso esteja mudando

        cargosEntity.setCargo(cargosRequest.cargo());

        return this.cargosRepository.save(cargosEntity);
    }

    public void deletarCargo(Long id) {
        CargosEntity cargosEntity = buscarCargoPorId(id);
        this.cargosRepository.delete(cargosEntity);
    }

    public List<CargosEntity> listarCargos() {
        return this.cargosRepository.findAll();
    }


}
