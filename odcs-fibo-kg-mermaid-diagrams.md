# ODCS + FIBO Knowledge Graph - Complete Mermaid Diagrams

## Diagram 1: System Architecture Overview

```mermaid
graph TB
    subgraph Data["📦 Data Products"]
        ODCS1["ODCS Contract v1<br/>orders-contract.yaml"]
        ODCS2["ODCS Contract v1<br/>customers-contract.yaml"]
        ODCS3["ODCS Contract v2<br/>products-contract.yaml"]
    end
    
    subgraph Ingest["🔄 Ingestion Pipeline"]
        PARSE["YAML Parser<br/>Strict Validation<br/>v3.1.0"]
        SCHEMA_EXTRACT["Schema Feature<br/>Extraction<br/>Tables→Columns"]
        EMBED_COLUMNS["Generate Embeddings<br/>nomic-embed-text<br/>Column vectors"]
    end
    
    subgraph Classification["🤖 Classification Pipeline"]
        PGVEC_CHECK["pgvector Lookup<br/>Similar Columns<br/>Cached"]
        SEMANTIC_SIM["Semantic Similarity<br/>Column → FIBO<br/>Cosine distance"]
        TOP_CANDIDATES["Extract Top 3<br/>Candidates<br/>Ranking"]
        CONFIDENCE_CHECK{"High Confidence?<br/>> 85%"}
        LLM_REASON["LLM Reasoning<br/>DeepSeek-R1<br/>Classification"]
        THRESHOLD_CHECK{"> 75%<br/>Threshold?"}
        AUTO_APPROVE["Auto-Approve<br/>Store in KG"]
        PEND_REVIEW["Pending Review<br/>Flag for Human"]
    end
    
    subgraph Storage["💾 Data Storage"]
        PG_CONTRACTS["PostgreSQL<br/>odcs_contracts<br/>Full YAML"]
        PG_FEATURES["PostgreSQL<br/>column_classifications<br/>+ Embeddings"]
        PG_AUDIT["PostgreSQL<br/>classification_audit<br/>Immutable log"]
    end
    
    subgraph Temporal_KG["⏰ Temporal Knowledge Graph"]
        GRAPHITI["Graphiti Engine<br/>Episode → KG Update<br/>Auto-dedupe"]
        NEO4J["Neo4j Instance<br/>Temporal Nodes<br/>valid_from/valid_to"]
        FIBO_ONTOLOGY["FIBO Ontology<br/>Class Hierarchy<br/>OWL-2-RL Inference"]
    end
    
    subgraph Query["🔍 Query & Reasoning"]
        DISCOVERY["Entity Discovery<br/>Cross-Contract<br/>Relationship Traverse"]
        REASONING["LLM Reasoning<br/>Entity → Insights<br/>Recommendations"]
        MCP_NEO["MCP Server<br/>Neo4j Endpoint<br/>Cypher queries"]
        MCP_PG["MCP Server<br/>PostgreSQL Endpoint<br/>Contract metadata"]
    end
    
    subgraph UI["🎨 Visualization"]
        STREAMLIT["Streamlit Dashboard<br/>- Contract Browser<br/>- Classification Review<br/>- Audit Trail<br/>- Entity Graph"]
    end
    
    ODCS1 --> PARSE
    ODCS2 --> PARSE
    ODCS3 --> PARSE
    PARSE --> SCHEMA_EXTRACT
    SCHEMA_EXTRACT --> EMBED_COLUMNS
    
    EMBED_COLUMNS --> PGVEC_CHECK
    PGVEC_CHECK -->|Cache Miss| SEMANTIC_SIM
    PGVEC_CHECK -->|Cache Hit >90%| AUTO_APPROVE
    
    SEMANTIC_SIM --> TOP_CANDIDATES
    TOP_CANDIDATES --> CONFIDENCE_CHECK
    CONFIDENCE_CHECK -->|Yes| AUTO_APPROVE
    CONFIDENCE_CHECK -->|No| LLM_REASON
    
    LLM_REASON --> THRESHOLD_CHECK
    THRESHOLD_CHECK -->|Yes| AUTO_APPROVE
    THRESHOLD_CHECK -->|No| PEND_REVIEW
    
    AUTO_APPROVE --> PG_FEATURES
    PEND_REVIEW --> PG_FEATURES
    AUTO_APPROVE --> PG_CONTRACTS
    
    PG_FEATURES --> PG_AUDIT
    PG_AUDIT --> GRAPHITI
    
    GRAPHITI --> NEO4J
    NEO4J -.->|References| FIBO_ONTOLOGY
    
    NEO4J --> DISCOVERY
    PG_AUDIT --> DISCOVERY
    DISCOVERY --> REASONING
    
    NEO4J --> MCP_NEO
    PG_CONTRACTS --> MCP_PG
    MCP_NEO --> REASONING
    MCP_PG --> REASONING
    
    REASONING --> STREAMLIT
    NEO4J --> STREAMLIT
    PG_FEATURES --> STREAMLIT
    
    classDef data fill:#e1f5ff,stroke:#01579b,color:#000
    classDef process fill:#fff3e0,stroke:#e65100,color:#000
    classDef storage fill:#f3e5f5,stroke:#4a148c,color:#000
    classDef output fill:#e8f5e9,stroke:#1b5e20,color:#000
    
    class ODCS1,ODCS2,ODCS3 data
    class PARSE,SCHEMA_EXTRACT,SEMANTIC_SIM,LLM_REASON process
    class PG_CONTRACTS,PG_FEATURES,PG_AUDIT,NEO4J storage
    class STREAMLIT,REASONING output
```

