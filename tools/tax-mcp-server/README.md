# Tax MCP server (moved)

The Node.js stdio MCP server was replaced by the Spring AI module:

**`openlifeops-tax-mcp-server/`** — Streamable HTTP MCP server (`spring.ai.mcp.server.protocol=STREAMABLE`).

Run it:

```powershell
cd openlifeops
.\mvnw.cmd -pl openlifeops-tax-mcp-server spring-boot:run
```

Listens on **http://localhost:8090/mcp** by default.
