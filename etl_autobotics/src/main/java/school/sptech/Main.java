package school.sptech;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        List<Captura> lista = Gerenciador.leCsv();
        Gerenciador.criaCsv(lista, "trusted_dados_hardware");
        Gerenciador.enviaCsvParaBucket("trusted_dados_hardware", "trusted-1d4a3f130793f4b0dfc576791dd86b34");
    }
}