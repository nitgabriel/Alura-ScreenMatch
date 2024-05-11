package br.com.alura.ScreenMatch.model;

public enum Categoria {
    ACAO("Action", "Ação"),
    ANIMACAO("Animation", "Animação"),
    AVENTURA("Adventure", "Aventura"),
    BIOGRAFIA("Biography", "Biografia"),
    COMEDIA("Comedy", "Comédia"),
    CRIME("Crime", "Crime"),
    DOCUMENTARIO("Documentary", "Documentário"),
    DRAMA("Drama", "Drama"),
    FANTASIA("Fantasy", "Fantasia"),
    NOIR("Film Noir", "Film Noir"),
    HISTORIA("History", "História"),
    TERROR("Horror", "Terror"),
    MUSICA("Music", "Música"),
    MUSICAL("Musical", "Musical"),
    MISTERIO("Mystery", "Mistério"),
    FAMILIA("Family", "Família"),
    ROMANCE("Romance", "Romance"),
    FICCAO("Sci-Fi", "Ficção Científica"),
    CURTA("Short", "Curta-metragem"),
    ESPORTE("Sport", "Esporte"),
    SUPERHEROIS("Superhero", "Super-herói"),
    SUSPENSE("Thriller", "Suspense"),
    GUERRA("War", "Guerra"),
    OCIDENTAL("Western", "Faroeste");

    private String categoriaOmdb;
    private String categoriaPortugues;



    Categoria(String categoriaOmdb, String  categoriaPortugues) {
        this.categoriaOmdb = categoriaOmdb;
        this.categoriaPortugues = categoriaPortugues;
    }

    public static Categoria fromString(String text) {
        for (Categoria c : Categoria.values()) {
            if (c.categoriaOmdb.equalsIgnoreCase(text)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Nenhuma categoria encontrada para a série.");
    }

    public static Categoria fromPortugues(String text) {
        for (Categoria c : Categoria.values()) {
            if (c.categoriaPortugues.equalsIgnoreCase(text)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Nenhuma categoria encontrada para a série.");
    }
}
