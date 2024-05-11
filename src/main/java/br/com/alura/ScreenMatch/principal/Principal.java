package br.com.alura.ScreenMatch.principal;

import br.com.alura.ScreenMatch.model.*;
import br.com.alura.ScreenMatch.repository.SerieRepository;
import br.com.alura.ScreenMatch.service.ConsumoAPI;
import br.com.alura.ScreenMatch.service.ConverteDados;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner leitura = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConverteDados converteDados = new ConverteDados();
    private final String ENDERECO = "https://omdbapi.com/?t=" ;
    private final String API_KEY = "&apikey=67d9f1fc"; //Chave pública.
    private List<DadosSerie> dadosSeriesList = new ArrayList<>();

    private SerieRepository repositorio;

    private List<Serie>  series = new ArrayList<>();

    public Principal(SerieRepository repositorio) {
        this.repositorio = repositorio;
    }


    public void exibeMenu() {
        var opcao = -1;
        while(opcao != 0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries buscadas
                    4 - Buscar série por título
                    5 - Buscar série por ator
                    6 - Buscar melhores séries por ator
                    7 - Top 5 séries
                    8 - Buscar séries por categoria
                    9 - Buscar séries por máximo de temporadas e avaliação mínima
                    
                    0 - Sair
                    """;
            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1 -> buscarSerieWeb();
                case 2 -> buscarEpisodioPorSerie();
                case 3 -> listarSeriesBuscadas();
                case 4 -> buscarSeriePorTitulo();
                case 5 -> buscarSeriesPorAtor();
                case 6 -> buscarMelhoreSeriesPorAtor();
                case 7 -> buscarTop5Series();
                case 8 -> buscarSeriesPorCategoria();
                case 9 -> buscarSeriesPorMaximoTemporadas();
                case 0 -> System.out.println("Saindo...");
                default -> System.out.println("Opção inválida.");
            }
        }

    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        //dadosSeriesList.add(dados);
        repositorio.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série: ");
        var nomeSerie = leitura.nextLine();
        var json = consumoAPI.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        return converteDados.obterDados(json, DadosSerie.class);
    }

    private void buscarEpisodioPorSerie() {
        listarSeriesBuscadas();
        System.out.println("Escolha uma série pelo nome: ");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serie.isPresent()) {
            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumoAPI.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = converteDados.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());
            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        } else {
            System.out.println("Série não encontrada.");
        }
    }

    private void listarSeriesBuscadas() {
        series = repositorio.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);

    }

    private void buscarSeriePorTitulo() {
        System.out.println("Escolha uma série pelo nome: ");
        var nomeSerie = leitura.nextLine();
        Optional<Serie> serieBuscada = repositorio.findByTituloContainingIgnoreCase(nomeSerie);
        if (serieBuscada.isPresent()) {
            System.out.println("Dados da série: " + serieBuscada.get());
        } else {
            System.out.println("Série não encontrada.");
        }
    }

    private void buscarSeriesPorAtor() {
        System.out.println("Digite o nome de um ator: ");
        var nomeAtor = leitura.nextLine();
        List<Serie> seriesEncontradas = repositorio.findByAtoresContainingIgnoreCase(nomeAtor);

        if (!seriesEncontradas.isEmpty()) {
            System.out.println("Séries em que " + nomeAtor + " atuou: ");
            seriesEncontradas.forEach(s ->
                    System.out.println(s.getTitulo() + " Avaliação: " + s.getAvaliacao()));
        } else {
            System.out.println("Nenhuma série encontrada com este ator.");
        }
    }

    private void buscarMelhoreSeriesPorAtor() {
        System.out.println("Digite o nome de um ator: ");
        var nomeAtor = leitura.nextLine();
        List<Serie> seriesEncontradas = repositorio.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, 8.8);

        if (!seriesEncontradas.isEmpty()) {
            System.out.println("Melhores séries em que " + nomeAtor + " atuou: ");
            seriesEncontradas.forEach(s ->
                    System.out.println(s.getTitulo() + " Avaliação: " + s.getAvaliacao()));
        } else {
            System.out.println("Nenhuma série encontrada com este ator ou nenhuma atendeu nossos critérios de avaliação.");
        }
    }

    private void buscarTop5Series() {
        List<Serie> seriesTop = repositorio.findTop5ByOrderByAvaliacaoDesc();
        seriesTop.forEach(s ->
                System.out.println(s.getTitulo() + " Avaliação: " + s.getAvaliacao()));
    }

    private void buscarSeriesPorCategoria() {
        System.out.println("Deseja buscar séries de que categoria/gênero? ");
        var nomeGenero = leitura.nextLine();
        Categoria categoria = Categoria.fromPortugues(nomeGenero);
        List<Serie> seriesPorCategoria = repositorio.findByGenero(categoria);
        System.out.println("Séries da categoria " + nomeGenero);
        seriesPorCategoria.forEach(System.out::println);
    }

    private void buscarSeriesPorMaximoTemporadas() {
        System.out.println("Deseja buscar séries com no máximo quantas temporadas? ");
        var maximoTemporadas = leitura.nextInt();
        leitura.nextLine();
        System.out.println("Qual a avaliação mínima das séries que deseja buscar? ");
        var minAvaliacao = leitura.nextDouble();
        leitura.nextLine();
        List<Serie> seriesPorMaxTemporadaseAvaliacao = repositorio.findByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(maximoTemporadas, minAvaliacao);
        System.out.println("Séries dentro dos parâmetros informados:");
        seriesPorMaxTemporadaseAvaliacao.forEach(System.out::println);

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
//        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
//                .flatMap(t -> t.episodios().stream())
//                .collect(Collectors.toList());

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

//        List<Episodio> episodios = temporadas.stream()
//                .flatMap(t -> t.episodios().stream()
//                    .map(d -> new Episodio(t.numero(), d))
//                ).collect(Collectors.toList());
//
//        episodios.forEach(System.out::println);

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
//        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
//                .filter(e -> e.getAvaliacao() > 0.0)
//                .collect(Collectors.groupingBy(Episodio::getTemporada,
//                        Collectors.averagingDouble(Episodio::getAvaliacao)));
//
//        System.out.println("Média de avaliações por temporada: ");
//        avaliacoesPorTemporada.forEach((temporada, media) -> System.out.println("Temporada: " + temporada + " Média: " + media));
//
//        DoubleSummaryStatistics estatisticas = episodios.stream()
//                .filter(e -> e.getAvaliacao() > 0.0)
//                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));
//
//        System.out.println("Média: " + estatisticas.getAverage());
//        System.out.println("Melhor avaliação: " + estatisticas.getMax());
//        System.out.println("Pior avaliação: " + estatisticas.getMin());
//        System.out.println("Quantidade: " + estatisticas.getCount());

}
