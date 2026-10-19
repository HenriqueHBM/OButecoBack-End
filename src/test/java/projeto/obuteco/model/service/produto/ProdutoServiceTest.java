package projeto.obuteco.model.service.produto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import projeto.obuteco.model.Enum.CategoriaEnum;
import projeto.obuteco.model.Enum.EStatus;
import projeto.obuteco.model.Enum.GrupoEnum;
import projeto.obuteco.model.entity.produto.ProdutosEntity;
import projeto.obuteco.model.repository.produto.ProdutosRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// @SpringBootTest
@ExtendWith(MockitoExtension.class)

public class ProdutoServiceTest {
    @Mock
    private ProdutosRepository produtosRepository;

    @InjectMocks
    private ProdutosService produtosService;

    @Captor
    private ArgumentCaptor<ProdutosEntity> produtoCaptor;

    ProdutosEntity produto = new ProdutosEntity(
            1L,
            "Espetinho",
            EStatus.ATIVO,
            CategoriaEnum.NORMAL,
            GrupoEnum.COMIDA,
            BigDecimal.valueOf(9.99),
            "Teste Prod"
    );

    @Test
    void buscarProdutoPorId(){
        when(produtosRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(produto));

        //produtosService.buscarProdutoPorId(produto.getId());
        assertEquals(produto, produtosService.buscarProdutoPorId(1L));
        verify(produtosRepository).findByIdAndDeletedAtIsNull(1L);
    }

    @Test
    void buscarPorIdNaoEncontrado(){
        when(produtosRepository.findByIdAndDeletedAtIsNull(0L))
                .thenReturn(Optional.empty());

        var resp = assertThrows(ResponseStatusException.class,
                () -> produtosService.buscarProdutoPorId(0L));

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
    }

    @Test
    void listarProdutos(){
        when(produtosRepository.findAllByDeletedAtIsNull())
                .thenReturn(List.of(produto));

        assertEquals(List.of(produto), produtosService.listarProdutos());
    }

    @Test
    void listarVazioProdutos(){
        when(produtosRepository.findAllByDeletedAtIsNull())
                .thenReturn(List.of());

        assertEquals(List.of(), produtosService.listarProdutos());
    }

    @Test
    void deletarProduto(){
        when(produtosRepository.findByIdAndDeletedAtIsNull(1L))
                .thenReturn(Optional.of(produto));

        long time_antes = System.currentTimeMillis();
        produtosService.deletarProduto(1L);
        long time_depois = System.currentTimeMillis();

        verify(produtosRepository).save(produtoCaptor.capture());
        var salvo = produtoCaptor.getValue();


        assertEquals(EStatus.INATIVO, salvo.getStatus());
        assertNotNull(salvo.getDeletedAt());

        assertTrue(salvo.getDeletedAt().getTime() >= time_antes
                && salvo.getDeletedAt().getTime() <= time_depois);
        //assertTrue();

        //assertTrue(verify(produtosRepository).save(produto).getStatus().equals("INATIVO"));
        //assertTrue(verify(produtosRepository).save(produto).getDeletedAt().equals(new Timestamp(System.currentTimeMillis())));
        //assertEquals(produto_excluido.getStatus(), verify(produtosRepository).save(produto).getStatus());
        // assertEquals(produto_excluido.getDeletedAt(), verify(produtosRepository).save(produto).getDeletedAt());
    }

}