---

## Diagram 2: ODCS → FIBO Classification Pipeline (Detailed)

```mermaid
graph LR
    subgraph Input["📄 Input: ODCS Contract"]
        CONTRACT["Contract: orders-v1.0<br/>Domain: ecommerce<br/>Version: 1.0.0"]
        SCHEMA["Schema:<br/>├─ orders (table)<br/>├─ customers (table)<br/>└─ transactions (table)"]
        COLUMNS["Columns:<br/>├─ order_id (PK)<br/>├─ customer_id (FK)<br/>├─ amount<br/>├─ created_at<br/>└─ status"]
    end
    
    subgraph Preparation["🔧 Preparation"]
        VALIDATION["ODCS v3.1.0<br/>Strict Schema<br/>Validation"]
        TEXT_GEN["Generate Text<br/>Field name +<br/>Description"]
        EMBEDDING["Embed Columns<br/>nomic-embed-text<br/>768-dim vectors"]
    end
    
    subgraph Cache["⚡ Cache Check"]
        SIMILARITY["pgvector<br/>Cosine Similarity<br/>Search"]
        CACHED["Similar Column<br/>Already<br/>Classified?"]
        CACHED_HIGH{"Confidence<br/>> 90%?"}
    end
    
    subgraph SemanticPath["📊 Semantic Similarity Path"]
        FIBO_EMBED["FIBO Classes<br/>Pre-computed<br/>Embeddings"]
        COSINE["Cosine Similarity<br/>Column Vector →<br/>FIBO Vectors"]
        TOP3["Top 3<br/>Candidates<br/>Ranked"]
        CONF_CHECK{"Confidence<br/>> 85%?"}
    end
    
    subgraph LLMPath["🧠 LLM Reasoning Path"]
        PROMPT["Prompt Template<br/>Column name +<br/>Description +<br/>Candidates"]
        DEEPSEEK["DeepSeek-R1<br/>Financial<br/>Reasoning"]
        EXTRACT["Extract Choice<br/>+ Confidence<br/>+ Reasoning"]
    end
    
    subgraph Decision["🔀 Decision Logic"]
        CONF_THRESHOLD["Confidence<br/>Threshold<br/>Check"]
        STATUS{"Status<br/>Decision"}
        AUTO["AUTO_APPROVED<br/>Confidence > 75%"]
        REVIEW["PENDING_REVIEW<br/>Confidence 50-75%"]
        UNCLASS["UNCLASSIFIED<br/>Confidence < 50%"]
    end
    
    subgraph Output["✅ Output"]
        MAPPING["Relationship:<br/>Column → FIBO Entity<br/>Confidence: X"]
        AUDIT_LOG["Audit Log:<br/>decision_id<br/>fibo_entity<br/>confidence<br/>timestamp<br/>method"]
        KG_NODE["Create Neo4j<br/>MAPS_TO<br/>relationship"]
    end
    
    CONTRACT --> VALIDATION
    SCHEMA --> VALIDATION
    COLUMNS --> TEXT_GEN
    TEXT_GEN --> EMBEDDING
    VALIDATION --> EMBEDDING
    
    EMBEDDING --> SIMILARITY
    SIMILARITY --> CACHED
    CACHED -->|Yes| CACHED_HIGH
    CACHED -->|No| COSINE
    
    CACHED_HIGH -->|Yes| AUTO
    CACHED_HIGH -->|No| COSINE
    
    FIBO_EMBED --> COSINE
    COSINE --> TOP3
    TOP3 --> CONF_CHECK
    CONF_CHECK -->|Yes| AUTO
    CONF_CHECK -->|No| PROMPT
    
    COLUMNS --> PROMPT
    TOP3 --> PROMPT
    PROMPT --> DEEPSEEK
    DEEPSEEK --> EXTRACT
    EXTRACT --> CONF_THRESHOLD
    
    CONF_THRESHOLD --> STATUS
    STATUS -->|High| AUTO
    STATUS -->|Medium| REVIEW
    STATUS -->|Low| UNCLASS
    
    AUTO --> MAPPING
    REVIEW --> MAPPING
    UNCLASS --> MAPPING
    
    MAPPING --> AUDIT_LOG
    AUDIT_LOG --> KG_NODE
    AUDIT_LOG -->|Feedback| SIMILARITY
    
    classDef input fill:#e1f5ff,stroke:#01579b
    classDef prep fill:#fff3e0,stroke:#e65100
    classDef decision fill:#fce4ec,stroke:#880e4f
    classDef output fill:#e8f5e9,stroke:#1b5e20
    classDef process fill:#f3e5f5,stroke:#4a148c
    
    class CONTRACT,SCHEMA,COLUMNS input
    class VALIDATION,TEXT_GEN,EMBEDDING prep
    class CACHED,CONF_CHECK,STATUS decision
    class MAPPING,AUDIT_LOG,KG_NODE output
    class COSINE,DEEPSEEK,EXTRACT process
```

---

## Diagram 3: Temporal Knowledge Graph Evolution

