/**
 * Representa um feixe mágico na grelha de runas.
 *
 * Cada feixe tem:
 *  - um identificador único (id), de 1 a B
 *  - a célula de origem (startRow, startCol)
 *  - um comprimento (length) em células
 *  - uma direção (direction) de deslocamento
 *
 * A partir destes dados calculam-se as células que o feixe ocupa
 * e a "ponta de saída" (a última célula na direção de movimento),
 * que é o ponto a partir do qual se verifica o caminho livre ao libertar.
 */
public class Beam {

    private final int id;
    private final int startRow;
    private final int startCol;
    private final int length;
    private final Direction direction;

    public Beam(int id, int startRow, int startCol, int length, Direction direction) {
        this.id        = id;
        this.startRow  = startRow;
        this.startCol  = startCol;
        this.length    = length;
        this.direction = direction;
    }

    // ── Getters básicos ────────────────────────────────────────────────────────

    public int       id()        { return id;        }
    public int       startRow()  { return startRow;  }
    public int       startCol()  { return startCol;  }
    public int       length()    { return length;    }
    public Direction direction() { return direction; }

    // ── Geometria ──────────────────────────────────────────────────────────────

    /**
     * Linha da última célula ocupada pelo feixe (ponta na direção de movimento).
     * É a partir daqui + 1 passo que se verifica se o caminho de saída está livre.
     */
    public int tipRow() {
        return startRow + direction.dr() * (length - 1);
    }

    /**
     * Coluna da última célula ocupada pelo feixe (ponta na direção de movimento).
     */
    public int tipCol() {
        return startCol + direction.dc() * (length - 1);
    }

    /**
     * Indica se a célula (row, col) é ocupada por este feixe.
     * Útil para marcar a grelha e para verificações de contenção.
     */
    public boolean occupies(int row, int col) {
        int dr = direction.dr(), dc = direction.dc();
        int r = startRow, c = startCol;
        for (int k = 0; k < length; k++) {
            if (r == row && c == col) return true;
            r += dr;
            c += dc;
        }
        return false;
    }

    /**
     * Indica se alguma célula ocupada por este feixe pertence ao
     * intervalo de colunas escolhidas [leftCol, leftCol + numCols - 1].
     */
    public boolean overlapsChosenColumns(int leftCol, int numCols) {
        int dr = direction.dr(), dc = direction.dc();
        int r = startRow, c = startCol;
        for (int k = 0; k < length; k++) {
            if (c >= leftCol && c < leftCol + numCols) return true;
            r += dr;
            c += dc;
        }
        return false;
    }
}
