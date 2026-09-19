package projeto.OButecoBack_End.controller.endereco;

import projeto.OButecoBack_End.client.viacep.ViaCepResponse;

public record EnderecoResponse(
        String cep,
        String logradouro, //rua
        String complemento, //numeracao das casas que esta entre
        String bairro,
        String localidade, //cidade
        String uf,
        String estado
) {
    public static EnderecoResponse de(ViaCepResponse v){
        return new EnderecoResponse(
                v.cep(),
                v.logradouro(),
                v.complemento(),
                v.bairro(),
                v.localidade(),
                v.uf(),
                v.estado()
        );
    }
}
