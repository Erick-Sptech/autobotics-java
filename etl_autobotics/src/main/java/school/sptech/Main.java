package school.sptech;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        String nomeArqLocal = "dados_tratados";
        String nomeBucketRaw = "java-etl-raw-sptech";
        String nomeBucketTrusted = "java-etl-trusted-sptech";

        List<Captura> lista = Gerenciador.leCsvBucketRaw(nomeBucketRaw);
        Gerenciador.criaCsv(lista, nomeArqLocal);
        Gerenciador.leCsvLocal(nomeArqLocal);
        Gerenciador.exibeListaCapturas(lista);
        Gerenciador.enviaCsvParaBucketTrusted(nomeArqLocal, nomeBucketTrusted);

    }
}