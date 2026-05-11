/**
 * Grelha de runas onde os feixes mágicos estão posicionados.
 *
 * Mantém uma matriz rows×cols cujas células guardam o identificador
 * do feixe que as ocupa (0 significa célula vazia).
 * Permite colocar feixes e consultar qual feixe ocupa uma dada célula.
 */
public class RuneGrid {

    private final int rows;
    private final int cols;

    /** grid[r][c] == id do feixe que ocupa (r,c), ou 0 se vazio. */
    private final int[][] grid;

    public RuneGrid(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new int[rows][cols];
    }

    // ── Colocação ──────────────────────────────────────────────────────────────

    /**
     * Regista na grelha todas as células ocupadas pelo feixe dado.
     * Deve ser chamado uma vez por feixe, após a sua criação.
     */
    public void place(Beam beam) {
        int r  = beam.startRow();
        int c  = beam.startCol();
        int dr = beam.direction().dr();
        int dc = beam.direction().dc();

        for (int k = 0; k < beam.length(); k++) {
            grid[r][c] = beam.id();
            r += dr;
            c += dc;
        }
    }

    // ── Consulta ───────────────────────────────────────────────────────────────

    /**
     * Devolve o id do feixe que ocupa a célula (r, c), ou 0 se estiver vazia.
     */
    public int occupantAt(int r, int c) {
        return grid[r][c];
    }

    /**
     * Indica se a célula (r, c) existe dentro dos limites da grelha.
     */
    public boolean inBounds(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    public int rows() { return rows; }
    public int cols() { return cols; }
}
