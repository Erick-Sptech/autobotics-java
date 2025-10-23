package school.sptech;

public class Captura {
    private String timestamp;
    private Double cpu;
    private Double ramTotal;
    private Double ramUsada;
    private Double discoTotal;
    private Double discoUsado;
    private Integer numProcessos;
    private String codigoMaquina;
    private String empresa;
    private String setor;
    private String top5Processos;

    public Captura() {

    }

    public Captura(String timestamp, Double cpu, Double ramTotal, Double ramUsada, Double discoTotal, Double discoUsado, Integer numProcessos, String codigoMaquina, String empresa, String setor, String top5Processos) {
        this.timestamp = timestamp;
        this.cpu = cpu;
        this.ramTotal = ramTotal;
        this.ramUsada = ramUsada;
        this.discoTotal = discoTotal;
        this.discoUsado = discoUsado;
        this.numProcessos = numProcessos;
        this.codigoMaquina = codigoMaquina;
        this.empresa = empresa;
        this.setor = setor;
        this.top5Processos = top5Processos;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Double getCpu() {
        return cpu;
    }

    public void setCpu(Double cpu) {
        this.cpu = cpu;
    }

    public Double getRamTotal() {
        return ramTotal;
    }

    public void setRamTotal(Double ramTotal) {
        this.ramTotal = ramTotal;
    }

    public Double getRamUsada() {
        return ramUsada;
    }

    public void setRamUsada(Double ramUsada) {
        this.ramUsada = ramUsada;
    }

    public Double getDiscoTotal() {
        return discoTotal;
    }

    public void setDiscoTotal(Double discoTotal) {
        this.discoTotal = discoTotal;
    }

    public Double getDiscoUsado() {
        return discoUsado;
    }

    public void setDiscoUsado(Double discoUsado) {
        this.discoUsado = discoUsado;
    }

    public Integer getNumProcessos() {
        return numProcessos;
    }

    public void setNumProcessos(Integer numProcessos) {
        this.numProcessos = numProcessos;
    }

    public String getCodigoMaquina() {
        return codigoMaquina;
    }

    public void setCodigoMaquina(String codigoMaquina) {
        this.codigoMaquina = codigoMaquina;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }

    public String getTop5Processos() {
        return top5Processos;
    }

    public void setTop5Processos(String top5Processos) {
        this.top5Processos = top5Processos;
    }

    @Override
    public String toString() {
        return "Captura{" +
                "timestamp='" + timestamp + '\'' +
                ", cpu=" + cpu +
                ", ramTotal=" + ramTotal +
                ", ramUsada=" + ramUsada +
                ", discoTotal=" + discoTotal +
                ", discoUsado=" + discoUsado +
                ", numProcessos=" + numProcessos +
                ", codigoMaquina='" + codigoMaquina + '\'' +
                ", empresa='" + empresa + '\'' +
                ", setor='" + setor + '\'' +
                '}';
    }
}
