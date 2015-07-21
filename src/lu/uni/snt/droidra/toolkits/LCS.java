package lu.uni.snt.droidra.toolkits;

public class LCS {

    public static void main(String[] args) {
    	
    	StringBuilder sb1 = new StringBuilder();
    	sb1.append(Character.toChars((int) 'a'));
    	sb1.append(Character.toChars((int) 'b'));
    	sb1.append(Character.toChars((int) 'c'));
    	
    	StringBuilder sb2 = new StringBuilder();
    	sb2.append(Character.toChars((int) 'a'));
    	sb2.append(Character.toChars((int) 'c'));
    	
        String x =  sb1.toString();
        String y = sb2.toString();
        int M = x.length();
        int N = y.length();

        // opt[i][j] = length of LCS of x[i..M] and y[j..N]
        int[][] opt = new int[M+1][N+1];

        // compute length of LCS and all subproblems via dynamic programming
        for (int i = M-1; i >= 0; i--) {
            for (int j = N-1; j >= 0; j--) {
                if (x.charAt(i) == y.charAt(j))
                    opt[i][j] = opt[i+1][j+1] + 1;
                else 
                    opt[i][j] = Math.max(opt[i+1][j], opt[i][j+1]);
            }
        }

        // recover LCS itself and print it to standard output
        int i = 0, j = 0;
        while(i < M && j < N) {
            if (x.charAt(i) == y.charAt(j)) {
                System.out.print(x.charAt(i));
                i++;
                j++;
            }
            else if (opt[i+1][j] >= opt[i][j+1]) i++;
            else                                 j++;
        }
        System.out.println();

    }

}
