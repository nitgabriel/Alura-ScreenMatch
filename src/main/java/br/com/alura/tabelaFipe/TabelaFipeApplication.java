package br.com.alura.tabelaFipe;

import br.com.alura.tabelaFipe.App.App;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TabelaFipeApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(br.com.alura.tabelaFipe.TabelaFipeApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        App app = new App();
        app.exibeMenu();
    }
}
