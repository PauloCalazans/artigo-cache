package com.artigo.cache.controller;

import com.artigo.cache.model.FormasPagamento;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author Paulo Calazans on 19/01/2025
 */


@RestController
@RequestMapping(value = "/api/cache-test")
public class ExamploCacheController {

    /*
     * O Header Etag é adicionado automaticamente pelo Spring por meio do Filter na classe WebConfig
     * O Body sendo uma tabela no banco por exemplo o Spring não consulta caso a eTag seja igual ao
     * If-None-Match
     * */
    @GetMapping
    public ResponseEntity<List<FormasPagamento>> listar() {
        List<FormasPagamento> formasPagamentos = getFormasPagamento();

        return ResponseEntity
                .ok()
                .cacheControl(CacheControl.maxAge(10, TimeUnit.SECONDS)) //Padrão possibilita salvar localmente ou em servidores compartilhados
                //.cacheControl(CacheControl.maxAge(10, TimeUnit.SECONDS).cachePrivate()) //Impede que o cache seja armazenado em servidores compartilhados
                //.cacheControl(CacheControl.noCache()) //  Exige sempre uma requisição de validação, fica sempre no status "stale"
                //.cacheControl(CacheControl.noStore()) //  Não permite salvar o cache, o servidor sempre faz a busca e retorna os dados
                .body(formasPagamentos);
    }


    /*
     * O Header Etag é adicionado manualmente neste caso, mas, a lógica é a mesma
     * */
    @GetMapping("/deep-etag")
    public ResponseEntity<List<FormasPagamento>> buscar(ServletWebRequest request) {
        //Desabilitando o ShallowEtag
        ShallowEtagHeaderFilter.disableContentCaching(request.getRequest());

        String eTag = generateEtagValue(getMaxId());
        //Esse metodo faz a validação a eTag foi alterada, caso não sai da execução e retorna statudcode 304 sem reponse
        if (request.checkNotModified(eTag)) {
            return null;
        }

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(10, TimeUnit.SECONDS))
                .eTag(eTag)
                .body(getFormasPagamento());
    }

    private List<FormasPagamento> getFormasPagamento() {
        List<FormasPagamento> formasPagamentos = new ArrayList<>();

        formasPagamentos.add(new FormasPagamento(1, "Dinheiro"));
        formasPagamentos.add(new FormasPagamento(2, "Cartão Crédito"));
        formasPagamentos.add(new FormasPagamento(3, "Cartão Débito"));
        formasPagamentos.add(new FormasPagamento(4, "Vale Alimentação"));
        formasPagamentos.add(new FormasPagamento(5, "Pix"));
        formasPagamentos.add(new FormasPagamento(6, "Transferência Bancária"));
        formasPagamentos.add(new FormasPagamento(7, "Cheque"));
        formasPagamentos.add(new FormasPagamento(8, "BitCoin"));

        return formasPagamentos;
    }

    private Integer getMaxId() {
        Integer maxId;
        Optional<FormasPagamento> formasPagamentos = getFormasPagamento().stream().max(Comparator.comparing(FormasPagamento::getId));

        if(formasPagamentos.isPresent()) {
           maxId = formasPagamentos.get().getId();
        } else {
            maxId = 0;
        }

        return maxId;
    }

    private String generateEtagValue(Integer maxId) {
        return String.valueOf(maxId * 1000000);
    }
}
