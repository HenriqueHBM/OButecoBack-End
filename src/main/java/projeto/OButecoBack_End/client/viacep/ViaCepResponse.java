package projeto.OButecoBack_End.client.viacep;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

//ignora campos caso a api puxe mais dados(no caso filtramos apenas para o que queremos)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ViaCepResponse(
        String cep,
        String logradouro, //rua
        String complemento, //numeracao das casas que esta entre
        String bairro,
        String localidade, //cidade
        String uf,
        String estado,
        Boolean erro //caso venha com erro (200) para nao existente
) {
}
