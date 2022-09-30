package me.dio.sacola.service.impl;

import lombok.RequiredArgsConstructor;
import me.dio.sacola.enumeration.FormaPagamento;
import me.dio.sacola.model.Item;
import me.dio.sacola.model.Restaurante;
import me.dio.sacola.model.Sacola;
import me.dio.sacola.repository.ItemRepository;
import me.dio.sacola.repository.ProdutoRepository;
import me.dio.sacola.repository.SacolaRepository;
import me.dio.sacola.resource.dto.ItemDTO;
import me.dio.sacola.service.SacolaService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SacolaServiceImpl implements SacolaService {
    private final SacolaRepository sacolaRepository;
    private final ProdutoRepository produtoRepository;
    private final ItemRepository itemRepository;

    @Override
    public Item incluirItemNaSacola(ItemDTO itemDto) {
        Sacola sacola = verSacola(itemDto.getSacolaId());

        if (sacola.isFechada()) {
            throw new RuntimeException("Não é possível incluir ítem, pois esta sacola já está fechada :(");
        }

        Item itemParaSerInserido = Item.builder()
                .qtd(itemDto.getQtd())
                .sacola(sacola)
                .produto(produtoRepository.findById(itemDto.getProdutoId()).orElseThrow(
                        () -> new RuntimeException("Esse produto não existe :(")
                ))
                .build();

        List<Item> itensDaSacola = sacola.getItens();
        if (itensDaSacola.isEmpty()) {
            itensDaSacola.add(itemParaSerInserido);
        } else {
            Restaurante restauranteAtual = itensDaSacola.get(0).getProduto().getRestaurante(); //get(0) pega o primeiro item da sacola
            Restaurante restauranteDoItemParaAdicionar = itemParaSerInserido.getProduto().getRestaurante();

            if (restauranteAtual.equals(restauranteDoItemParaAdicionar)) { //ver se é o mesmo restaurante para poder adicionar
                itensDaSacola.add(itemParaSerInserido); //se for o mesmo, adiciona o pedido na sacola
            } else { // se não for, lança a exceção
                throw new RuntimeException("Não é possível incluir produtos de restaurantes diferentes :(");
            }
        }

        List<Double> valorDosItens = new ArrayList<>();

        for (Item itemDaSacola: itensDaSacola) {
            double valotTotalItem = itemDaSacola.getProduto().getValorUnitario() * itemDaSacola.getQtd();
            valorDosItens.add(valotTotalItem);
        }

        double valorTotalSacola = valorDosItens.stream()
                .mapToDouble(valorTotalDeCadaItem -> valorTotalDeCadaItem)
                .sum();

        sacola.setValorTotal(valorTotalSacola);

        sacolaRepository.save(sacola);

        return itemParaSerInserido;
    }

    @Override
    public Sacola verSacola(Long id) {
        return sacolaRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Essa sacola não existe :(")
        );
    }

    @Override
    public Sacola fecharSacola(Long id, int numFormaPagamento) {
        Sacola sacola = verSacola(id);

        if (sacola.getItens().isEmpty()) {
            throw new RuntimeException("Inclua ítens na sacola!");
        }

        FormaPagamento formaPagamento = numFormaPagamento == 0 ? FormaPagamento.DINHEIRO : FormaPagamento.MAQUINETA;

        sacola.setFormaPagamento(formaPagamento);
        sacola.setFechada(true);
        return sacolaRepository.save(sacola);
    }
}
