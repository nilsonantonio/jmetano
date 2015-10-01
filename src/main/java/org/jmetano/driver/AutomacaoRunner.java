package org.jmetano.driver;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jmetano.dadosdeteste.AbstractCarga;
import org.jmetano.dadosdeteste.Carga;
import org.jmetano.dadosdeteste.DDT;
import org.jmetano.dadosdeteste.DDTUtils;
import org.jmetano.poscondicoes.PosCondicao;
import org.jmetano.precondicoes.PreCondicao;
import org.jmetano.precondicoes.PreCondicoes;
import org.jmetano.verificacao.Verify;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

public class AutomacaoRunner extends BlockJUnit4ClassRunner {

    private List<AbstractCarga> classesDeCarga;

    private List<String> casosJaExecutados;

    private List<PosCondicao> posCondicoes;

    private FrameworkMethod casoDeTesteAtual;

    private static List<String> preCondicoesQueSeraoExecutadas;

    private static List<String> ordemExecucaoPosCondicaoDescargaPorHierarquia;

    private int proximaPosCondicaoExecutar;

    private int proximaDescargaExecutar;

    private void mapearCasosDeTesteQueJaSeraoExecutadosComoPreCondicao(FrameworkMethod preCondicao) throws Throwable {
        List<FrameworkMethod> preCondicoes = obterPreCondicoes(preCondicao);
        for (FrameworkMethod preCondicaoDaPreCondicao : preCondicoes) {
            mapearCasosDeTesteQueJaSeraoExecutadosComoPreCondicao(preCondicaoDaPreCondicao);
        }
        String nomeCompleto = getNomeCompletoMetodo(preCondicao);
        if (!preCondicoesQueSeraoExecutadas.contains(nomeCompleto)) {
            preCondicoesQueSeraoExecutadas.add(nomeCompleto);
        }
    }

    private String getNomeCompletoMetodo(FrameworkMethod casoDeTeste) {
        return getNomeCompletoMetodo(casoDeTeste, 0);
    }

    private String getNomeCompletoMetodo(FrameworkMethod casoDeTeste, int indice) {
        String nomeCompletoMetodo = "";
        nomeCompletoMetodo += casoDeTeste.getMethod().getDeclaringClass().getName();
        nomeCompletoMetodo += ".";
        nomeCompletoMetodo += casoDeTeste.getName();
        nomeCompletoMetodo += "[";
        nomeCompletoMetodo += indice;
        nomeCompletoMetodo += "]";
        return nomeCompletoMetodo;
    }

    private List<FrameworkMethod> obterPreCondicoes(FrameworkMethod casoDeTeste) throws Throwable {
        PreCondicoes anotacaoPreCondicoes = casoDeTeste.getAnnotation(PreCondicoes.class);
        List<FrameworkMethod> resultado = new ArrayList<FrameworkMethod>();
        List<PreCondicao> preCondicoes = new ArrayList<PreCondicao>();
        if (anotacaoPreCondicoes != null) {
            if (casoDeTeste.getAnnotation(PreCondicao.class) != null) {
                String nomeCompletoPreCondicao = getNomeCompletoMetodo(casoDeTeste);
                throw new IllegalArgumentException("O caso de teste \"" + nomeCompletoPreCondicao
                        + "()\" não pode possuir a anotação @PreCondicao e a anotação @PreCondicoes simultaneamente.");
            }
            for (int i = 0; i < anotacaoPreCondicoes.preCondicoes().length; i++) {
                preCondicoes.add(anotacaoPreCondicoes.preCondicoes()[i]);
            }
        } else if (casoDeTeste.getAnnotation(PreCondicao.class) != null) {
            preCondicoes.add(casoDeTeste.getAnnotation(PreCondicao.class));
        }
        for (PreCondicao preCondicao : preCondicoes) {
            Class<?> classe;
            if (preCondicao.alvo() == Object.class) {
                classe = casoDeTeste.getMethod().getDeclaringClass();
            } else {
                classe = preCondicao.alvo();
            }
            resultado.add(new FrameworkMethod(classe.getMethod(preCondicao.casoDeTeste())));
        }
        return resultado;
    }