```mermaid
graph TB
    subgraph T1["📅 T1: 2026-01-10 - Contract v1.0 Ingested"]
        EP1["Episode: orders:v1.0<br/>Timestamp: 2026-01-10T00:00:00Z"]
        N1["(:Contract)<br/>id: orders<br/>version: 1.0"]
        N2["(:Column)<br/>name: order_id<br/>type: uuid"]
        N3["(:Column)<br/>name: customer_id<br/>type: varchar"]
        F1["(:FIBOEntity)<br/>entity_type: fibo:Identifier"]
        F2["(:FIBOEntity)<br/>entity_type: fibo:Party"]
        R1["CONTAINS {<br/>valid_from: T1<br/>valid_to: null<br/>}"]
        R2["MAPS_TO {<br/>confidence: 0.95<br/>valid_from: T1<br/>valid_to: null<br/>method: semantic<br/>}"]
        R3["MAPS_TO {<br/>confidence: 0.88<br/>valid_from: T1<br/>valid_to: null<br/>method: semantic<br/>}"]
    end
    
    subgraph T2["📅 T2: 2026-01-18 - Contract v1.1 Released"]
        EP2["Episode: orders:v1.1<br/>Timestamp: 2026-01-18T10:00:00Z"]
        N4["(:Column)<br/>name: transaction_date<br/>type: timestamp"]
        N5["(:Column)<br/>name: amount (updated)<br/>includes_fees: true"]
        F3["(:FIBOEntity)<br/>entity_type: fibo:DateTime"]
        R4["MAPS_TO {<br/>confidence: 0.94<br/>valid_from: T2<br/>valid_to: null<br/>method: semantic<br/>}"]
        R1_OLD["CONTAINS {<br/>valid_from: T1<br/>valid_to: T2<br/>}"]
        R2_OLD["MAPS_TO {<br/>valid_from: T1<br/>valid_to: T2<br/>}"]
    end
    
    subgraph T3["📅 T3: 2026-01-20 - Historical Query"]
        REPLAY["Point-in-Time Query<br/>as_of: 2026-01-15T00:00:00Z"]
        RESULT1["Results for v1.0<br/>order_id → Identifier<br/>customer_id → Party<br/>No transaction_date<br/>No updated amount"]
        RESULT2["Reproduces exact<br/>KG state from<br/>2026-01-15<br/>(deterministic)"]
    end
    
    EP1 -->|Graphiti processes| N1
    N1 -->|contains| N2
    N1 -->|contains| N3
    N2 -->|maps to| F1
    N3 -->|maps to| F2
    N2 -.->|relationship| R2
    N3 -.->|relationship| R3
    
    EP2 -->|Graphiti processes<br/>Finds changes| N4
    N4 -->|new column| F3
    N4 -->|maps to| R4
    
    EP2 -->|Updates old<br/>relationships| R1_OLD
    R1_OLD -->|valid_to=T2| R1
    
    EP2 -->|Updates old<br/>relationships| R2_OLD
    R2_OLD -->|valid_to=T2| R2
    
    T1 -->|Historical view| REPLAY
    T2 -->|Historical view| REPLAY
    REPLAY -->|Execute Cypher<br/>with timestamp filter| RESULT1
    RESULT1 -->|Audit compliant| RESULT2
    
    classDef timeline fill:#e1f5ff,stroke:#01579b
    classDef entity fill:#fff3e0,stroke:#e65100
    classDef relationship fill:#f3e5f5,stroke:#4a148c
    classDef fibo fill:#ffe0b2,stroke:#d84315
    classDef query fill:#e8f5e9,stroke:#1b5e20
    
    class T1,T2,T3 timeline
    class N1,N2,N3,N4,N5 entity
    class R1,R2,R3,R4,R1_OLD,R2_OLD relationship
    class F1,F2,F3 fibo
    class REPLAY,RESULT1,RESULT2 query
```

---

## Diagram 4: Human-in-the-Loop Classification Review

