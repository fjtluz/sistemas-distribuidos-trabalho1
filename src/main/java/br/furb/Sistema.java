package br.furb;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

@Getter
@Setter
public class Sistema {
    private int cicloConsumo;
    private int cicloCoordenador;
    private int cicloNovoProcesso;
    private int relogioGlobal = 1;
    ArrayList<Processo> processos = new ArrayList<>();
    Queue<Processo> listaExecucao = new LinkedList<>();

    public Sistema(int ciclosCoordenador, int cicloConsumo, int cicloNovoProcesso) {
        this.cicloCoordenador = ciclosCoordenador;
        this.cicloConsumo = cicloConsumo;
        this.cicloNovoProcesso = cicloNovoProcesso;
        processos.add(new Processo(0).viraCoordenador());
    }

    public void start() {
        while (relogioGlobal < 10000) {

            if (relogioGlobal % 40 == 0) {
                this.getProcessos().add(new Processo(relogioGlobal));
                if (this.getProcessos().size() == 1) {
                    this.getProcessos().get(0).coordenador = true;
                }
            }

            if (relogioGlobal % 60 == 0) {
                this.mataCoordenador();
            }

            for (Processo processo : this.getProcessos()) {
                if (processo.getTempoParaAProximaUtilizacao() == 0 && !processo.aguardandoUtilizacao && !processo.utilizandoRecurso) {
                    requisitaRecurso(processo);
                } else {
                    processo.tempoParaAProximaUtilizacao -= 1;
                }
            }

            if (this.listaExecucao.size() != 0) {
                Processo processoExecutando = this.listaExecucao.peek();
                processoExecutando.tempoExecucao -= 1;
                if (processoExecutando.tempoExecucao == 0) {
                    System.out.println(processoExecutando.id + " terminou de utilizar o recurso");
                    processoExecutando.utilizandoRecurso = false;
                    this.listaExecucao.poll();
                    processoExecutando = this.listaExecucao.peek();
                    if (processoExecutando != null) {
                        System.out.println("Processo " + processoExecutando.id + " saiu da fila");
                        System.out.println(processoExecutando.id + " começou a utilizar o recurso");
                        processoExecutando.utilizaRecurso();
                    }
                }
            }

            System.out.println("Relógio: " + relogioGlobal);
            System.out.println("Qtd Processos: " +this.processos.size());

            relogioGlobal++;
        }
    }

    public void mataCoordenador() {
        Processo coordenador = processos.stream().filter(Processo::isCoordenador).findFirst().get().morre();
        System.out.println("coordenador morreu");

       this.processos.remove(coordenador);
    }

    public void elegeCoordenador() {
        System.out.println("coordenador eleito" + processos.stream().filter(Processo::isEstaVivo).findFirst().get().viraCoordenador().getId());
    }

    public void requisitaRecurso(Processo processo) {
        Optional<Processo> optionalProcesso = processos.stream().filter(Processo::isCoordenador).findFirst();

        if (optionalProcesso.isEmpty()) {
            System.out.println("Detectada morte do coordenador pelo processo consumidor");
            this.elegeCoordenador();
            this.listaExecucao = new LinkedList<>();
        } else {
            System.out.println("Processo " + processo.id + " entrou na fila");
            processo.aguardandoUtilizacao = true;
            this.listaExecucao.add(processo);
            if (this.listaExecucao.size() == 1) {
                System.out.println("Processo " + processo.id + " saiu da fila");
                System.out.println(processo.id + " começou a utilizar o recurso");
                processo.utilizaRecurso();
            }
        }
    }
}
