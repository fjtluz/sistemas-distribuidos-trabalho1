package br.furb;

import lombok.Data;
import lombok.NonNull;


@Data
public class Processo {

    @NonNull
    long id;
    boolean estaVivo = true;
    boolean coordenador = false;
    boolean executandoRecurso = false;
    long tempoExecucao = 0;
    long tempoParaAProximaUtilizacao = 10;
    boolean utilizandoRecurso = false;
    boolean aguardandoUtilizacao = false;

    public Processo viraCoordenador() {
        this.coordenador = true;
        return this;
    }

    public Processo morre() {
        this.coordenador = false;
        this.estaVivo = false;
        return this;
    }

    public void utilizaRecurso() {
        this.aguardandoUtilizacao = false;
        long tempo = (long) (Math.random() * 15);

        if (tempo < 5) {
            tempo += 5;
        }

        this.utilizandoRecurso = true;
        this.tempoExecucao = tempo;

        tempo = (long) (Math.random() * 25);

        if (tempo < 10) {
            tempo += 10;
        }

        this.tempoParaAProximaUtilizacao = tempo;
    }
}
