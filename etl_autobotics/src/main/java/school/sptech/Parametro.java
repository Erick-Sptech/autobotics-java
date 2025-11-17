package school.sptech;

public class Parametro {
    private String componente;
    private double valorMin;
    private int criticidade; // 2 = médio, 3 = crítico

    public Parametro(String componente, double valorMin, int criticidade) {
        this.componente = componente;
        this.valorMin = valorMin;
        this.criticidade = criticidade;
    }

    public String getComponente() { return componente; }
    public double getValorMin() { return valorMin; }
    public int getCriticidade() { return criticidade; }
}