```mermaid
graph TB
    subgraph Auto["🤖 Automatic Classification"]
        CLASSIFY["Classification Pipeline<br/>Produces Results"]
        CALC_CONF["Calculate<br/>Confidence Score"]
        RANK_CANDIDATES["Rank Top 3<br/>Candidates"]
    end
    
    subgraph Threshold["⚙️ Threshold Logic"]
        CONF1{"Confidence<br/>> 95%?"}
        CONF2{"Confidence<br/>75-95%?"}
        CONF3{"Confidence<br/>< 75%?"}
    end
    
    subgraph Actions["📋 Classification Status"]
        AUTO_APPROVE["✅ AUTO_APPROVED<br/>Confidence > 95%<br/>Immediately create KG node"]
        HUMAN_REVIEW["👤 PENDING_REVIEW<br/>Confidence 75-95%<br/>Requires manual approval"]
        AUTO_REJECT["⚠️ UNCLASSIFIED<br/>Confidence < 75%<br/>Mark for expert review"]
    end
    
    subgraph ReviewUI["🎨 Streamlit Review Dashboard"]
        SHOW_PENDING["Show Pending Items<br/>- Column name<br/>- Description<br/>- Top 3 candidates"]
        SHOW_LLM["Display LLM<br/>Reasoning<br/>- Why candidate 1?<br/>- Why candidate 2?"]
        SHOW_FIBO["Show FIBO Details<br/>- Class definition<br/>- Properties<br/>- Related classes"]
        SHOW_STATS["Show Statistics<br/>- Total classified<br/>- Approval rate<br/>- Confidence dist"]
    end
    
    subgraph Decision["🔍 Human Decision"]
        APPROVE["APPROVE<br/>Select best match<br/>Optional notes"]
        REJECT["REJECT<br/>Provide feedback<br/>Request reclassification"]
        REVISE["REVISE<br/>Select different<br/>candidate from top 3"]
    end
    
    subgraph PostReview["💾 Post-Review Actions"]
        UPDATE_AUDIT["Update Audit Log<br/>- human_decision<br/>- approver_id<br/>- approval_timestamp<br/>- approval_notes"]
        CREATE_KG["Create KG Nodes<br/>MAPS_TO relationship<br/>approval_required=false"]
        FEEDBACK["Provide Feedback<br/>to Model<br/>for retraining"]
    end
    
    subgraph Metrics["📊 Quality Metrics"]
        COHENS_KAPPA["Cohen's Kappa<br/>Human vs Model<br/>Agreement"]
        PRECISION["Precision<br/>per FIBO Class"]
        RECALL["Recall<br/>per FIBO Class"]
        CONF_DIST["Confidence<br/>Distribution<br/>Over time"]
    end
    
    CLASSIFY --> CALC_CONF
    CALC_CONF --> RANK_CANDIDATES
    RANK_CANDIDATES --> CONF1
    
    CONF1 -->|Yes| AUTO_APPROVE
    CONF1 -->|No| CONF2
    CONF2 -->|Yes| HUMAN_REVIEW
    CONF2 -->|No| CONF3
    CONF3 -->|Yes| AUTO_REJECT
    
    AUTO_APPROVE --> UPDATE_AUDIT
    AUTO_REJECT --> UPDATE_AUDIT
    HUMAN_REVIEW --> SHOW_PENDING
    
    SHOW_PENDING --> SHOW_LLM
    SHOW_LLM --> SHOW_FIBO
    SHOW_FIBO --> SHOW_STATS
    SHOW_STATS --> DECISION
    
    DECISION -->|User selects| APPROVE
    DECISION -->|User selects| REJECT
    DECISION -->|User selects| REVISE
    
    APPROVE --> UPDATE_AUDIT
    REJECT --> UPDATE_AUDIT
    REVISE --> UPDATE_AUDIT
    
    UPDATE_AUDIT --> CREATE_KG
    CREATE_KG --> FEEDBACK
    FEEDBACK -->|Collect for<br/>model retraining| COHENS_KAPPA
    
    UPDATE_AUDIT --> PRECISION
    UPDATE_AUDIT --> RECALL
    UPDATE_AUDIT --> CONF_DIST
    
    COHENS_KAPPA --> METRICS
    PRECISION --> METRICS
    RECALL --> METRICS
    CONF_DIST --> METRICS
    
    classDef auto fill:#c8e6c9,stroke:#1b5e20
    classDef manual fill:#fff9c4,stroke:#f57f17
    classDef stored fill:#bbdefb,stroke:#1565c0
    classDef measured fill:#ffe0b2,stroke:#d84315
    
    class AUTO_APPROVE,AUTO_REJECT auto
    class HUMAN_REVIEW,APPROVE,REJECT,REVISE manual
    class UPDATE_AUDIT,CREATE_KG,FEEDBACK stored
    class COHENS_KAPPA,PRECISION,RECALL,CONF_DIST measured
```

---

## Diagram 5: Cross-Contract Discovery & Linking

