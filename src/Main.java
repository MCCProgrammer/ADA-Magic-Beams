import java.util.*;

/**
 * Magic Beams — ponto de entrada do programa.
 *
 * Esta classe é responsável EXCLUSIVAMENTE por:
 *  - ler o input (Scanner sobre System.in)
 *  - construir os objetos de domínio (RuneGrid, Beam[])
 *  - delegar a resolução a {@link MagicBeamSystem}
 *  - escrever o output (um resultado por linha)
 *
 * Toda a lógica de negócio reside em {@link MagicBeamSystem}.
 */
public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        StringBuilder output = new StringBuilder();

        int T = sc.nextInt();   // número de casos de teste

        for (int t = 0; t < T; t++) {
            output.append(solveTestCase(sc)).append('\n');
        }

        System.out.print(output);
    }

    /**
     * Lê um caso de teste do scanner, constrói os objetos necessários
     * e devolve a string de resultado produzida por {@link MagicBeamSystem#solve()}.
     */
    private static String solveTestCase(Scanner sc) {
        int R = sc.nextInt();   // linhas da grelha
        int C = sc.nextInt();   // colunas da grelha
        int N = sc.nextInt();   // número de colunas escolhidas
        int L = sc.nextInt();   // coluna mais à esquerda das colunas escolhidas
        int B = sc.nextInt();   // número de feixes

        RuneGrid grid  = new RuneGrid(R, C);
        Beam[]   beams = new Beam[B + 1];   // índice 0 não utilizado; ids de 1 a B

        for (int i = 1; i <= B; i++) {
            int       row    = sc.nextInt();
            int       col    = sc.nextInt();
            int       length = sc.nextInt();
            Direction dir    = Direction.fromChar(sc.next().charAt(0));

            beams[i] = new Beam(i, row, col, length, dir);
            grid.place(beams[i]);
        }

        MagicBeamSystem system = new MagicBeamSystem(grid, beams, B, L, N);
        return system.solve();
    }
}
