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
 * Algoritmos utilizados (conforme material ADA 2025/26):
 *  1. BFS para propagação transitiva das dependências (Cap. III – percurso em largura)
 *  2. Algoritmo de Kahn com PriorityQueue para ordenação topológica com desempate
 *     pelo menor id (Cap. V – Ordenação Topológica / Algoritmo de Kahn)
 */
public class MagicBeamSystem {

    // ── Constantes de output ───────────────────────────────────────────────────

    public static final String DISASTER    = "Disaster";
    public static final String FALSE_ALARM = "False alarm";

    // ── Estado do caso de teste ────────────────────────────────────────────────

    private final RuneGrid grid;
    private final Beam[]   beams;      // beams[0] não usado; ids de 1 a B
    private final int      numBeams;
    private final int      chosenLeft;  // coluna mais à esquerda do corredor
    private final int      chosenNum;   // número de colunas do corredor

    /**
     * @param grid       grelha já preenchida com todos os feixes
     * @param beams      array de feixes indexado de 1 a B (posição 0 ignorada)
     * @param numBeams   número de feixes B
     * @param chosenLeft coluna mais à esquerda das colunas escolhidas (L)
     * @param chosenNum  número de colunas escolhidas (N)
     */
    public MagicBeamSystem(RuneGrid grid, Beam[] beams, int numBeams,
                           int chosenLeft, int chosenNum) {
        this.grid       = grid;
        this.beams      = beams;
        this.numBeams   = numBeams;
        this.chosenLeft = chosenLeft;
        this.chosenNum  = chosenNum;
    }

    // ── Método principal ───────────────────────────────────────────────────────

    /**
     * Resolve o caso de teste e devolve o resultado como String pronta a imprimir.
     */
    public String solve() {
        boolean[]           mustFree = computeMustFree();
        int[]               inDegree = computeInDegree(mustFree);
        List<List<Integer>> adj      = computeAdj(mustFree);

        return kahnTopologicalSort(mustFree, inDegree, adj);
    }

    // ── Fase 1+2: determinar feixes a libertar ─────────────────────────────────

    /**
     * Determina o conjunto de feixes que precisam de ser libertados.
     *
     * Fase 1 — feixes que ocupam diretamente as colunas escolhidas.
     * Fase 2 — propagação transitiva por BFS: se X bloqueia Y (mustFree),
     *           então X também tem de ser libertado.
     *
     * Complexidade: O(B × maxLength) amortizado sobre todos os feixes.
     */
    private boolean[] computeMustFree() {
        boolean[] mustFree = new boolean[numBeams + 1];

        // Fase 1: feixes diretamente nas colunas escolhidas
        for (int i = 1; i <= numBeams; i++) {
            if (beams[i].overlapsChosenColumns(chosenLeft, chosenNum)) {
                mustFree[i] = true;
            }
        }

        // Fase 2: propagação transitiva por BFS
        Queue<Integer> queue = new LinkedList<>();
        for (int i = 1; i <= numBeams; i++) {
            if (mustFree[i]) queue.add(i);
        }

        while (!queue.isEmpty()) {
            int beamId = queue.poll();
            Beam b = beams[beamId];

            // Percorrer as células à frente da ponta do feixe b
            int r  = b.tipRow()       + b.direction().dr();
            int c  = b.tipCol()       + b.direction().dc();
            int dr = b.direction().dr();
            int dc = b.direction().dc();

            while (grid.inBounds(r, c)) {
                int occupant = grid.occupantAt(r, c);
                if (occupant != 0 && !mustFree[occupant]) {
                    mustFree[occupant] = true;
                    queue.add(occupant);
                }
                r += dr;
                c += dc;
            }
        }

        return mustFree;
    }

    // ── Fase 3: grafo de dependências ─────────────────────────────────────────

