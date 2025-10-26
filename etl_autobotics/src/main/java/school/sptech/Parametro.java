package school.sptech;

public class Parametro {
    private String componente;
    private double valorMin;
    private double valorMax;
    private int criticidade; // 2 = médio, 3 = crítico

    public Parametro(String componente, double valorMin, double valorMax, int criticidade) {
        this.componente = componente;
        this.valorMin = valorMin;
        this.valorMax = valorMax;
        this.criticidade = criticidade;
    }

    public String getComponente() { return componente; }
    public double getValorMin() { return valorMin; }
    public double getValorMax() { return valorMax; }
    public int getCriticidade() { return criticidade; }
}