    public AutomacaoRunner(Class<?> klass) throws InitializationError {
        super(klass);
        TestClass classeDeTeste = new TestClass(klass);
        if (preCondicoesQueSeraoExecutadas == null) {
            preCondicoesQueSeraoExecutadas = new ArrayList<String>();
        }
        try {
            List<FrameworkMethod> preCondicoes;
            for (FrameworkMethod casoDeTeste : classeDeTeste.getAnnotatedMethods(Test.class)) {
                if (casoDeTeste.getAnnotation(Ignore.class) == null) {
                    preCondicoes = obterPreCondicoes(casoDeTeste);
                    for (FrameworkMethod preCondicao : preCondicoes) {
                        mapearCasosDeTesteQueJaSeraoExecutadosComoPreCondicao(preCondicao);
                    }
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
        casoDeTesteAtual = method;
        if (method.getAnnotation(Ignore.class) != null || preCondicoesQueSeraoExecutadas.contains(getNomeCompletoMetodo(method))) {
            notifier.fireTestIgnored(describeChild(method));
        } else {
            classesDeCarga = new ArrayList<AbstractCarga>();
            casosJaExecutados = new ArrayList<String>();
            posCondicoes = new ArrayList<PosCondicao>();
            ordemExecucaoPosCondicaoDescargaPorHierarquia = new ArrayList<String>();
            EachTestNotifier eachNotifier = new EachTestNotifier(notifier, describeChild(method));
            eachNotifier.fireTestStarted();
            try {
                Collection<Object[]> listaDados = new ArrayList<Object[]>();
                DDT ddt = method.getAnnotation(DDT.class);
                Method metodo;
                if (ddt != null) {
                    metodo = ddt.alvo().getMethod(ddt.metodo());
                    Object resultado = metodo.invoke(ddt.alvo().newInstance(), new Object[0]);
                    listaDados = (Collection<Object[]>) resultado;
                }
                System.out.println("-------------------------------------------");
                System.out.println("Executando caso de teste: " + method.getName());
                if (listaDados.size() == 0) {
                    executarCaso(method, notifier, 0);
                } else {
                    Object[] linha;
                    for (int i = 0; i < listaDados.size(); i++) {
                        linha = (Object[]) listaDados.toArray(new Object[0])[i];
                        DDTUtils.setDados(linha);
                        executarCaso(method, notifier, i);
                    }
                }
                System.out.println("-------------------------------------------");
            } catch (Throwable e) {
                eachNotifier.addFailure(e);
            } finally {
                try {
                    proximaPosCondicaoExecutar = 0;
                    proximaDescargaExecutar = 0;
                    for (String ordem : ordemExecucaoPosCondicaoDescargaPorHierarquia) {
                        if (ordem.equals("posCondicao")) {
                            executarPosCondicao(method);
                        } else if (ordem.equals("descarga")) {
                            executarDescarga();
                        }
                    }
                    executarPosCondicoes(method);
                } catch (Throwable e) {
                    eachNotifier.addFailure(e);
                }
                realizarDescarga();
            }
            try {
                Verify.finalizaExecucao();
            } catch (Throwable e) {
                eachNotifier.addFailure(e);
            }
            eachNotifier.fireTestFinished();
        }
    }

    private void enfileirarPosCondicao(FrameworkMethod method) throws InstantiationException, IllegalAccessException {
        PosCondicao anotacaoPosCondicao = method.getAnnotation(PosCondicao.class);
        if (anotacaoPosCondicao != null) {
            posCondicoes.add(anotacaoPosCondicao);
            ordemExecucaoPosCondicaoDescargaPorHierarquia.add("posCondicao");
        }
        Carga anotacaoCarga = method.getAnnotation(Carga.class);
        if (anotacaoCarga != null) {
            for (int i = 0; i < anotacaoCarga.alvo().length; i++) {
                AbstractCarga abstractCarga = anotacaoCarga.alvo()[i].newInstance();
                for (AbstractCarga carga : classesDeCarga) {
                    if (carga.getClass() == abstractCarga.getClass()) {
                        throw new IllegalArgumentException(method.getMethod().toString() + ":A classe de carga \""
                                + abstractCarga.getClass().getName() + "\" j� foi executada.");
                    }
                }
                classesDeCarga.add(abstractCarga);
                ordemExecucaoPosCondicaoDescargaPorHierarquia.add("descarga");
            }
        }
    }

    private void executarPosCondicoes(final FrameworkMethod method) throws Throwable {
        Class<?> classe;
        Method metodo;
        Exception excecao = null;
        for (int i = posCondicoes.size() - 1; i >= 0; i--) {
            if (posCondicoes.get(i).alvo() == Object.class) {
                classe = method.getMethod().getDeclaringClass();
            } else {
                classe = posCondicoes.get(i).alvo();
            }
            metodo = classe.getMethod(posCondicoes.get(i).metodo());
            try {
                metodo.invoke(classe.newInstance());
            } catch (Exception e) {
                excecao = e;
                System.out.println(posCondicoes.get(i));
            }
        }
        if (excecao != null) {
            throw new RuntimeException(excecao);
        }
    }

    private void executarPosCondicao(final FrameworkMethod method) throws Throwable {
        Class<?> classe;
        Method metodo;
        Exception excecao = null;
        if (posCondicoes.get(proximaPosCondicaoExecutar).alvo() == Object.class) {
            classe = method.getMethod().getDeclaringClass();
        } else {
            classe = posCondicoes.get(proximaPosCondicaoExecutar).alvo();
        }
        metodo = classe.getMethod(posCondicoes.get(proximaPosCondicaoExecutar).metodo());
        try {
            metodo.invoke(classe.newInstance());
            proximaPosCondicaoExecutar++;
        } catch (Exception e) {
            excecao = e;
            System.out.println(posCondicoes.get(proximaPosCondicaoExecutar));
        }
        if (excecao != null) {
            throw new RuntimeException(excecao);
        }
    }

    protected Object createTest() throws Exception {
        return new TestClass(casoDeTesteAtual.getMethod().getDeclaringClass()).getOnlyConstructor().newInstance();
    }

    protected void executarCaso(final FrameworkMethod method, RunNotifier notifier, int indice) throws Throwable {
        if (validarPremissasDeExecucao(method, indice)) {
            executarPreCondicoes(method, notifier, indice);
            enfileirarPosCondicao(casoDeTesteAtual);
            casoDeTesteAtual = method;
            realizarCarga(method);
            enfileirarPosCondicao(method);
            methodBlock(method).evaluate();
        }
    }

    private boolean validarPremissasDeExecucao(FrameworkMethod method, int indice) throws Throwable {
        String nomeCompletoPreCondicao = getNomeCompletoMetodo(method, indice);
        if (casosJaExecutados.contains(nomeCompletoPreCondicao)) {
            return false;
        } else {
            casosJaExecutados.add(nomeCompletoPreCondicao);
            return true;
        }
    }

    private void executarPreCondicoes(final FrameworkMethod method, RunNotifier notifier, int indice) throws Throwable {
        PreCondicoes anotacaoPreCondicoes = method.getAnnotation(PreCondicoes.class);
        List<PreCondicao> preCondicoes = new ArrayList<PreCondicao>();
        if (anotacaoPreCondicoes != null) {
            if (method.getAnnotation(PreCondicao.class) != null) {
                String nomeCompletoPreCondicao = getNomeCompletoMetodo(method);
                throw new IllegalArgumentException("O caso de teste \"" + nomeCompletoPreCondicao
                        + "()\" não pode possuir a anotação @PreCondicao e a anotação @PreCondicoes simultaneamente.");
            }
            for (int i = 0; i < anotacaoPreCondicoes.preCondicoes().length; i++) {
                preCondicoes.add(anotacaoPreCondicoes.preCondicoes()[i]);
            }
        } else if (method.getAnnotation(PreCondicao.class) != null) {
            preCondicoes.add(method.getAnnotation(PreCondicao.class));
        }
        for (PreCondicao preCondicao : preCondicoes) {
            Class<?> classe;
            Method metodo;
            if (preCondicao.alvo() == Object.class) {
                classe = method.getMethod().getDeclaringClass();
            } else {
                classe = preCondicao.alvo();
            }
            metodo = classe.getMethod(preCondicao.casoDeTeste());
            executarCaso(new FrameworkMethod(metodo), notifier, indice);
            System.out.println("Pré-Condição executada: " + classe + metodo.getName());
        }
    }

    private void realizarCarga(final FrameworkMethod method) throws Throwable {
        Carga anotacaoCarga = method.getAnnotation(Carga.class);
        if (anotacaoCarga != null) {
            for (int i = 0; i < anotacaoCarga.alvo().length; i++) {
                AbstractCarga abstractCarga = anotacaoCarga.alvo()[i].newInstance();
                for (AbstractCarga carga : classesDeCarga) {
                    if (carga.getClass() == abstractCarga.getClass()) {
                        throw new IllegalArgumentException(method.getMethod().toString() + ":A classe de carga \""
                                + abstractCarga.getClass().getName() + "\" já foi executada.");
                    }
                }
                classesDeCarga.add(abstractCarga);
                abstractCarga.carregar();
            }
        }
    }

    private void realizarDescarga() {
        for (int i = (classesDeCarga.size() - 1); i >= 0; i--) {
            try {
                classesDeCarga.get(i).descarregar();
            } catch (Throwable e) {
                Verify.fail(e.getMessage());
            }
        }
    }

    private void executarDescarga() {
        try {
            classesDeCarga.get(proximaDescargaExecutar).descarregar();
            proximaDescargaExecutar++;
        } catch (Throwable e) {
            Verify.fail(e.getMessage());
        }
    }

}