    /**
     * Constrói o vetor de graus de entrada (inDegree) para os feixes em mustFree.
     * arco j→i significa "j tem de ser libertado antes de i".
     */
    private int[] computeInDegree(boolean[] mustFree) {
        int[] inDegree = new int[numBeams + 1];

        for (int i = 1; i <= numBeams; i++) {
            if (!mustFree[i]) continue;
            Set<Integer> seen = new HashSet<>();

            int r  = beams[i].tipRow()       + beams[i].direction().dr();
            int c  = beams[i].tipCol()       + beams[i].direction().dc();
            int dr = beams[i].direction().dr();
            int dc = beams[i].direction().dc();

            while (grid.inBounds(r, c)) {
                int occupant = grid.occupantAt(r, c);
                if (occupant != 0 && occupant != i
                        && mustFree[occupant] && seen.add(occupant)) {
                    inDegree[i]++;
                }
                r += dr;
                c += dc;
            }
        }
        return inDegree;
    }

    /**
     * Constrói as listas de adjacência do grafo de dependências:
     * adj.get(j) contém os feixes que necessitam de j libertado primeiro.
     */
    private List<List<Integer>> computeAdj(boolean[] mustFree) {
        List<List<Integer>> adj = new ArrayList<>();
        for (int i = 0; i <= numBeams; i++) adj.add(new ArrayList<>());

        for (int i = 1; i <= numBeams; i++) {
            if (!mustFree[i]) continue;
            Set<Integer> seen = new HashSet<>();

            int r  = beams[i].tipRow()       + beams[i].direction().dr();
            int c  = beams[i].tipCol()       + beams[i].direction().dc();
            int dr = beams[i].direction().dr();
            int dc = beams[i].direction().dc();

            while (grid.inBounds(r, c)) {
                int occupant = grid.occupantAt(r, c);
                if (occupant != 0 && occupant != i
                        && mustFree[occupant] && seen.add(occupant)) {
                    adj.get(occupant).add(i);  // occupant → i
                }
                r += dr;
                c += dc;
            }
        }
        return adj;
    }

    // ── Fase 4: Algoritmo de Kahn ──────────────────────────────────────────────

    /**
     * Aplica o Algoritmo de Kahn (ordenação topológica) ao grafo de dependências.
     *
     * Usa uma PriorityQueue (min-heap) como "saco" de prontos, garantindo que,
     * quando existem vários feixes prontos ao mesmo tempo, se escolhe sempre
     * o de menor identificador — tal como exigido pelo enunciado.
     *
     * Devolve:
     *  - {@link #DISASTER}    se houver ciclo (nem todos os feixes foram processados)
     *  - {@link #FALSE_ALARM} se nenhum feixe precisava de ser libertado
     *  - os ids dos feixes em ordem de libertação, separados por espaço
     *
     * Complexidade temporal: Θ(|V| + |A|) onde V = feixes mustFree, A = dependências
     * Complexidade espacial: Θ(|V|)
     */
    private String kahnTopologicalSort(boolean[] mustFree,
                                       int[] inDegree,
                                       List<List<Integer>> adj) {

        // Saco de prontos: min-heap para desempate pelo menor id (Algoritmo de Kahn)
        PriorityQueue<Integer> ready = new PriorityQueue<>();
        int numToFree = 0;

        for (int i = 1; i <= numBeams; i++) {
            if (mustFree[i]) {
                numToFree++;
                if (inDegree[i] == 0) {
                    ready.add(i);
                }
            }
        }

        List<Integer> order = new ArrayList<>();

        while (!ready.isEmpty()) {
            int beamId = ready.poll();       // remove o menor id disponível
            order.add(beamId);

            for (int successor : adj.get(beamId)) {
                inDegree[successor]--;
                if (inDegree[successor] == 0) {
                    ready.add(successor);
                }
            }
        }

        // Interpretação do resultado
        if (order.size() < numToFree) {
            return DISASTER;               // ciclo detectado
        }
        if (order.isEmpty()) {
            return FALSE_ALARM;            // nenhum feixe nas colunas escolhidas
        }

        // Formatar a lista de ids separados por espaço
        StringJoiner sj = new StringJoiner(" ");
        for (int id : order) sj.add(String.valueOf(id));
        return sj.toString();
    }
}
