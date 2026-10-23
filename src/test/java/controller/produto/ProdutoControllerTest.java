package controller.produto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import projeto.obuteco.controller.produto.ProdutosController;
import projeto.obuteco.controller.produto.dto.ProdutoRequest;
import projeto.obuteco.controller.produto.dto.ProdutoResponse;
import projeto.obuteco.model.entity.produto.ProdutosEntity;
import projeto.obuteco.model.enums.CategoriaEnum;
import projeto.obuteco.model.enums.EStatus;
import projeto.obuteco.model.enums.GrupoEnum;
import projeto.obuteco.model.service.produto.ProdutosService;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoControllerTest {
    @Mock
    private ProdutosService produtosService;

    @InjectMocks
    private ProdutosController produtosController;

    ProdutosEntity produto = new ProdutosEntity(
            1L,
            "Espetinho",
            EStatus.ATIVO,
            CategoriaEnum.NORMAL,
            GrupoEnum.COMIDA,
            BigDecimal.valueOf(9.99),
            "Teste Prod"
    );

    ProdutoRequest prodRequest = new ProdutoRequest(
            1L,
            "Espetinho",
            EStatus.ATIVO,
            CategoriaEnum.NORMAL,
            GrupoEnum.COMIDA,
            BigDecimal.valueOf(9.99),
            "Teste Prod",
            List.of()
    );

    // |--------------- [CREATE]

    @Test
    @DisplayName("Cadastrar produto retorna 201 com o produto criado")
    void salvarProduto(){
        when(produtosService.salvarProduto(prodRequest)).thenReturn(produto);

        var response = produtosController.salvarProduto(prodRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(ProdutoResponse.de(produto), response.getBody());
        verify(produtosService).salvarProduto(prodRequest);
    }

    // |--------------- [UPDATE]

    @Test
    @DisplayName("Atualizar produto retorna 200 com o produto atualizado")
    void atualizarProduto(){
        when(produtosService.atualizarProduto(1L, prodRequest)).thenReturn(produto);

        var response = produtosController.atualizarProduto(1L, prodRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ProdutoResponse.de(produto), response.getBody());
        verify(produtosService).atualizarProduto(1L, prodRequest);
    }

    @Test
    @DisplayName("Atualizar produto parcialmente retorna 200 com o produto atualizado")
    void atualizarProdutoParcial(){
        var request = new ProdutoRequest(null, "Espetinho de Carne", null, null,
                null, null, null, null);
        produto.setNome("Espetinho de Carne");
        when(produtosService.atualizarProdutoParcial(1L, request)).thenReturn(produto);

        var response = produtosController.atualizarProdutoParcial(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ProdutoResponse.de(produto), response.getBody());
        verify(produtosService).atualizarProdutoParcial(1L, request);
    }

    @Test
    @DisplayName("Alterar status do produto retorna 200 com o produto atualizado")
    void atualizarStatusProduto(){
        produto.setStatus(EStatus.INATIVO);
        when(produtosService.atualizarStatusProduto(1L)).thenReturn(produto);

        var response = produtosController.atualizarStatusProduto(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ProdutoResponse.de(produto), response.getBody());
        verify(produtosService).atualizarStatusProduto(1L);
    }

    // |--------------- [DELETE]

    @Test
    @DisplayName("Deletar produto retorna 204 sem corpo")
    void deletarProduto(){
        var response = produtosController.deletarProduto(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(produtosService).deletarProduto(1L);
    }

    // |--------------- [READ]

    @Test
    @DisplayName("Listar produtos retorna 200 com os produtos convertidos")
    void listarProdutos(){
        ProdutosEntity insumo = new ProdutosEntity(2L, "Queijo", EStatus.ATIVO,
                CategoriaEnum.INSUMO, GrupoEnum.COMIDA, BigDecimal.valueOf(2.50), "Teste Insumo");
        when(produtosService.listarProdutos()).thenReturn(List.of(produto, insumo));

        var response = produtosController.listarProdutos();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(List.of(ProdutoResponse.de(produto), ProdutoResponse.de(insumo)), response.getBody());
    }

    @Test
    @DisplayName("Listar produtos sem cadastro retorna 200 com lista vazia")
    void listarProdutosVazio(){
        when(produtosService.listarProdutos()).thenReturn(List.of());

        var response = produtosController.listarProdutos();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("Buscar produto por id retorna 200 com o produto")
    void buscarPorId(){
        when(produtosService.buscarProdutoPorId(1L)).thenReturn(produto);

        var response = produtosController.buscarPorId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ProdutoResponse.de(produto), response.getBody());
    }

    @Test
    @DisplayName("Buscar produto inexistente repassa o 404 do service")
    void buscarPorIdNaoEncontrado(){
        when(produtosService.buscarProdutoPorId(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

        var ex = assertThrows(ResponseStatusException.class,
                () -> produtosController.buscarPorId(99L));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