```mermaid
graph TB
    subgraph Contracts["📚 Data Contracts"]
        C1["Contract A: Orders<br/>├─ order_id (Identifier)<br/>├─ customer_id (Party)<br/>├─ product_id (Product)<br/>└─ amount (Amount)"]
        C2["Contract B: Customers<br/>├─ customer_id (Party)<br/>├─ name (Text)<br/>├─ email (Text)<br/>└─ created_date (DateTime)"]
        C3["Contract C: Products<br/>├─ product_id (Product)<br/>├─ sku (Identifier)<br/>├─ category (Classification)<br/>└─ price (Amount)"]
    end
    
    subgraph PhaseOne["Phase 1: Local Classification"]
        CLASS_A["Classify Contract A<br/>Locally"]
        CLASS_B["Classify Contract B<br/>Locally"]
        CLASS_C["Classify Contract C<br/>Locally"]
        STORE_LOCAL["Store in Neo4j<br/>Local relationships<br/>only"]
    end
    
    subgraph PhaseTwo["Phase 2: Reference Resolution"]
        FIND_REFS["Find References<br/>customer_id in A<br/>references customer_id in B"]
        RESOLVE["Resolve Contract IDs<br/>A.customer_id →<br/>B.customer_id"]
        CREATE_CROSS["Create Cross-Contract<br/>Relationships<br/>Contract A -[REFERENCES]→<br/>Contract B"]
    end
    
    subgraph Graph["🕸️ Knowledge Graph Result"]
        G1["(:Contract A)<br/>-[:CONTAINS]→<br/>(:Column customer_id)<br/>-[:MAPS_TO]→<br/>(:FIBOEntity Party)"]
        G2["(:Contract B)<br/>-[:CONTAINS]→<br/>(:Column customer_id)<br/>-[:MAPS_TO]→<br/>(:FIBOEntity Party)"]
        G3["(:Contract A)<br/>-[:REFERENCES]→<br/>(:Contract B)<br/>{field: customer_id}"]
        G4["Cross-Contract Link<br/>A.customer_id<br/>semantically maps to<br/>B.customer_id<br/>(both → Party)"]
    end
    
    subgraph Query["🔍 Cross-Contract Discovery"]
        Q1["Query:<br/>Find all contracts<br/>mapping to fibo:Party<br/>with customer_id column"]
        Q2["Result Path:<br/>Order Contract -contains-<br/>customer_id -maps_to-<br/>Party"]
        Q3["Related:<br/>Customer Contract -contains-<br/>customer_id -maps_to-<br/>Party"]
        Q4["Link:<br/>Both map to Party<br/>→ Can join on customer_id"]
    end
    
    subgraph Insights["💡 Insights & Recommendations"]
        INS1["Insight: party_id exists<br/>in 3 contracts<br/>Could standardize"]
        INS2["Recommendation:<br/>Create shared Party<br/>master table"]
        INS3["Risk Analysis:<br/>If Party definition<br/>changes, affects 3<br/>contracts"]
    end
    
    C1 --> CLASS_A
    C2 --> CLASS_B
    C3 --> CLASS_C
    
    CLASS_A --> STORE_LOCAL
    CLASS_B --> STORE_LOCAL
    CLASS_C --> STORE_LOCAL
    
    STORE_LOCAL --> FIND_REFS
    FIND_REFS --> RESOLVE
    RESOLVE --> CREATE_CROSS
    
    CREATE_CROSS --> G1
    CREATE_CROSS --> G2
    CREATE_CROSS --> G3
    
    G1 --> Q1
    G2 --> Q1
    G3 --> Q2
    
    Q2 --> Q3
    Q3 --> Q4
    Q4 --> INS1
    Q4 --> INS2
    
    INS1 --> INS3
    INS2 --> INS3
    
    classDef contract fill:#e1f5ff,stroke:#01579b
    classDef local fill:#fff3e0,stroke:#e65100
    classDef cross fill:#f3e5f5,stroke:#4a148c
    classDef insight fill:#e8f5e9,stroke:#1b5e20
    
    class C1,C2,C3 contract
    class CLASS_A,CLASS_B,CLASS_C,STORE_LOCAL local
    class FIND_REFS,RESOLVE,CREATE_CROSS,G1,G2,G3,G4 cross
    class Q1,Q2,Q3,Q4,INS1,INS2,INS3 insight
```

---

## Diagram 6: MCP Server Query Flow

```mermaid
sequenceDiagram
    participant Agent as AI Agent
    participant MCPN as MCP-Neo4j Server
    participant MCPP as MCP-PostgreSQL Server
    participant NEO as Neo4j KG
    participant PG as PostgreSQL
    participant LLM as LLM Reasoning
    
    Agent->>MCPN: /mcp/discovery/contracts<br/>?entity=fibo:Party
    activate MCPN
    MCPN->>NEO: MATCH (e:FIBOEntity)<br/>WHERE e.entity_type=...<br/>RETURN contracts
    activate NEO
    NEO-->>MCPN: [contract_a, contract_b]
    deactivate NEO
    MCPN-->>Agent: {contracts: [...], metadata: {...}}
    deactivate MCPN
    
    Agent->>MCPP: /mcp/contract/details<br/>?contract_id=order-contract
    activate MCPP
    MCPP->>PG: SELECT * FROM<br/>odcs_contracts<br/>WHERE contract_id=...
    activate PG
    PG-->>MCPP: {odcs_definition: {...}}
    deactivate PG
    MCPP-->>Agent: {schema: [...], team: [...]}
    deactivate MCPP
    
    Agent->>LLM: Summarize contracts<br/>mapping to Party<br/>+ full details
    activate LLM
    LLM->>LLM: Process KG results<br/>+ ODCS metadata<br/>+ Generate insights
    LLM-->>Agent: "These 3 contracts<br/>share Party entity.<br/>Recommendation:<br/>Create master table"
    deactivate LLM
    
    Agent->>Agent: Display results<br/>in UI/Report
    
    Note over Agent,LLM: Full lineage captured<br/>for audit trail
```

---

## Diagram 7: Idempotent Classification with Versioning

