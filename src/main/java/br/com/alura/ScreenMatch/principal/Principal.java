package br.com.alura.ScreenMatch.principal;

import br.com.alura.ScreenMatch.model.DadosEpisodio;
import br.com.alura.ScreenMatch.model.DadosSerie;
import br.com.alura.ScreenMatch.model.DadosTemporada;
import br.com.alura.ScreenMatch.model.Episodio;
import br.com.alura.ScreenMatch.service.ConsumoAPI;
import br.com.alura.ScreenMatch.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Principal {
    private Scanner leitura = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private final String ENDERECO = "https://omdbapi.com/?t=" ;
    private final String API_KEY = "&apikey=67d9f1fc";
    private ConverteDados converteDados = new ConverteDados();


    public void exibeMenu() {
        System.out.println("Digite o nome da série: ");
        var nomeSerie = leitura.nextLine();
        var json = consumoAPI.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = converteDados.obterDados(json, DadosSerie.class);
        System.out.println(dados);

        List<DadosTemporada> temporadas = new ArrayList<>();

        for (int i = 1; i <= dados.totalTemporadas(); i++) {
            json = consumoAPI.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
            DadosTemporada dadosTemporada = converteDados.obterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }
//        temporadas.forEach(System.out::println);

//        for(int i = 0; i < dados.totalTemporadas(); i++) {
//             List<DadosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
//             for(int j = 0; j < episodiosTemporada.size(); j++) {
//                 System.out.println(episodiosTemporada.get(j).titulo());
//             }
//        }

//        temporadas.forEach(temporada -> temporada.episodios().forEach(episodio -> System.out.println(episodio.titulo())));
        // Imprimindo tudo em versão Lambda

//        List<String> nomes = Arrays.asList("Jacque", "Iasmin", "Paulo", "Rodrigo", "Nico");
//        nomes.stream()
//                .sorted()
//                .limit(3)
//                .filter(n -> n.startsWith("N"))
//                .map(n -> n.toUpperCase())
//                .forEach(System.out::println);

        // Criando uma lista de episódios para checar a ordem de avaliação.
        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .collect(Collectors.toList());

//        System.out.println("Os 10 melhores episódios são: ");
//        dadosEpisodios.stream()
//                .filter(e -> !e.avaliacao()
//                .equalsIgnoreCase("N/A"))
//                .peek(e -> System.out.println("Primeiro Filtro(N/A): " + e))
//                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
//                .peek(e -> System.out.println("Ordenação: " + e))
//                .limit(10)
//                .peek(e -> System.out.println("Limite " + e))
//                .map(e -> e.titulo().toUpperCase())
//                .peek(e -> System.out.println("Mapeamento: " + e))
//                .forEach(System.out::println);
        //Criando modelo de episódio para impressão dos objetos e das suas respectivas temporadas.

        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                    .map(d -> new Episodio(t.numero(), d))
                ).collect(Collectors.toList());

        episodios.forEach(System.out::println);

//        System.out.println("Digite um trecho do título do episódio que deseja buscar: ");
//        var trechoTitulo = leitura.nextLine();
//
//        Optional<Episodio> episodioBuscado = episodios.stream()
//                .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
//                .findFirst();
//
//        if(episodioBuscado.isPresent()) {
//            System.out.println("Episódio encontrado: ");
//            System.out.println(episodioBuscado.get());
//        } else {
//            System.out.println("Episódio não encontrado.");
//        }



//
//        System.out.println("A partir de que ano você deseja ver os episódios? ");
//        var ano = leitura.nextInt();
//        leitura.nextLine();
//
//        LocalDate dataBusca = LocalDate.of(ano, 1, 1);
//        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//        episodios.stream()
//                        .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
//                        .forEach(e -> System.out.println(
//                                "Temporada: " + e.getTemporada() +
//                                " Episódio: " + e.getNumeroEpisodio() +
//                                " Título: " + e.getTitulo() +
//                                " Data de Lançamento: " + e.getDataLancamento().format(formatador)
//                        ));
//
//
        // Média de avaliações por temporada:
        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getAvaliacao)));

        System.out.println("Média de avaliações por temporada: ");
        avaliacoesPorTemporada.forEach((temporada, media) -> System.out.println("Temporada: " + temporada + " Média: " + media));

        DoubleSummaryStatistics estatisticas = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));

        System.out.println("Média: " + estatisticas.getAverage());
        System.out.println("Melhor avaliação: " + estatisticas.getMax());
        System.out.println("Pior avaliação: " + estatisticas.getMin());
        System.out.println("Quantidade: " + estatisticas.getCount());

    }
}
