package com.artigo.cache.model;

/**
 * @author Paulo Calazans on 19/01/2025
 */

public class FormasPagamento {
    private final Integer id;
    private final String descricao;

    public FormasPagamento(Integer id, String descricao) {
        this.id = id;
        this.descricao = descricao;
    }

    public Integer getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }
}
