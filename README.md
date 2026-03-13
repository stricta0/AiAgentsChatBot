# AI Support Agents Router

## General Overview

This project is a **Java-based conversational multi-agent support system** that routes and executes user requests inside a single conversation.

The system combines:

- an **LLM-powered router** that decides which agent should handle a request,
- a **Technical Specialist agent** grounded in local technical documentation,
- a **Billing Specialist agent** backed by a PostgreSQL database,
- a **General agent** for explicitly allowed non-specialist conversation paths,
- a multi-turn conversation flow with clarification and fallback handling.

The goal of the project is to demonstrate how to build a **tool-augmented support assistant** without using external agent frameworks such as LangChain, while keeping orchestration logic fully controlled in Java.

At runtime, the application:

1. accepts a user message in the console,
2. asks the router LLM to generate a structured routing plan,
3. resolves ambiguous `NONE` steps if needed,
4. executes the plan using the most appropriate specialist agent,
5. continues the same conversation across multiple turns.

---

## Getting Started

### 1. Clone the repository

```bash
git clone <YOUR_REPOSITORY_URL>
cd AiAgents
```

### 2. Create a `.env` file

The application uses a `.env` file for configuration.

Copy the example file:

```bash
cp .env.example .env
```

Then fill in your Gemini API key and keep the other values as needed.

Example:

```env
GEMINI_API_KEY=your_api_key
GEMINI_MODEL=models/gemini-2.5-flash
GEMINI_API_URL=https://generativelanguage.googleapis.com/v1beta

POSTGRES_IMAGE=postgres:16
POSTGRES_DB=aiagents
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
```

### 3. Build the project

```bash
mvn compile
```

### 4. Run tests

```bash
mvn test
```

### 5. Start the application

```bash
mvn exec:java -Dexec.mainClass="org.example.Main"
```

---

## Configuration

The application is configured through both:

1. **environment variables** (`.env`)
2. **JSON resource files** in `src/main/resources`

### Environment configuration

Runtime secrets and container/database settings are stored in `.env` and loaded through:

```text
src/main/java/org/example/config/EnvConfig.java
```

### JSON configuration files

The application behavior is also driven by JSON files stored in resources:

- `src/main/resources/router/agents.json`
- `src/main/resources/router/router_prompt.json`
- `src/main/resources/router/unknown_resolution_prompt.json`
- `src/main/resources/agent/billing_specialist_prompt.json`
- `src/main/resources/agent/billing_specialist_error_prompt.json`
- `src/main/resources/agent/technical_specialist_prompt.json`
- `src/main/resources/ui/console_commands.json`
- `src/main/resources/ui/console_messages.json`
- `src/main/resources/docs/docs_config.json`

This makes the system easy to adjust without modifying Java logic.

---

## Agents

The system currently supports three runtime agents:

- `TECHNICAL_SPECIALIST`
- `BILLING_SPECIALIST`
- `GENERAL`

### TECHNICAL_SPECIALIST

`TECHNICAL_SPECIALIST` answers technical questions using **local technical documentation** stored in `src/main/resources/docs/sources`.

It does not rely on general model knowledge alone. Instead, it uses a deterministic retrieval pipeline:

1. load local documents,
2. split them into chunks,
3. compute embeddings,
4. retrieve the most relevant chunks for a user query,
5. build a grounded prompt using only retrieved excerpts.

This agent is intended for questions such as:

- API usage questions
- integration troubleshooting
- configuration issues
- deployment questions
- documented technical errors

Example requests:

- `Why do I get a 401 error?`
- `How do I configure webhooks?`
- `What environment variables are required for deployment?`
- `How do I authenticate API requests?`

Behavior rules:

- answers should be grounded in retrieved documentation,
- if the docs do not cover the issue, the agent should say so,
- if the question is too vague, the agent should ask for clarification,
- the agent should not invent undocumented facts.

---

### BILLING_SPECIALIST

`BILLING_SPECIALIST` handles billing-related requests using a PostgreSQL-backed billing module.

It supports three main capabilities:

#### 1. Check customer subscription plan

The agent can:

- identify the customer’s current subscription plan,
- return the monthly price,
- confirm whether the subscription is active.

Typical flow:

1. ask for email if missing,
2. find the customer by email,
3. find the active subscription,
4. return plan details.

Example requests:

- `What plan am I on?`
- `Check my subscription`
- `What is my current plan? My email is john.doe@example.com`

#### 2. Explain refund policy

The agent can:

- explain whether a refund is available,
- describe refund conditions,
- provide the estimated refund processing time.

Typical flow:

1. ask for email if missing,
2. find the customer,
3. find the active subscription,
4. determine the current plan,
5. retrieve the refund policy,
6. return refund details.

Example requests:

- `Can I get a refund?`
- `What is the refund policy for my subscription?`
- `Is refund available for my account? My email is alice.smith@example.com`

#### 3. Open refund support case

The agent can:

- create a refund support case,
- generate and return a case number,
- confirm that the case was saved,
- ask for a short refund description if needed.

Typical flow:

1. ask for email if missing,
2. find the customer,
3. ask for refund description if missing,
4. create support case,
5. return confirmation and case number.

Example requests:

- `Open a refund case for me`
- `I want to request a refund`
- `Create a refund ticket for john.doe@example.com`

---

### GENERAL

`GENERAL` is a fallback conversational agent used only when a request is explicitly resolved into a general, non-specialist path.

It is not intended to replace the specialist agents.  
Its purpose is to handle situations where:

- the user explicitly agrees to a general response,
- the request does not require billing tools,
- the request does not require technical documentation lookup.

Example requests:

- `hello`
- `Can you just answer this generally?`
- `What can you help me with?`

---

## Commands

The console application supports the following commands:

### `help`
Shows all available commands.

### `agents`
Prints all configured agents and their capabilities.

### `history`
Shows the full conversation history collected during the current session.

### `exit`
Exits the application.

---

## Architecture

The system is built as a **multi-step conversational orchestration pipeline**.

### High-level flow

```text
User message
   ↓
RouterService
   ↓
RoutingPlan
   ↓
UnknownResolutionService (if any NONE steps exist)
   ↓
PlanStepExecutor
   ↓
Appropriate runtime agent
   ↓
Final response / clarification request / abort
```

### Core architecture ideas

- the router does not answer the user directly,
- it produces a structured `RoutingPlan`,
- each `PlanStep` is executed by a registered runtime agent,
- the same `ConversationHistory` is preserved across turns,
- ambiguous steps can be resolved through `UnknownResolutionService`,
- specialist agents handle their full domain flow rather than a single micro-step.

---

## Project Structure

```text
src
├── main
│   ├── java/org/example
│   │   ├── agent
│   │   │   ├── impl
│   │   │   ├── prompt
│   │   │   └── ...
│   │   ├── billing
│   │   ├── config
│   │   ├── conversation
│   │   ├── database
│   │   ├── llm
│   │   ├── router
│   │   ├── technicaldocs
│   │   ├── ui
│   │   └── Main.java
│   └── resources
│       ├── agent
│       ├── db
│       ├── docs
│       ├── router
│       └── ui
└── test
    ├── java/org/example
    │   ├── agent
    │   ├── billing
    │   └── technicaldocs
    └── resources/docs
```

### Important directories

- `agent/` – agent contracts, execution logic, runtime implementations
- `billing/` – billing domain services, repositories, models
- `router/` – router prompt building, routing plan parsing, unknown resolution flow
- `technicaldocs/` – document loading, chunking, embeddings, retrieval
- `ui/` – console application and command system
- `resources/` – JSON configs, SQL initialization, technical documentation sources
- `test/resources/docs/` – dedicated technical documentation fixtures for tests

---

## Core Components

### `Main`
Application bootstrap / composition root.  
Creates all services, loads JSON config, initializes database and technical documentation pipeline, then starts the console chat.

### `LlmClient`
Encapsulates communication with the Gemini API.

Responsibilities:
- build request payloads,
- call Gemini generate endpoint,
- parse model responses.

### `RouterService`
Builds a router prompt and asks the LLM to generate a `RoutingPlan`.

### `UnknownResolutionService`
Handles routing plans that contain `NONE` steps.  
It lets the system resolve ambiguity before executing the plan.

### `PlanStepExecutor`
Executes a single `PlanStep` using the correct runtime agent from `AgentRegistry`.

### `AgentRegistry`
Maps configured agent names to actual runtime agent implementations and validates consistency between config and code.

### `BillingService`
Application-level billing service used by `BILLING_SPECIALIST`.

### `TechnicalDocumentationService`
Preloads technical documents, embeds them once at startup, and retrieves relevant chunks for technical questions.

### `TechnicalDocumentLoader`
Loads supported technical documentation files from resources.

### `TechnicalDocumentChunker`
Splits `.md` and `.txt` documentation into structured chunks.

### `TechnicalChunkEmbeddingService`
Computes embeddings for generated document chunks.

### `TechnicalChunkRetriever`
Finds top matching chunks using cosine similarity.

### `ConsoleChatApplication`
Runs the interactive console loop, handles commands, routes messages, executes plans, and preserves conversation history.

---

## Technical Documentation Retrieval

The `TECHNICAL_SPECIALIST` agent uses a deterministic retrieval pipeline based on local documentation.

### Supported document types

- `.md`
- `.txt`

Unsupported files are skipped.

### Chunking strategy

For Markdown documents:
- split by headings (`#`, `##`, `###`, etc.),
- if needed, fall back to paragraph splitting,
- then sentence splitting,
- finally character-based fallback if required.

For plain text documents:
- split by paragraphs,
- then sentences,
- then character fallback if needed.

### Chunk metadata

Each chunk contains metadata such as:

- `documentName`
- `documentType`
- `headingPath`
- `chunkIndex`
- `content`

This metadata helps preserve context during retrieval and prompting.

### Why this matters

This design allows the technical agent to:
- stay grounded in actual docs,
- retrieve only the most relevant excerpts,
- avoid hallucinating unsupported technical answers,
- remain deterministic and testable.

