package school.sptech;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<Captura> listaCaptura = Gerenciador.leCsv("dados_hardware", "https://s3-bucket-java-teste.s3.amazonaws.com/dados_hardware.csv");
        Gerenciador.exibeListaCapturas(listaCaptura);
        Gerenciador.criaCsv(listaCaptura, "dados_hardware");
        Gerenciador.enviaCsvParaBucket("dados_hardware", "s3-bucket-java-teste");
    }
}