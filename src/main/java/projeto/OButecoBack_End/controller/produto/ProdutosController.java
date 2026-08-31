package projeto.OButecoBack_End.controller.produto;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import projeto.OButecoBack_End.model.service.produto.ProdutosService;

@RestController
@RequestMapping("/produtos")
@RequiredArgsConstructor
public class ProdutosController {
    private final ProdutosService produtosService;

}