---

## Database

The application uses a **PostgreSQL database** initialized automatically at startup using:

```text
src/main/resources/db/init.sql
```

The schema contains the following tables:

### `customers`
Stores basic customer data.

Fields:
- `id`
- `email`
- `full_name`

### `subscriptions`
Stores customer subscriptions.

Fields:
- `id`
- `customer_id`
- `plan_name`
- `monthly_price`
- `status`
- `started_at`

### `support_cases`
Stores support/refund cases created by the billing flow.

Fields:
- `id`
- `case_number`
- `customer_id`
- `case_type`
- `status`
- `description`
- `created_at`

### `refund_policies`
Defines refund rules for subscription plans.

Fields:
- `id`
- `plan_name`
- `refund_available`
- `refund_window_days`
- `processing_time_days`
- `policy_description`

### Seed data

The `init.sql` script also contains example development/test data, including:

- sample customers,
- sample subscriptions,
- sample refund policies.

Example records:

| Email | Plan |
|------|------|
| john.doe@example.com | PRO |
| alice.smith@example.com | BASIC |

This data is useful for local testing of the `BILLING_SPECIALIST` agent.  
In a production environment, this seed section would typically be removed and replaced with proper migrations or real data sources.

---

## Example Usage

### Technical question

```text
Why do I get a 401 error?
```

Expected behavior:
- router selects `TECHNICAL_SPECIALIST`
- technical docs are retrieved
- answer is grounded in local documentation

### Billing question

```text
What plan am I on?
```

Expected behavior:
- router selects `BILLING_SPECIALIST`
- agent asks for email if missing
- after receiving email, subscription details are returned

### Refund case creation

```text
Open a refund case for me
```

Expected behavior:
- router selects `BILLING_SPECIALIST`
- agent asks for missing email / refund description if needed
- creates support case and returns case number

### General request

```text
hello
```

Expected behavior:
- may be handled through `GENERAL` when explicitly resolved to that path

### Mixed multi-step request

```text
Why do I get a 401 error and check my subscription for john.doe@example.com
```

Expected behavior:
- router can generate multiple steps
- technical question handled by `TECHNICAL_SPECIALIST`
- billing question handled by `BILLING_SPECIALIST`

---

## Environment Variables

The application uses a `.env` file for configuration.  
An example configuration should be stored in `.env.example`.

Before running the application:

```bash
cp .env.example .env
```

### Required variables

| Variable | Description |
|--------|--------|
| GEMINI_API_KEY | API key used to call the Gemini LLM |
| GEMINI_MODEL | Gemini model name |
| GEMINI_API_URL | Base URL for the Gemini API |
| POSTGRES_IMAGE | Docker image used by Testcontainers |
| POSTGRES_DB | Database name |
| POSTGRES_USER | Database username |
| POSTGRES_PASSWORD | Database password |

Example `.env`:

```env
GEMINI_API_KEY=your_api_key
GEMINI_MODEL=models/gemini-2.5-flash
GEMINI_API_URL=https://generativelanguage.googleapis.com/v1beta

POSTGRES_IMAGE=postgres:16
POSTGRES_DB=aiagents
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
```

The PostgreSQL database is started automatically using **Testcontainers** when the application launches.

---

## Running the Project

Compile:

```bash
mvn compile
```

Run all tests:

```bash
mvn test
```

Start the console application:

```bash
mvn exec:java -Dexec.mainClass="org.example.Main"
```

---

## Design Decisions

### JSON-based configuration
Prompts, commands, console messages, agent definitions, and technical docs config are stored as JSON resources rather than hardcoded directly in Java.

### Constructor-based dependency wiring
Runtime services and agents receive their dependencies explicitly instead of loading config internally.

### Structured router output
The router returns JSON that is parsed into `RoutingPlan` / `PlanStep` objects.

### Deterministic technical retrieval
Technical documentation retrieval is based on explicit chunking + embeddings + cosine similarity rather than opaque tool behavior.

### Test/resource separation
Technical documentation fixtures used by tests are stored in `src/test/resources`, so runtime docs can evolve without breaking tests unexpectedly.

---

## Future Improvements

Possible next steps include:

- improving the technical agent prompt for better `NEEDS_USER_INPUT` behavior,
- extracting a dedicated bootstrap/composition class from `Main`,
- replacing simple SQL initialization with proper database migrations,
- adding more realistic billing workflows,
- improving conversation UX in the console interface,
- adding richer observability/logging,
- supporting more specialist agents,
- extending technical retrieval with citation-style source references,
- adding integration tests for full end-to-end conversation flows.

---

## Summary

This project demonstrates how to build a **multi-agent conversational support system in Java** with:

- an LLM-based router,
- a documentation-grounded technical support agent,
- a database-backed billing support agent,
- a general fallback agent,
- explicit orchestration and execution logic,
- deterministic retrieval over local technical docs,
- multi-turn conversation support.

The architecture emphasizes:

- configurability,
- structured outputs,
- grounded specialist behavior,
- separation of concerns,
- extensibility for future agents and workflows.