package projeto.OButecoBack_End.model.service.produto;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import projeto.OButecoBack_End.model.entity.produto.CategoriasEntity;
import projeto.OButecoBack_End.model.entity.produto.GruposEntity;
import projeto.OButecoBack_End.model.repository.produto.CategoriasRepository;
import projeto.OButecoBack_End.model.repository.usuario.CargosRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriasService {
    private final CategoriasRepository categoriasRepository;

    public CategoriasEntity buscarCategoriaPorId(Long id){
        return this.categoriasRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Categoria nao encontrada com id")
        );
    }

    public CategoriasEntity salvarCategoria(){

    }

    public CategoriasEntity atualizarCategoira(Long id){

    }

    public void deletarCateogira(Long id){

    }

    public List<CategoriasEntity> listarCategorias(){
        return this.categoriasRepository.findAll();
    }

}