```mermaid
graph TB
    subgraph Time["⏱️ Timeline"]
        T0["T0: 2026-01-18<br/>Model v1.0"]
        T1["T1: 2026-01-18<br/>Contract v1.0<br/>Received"]
        T2["T2: 2026-01-18<br/>Classification"]
        T3["T3: 2026-01-20<br/>Model v1.1<br/>Retraining"]
        T4["T4: 2026-01-20<br/>Reclassify v1.0"]
        T5["T5: 2026-01-25<br/>Contract v1.1<br/>Received"]
    end
    
    subgraph Iteration1["🔄 Iteration 1: Initial Classification (T2)"]
        CLASS1["Classify v1.0<br/>customer_id → Party<br/>Confidence: 0.85"]
        STORE1["Store in PostgreSQL<br/>classification_audit<br/>- contract_id: order-v1.0<br/>- column: customer_id<br/>- fibo_entity: Party<br/>- model_version: 1.0<br/>- confidence: 0.85<br/>- created_at: T2"]
        KG1["Create Neo4j<br/>customer_id -[MAPS_TO]→ Party<br/>valid_from: T2<br/>valid_to: null"]
    end
    
    subgraph Iteration2["🔄 Iteration 2: Model Retraining (T4)"]
        RECLASS["Reclassify v1.0<br/>customer_id → Party<br/>Confidence: 0.88<br/>Model v1.1 (better)"]
        CHECK["Check PostgreSQL:<br/>Already classified?<br/>Yes: order-v1.0 +<br/>customer_id"]
        IDEM["Idempotent Logic:<br/>If model_version<br/>different: Re-evaluate"]
        DECIDE{"Prediction<br/>changed?"}
        SKIP["Skip update<br/>(same result)"]
        UPDATE["Update relationship<br/>valid_to: T4<br/>Create new:<br/>valid_from: T4<br/>confidence: 0.88<br/>model_version: 1.1"]
    end
    
    subgraph Iteration3["🔄 Iteration 3: New Version (T5)"]
        NEW_VERSION["Contract v1.1<br/>New field:<br/>created_date"]
        CLASSIFY_NEW["Classify v1.1<br/>created_date →<br/>DateTime"]
        STORE_NEW["Store with<br/>contract_id: order-v1.1<br/>(different from v1.0)"]
        EPISODE["Add to Graphiti<br/>episode_id:<br/>order:v1.1"]
        KG_NEW["Create Neo4j<br/>for v1.1<br/>Old v1.0 unchanged"]
    end
    
    subgraph AuditTrail["📋 Audit Trail (Immutable)"]
        AUDIT1["2026-01-18 10:00<br/>order-v1.0 customer_id<br/>→ Party (model v1.0)"]
        AUDIT2["2026-01-20 15:30<br/>order-v1.0 customer_id<br/>→ Party (model v1.1)"]
        AUDIT3["2026-01-25 09:00<br/>order-v1.1 created_date<br/>→ DateTime (model v1.1)"]
    end
    
    T1 --> CLASS1
    CLASS1 --> STORE1
    STORE1 --> KG1
    T2 -.-> AUDIT1
    
    T3 --> RECLASS
    T4 --> CHECK
    CHECK --> IDEM
    IDEM --> DECIDE
    DECIDE -->|No| SKIP
    DECIDE -->|Yes| UPDATE
    UPDATE --> STORE1
    T4 -.-> AUDIT2
    
    T5 --> NEW_VERSION
    NEW_VERSION --> CLASSIFY_NEW
    CLASSIFY_NEW --> STORE_NEW
    STORE_NEW --> EPISODE
    EPISODE --> KG_NEW
    T5 -.-> AUDIT3
    
    classDef timeline fill:#e1f5ff,stroke:#01579b
    classDef process fill:#fff3e0,stroke:#e65100
    classDef store fill:#f3e5f5,stroke:#4a148c
    classDef audit fill:#fce4ec,stroke:#880e4f
    
    class T0,T1,T2,T3,T4,T5 timeline
    class CLASS1,RECLASS,IDEM,CLASSIFY_NEW process
    class STORE1,KG1,UPDATE,KG_NEW store
    class AUDIT1,AUDIT2,AUDIT3 audit
```

---

## Diagram 8: Conflict Resolution Scenarios

```mermaid
graph TB
    subgraph Scenario1["Scenario 1: Same Contract, Different Model Version"]
        S1_CURRENT["Current State:<br/>order-v1.0<br/>customer_id → Party (v1.0)"]
        S1_NEW["New Classification:<br/>Same contract<br/>customer_id → Party (v1.1)<br/>Confidence improved: 0.85→0.88"]
        S1_CHECK["Check: Already<br/>classified with<br/>model v1.0"]
        S1_DECIDE{"Confidence<br/>improved?"}
        S1_RESOLVE["✅ Update:<br/>Invalidate old<br/>Create new with v1.1"]
        S1_AUDIT["✅ Log:<br/>Reason: Model update<br/>v1.0 → v1.1"]
    end
    
    subgraph Scenario2["Scenario 2: Contract Version Conflict"]
        S2_A["Model run 1:<br/>order-v1.0<br/>customer_id →<br/>Party (0.85)"]
        S2_B["Model run 2:<br/>order-v1.0<br/>customer_id →<br/>PartyIdentifier (0.87)"]
        S2_CONFLICT["🚨 Conflict:<br/>Same contract version<br/>Different classification"]
        S2_FLAG["⚠️ Flag:<br/>Uncertainty in model<br/>Candidates too close"]
        S2_HUMAN["👤 Send to human:<br/>Decide: Party vs<br/>PartyIdentifier"]
    end
    
    subgraph Scenario3["Scenario 3: Late-Arriving Data"]
        S3_OLD["Classification created:<br/>2026-01-18 10:00<br/>order-v1.0<br/>customer_id → Party"]
        S3_BACKUP["Backup system sends<br/>old classification:<br/>2026-01-17 23:00<br/>order-v1.0<br/>customer_id → Party"]
        S3_TIMESTAMP["Check timestamp:<br/>Backup is OLDER<br/>(2026-01-17)"]
        S3_DISCARD["✅ Discard:<br/>Already have<br/>newer version"]
        S3_AUDIT_SKIP["✅ Log:<br/>Reason: Duplicate<br/>older timestamp"]
    end
    
    subgraph Scenario4["Scenario 4: Circular Dependencies"]
        S4_A["Contract A:<br/>references B"]
        S4_B["Contract B:<br/>references A"]
        S4_CYCLIC["🔄 Circular<br/>dependency<br/>detected"]
        S4_TWO_PASS["✅ Solution:<br/>Use two-pass approach<br/>Pass 1: Local classify<br/>Pass 2: Resolve refs"]
    end
    
    S1_CURRENT --> S1_CHECK
    S1_NEW --> S1_CHECK
    S1_CHECK --> S1_DECIDE
    S1_DECIDE -->|Yes| S1_RESOLVE
    S1_RESOLVE --> S1_AUDIT
    
    S2_A --> S2_CONFLICT
    S2_B --> S2_CONFLICT
    S2_CONFLICT --> S2_FLAG
    S2_FLAG --> S2_HUMAN
    
    S3_OLD --> S3_TIMESTAMP
    S3_BACKUP --> S3_TIMESTAMP
    S3_TIMESTAMP --> S3_DISCARD
    S3_DISCARD --> S3_AUDIT_SKIP
    
    S4_A --> S4_CYCLIC
    S4_B --> S4_CYCLIC
    S4_CYCLIC --> S4_TWO_PASS
    
    classDef resolved fill:#c8e6c9,stroke:#1b5e20
    classDef conflict fill:#ffcdd2,stroke:#b71c1c
    classDef human fill:#fff9c4,stroke:#f57f17
    classDef solution fill:#e0f2f1,stroke:#00695c
    
    class S1_RESOLVE,S1_AUDIT,S3_DISCARD,S3_AUDIT_SKIP resolved
    class S2_CONFLICT,S2_FLAG,S4_CYCLIC conflict
    class S2_HUMAN human
    class S4_TWO_PASS solution
```

