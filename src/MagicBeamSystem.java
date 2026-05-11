import java.util.*;

/**
 * Contém toda a lógica de negócio do problema Magic Beams.
 *
 * Dado um caso de teste (grelha, colunas escolhidas, feixes),
 * o método {@link #solve()} devolve o resultado como String:
 *  - {@link #DISASTER}    se as colunas não podem ser limpas (ciclo de dependências)
 *  - {@link #FALSE_ALARM} se nenhum feixe precisa de ser libertado
 *  - Os ids dos feixes a libertar, por ordem, separados por espaço
 *
 * ═══════════════════════════════════════════════════════════════════════
 * COMPLEXIDADE TEMPORAL — análise por caso de teste
 * ═══════════════════════════════════════════════════════════════════════
 *
 * Sejam:
 *   B   = número de feixes
 *   R,C = dimensões da grelha  (R,C ≤ 200)
 *   G   = max(R,C)             comprimento máximo de um caminho de saída
 *   V   = |feixes mustFree|    ≤ B
 *   A   = |arcos no grafo de dependências|
 *
 * Fase 1 (mustFree direto):
 *   Para cada feixe percorre-se o seu corpo (comprimento ≤ 20):  O(B)
 *
 * Fases 2+3 fundidas — BFS + construção do grafo:
 *   Cada feixe é enfileirado no BFS no máximo uma vez.
 *   Quando processado, percorre-se o seu caminho de saída (≤ G células).
 *   Durante essa passagem única constroem-se simultaneamente mustFree,
 *   inDegree[] e adj[].
 *   Total: O(V × G)  com V ≤ B  →  O(B × G)  no pior caso.
 *
 *   A deduplicação de arestas usa um array boolean[] seen de tamanho B+1
 *   reiniciado com uma lista de "sujados" — custo O(arcos encontrados)
 *   sem qualquer alocação por feixe.
 *
 * Fase 4 — Algoritmo de Kahn com PriorityQueue:
 *   O(V log V + A)
 *
 * TOTAL: O(B × G + A + V log V)  =  O(B × max(R,C))
 *
 * É o mínimo possível?
 *   Sim: cada célula do caminho de saída de cada feixe tem de ser examinada
 *   pelo menos uma vez → Ω(B × G).  O Kahn é ótimo para ordenação topológica.
 *
 * Algoritmos utilizados (conforme material ADA 2025/26):
 *   Cap. III — BFS para propagação transitiva
 *   Cap. V   — Algoritmo de Kahn para ordenação topológica
 * ═══════════════════════════════════════════════════════════════════════
 */
public class MagicBeamSystem {

    // ── Constantes de output ───────────────────────────────────────────────────

    public static final String DISASTER    = "Disaster";
    public static final String FALSE_ALARM = "False alarm";

    // ── Estado do caso de teste ────────────────────────────────────────────────

    private final RuneGrid grid;
    private final Beam[]   beams;       // beams[0] não usado; ids de 1 a B
    private final int      numBeams;
    private final int      chosenLeft;  // coluna mais à esquerda do corredor (L)
    private final int      chosenNum;   // número de colunas do corredor (N)

    public MagicBeamSystem(RuneGrid grid, Beam[] beams, int numBeams,
                           int chosenLeft, int chosenNum) {
        this.grid       = grid;
        this.beams      = beams;
        this.numBeams   = numBeams;
        this.chosenLeft = chosenLeft;
        this.chosenNum  = chosenNum;
    }

    // ── Método principal ───────────────────────────────────────────────────────

    public String solve() {
        DependencyGraph dg = buildDependencyGraph();
        return kahnTopologicalSort(dg);
    }

    // ── Fases 1 + 2 + 3 fundidas num único BFS ────────────────────────────────

