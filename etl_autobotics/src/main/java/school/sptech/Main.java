package school.sptech;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<Captura> lista = Gerenciador.leCsv("java-etl-raw-sptech");
        Gerenciador.exibeListaCapturas(lista);
    }
}