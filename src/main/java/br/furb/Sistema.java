package br.furb;

import lombok.Getter;
import lombok.Setter;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

@Getter
@Setter
public class Sistema {
    private int tamanhoSimulacao;
    private int cicloCoordenador;
    private int cicloNovoProcesso;
    private int relogioGlobal = 1;
    ArrayList<Processo> processos = new ArrayList<>();
    Queue<Processo> listaExecucao = new LinkedList<>();

    private int numeroDeProcessoCriados = 0;
    private int numeroDeCoordenadoresMortos = 0;
    private int numeroDeRecursosUtilizados = 0;

    public Sistema(int ciclosCoordenador, int cicloNovoProcesso, int tamanhoSimulacao) {
        this.cicloCoordenador = ciclosCoordenador;
        this.tamanhoSimulacao = tamanhoSimulacao;
        this.cicloNovoProcesso = cicloNovoProcesso;
        processos.add(new Processo(0).viraCoordenador());
    }

    public void start() {

        while (relogioGlobal < this.tamanhoSimulacao) {

            if (relogioGlobal % this.cicloNovoProcesso == 0) {
                this.getProcessos().add(new Processo(relogioGlobal));
                this.numeroDeProcessoCriados += 1;
                if (this.getProcessos().size() == 1) {
                    this.getProcessos().get(0).coordenador = true;
                }
            }

            if (relogioGlobal % this.cicloCoordenador == 0) {
                this.mataCoordenador();
                this.numeroDeCoordenadoresMortos += 1;
            }

            for (Processo processo : this.getProcessos()) {
                if (!processo.aguardandoUtilizacao && !processo.utilizandoRecurso) {
                    if (processo.getTempoParaAProximaUtilizacao() == 0) {
                        requisitaRecurso(processo);
                    } else {
                        processo.tempoParaAProximaUtilizacao -= 1;
                    }
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
                        this.numeroDeRecursosUtilizados += 1;
                    }
                }
            }

            System.out.println("Relógio: " + relogioGlobal);
            System.out.println("Qtd Processos: " +this.processos.size());

            relogioGlobal++;
        }

        String resultado = "";
        resultado += String.format("| Processos criados:     %d\t|\n", this.numeroDeProcessoCriados);
        resultado += String.format("| Coordenadores mortos:  %d\t|\n", this.numeroDeCoordenadoresMortos);
        resultado += String.format("| Recursos utilizados:   %d\t|", this.numeroDeRecursosUtilizados);

        System.out.println(resultado);
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
                this.numeroDeRecursosUtilizados += 1;
            }
        }
    }
}
