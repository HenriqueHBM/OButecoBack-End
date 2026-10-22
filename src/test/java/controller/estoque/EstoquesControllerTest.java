package controller.estoque;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import projeto.obuteco.OButecoBackEndApplication;
import projeto.obuteco.controller.estoque.EstoquesController;
import projeto.obuteco.controller.estoque.dto.EstoquesRequest;
import projeto.obuteco.controller.estoque.dto.MovimentacoesEstoqueRequest;
import projeto.obuteco.model.entity.conversao.ConversoesEntity;
import projeto.obuteco.model.entity.estoque.EstoquesEntity;
import projeto.obuteco.model.entity.estoque.MovimentacoesEstoqueEntity;
import projeto.obuteco.model.entity.produto.ProdutosEntity;
import projeto.obuteco.model.entity.usuario.UsuariosEntity;
import projeto.obuteco.model.enums.CategoriaEnum;
import projeto.obuteco.model.enums.EStatus;
import projeto.obuteco.model.enums.GrupoEnum;
import projeto.obuteco.model.service.estoque.EstoquesService;
import projeto.obuteco.model.service.estoque.MovimentacoesEstoqueService;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EstoquesController.class)
@ContextConfiguration(classes = OButecoBackEndApplication.class)
class EstoquesControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    EstoquesService estoquesService;

    @MockitoBean
    MovimentacoesEstoqueService movimentacoesEstoqueService;

    ProdutosEntity produto = new ProdutosEntity(
            1L, "Espetinho", EStatus.ATIVO, CategoriaEnum.NORMAL,
            GrupoEnum.COMIDA, BigDecimal.valueOf(9.99), "Teste Prod");

    ConversoesEntity conversao = new ConversoesEntity(
            1L, "Quilograma", "KG", BigDecimal.valueOf(1));

    EstoquesEntity estoque = new EstoquesEntity(
            1L, produto, BigDecimal.valueOf(10), conversao, "Prateleira");

    UsuariosEntity usuario = new UsuariosEntity(1L, "Jonas", "jonas123", "jonas123", EStatus.ATIVO);

    private MovimentacoesEstoqueRequest criarRequest() {
        return new MovimentacoesEstoqueRequest(
                1L,
                "ENTRADA",
                5.0,
                10.0,
                1L,
                1L,
                1.0,
                "Teste",
                1L,
                "Prateleira"
        );
    }

    private MovimentacoesEstoqueEntity criarMovimentacao() {
        var movimentacao = new MovimentacoesEstoqueEntity();
        movimentacao.setId(1L);
        movimentacao.setEstoqueEntity(estoque);
        movimentacao.setProdutosEntity(produto);
        movimentacao.setConversoesEntity(conversao);
        movimentacao.setUsuarioEntity(usuario);
        movimentacao.setTipo("ENTRADA");
        movimentacao.setQuantidade(BigDecimal.valueOf(5));
        movimentacao.setQtdeConversao(BigDecimal.valueOf(5));
        movimentacao.setValorUnitario(BigDecimal.valueOf(10));
        movimentacao.setValorTotal(BigDecimal.valueOf(50));
        movimentacao.setObservacao("Teste");
        return movimentacao;
    }

    private String json(Object objeto) {
        return objectMapper.writeValueAsString(objeto);
    }

    // ---------- estoques ----------
    @Test
    @DisplayName("GET /estoques deve listar os estoques")
    void listarEstoques() throws Exception {
        //Arrange
        when(estoquesService.listarEstoques()).thenReturn(List.of(estoque));

        //Act
        var resposta = mockMvc.perform(get("/estoques"));

        //Assert
        resposta.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        verify(estoquesService).listarEstoques();
    }

    @Test
    @DisplayName("GET /estoques deve retornar lista vazia quando nao ha estoques")
    void listarEstoquesVazio() throws Exception {
        //Arrange
        when(estoquesService.listarEstoques()).thenReturn(List.of());

        //Act
        var resposta = mockMvc.perform(get("/estoques"));

        //Assert
        resposta.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /estoques/{id} deve retornar o estoque mapeado no corpo")
    void buscarPorId() throws Exception {
        //Arrange
        when(estoquesService.buscarEstoquePorId(1L)).thenReturn(estoque);

        //Act
        var resposta = mockMvc.perform(get("/estoques/1"));

        //Assert
        resposta.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1));
        verify(estoquesService).buscarEstoquePorId(1L);
    }

    @Test
    @DisplayName("GET /estoques/{id} deve retornar 404 quando nao encontrado")
    void buscarPorIdNaoEncontrado() throws Exception {
        //Arrange
        when(estoquesService.buscarEstoquePorId(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Estoque nao encontrado com id."));

        //Act
        var resposta = mockMvc.perform(get("/estoques/99"));

        //Assert
        resposta.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /estoques/{id} deve retornar excecao quando o id nao e numerico")
    void buscarPorIdInvalido() throws Exception {
        //Act
        var resposta = mockMvc.perform(get("/estoques/abc"));

        //Assert
        resposta.andExpect(status().isInternalServerError());
        verify(estoquesService, never()).buscarEstoquePorId(any());
    }

    @Test
    @DisplayName("POST /estoques deve criar o estoque e retornar 201")
    void salvarEstoque() throws Exception {
        //Arrange
        var request = new EstoquesRequest(1L, 10.0, 1L, "Prateleira");
        when(estoquesService.salvarEstoque(any(EstoquesRequest.class))).thenReturn(estoque);

        //Act
        var resposta = mockMvc.perform(post("/estoques")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(request)));

        //Assert
        resposta.andExpect(status().isCreated());
        verify(estoquesService).salvarEstoque(any(EstoquesRequest.class));
    }

    @Test
    @DisplayName("POST /estoques deve retornar excecao quando o JSON esta mal formado")
    void salvarEstoqueJsonMalformado() throws Exception {
        //Arrange
        var corpoInvalido = "{ nao e json";

        //Act
        var resposta = mockMvc.perform(post("/estoques")
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpoInvalido));

        //Assert
        resposta.andExpect(status().isInternalServerError());
        verify(estoquesService, never()).salvarEstoque(any());
    }

    // ---------- movimentacoes ----------
    @Test
    @DisplayName("POST /estoques/movimentacoes/entrada deve registrar e retornar 201")
    void entrada() throws Exception {
        //Arrange
        when(movimentacoesEstoqueService.entrada(any(MovimentacoesEstoqueRequest.class)))
                .thenReturn(criarMovimentacao());

        //Act
        var resposta = mockMvc.perform(post("/estoques/movimentacoes/entrada")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(criarRequest())));

        //Assert
        resposta.andExpect(status().isCreated());
        verify(movimentacoesEstoqueService).entrada(any(MovimentacoesEstoqueRequest.class));
    }

    @Test
    @DisplayName("POST /estoques/movimentacoes/saida deve registrar e retornar 201")
    void saida() throws Exception {
        //Arrange
        when(movimentacoesEstoqueService.saida(any(MovimentacoesEstoqueRequest.class)))
                .thenReturn(criarMovimentacao());

        //Act
        var resposta = mockMvc.perform(post("/estoques/movimentacoes/saida")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(criarRequest())));

        //Assert
        resposta.andExpect(status().isCreated());
        verify(movimentacoesEstoqueService).saida(any(MovimentacoesEstoqueRequest.class));
    }

    @Test
    @DisplayName("POST /estoques/movimentacoes/saida deve repassar o erro da service (saldo insuficiente)")
    void saidaSaldoInsuficiente() throws Exception {
        //Arrange
        when(movimentacoesEstoqueService.saida(any(MovimentacoesEstoqueRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Saldo insuficiente"));

        //Act
        var resposta = mockMvc.perform(post("/estoques/movimentacoes/saida")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(criarRequest())));

        //Assert
        resposta.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /estoques/movimentacoes/saida-insumo/{idProduto} deve retornar 201 sem corpo")
    void saidaInsumo() throws Exception {
        //Arrange
        var request = criarRequest();

        //Act
        var resposta = mockMvc.perform(post("/estoques/movimentacoes/saida-insumo/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(request)));

        //Assert
        resposta.andExpect(status().isCreated())
                .andExpect(content().string(""));
        verify(movimentacoesEstoqueService).saidaInsumo(eq(1L), any(MovimentacoesEstoqueRequest.class));
    }

    @Test
    @DisplayName("POST /estoques/movimentacoes/saida-insumo/{idProduto} deve repassar o erro da service")
    void saidaInsumoSemInsumos() throws Exception {
        //Arrange
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto nao possui insumos cadastrados"))
                .when(movimentacoesEstoqueService)
                .saidaInsumo(eq(1L), any(MovimentacoesEstoqueRequest.class));

        //Act
        var resposta = mockMvc.perform(post("/estoques/movimentacoes/saida-insumo/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(criarRequest())));

        //Assert
        resposta.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /estoques/movimentacoes/{id} deve editar e retornar 200")
    void editarMovimentacao() throws Exception {
        //Arrange
        when(movimentacoesEstoqueService.editarMovimentacao(eq(1L), any(MovimentacoesEstoqueRequest.class)))
                .thenReturn(criarMovimentacao());

        //Act
        var resposta = mockMvc.perform(patch("/estoques/movimentacoes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(criarRequest())));

        //Assert
        resposta.andExpect(status().isOk());
        verify(movimentacoesEstoqueService).editarMovimentacao(eq(1L), any(MovimentacoesEstoqueRequest.class));
    }

    @Test
    @DisplayName("PATCH /estoques/movimentacoes/{id} deve retornar 404 quando a movimentacao nao existe")
    void editarMovimentacaoNaoEncontrada() throws Exception {
        //Arrange
        when(movimentacoesEstoqueService.editarMovimentacao(eq(99L), any(MovimentacoesEstoqueRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        //Act
        var resposta = mockMvc.perform(patch("/estoques/movimentacoes/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(criarRequest())));

        //Assert
        resposta.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /estoques/movimentacoes/{id} deve excluir e retornar 204")
    void deletarMovimentacao() throws Exception {
        //Act
        var resposta = mockMvc.perform(delete("/estoques/movimentacoes/1"));

        //Assert
        resposta.andExpect(status().isNoContent());
        verify(movimentacoesEstoqueService).excluirMovimentacao(1L);
    }

    @Test
    @DisplayName("DELETE /estoques/movimentacoes/{id} deve retornar 404 quando nao existe")
    void deletarMovimentacaoNaoEncontrada() throws Exception {
        //Arrange
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
                .when(movimentacoesEstoqueService).excluirMovimentacao(99L);

        //Act
        var resposta = mockMvc.perform(delete("/estoques/movimentacoes/99"));

        //Assert
        resposta.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /estoques/movimentacoes/historico deve listar o historico")
    void historicoMovimentacoes() throws Exception {
        //Arrange
        when(movimentacoesEstoqueService.historicoMovimentacoes())
                .thenReturn(List.of(criarMovimentacao()));

        //Act
        var resposta = mockMvc.perform(get("/estoques/movimentacoes/historico"));

        //Assert
        resposta.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("GET /estoques/movimentacoes/historico/{idEstoque} deve listar o historico do estoque")
    void historicoPorEstoque() throws Exception {
        //Arrange
        when(movimentacoesEstoqueService.historicoPorEstoque(1L))
                .thenReturn(List.of(criarMovimentacao()));

        //Act
        var resposta = mockMvc.perform(get("/estoques/movimentacoes/historico/1"));

        //Assert
        resposta.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        verify(movimentacoesEstoqueService).historicoPorEstoque(1L);
    }

    @Test
    @DisplayName("GET /estoques/movimentacoes/historico/{idEstoque} deve retornar lista vazia")
    void historicoPorEstoqueVazio() throws Exception {
        //Arrange
        when(movimentacoesEstoqueService.historicoPorEstoque(1L)).thenReturn(List.of());

        //Act
        var resposta = mockMvc.perform(get("/estoques/movimentacoes/historico/1"));

        //Assert
        resposta.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}