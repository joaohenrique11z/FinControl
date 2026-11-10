# FinControl — Aplicativo Android Nativo (Kotlin & XML)

**Entrega de Marco Acadêmico de Engenharia de Software:**  
> *"Apresentação das melhorias estruturais da aplicação e a persistência de dados funcionando"*

---

## 1. Visão Geral do Projeto

O **FinControl** é um aplicativo mobile nativo desenvolvido em **Kotlin** e layouts declarativos em **XML**, recriado a partir do protótipo web original com arquitetura profissional, modular e orientada a boas práticas de Engenharia de Software.

O aplicativo oferece:
1. **Painel de Início (Dashboard):** Acompanhamento do saldo livre atual, monitoramento de entradas/saídas e termômetro do limite orçamentário (*budget* mensal).
2. **Edição e Gestão de Transações:** Permite clicar em qualquer item da lista de transações recentes para editar detalhes (valor, descrição, categoria, mês) ou excluir o lançamento, atualizando o banco de dados SQLite em tempo real.
3. **Simulador de Compras Parceladas ("Posso Comprar?"):** Projeção preditiva do impacto de compras futuras parceladas em até 24x ao longo de todos os 12 meses do ano através de gráfico de barras empilhadas e análise de veredito orçamentário.
4. **Registro Rápido de Gastos (BottomSheet):** Interface com validação de campos, categorização expandida (12 categorias com ícones temáticos), seleção de mês e divisão matemática de centavos em parcelamentos.
5. **Menu de Configurações & Dark Mode:** Alternância instantânea entre Modo Claro e Modo Escuro com suporte a recursos de tema DayNight, além do ajuste do limite de gastos e salário mensal persistidos em `SharedPreferences`.
6. **Dicas Financeiras:** Feed educativo com recomendações de boas práticas financeiras e planejamento.

---

## 2. Novas Funcionalidades e Melhorias Implementadas

### 🎯 1. Edição de Transações Recentes
- **Interatividade:** Os itens da lista de 'Transações Recentes' agora possuem escuta de clique (`onItemClickListener`).
- **Modal de Edição (`EditTransactionBottomSheet`):** Ao tocar em qualquer transação (ex: *Presente de R$ 100,00*), abre-se um modal pré-preenchido onde o usuário pode alterar valor, descrição, categoria e mês de incidência.
- **Exclusão Segura:** Permite remover o lançamento indesejado diretamente pelo botão "Excluir este lançamento", com confirmação e recálculo instantâneo do saldo e budget.

### 📊 2. Gestão de Limite Mensal (Budget)
- **Definição Dinâmica:** O usuário pode ajustar o teto do limite mensal de gastos a qualquer momento através do Menu de Configurações ou tocando diretamente no Card de Termômetro de Budget.
- **Feedback Visual em Tempo Real:** O termômetro recalcula instantaneamente o percentual consumido e a folga orçamentária ou valor excedente (com cores dinâmicas verde e vermelha).

### 🏷️ 3. Expansão de Categorias e Prazos de Parcelamento
- **12 Categorias Financeiras:** Expansão de 5 para 12 categorias com cores e ícones dedicados: *Fixo (📌), Alimentação (🍽️), Lazer (🎮), Viagem (✈️), Imprevisto (⚠️), Moradia (🏠), Saúde (💊), Educação (📚), Transporte (🚗), Investimento (📈), Presente (🎁), Outros (📦)*.
- **Prazos Estendidos de Parcelamento:** Agora inclui opções de 1x (à vista), 2x, 3x, 4x, 5x, 6x, 10x, 12x, 18x e até 24x.
- **Calendário Completo (12 Meses):** Tanto o simulador quanto o registro e edição cobrem de Janeiro a Dezembro.

### ⚙️ 4. Menu de Configurações (Dark Mode & Ajustes)
- **Botão de Acesso Rápido:** Adicionado botão ⚙️ no cabeçalho superior do Dashboard.
- **Dark Mode Nativo:** Switch que alterna em tempo de execução entre o Tema Claro e o Tema Escuro através de `AppCompatDelegate.setDefaultNightMode`, respeitando paletas de cores específicas (`res/values/colors.xml` e `res/values-night/colors.xml`).
- **Persistência das Preferências:** O modo escuro e os limites configurados são salvos no `SharedPreferences` e restaurados automaticamente na inicialização do aplicativo (`MainActivity`).

---

## 3. Estrutura e Arquitetura do Projeto

