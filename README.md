# Geting startet

1. Add your open AI API key as env variable
link: https://platform.openai.com/api-keys

    copy <YOUR KEY> and add it to the envirament 
```bash
export GEMINI_API_KEY="<YOUR KEY>"
```

You can check it with 
```bash
echo $GEMINI_API_KEY
```

wybrano model:
gemini-2.5-flash

gemini-2.5-flash-lite - wieksze darmowe limity


# TODO: setup Dumy acc with api key
# TODO: dodać może ten key jakoś do .env czy czegoś w tym stylu idk
# TODO: zmienić router_prompt.json tak żeby również miał pole outputFormat tak jak uknown_resoultion_prompt
# TODO: jest w wielu miejscach hardocowane sciezki to resoursow - fajnie by to bylo przeniesc do jakiegos .env czy cos w tym stylu
# TODO: routing_plan zdefiniowany osobno w router_prompt.json i unknow_resolution_prompt.json
# TODO: hardcodowane niektóre nazwy np. w UnknownResolutionService ABORT I RESOLVED 
# TODO: niektóre funkcje się powtarzają np. stripCodeFences w Router i UnknownResolution Service
# TODO: pousuwać .load() z polowy tych serwisów i zamienic na przekazywanie przez konstruktor
# AI Support Agents Router

## Overview

This project implements a **conversational support AI routing system** in Java.  
The system analyzes a user's message and produces a structured **routing plan** that determines which specialized support agent should handle each task.

The router is powered by a modern LLM (Google Gemini) but all orchestration logic is implemented manually in Java without using agent frameworks such as LangChain.

The project demonstrates how to build a **multi-agent conversational architecture** where:

- an AI model analyzes the user request,
- the system produces a structured execution plan,
- different agents can later execute the steps of that plan.

At the current stage, the project focuses on **the routing layer**, which is the most important architectural component of a multi-agent system.

---

# Key Features

- Java-based implementation
- Integration with a modern LLM (Gemini API)
- Configurable agent definitions via JSON
- Configurable router prompts via JSON
- Structured routing output (JSON → Java objects)
- Support for multi-step routing plans
- Modular architecture ready for agent execution layer
- No external agent frameworks

---

# Architecture Overview

The system is structured around three main layers:

1. LLM communication layer
2. Routing layer
3. Configuration layer

High-level flow:

User message  
│  
▼  
RouterService  
│  
▼  
RouterPromptFactory  
│  
▼  
Gemini LLM  
│  
▼  
Structured JSON RoutingPlan  
│  
▼  
Java objects (RoutingPlan / PlanStep)

The router does not execute tasks itself.  
Instead, it produces a **plan describing how the request should be handled**.

---

# Project Structure

src  
└── main  
&nbsp;&nbsp;&nbsp;&nbsp;├── java  
&nbsp;&nbsp;&nbsp;&nbsp;│   └── org/example  
&nbsp;&nbsp;&nbsp;&nbsp;│       ├── config  
&nbsp;&nbsp;&nbsp;&nbsp;│       │   └── EnvConfig.java  
&nbsp;&nbsp;&nbsp;&nbsp;│       │  
&nbsp;&nbsp;&nbsp;&nbsp;│       ├── llm  
&nbsp;&nbsp;&nbsp;&nbsp;│       │   └── LlmClient.java  
&nbsp;&nbsp;&nbsp;&nbsp;│       │  
&nbsp;&nbsp;&nbsp;&nbsp;│       ├── router  
&nbsp;&nbsp;&nbsp;&nbsp;│       │   ├── RouterService.java  
&nbsp;&nbsp;&nbsp;&nbsp;│       │   ├── RouterPromptFactory.java  
&nbsp;&nbsp;&nbsp;&nbsp;│       │   │  
&nbsp;&nbsp;&nbsp;&nbsp;│       │   └── model  
&nbsp;&nbsp;&nbsp;&nbsp;│       │       ├── AgentCatalog.java  
&nbsp;&nbsp;&nbsp;&nbsp;│       │       ├── AgentDefinition.java  
&nbsp;&nbsp;&nbsp;&nbsp;│       │       ├── PlanStep.java  
&nbsp;&nbsp;&nbsp;&nbsp;│       │       ├── RouterPromptDefinition.java  
&nbsp;&nbsp;&nbsp;&nbsp;│       │       └── RoutingPlan.java  
&nbsp;&nbsp;&nbsp;&nbsp;│       │  
&nbsp;&nbsp;&nbsp;&nbsp;│       └── Main.java  
&nbsp;&nbsp;&nbsp;&nbsp;│  
&nbsp;&nbsp;&nbsp;&nbsp;└── resources  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└── router  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├── agents.json  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└── router_prompt.json

