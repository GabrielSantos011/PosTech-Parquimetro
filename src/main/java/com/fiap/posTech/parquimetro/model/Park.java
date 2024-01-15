package com.fiap.posTech.parquimetro.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Document("parking")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Park {
    @Id
    private String id;

    private LocalDateTime entrada;
    private LocalDateTime saida;
    private double valorHora;
    private double valorCobrado;
    private String tipoTempo; // Fixo ou Hora
    private String permanencia;

    @DBRef
    private Endereco enderecoEstacionado;

    @DBRef
    private Pessoa pessoa;

    @DBRef
    private Veiculo veiculo;

    @JsonIgnore
    @Version
    private Long version;
}
