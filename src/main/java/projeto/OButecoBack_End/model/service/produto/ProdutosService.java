package projeto.OButecoBack_End.model.service.produto;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import projeto.OButecoBack_End.controller.produto.dto.ProdutoRequest;
import projeto.OButecoBack_End.model.Enum.EStatus;
import projeto.OButecoBack_End.model.entity.produto.CategoriasEntity;
import projeto.OButecoBack_End.model.entity.produto.GruposEntity;
import projeto.OButecoBack_End.model.entity.produto.ProdutosEntity;
import projeto.OButecoBack_End.model.repository.produto.CategoriasRepository;
import projeto.OButecoBack_End.model.repository.produto.GruposRepository;
import projeto.OButecoBack_End.model.repository.produto.ProdutosRepository;

import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
public class ProdutosService {

    private final ProdutosRepository produtosRepository;
    private final CategoriasRepository categoriasRepository;
    private final GruposRepository gruposRepository;


    public ProdutosEntity buscarProdutoPorId(Long id){
        return this.produtosRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Produto não encontrado")
            );
    }

    public ProdutosEntity salvarProduto(ProdutoRequest request){
        ProdutosEntity produto = new ProdutosEntity();
        produto.setNome(request.nome());
        produto.setStatus(EStatus.ATIVO);

        CategoriasEntity categoria = this.categoriasRepository.findById(request.categoriaId())
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria Não Encontrada")
                );
        produto.setCategoria(categoria);

        GruposEntity grupo = this.gruposRepository.findById(request.grupoId())
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
                );
        produto.setGrupo(grupo);
        produto.setPrecoVenda(request.precoVenda());
        produto.setObservacao(request.observacao());
        produto.setDataCriacao(new Timestamp(System.currentTimeMillis()));

        return this.produtosRepository.save(produto);

    }
}