    /**
     * Constrói o grafo de dependências em três fases executadas numa única passagem BFS.
     *
     * Fase 1 — identifica feixes que ocupam diretamente as colunas escolhidas
     *           e insere-os na fila BFS.
     * Fase 2 — ao processar feixe Y, percorre o seu caminho de saída;
     *           qualquer feixe X encontrado que ainda não seja mustFree
     *           passa a sê-lo e é enfileirado (propagação transitiva).
     * Fase 3 — durante o mesmo percurso, para cada X já mustFree,
     *           regista-se o arco X→Y no grafo (X deve ser libertado antes de Y).
     *
     * Deduplicação de arestas sem HashSet:
     *   O array boolean[] seen (tamanho B+1) é partilhado entre todos os feixes.
     *   Após processar Y, apenas as entradas usadas são limpas, percorrendo
     *   a lista "dirty" — custo O(arcos deste feixe), sem realocar estruturas.
     *
     * Complexidade: O(B × G)  onde G = max(R, C)
     */
    private DependencyGraph buildDependencyGraph() {
        boolean[]           mustFree = new boolean[numBeams + 1];
        int[]               inDegree = new int[numBeams + 1];
        List<List<Integer>> adj      = new ArrayList<>(numBeams + 1);
        for (int i = 0; i <= numBeams; i++) adj.add(new ArrayList<>());

        // Reutilizado por todos os feixes para deduplicar arestas sem realocar
        boolean[]    seen  = new boolean[numBeams + 1];
        List<Integer> dirty = new ArrayList<>();

        // ── Fase 1: feixes que ocupam diretamente as colunas escolhidas ──────
        Queue<Integer> queue = new ArrayDeque<>();
        for (int i = 1; i <= numBeams; i++) {
            if (beams[i].overlapsChosenColumns(chosenLeft, chosenNum)) {
                mustFree[i] = true;
                queue.add(i);
            }
        }

        // ── Fases 2 + 3: BFS único ────────────────────────────────────────────
        while (!queue.isEmpty()) {
            int  beamId = queue.poll();
            Beam b      = beams[beamId];
            int  dr     = b.direction().dr();
            int  dc     = b.direction().dc();

            // Percorrer o caminho de saída a partir da célula após a ponta
            int r = b.tipRow() + dr;
            int c = b.tipCol() + dc;

            dirty.clear();

            while (grid.inBounds(r, c)) {
                int occupant = grid.occupantAt(r, c);

                if (occupant != 0 && occupant != beamId) {

                    // Fase 2: propagar mustFree transitivamente
                    if (!mustFree[occupant]) {
                        mustFree[occupant] = true;
                        queue.add(occupant);
                    }

                    // Fase 3: registar arco occupant→beamId (sem duplicados)
                    if (!seen[occupant]) {
                        seen[occupant] = true;
                        dirty.add(occupant);
                        adj.get(occupant).add(beamId);
                        inDegree[beamId]++;
                    }
                }
                r += dr;
                c += dc;
            }

            // Repor seen[] apenas nas posições usadas — O(|dirty|) sem realocar
            for (int id : dirty) seen[id] = false;
        }

        return new DependencyGraph(mustFree, inDegree, adj);
    }

    // ── Fase 4: Algoritmo de Kahn ──────────────────────────────────────────────

    /**
     * Aplica o Algoritmo de Kahn (ordenação topológica) ao grafo de dependências.
     *
     * Usa uma PriorityQueue (min-heap) como "saco" de prontos, garantindo que,
     * quando existem vários feixes prontos ao mesmo tempo, se escolhe sempre
     * o de menor identificador — tal como exigido pelo enunciado.
     *
     * Complexidade temporal: O(V log V + A)
     * Complexidade espacial: O(V)
     */
    private String kahnTopologicalSort(DependencyGraph dg) {
        boolean[]           mustFree = dg.mustFree();
        int[]               inDegree = dg.inDegree();
        List<List<Integer>> adj      = dg.adj();

        PriorityQueue<Integer> ready = new PriorityQueue<>();
        int numToFree = 0;

        for (int i = 1; i <= numBeams; i++) {
            if (mustFree[i]) {
                numToFree++;
                if (inDegree[i] == 0) ready.add(i);
            }
        }

        List<Integer> order = new ArrayList<>(numToFree);

        while (!ready.isEmpty()) {
            int beamId = ready.poll();
            order.add(beamId);

            for (int successor : adj.get(beamId)) {
                if (--inDegree[successor] == 0) ready.add(successor);
            }
        }

        if (order.size() < numToFree) return DISASTER;
        if (order.isEmpty())          return FALSE_ALARM;

        StringJoiner sj = new StringJoiner(" ");
        for (int id : order) sj.add(String.valueOf(id));
        return sj.toString();
    }

    // ── Objeto de transferência entre fases ───────────────────────────────────

    /**
     * Agrupa os três arrays que descrevem o grafo de dependências,
     * evitando retornar múltiplos valores de buildDependencyGraph().
     */
    private record DependencyGraph(
            boolean[]           mustFree,
            int[]               inDegree,
            List<List<Integer>> adj) {}
}