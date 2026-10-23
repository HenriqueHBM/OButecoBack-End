package projeto.obuteco.model.service.endereco;

import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import projeto.obuteco.client.viacep.ViaCepCliente;
import projeto.obuteco.client.viacep.ViaCepResponse;
import projeto.obuteco.controller.endereco.EnderecoResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnderecoServiceTest {
    @Mock
    private ViaCepCliente viaCepCliente;

    @InjectMocks
    private EnderecoService enderecoService;

    private static final String CEP = "85720408";

    private ViaCepResponse viaCep(Boolean erro) {
        return new ViaCepResponse(
                "85720-408",
                "Rua Teste",
                "",
                "Centro",
                "Cidade Teste",
                "PR",
                "Paraná",
                erro
        );
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = false)
    @DisplayName("Retorna o endereco quando o ViaCEP encontra o CEP")
    void buscarPorCep(Boolean erro){
        var viaCepResponse = viaCep(erro);
        when(viaCepCliente.buscarPorCep(CEP)).thenReturn(viaCepResponse);

        var resultado = enderecoService.buscarPorCep(CEP);

        assertEquals(EnderecoResponse.de(viaCepResponse), resultado);
    }

    @ParameterizedTest
    @ValueSource(strings = {"85720-408", "85.720-408", " 85720 408 "})
    @DisplayName("Remove pontos, traco e espacos antes de consultar o ViaCEP")
    void buscarPorCepComMascara(String cep){
        when(viaCepCliente.buscarPorCep(CEP)).thenReturn(viaCep(null));

        enderecoService.buscarPorCep(cep);

        verify(viaCepCliente).buscarPorCep(CEP);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "857", "857204081", "abcdefgh"})
    @DisplayName("CEP com tamanho invalido retorna 400 sem consultar o ViaCEP")
    void buscarPorCepInvalido(String cep){
        var ex = assertThrows(ResponseStatusException.class,
                () -> enderecoService.buscarPorCep(cep));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("CEP invalido", ex.getReason());
        verifyNoInteractions(viaCepCliente);
    }

    @Test
    @DisplayName("CEP nao encontrado no ViaCEP retorna 400")
    void buscarPorCepNaoEncontrado(){
        when(viaCepCliente.buscarPorCep(CEP)).thenReturn(viaCep(true));

        var ex = assertThrows(ResponseStatusException.class,
                () -> enderecoService.buscarPorCep(CEP));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("CEP nao encontrado", ex.getReason());
    }

    @Test
    @DisplayName("Falha na comunicacao com o ViaCEP retorna 503")
    void buscarPorCepServicoIndisponivel(){
        when(viaCepCliente.buscarPorCep(CEP)).thenThrow(FeignException.class);

        var ex = assertThrows(ResponseStatusException.class,
                () -> enderecoService.buscarPorCep(CEP));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, ex.getStatusCode());
        assertEquals("Serviço de consulta de CEP indisponível", ex.getReason());
    }
}