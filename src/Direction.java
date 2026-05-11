/**
 * Representa as quatro direções cardeais possíveis para um feixe mágico.
 * Cada direção encapsula os deltas de linha (dr) e coluna (dc)
 * que descrevem o movimento célula a célula nessa direção.
 */
public enum Direction {

    N(-1,  0),
    S( 1,  0),
    E( 0,  1),
    W( 0, -1);

    private final int dr;
    private final int dc;

    Direction(int dr, int dc) {
        this.dr = dr;
        this.dc = dc;
    }

    /** Delta de linha por passo nesta direção. */
    public int dr() { return dr; }

    /** Delta de coluna por passo nesta direção. */
    public int dc() { return dc; }

    /** Converte o caráter lido do input ('N','S','E','W') para a direção correspondente. */
    public static Direction fromChar(char c) {
        return switch (c) {
            case 'N' -> N;
            case 'S' -> S;
            case 'E' -> E;
            case 'W' -> W;
            default  -> throw new IllegalArgumentException("Direção inválida: " + c);
        };
    }
}
