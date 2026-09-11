# SyncCarreira 🚀

Bem-vindo ao repositório oficial do **SyncCarreira**, um Projeto de Conclusão de Curso (PCC) do curso de Análise e Desenvolvimento de Sistemas (IFSP). 

O SyncCarreira é um software proprietário do tipo SaaS (Software as a Service) destinado à automação e gestão de processos de orientação vocacional em instituições de ensino. A plataforma visa otimizar o fluxo de trabalho de psicólogos escolares, substituindo métodos manuais por um sistema digital que guia o aluno por quatro trilhas de autoconhecimento (Autoconhecimento, Influências, Informação e Projeto de Futuro).

---

## 📂 Estrutura do Repositório

Este repositório foi organizado para garantir o versionamento adequado do código, da documentação acadêmica e dos artefatos de gestão ágil. A estrutura principal é dividida em três diretórios fundamentais:

### 1. `📁 Documentação/`
Este diretório é o núcleo acadêmico e de engenharia de software do projeto. Aqui, todos os membros da equipe devem contribuir com a escrita e atualização dos documentos. 
* **`*.tex` / Código LaTeX:** Arquivos de texto e estruturação do documento final do TCC.
* **Artefatos Técnicos:** Documentos de Engenharia de Requisitos, como Matriz de Requisitos Funcionais e Não Funcionais, Regras de Negócio, Histórias de Usuário e Casos de Uso
* **Modelagem:** Diagramas UML, Contratos de API e o Diagrama de Entidade-Relacionamento (DER).
* **Materiais de Pesquisa:** Questionários das trilhas validadas com especialistas (Projeto de Vida, Autoconhecimento, etc.) e resumos de entrevistas.

### 2. `📁 Projeto/`
Diretório voltado para a governança e o acompanhamento do desenvolvimento utilizando metodologias ágeis (Scrum/Kanban).
* **`diario.md`:** Arquivo de registro da evolução semanal (diário de bordo) do projeto, preenchido a cada ciclo/Sprint.
* **`*.pod` / `*.xml` (LibreProject):** Arquivo de gestão do projeto contendo o cronograma, duração das Sprints, responsáveis e Gráfico de Gantt, gerado via software LibreProject.

### 3. `📁 Source/`
Neste diretório encontra-se todo o código-fonte (código de desenvolvimento) da aplicação SyncCarreira.
* **Front-end:** Interface de usuário interativa e responsiva.
* **Back-end:** Motor do sistema e lógica de negócios, desenvolvido em arquitetura de camadas utilizando Java e Spring Boot.
* **Banco de Dados:** Scripts de criação e população do banco de dados (MySQL) para o armazenamento seguro e criptografado de dados sensíveis e perfis de usuários (Administrador, Psicóloga e Aluno).

---

## 🛠️ Tecnologias Utilizadas

* **Linguagem & Framework (Back-end):** Java com Spring Boot
* **Banco de Dados:** PostgreSQL
* **Arquitetura & Infraestrutura:** Modelo SaaS hospedado na nuvem (AWS)
* **Autenticação:** Segurança baseada em tokens JWT e isolamento por perfis
* **Documentação de API:** Swagger/OpenAPI
* **Gestão e Versionamento:** Git/GitHub, LibreProject e LaTeX

---

## 🔒 Acesso e Permissões

As permissões de acesso e edição ao código-fonte e à documentação acadêmica são restritas aos perfis dos desenvolvedores da equipe e ao professor orientador, que possuem perfis concedidos diretamente na plataforma para acompanhamento, leitura e validação dos artefatos.