---

## Diagram 9: Classification Model Training Loop

```mermaid
graph TB
    subgraph Bootstrap["🌱 Bootstrap Phase (Week 1)"]
        FIBO_DESC["Extract from FIBO<br/>RDF definitions<br/>rdfs:label<br/>rdfs:comment"]
        GEN_SYNTHETIC["Generate Synthetic<br/>Training Data<br/>Column name +<br/>Description"]
        LABEL_MANUAL["Manual Labeling<br/>20-30 expert hours<br/>500 examples<br/>10 per FIBO class"]
        INIT_MODEL["Train Seed Model<br/>sklearn/scikit<br/>With 500 labels"]
    end
    
    subgraph IterOne["🔄 Iteration 1 (Week 2-3)"]
        DEPLOY1["Deploy Model v1.0<br/>on 10 test<br/>contracts"]
        EVAL1["Evaluate Results<br/>Accuracy: 82%<br/>Need improvement"]
        SELECT_HARD["Select 100 'hard'<br/>examples<br/>Confidence 40-60%"]
        LABEL_ITER1["Expert labels<br/>100 examples<br/>5 hours"]
        RETRAIN1["Retrain Model v1.1<br/>With 600 labels"]
    end
    
    subgraph IterTwo["🔄 Iteration 2 (Week 4)"]
        DEPLOY2["Deploy Model v1.1<br/>Accuracy: 88%<br/>Getting closer"]
        SELECT_HARD2["Select 100 more<br/>'hard' examples"]
        LABEL_ITER2["Expert labels<br/>100 examples<br/>5 hours"]
        RETRAIN2["Retrain Model v1.2<br/>With 700 labels"]
    end
    
    subgraph IterThree["🔄 Iteration 3 (Week 5)"]
        DEPLOY3["Deploy Model v1.2<br/>Accuracy: 91%<br/>Target reached!"]
        METRIC_CHECK{"Metrics OK?<br/>Precision >85%<br/>Recall >80%?"}
        PLATEAU["Model plateau<br/>Accuracy plateauing<br/>→ diminishing returns"]
        FREEZE["Freeze Model v1.2<br/>Production ready"]
    end
    
    subgraph Maintenance["🔧 Ongoing Maintenance (Post-Launch)"]
        COLLECT["Collect feedback<br/>from human reviewers<br/>- Rejections<br/>- Revisions<br/>- High-confidence errors"]
        BATCH_RETRAIN["Monthly batch<br/>retraining<br/>Accumulated<br/>feedback"]
        ABLATION["Ablation study:<br/>Test on holdout<br/>contracts"]
        DEPLOY_NEXT["Deploy if<br/>accuracy improves<br/>v1.3, v1.4, ..."]
    end
    
    subgraph Metrics["📊 Tracking Metrics"]
        COHENS["Cohen's Kappa<br/>Human agreement"]
        PREC["Precision per<br/>FIBO class"]
        REC["Recall per<br/>FIBO class"]
        CONF_DIST["Confidence<br/>distribution"]
    end
    
    FIBO_DESC --> GEN_SYNTHETIC
    GEN_SYNTHETIC --> LABEL_MANUAL
    LABEL_MANUAL --> INIT_MODEL
    
    INIT_MODEL --> DEPLOY1
    DEPLOY1 --> EVAL1
    EVAL1 --> SELECT_HARD
    SELECT_HARD --> LABEL_ITER1
    LABEL_ITER1 --> RETRAIN1
    
    RETRAIN1 --> DEPLOY2
    DEPLOY2 --> SELECT_HARD2
    SELECT_HARD2 --> LABEL_ITER2
    LABEL_ITER2 --> RETRAIN2
    
    RETRAIN2 --> DEPLOY3
    DEPLOY3 --> METRIC_CHECK
    METRIC_CHECK -->|Yes| FREEZE
    METRIC_CHECK -->|No| PLATEAU
    PLATEAU --> FREEZE
    
    FREEZE --> COLLECT
    COLLECT --> BATCH_RETRAIN
    BATCH_RETRAIN --> ABLATION
    ABLATION --> DEPLOY_NEXT
    
    EVAL1 --> COHENS
    EVAL1 --> PREC
    EVAL1 --> REC
    EVAL1 --> CONF_DIST
    
    classDef init fill:#c8e6c9,stroke:#1b5e20
    classDef iter fill:#fff9c4,stroke:#f57f17
    classDef prod fill:#bbdefb,stroke:#1565c0
    classDef maint fill:#ffe0b2,stroke:#d84315
    classDef metric fill:#f0f4c3,stroke:#827717
    
    class FIBO_DESC,GEN_SYNTHETIC,LABEL_MANUAL,INIT_MODEL init
    class DEPLOY1,EVAL1,SELECT_HARD,LABEL_ITER1,RETRAIN1 iter
    class DEPLOY2,SELECT_HARD2,LABEL_ITER2,RETRAIN2 iter
    class DEPLOY3,METRIC_CHECK,PLATEAU,FREEZE prod
    class COLLECT,BATCH_RETRAIN,ABLATION,DEPLOY_NEXT maint
    class COHENS,PREC,REC,CONF_DIST metric
```

