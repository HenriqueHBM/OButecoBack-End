package projeto.OButecoBack_End.model.service.endereco;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import projeto.OButecoBack_End.client.viacep.ViaCepCliente;
import projeto.OButecoBack_End.client.viacep.ViaCepResponse;
import projeto.OButecoBack_End.controller.endereco.EnderecoResponse;

@Service //mantendo como service
@RequiredArgsConstructor //constructor
@Slf4j //logs
public class EnderecoService {

    private final ViaCepCliente viaCepCliente;

    public EnderecoResponse buscarPorCep(String cep){
        String cepLimpo = cep.replaceAll("\\D", ""); //tira pontos e traco

        //verifica se o tamanho bate com um cep
        if (cepLimpo.length() != 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CEP invalido");
        }

        try{
            //faz a busca e retorna o responde dos dados
            ViaCepResponse response = viaCepCliente.buscarPorCep(cepLimpo);

            //caso venha preenchido o campo de erro
            if(response.erro() != null && response.erro()){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CEP nao encontrado");
            }
            log.info("CEP consultado: {}", response);

            return EnderecoResponse.de(response);
        }catch (FeignException e ){
            log.error("Erro ao consultar ViaCEP para {}: {}", cepLimpo, e.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Serviço de consulta de CEP indisponível");
        }
    }

}
