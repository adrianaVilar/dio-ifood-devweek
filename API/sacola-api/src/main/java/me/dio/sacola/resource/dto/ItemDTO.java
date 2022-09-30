package me.dio.sacola.resource.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

@AllArgsConstructor
@Builder
@Data
@Embeddable
@NoArgsConstructor
public class ItemDTO {

    private Long produtoId;
    private int qtd;
    private Long sacolaId;

}