---

# Configuration Files

Two JSON configuration files define the system behavior.

## agents.json

Defines the available agents and their capabilities.

Example:

{
"agents": [
{
"name": "TECHNICAL_SPECIALIST",
"description": "Handles technical questions grounded in local technical documentation.",
"canHandle": [
"API usage questions",
"integration troubleshooting",
"configuration issues"
],
"cannotHandle": [
"refunds",
"pricing"
],
"examples": [
"Why do I get a 401 error?"
]
}
]
}

Adding a new agent requires **only editing this file**.

---

## router_prompt.json

Defines the router instructions used by the LLM.

Example:

{
"taskLines": [
"You are an AI routing planner for a support system.",
"Your task is to analyze the user's message and return a routing plan in JSON.",
"Do not answer the user directly.",
"Your job is to create one or more execution steps for specialist agents.",
"Return only valid JSON."
],
"rules": [
"Choose only agent names that exist in AVAILABLE AGENTS.",
"If the request does not clearly match a specialist, choose NONE.",
"Never invent new agent names."
]
}

This allows **prompt changes without modifying Java code**.

---

# Routing Output Format

The router produces a structured JSON plan:

{
"whole_original_message": "string",
"routing_notes": "string",
"steps": [
{
"agent": "string",
"task": "string",
"original_message_section": "string",
"additional_context": "string",
"confidence": 0.0
}
]
}

Each step represents one task for a specific agent.

---

# Core Components

## LlmClient

Handles communication with the Gemini API.

Responsibilities:

- sending prompts
- handling HTTP requests
- parsing API responses
- extracting the generated text

---

## RouterPromptFactory

Constructs the router prompt dynamically using:

- router prompt configuration
- agent definitions
- user message

This ensures the prompt always reflects the current configuration.

---

## RouterService

Main orchestration component.

Responsibilities:

1. Load agent catalog
2. Load router prompt configuration
3. Generate the router prompt
4. Send the prompt to the LLM
5. Parse the JSON response
6. Convert it to a RoutingPlan object

---

## AgentCatalog

Loads agent definitions from `agents.json`.

This enables adding or modifying agents without code changes.

---

## RoutingPlan / PlanStep

Java representations of the structured routing output returned by the LLM.

They allow the routing result to be processed programmatically.

---

# Example Usage

Example test cases in `Main.java`:

RoutingPlan plan1 = routerService.route("I want a refund for my Pro plan");

RoutingPlan plan2 = routerService.route(
"Why do I get a 401 error when calling the API? If the reason is not having a Pro plan, get a Pro plan"
);

RoutingPlan plan3 = routerService.route(
"What is your favorite programming language?"
);

Example output:

RoutingPlan{
wholeOriginalMessage='I want a refund for my Pro plan',
routingNotes='Single billing request',
steps=[
PlanStep{agent='BILLING_SPECIALIST', ...}
]
}

---

# Environment Variables

The application requires the following variables:

GEMINI_API_KEY  
GEMINI_MODEL  
GEMINI_API_URL

Example `.env`:

GEMINI_API_KEY=your_api_key  
GEMINI_MODEL=models/gemini-2.5-flash  
GEMINI_API_URL=https://generativelanguage.googleapis.com/v1beta

---

# Running the Project

Compile:

mvn compile

Run:

mvn exec:java -Dexec.mainClass="org.example.Main"

---

# Design Decisions

## JSON-Based Configuration

Both agents and router prompts are externalized into JSON files.

Benefits:

- easy modification
- no recompilation required
- clean separation between configuration and code

---

## Structured LLM Output

The router forces the LLM to produce a **strict JSON schema**.  
This allows the application to safely parse results into Java objects.

---

## Prompt Composition

The router prompt is dynamically constructed from:

- task description
- routing rules
- available agents
- user message

This keeps prompts consistent with configuration.

---

# Future Improvements

Possible next steps include:

- implementing actual agent execution layer
- adding conversation memory
- improving routing validation
- introducing tool-calling for agents
- adding retrieval for technical documentation
- supporting clarification flows when routing confidence is low

---

# Summary

This project demonstrates how to build the **routing layer of a multi-agent conversational AI system** in Java using a modern LLM while keeping the orchestration logic fully under developer control.

The architecture emphasizes:

- configurability
- structured outputs
- clear separation of responsibilities
- extensibility for future agent implementations.