---

## Diagram 10: Complete Data Flow (End-to-End)

```mermaid
graph LR
    subgraph Source["📦 Source"]
        DATA_PROD["New Data<br/>Product<br/>Announced"]
    end
    
    subgraph Contract["📄 ODCS Contract"]
        WRITE_ODCS["Write ODCS<br/>v3.1.0<br/>Schema,<br/>Quality Rules"]
        VALIDATE["Validate<br/>Schema<br/>Strict Mode"]
    end
    
    subgraph Ingest["🔄 Ingestion"]
        PARSE_YAML["Parse YAML<br/>Extract schema<br/>Generate text"]
        TO_PG["Store in<br/>PostgreSQL<br/>odcs_contracts<br/>table"]
        EMBED["Embed columns<br/>nomic-embed-text<br/>pgvector"]
    end
    
    subgraph Classify["🤖 Classification"]
        CACHE_CHECK["Check pgvector<br/>Similar<br/>columns?"]
        SEMANTIC["Semantic<br/>Similarity<br/>to FIBO"]
        LLM["LLM<br/>Reasoning<br/>DeepSeek"]
        THRESHOLD["Apply<br/>Threshold<br/>Logic"]
        STORE_CLASS["Store in<br/>PostgreSQL<br/>classification_audit"]
    end
    
    subgraph KG["🕸️ Knowledge Graph"]
        GRAPHITI["Graphiti<br/>Add Episode<br/>temporal KG"]
        NEO["Neo4j<br/>Create nodes<br/>+ relationships"]
        FIBO["FIBO Ontology<br/>Reference"]
    end
    
    subgraph Query["🔍 Query"]
        DISC["Entity<br/>Discovery<br/>Cross-contract"]
        TRAVERSE["Graph<br/>Traversal<br/>Find links"]
        AGGREGATE["Aggregate<br/>Results"]
    end
    
    subgraph Reason["🧠 Reasoning"]
        LLM_REASON["LLM<br/>Synthesize<br/>Insights"]
        RECOMMEND["Generate<br/>Recommendations"]
    end
    
    subgraph Output["📤 Output"]
        DASHBOARD["Streamlit<br/>Dashboard"]
        API["MCP Server<br/>APIs"]
        REPORT["Audit Report<br/>Compliance"]
    end
    
    DATA_PROD --> WRITE_ODCS
    WRITE_ODCS --> VALIDATE
    VALIDATE --> PARSE_YAML
    
    PARSE_YAML --> TO_PG
    PARSE_YAML --> EMBED
    TO_PG --> CACHE_CHECK
    EMBED --> CACHE_CHECK
    
    CACHE_CHECK --> SEMANTIC
    SEMANTIC --> LLM
    LLM --> THRESHOLD
    THRESHOLD --> STORE_CLASS
    
    STORE_CLASS --> GRAPHITI
    GRAPHITI --> NEO
    NEO -.->|Reference| FIBO
    
    NEO --> DISC
    STORE_CLASS --> DISC
    DISC --> TRAVERSE
    TRAVERSE --> AGGREGATE
    
    AGGREGATE --> LLM_REASON
    LLM_REASON --> RECOMMEND
    
    RECOMMEND --> DASHBOARD
    RECOMMEND --> API
    STORE_CLASS --> REPORT
    
    classDef external fill:#e1f5ff,stroke:#01579b
    classDef local fill:#fff3e0,stroke:#e65100
    classDef ML fill:#fce4ec,stroke:#880e4f
    classDef graph fill:#f3e5f5,stroke:#4a148c
    classDef ui fill:#e8f5e9,stroke:#1b5e20
    
    class DATA_PROD external
    class WRITE_ODCS,VALIDATE,PARSE_YAML,TO_PG,EMBED local
    class CACHE_CHECK,SEMANTIC,LLM,THRESHOLD,STORE_CLASS ML
    class GRAPHITI,NEO,FIBO,DISC,TRAVERSE,AGGREGATE graph
    class LLM_REASON,RECOMMEND,DASHBOARD,API,REPORT ui
```

---

**All diagrams created with Mermaid syntax. Copy-paste into any Mermaid renderer (mermaid.live, GitHub Markdown, GitLab, etc.) for visualization.**