```
app/src/main/java/com/example/fincontrol/
├── data/
│   ├── db/
│   │   └── DatabaseHelper.kt             # Persistência relacional SQLite (SQLiteOpenHelper)
│   ├── model/
│   │   ├── Transacao.kt                  # Entidade de dados de lançamentos
│   │   ├── Simulacao.kt                  # Modelos para simulação, veredito e detalhe mensal
│   │   └── Tip.kt                        # Modelo para os cards de dicas
│   ├── preferences/
│   │   └── PreferencesManager.kt         # Persistência chave-valor (SharedPreferences)
│   └── repository/
│       └── FinControlRepository.kt       # Padrão Repository (Single Source of Truth)
├── util/
│   └── FinanceCalculator.kt              # Funções puras de cálculo matemático e formatação
├── MainActivity.kt                       # Single-Activity com Jetpack Navigation & tema
├── DashboardFragment.kt                  # Controlador da tela Início (saldo, budget, lista)
├── SimulatorFragment.kt                  # Controlador do Simulador, Gráfico e Veredito
├── TipsFragment.kt                       # Controlador do feed de Dicas
├── ExpenseBottomSheet.kt                 # Modal de inserção de novo gasto
├── EditTransactionBottomSheet.kt         # Modal de edição e exclusão de transações
├── SettingsBottomSheet.kt                # Modal de configurações (Dark Mode e limites)
├── TransactionAdapter.kt                 # Adaptador RecyclerView com click listener
├── MonthlyBreakdownAdapter.kt            # Adaptador RecyclerView da tabela de projeção
└── TipAdapter.kt                         # Adaptador RecyclerView do feed de dicas
```

---

## 4. Persistência de Dados em Funcionamento

O projeto implementa **duas camadas complementares de persistência local** em conformidade com as diretrizes acadêmicas:

### A) Banco de Dados Relacional Local (`SQLiteOpenHelper`)
Implementado na classe `DatabaseHelper.kt` utilizando o motor **SQLite** nativo do Android:
- **Tabela `transacoes`:**
  - `id` (INTEGER PRIMARY KEY AUTOINCREMENT)
  - `descricao` (TEXT NOT NULL)
  - `categoria` (TEXT NOT NULL)
  - `valor` (REAL NOT NULL)
  - `mes` (INTEGER NOT NULL — 0 a 11 representando Janeiro a Dezembro)
  - `data_criacao` (INTEGER NOT NULL — timestamp)
- **Operações CRUD Completas:**
  - *Create:* `inserirTransacao(transacao)`
  - *Read:* `listarPorMes(mes)` e `obterTransacaoPorId(id)`
  - *Update:* `atualizarTransacao(transacao)`
  - *Delete:* `deletarTransacao(id)`
- **Segurança:** Operações de escrita utilizam `ContentValues` e consultas utilizam argumentos parametrizados (`selectionArgs`), blindando a aplicação contra SQL Injection.

### B) Armazenamento Chave-Valor (`SharedPreferences`)
Implementado na classe `PreferencesManager.kt` para retenção permanente das preferências do usuário:
- `pref_salario`: Salário mensal do usuário (padrão: `R$ 2.500,00`).
- `pref_budget`: Teto orçamentário mensal definido (padrão: `R$ 1.200,00`).
- `pref_mes_atual`: Índice do mês atualmente em foco (padrão: `0` = Janeiro).
- `pref_dark_mode`: Booleano para persistência do estado do Dark Mode.

---

## 5. Roteiro Sugerido para Apresentação Acadêmica (5 Minutos)

Se você precisar defender este projeto perante o professor e colegas, utilize a seguinte sequência:

1. **Introdução (1 min):**  
   *"Apresento o FinControl, migrado de um protótipo web para uma aplicação Android 100% nativa em Kotlin e XML. Transformamos uma estrutura simples em uma arquitetura móvel escalável, desacoplada e com persistência local de dados funcional."*
2. **Demonstração das Novas Funcionalidades (1,5 min):**  
   *"Implementamos a edição completa de transações com toque direto na lista, o gerenciamento dinâmico do limite mensal com feedback visual imediato no termômetro, a expansão para 12 categorias com parcelamentos até 24x, e uma tela de configurações com Dark Mode nativo."*
3. **Persistência de Dados Funcionando (1,5 min):**  
   *"A persistência foi estruturada em dois níveis: SQLite nativo com a classe `DatabaseHelper` (operações completas de inserção, leitura, atualização e exclusão) e `SharedPreferences` para preferências de sistema e limites financeiros. Ao reiniciar o aplicativo, todas as alterações são preservadas."*
4. **Simulador Preditivo e Conclusão (1 min):**  
   *"Na aba 'Simular', o aplicativo projeta o impacto financeiro de compras parceladas mês a mês em um gráfico empilhado, auxiliando na tomada de decisão orçamentária do usuário."*

---

## 6. Como Compilar e Executar

### Opção 1: Via Android Studio
1. Abra o Android Studio.
2. Selecione **File > Open** e aponte para este diretório:
   `C:\Users\joaog\OneDrive\Desktop\Faculdade\Mobile`
3. Aguarde o Gradle sincronizar os arquivos.
4. Clique em **Run 'app'** (<kbd>Shift + F10</kbd>) no emulador ou dispositivo físico conectado via USB.

### Opção 2: Linha de Comando (Gradle Wrapper)
```powershell
# Executar todos os testes unitários automatizados:
.\gradlew.bat testDebugUnitTest

# Gerar o arquivo APK instalável:
.\gradlew.bat assembleDebug
```

O arquivo executável pronto para instalação está disponível na raiz da pasta:
📁 `FinControl.apk`
