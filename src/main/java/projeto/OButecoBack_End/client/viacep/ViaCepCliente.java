package projeto.OButecoBack_End.client.viacep;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "ViaCepCliente", url = "https://viacep.com.br/ws")
public interface ViaCepCliente {

    @GetMapping("/{cep}/json/")
    ViaCepResponse buscarPorCep(@PathVariable String cep);
}
