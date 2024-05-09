package br.com.alura.tabelaFipe.App;


import br.com.alura.tabelaFipe.model.Marcas;
import br.com.alura.tabelaFipe.model.Modelos;
import br.com.alura.tabelaFipe.model.Veiculo;
import br.com.alura.tabelaFipe.service.ConverteDados;
import br.com.alura.tabelaFipe.service.APIConsumo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class App {
    private Scanner leitura = new Scanner(System.in);
    private APIConsumo APIConsumo = new APIConsumo();
    private final String ENDERECO = "https://parallelum.com.br/fipe/api/v1/" ;
    private ConverteDados converteDados = new ConverteDados();

    public void exibeMenu() {
        System.out.println("""
                Digite o tipo do veículo para consultar a tabela FIPE.
                1 - "carro",
                2 - "moto",
                3 - "caminhão"
                """);
        var opcaoVeiculo = leitura.nextLine();
        switch (opcaoVeiculo) {
            case "1" -> opcaoVeiculo = "carros";
            case "2" -> opcaoVeiculo = "motos";
            case "3" -> opcaoVeiculo = "caminhoes";
            default -> {
                System.out.println("Opção inválida.");
                return;
            }
        }

        var json = APIConsumo.obterDados(ENDERECO + opcaoVeiculo + "/marcas/");
        var marcasLista = converteDados.obterLista(json, Marcas.class);
        marcasLista.stream()
                .map(marca ->  "Código: " + marca.codigo() + " - " + marca.nome())
                .forEach(System.out::println);

        System.out.println("Informe o código da marca para consulta: ");
        var codigoMarca = leitura.nextLine();
        json = APIConsumo.obterDados(ENDERECO + opcaoVeiculo + "/marcas/" + codigoMarca + "/modelos/");


        System.out.println("\nMarca escolhida: " + marcasLista.stream()
                .filter(marca -> marca.codigo().equals(codigoMarca))
                .findFirst()
                .orElseThrow()
                .nome() + "\n");
        System.out.println("Modelos disponíveis: ");
        var modelosLista = converteDados.obterDados(json, Modelos.class);
        modelosLista.modelos().stream()
                .map(modelo -> "Código: " + modelo.codigo() + " - " + modelo.nome())
                .forEach(System.out::println);

        System.out.println("\nDigite um trecho do nome do carro a ser buscado");
        var nomeVeiculo = leitura.nextLine();

        List<Marcas> modelosFiltrados = modelosLista.modelos().stream()
                .filter(m -> m.nome().toLowerCase().contains(nomeVeiculo.toLowerCase()))
                .collect(Collectors.toList());

        System.out.println("\nModelos filtrados: ");
        modelosFiltrados.stream()
                .map(modelo -> "Código: " + modelo.codigo() + " - " + modelo.nome())
                .forEach(System.out::println);

        System.out.println("\nInforme o código do modelo para consulta: ");
        var codigoModelo = leitura.nextLine();
        json = APIConsumo.obterDados(ENDERECO + opcaoVeiculo + "/marcas/" + codigoMarca + "/modelos/" + codigoModelo + "/anos/");
        List<Marcas> anosLista = converteDados.obterLista(json, Marcas.class);
        List<Veiculo> veiculosLista = new ArrayList<>();

        for (int i = 0; i < anosLista.size(); i++) {
            json = APIConsumo.obterDados(ENDERECO + opcaoVeiculo + "/marcas/" + codigoMarca + "/modelos/" + codigoModelo + "/anos/" + anosLista.get(i).codigo());
            Veiculo veiculo = converteDados.obterDados(json, Veiculo.class);
            veiculosLista.add(veiculo);
        }

        System.out.println("\nTodos os veículos filtrados com avaliações por ano: ");
        veiculosLista.forEach(System.out::println);

    }
}
