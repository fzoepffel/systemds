package org.apache.sysds.runtime.matrix.data;

public class LibMatrixFourier {

    public static ComplexDouble[] czt(ComplexDouble[] x, int m) {
        int n = x.length;
        ComplexDouble w = new ComplexDouble(Math.cos(-2 * Math.PI / m), Math.sin(-2 * Math.PI / m));
        ComplexDouble a = new ComplexDouble(1, 0);

        ComplexDouble[] chirp = new ComplexDouble[Math.max(m, n)];
        for (int i = 1 - n; i < Math.max(m, n); i++) {
            chirp[i] = w.pow(i * i / 2.0);
        }

        ComplexDouble[] xp = new ComplexDouble[m];
        for (int i = 0; i < n; i++) {
            xp[i] = x[i].multiply(a.pow(-i)).multiply(chirp[n - 1 + i]);
        }

        ComplexDouble[] ichirp = new ComplexDouble[m];
        for (int i = 0; i < m + n - 1; i++) {
            ichirp[i] = chirp[i].reciprocal();
        }

        ComplexDouble[] fftXp = fft(xp);
        ComplexDouble[] fftIchirp = fft(ichirp);

        ComplexDouble[] product = new ComplexDouble[fftXp.length];
        for (int i = 0; i < fftXp.length; i++) {
            product[i] = fftXp[i].multiply(fftIchirp[i]);
        }

        ComplexDouble[] r = ifft(product);

        ComplexDouble[] result = new ComplexDouble[m];
        for (int i = n - 1; i < m + n - 1; i++) {
            result[i] = r[i].multiply(chirp[n - 1 + i]);
        }

        return result;
    }

    public static ComplexDouble[] ifft(ComplexDouble[] in){
        int n = in.length;
    
        ComplexDouble[] conjugated = new ComplexDouble[n];
        for(int i = 0; i < n; i++){
            conjugated[i] = in[i].conjugate();
        }

        ComplexDouble[] fftResult = fft(conjugated);
    
        ComplexDouble[] ifftResult = new ComplexDouble[n];
        for(int i = 0; i < n; i++){
            ifftResult[i] = fftResult[i].conjugate().divide(n);
        }
    
        return ifftResult;
    }
    

    /**
     * Function to perform Fast Fourier Transformation on a given array.
     * Its length has to be a power of two.
     *
     * @param in array of ComplexDoubles
     * @return array of ComplexDoubles
     */
    public static ComplexDouble[] fft(ComplexDouble[] in) {
        int n = in.length;
    
        // Check if the length is a power of two
        if (isPowerOfTwo(n)) {
            // Existing FFT implementation for power-of-two lengths
            if (n == 1) {
                return in;
            }
    
            double angle = 2 * Math.PI / n;
            ComplexDouble omega = new ComplexDouble(Math.cos(angle), Math.sin(angle));
    
            ComplexDouble[] even = new ComplexDouble[n / 2];
            ComplexDouble[] odd = new ComplexDouble[n / 2];
            for (int i = 0; i < n / 2; i++) {
                even[i] = in[i * 2];
                odd[i] = in[i * 2 + 1];
            }
    
            ComplexDouble[] resEven = fft(even);
            ComplexDouble[] resOdd = fft(odd);
    
            ComplexDouble[] res = new ComplexDouble[n];
            for (int j = 0; j < n / 2; j++) {
                res[j] = resEven[j].add(omega.pow(j).mul(resOdd[j]));
                res[j + n / 2] = resEven[j].sub(omega.pow(j).mul(resOdd[j]));
            }
            return res;
        } else {
            // Use CZT for non-power-of-two lengths
            return czt(in, n);
        }
    }
    
    private static boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }
    

    /**
     * Function to perform Fast Fourier Transformation on a given array.
     * Its length has to be a power of two.
     *
     * @param in array of doubles
     * @return array of ComplexDoubles
     */
    public static ComplexDouble[] fft(double[] in){
        ComplexDouble[] complex = new ComplexDouble[in.length];
        for(int i=0; i<in.length; i++){
            complex[i] = new ComplexDouble(in[i],0);
        }
        return fft(complex);
    }

    /**
     * Function to fill a given array of ComplexDoubles with 0-ComplexDoubles, so that the length is a power of two.
     * Needed for FastFourierTransformation
     *
     * @param in array of ComplexDoubles
     * @return array of ComplexDoubles
     */
    private static ComplexDouble[] fillToPowerOfTwo(ComplexDouble[] in){
        int missing = nextPowerOfTwo(in.length)- in.length;
        ComplexDouble[] res = new ComplexDouble[in.length+missing];
        for(int i=0; i<in.length; i++){
            res[i] = in[i];
        }
        for(int j=0; j<missing; j++){
            res[in.length+j] = new ComplexDouble(0,0);
        }
        return res;
    }

    /**
     * Function for calculating the next larger int which is a power of two
     *
     * @param n integer
     * @return next larger int which is a power of two
     */
    private static int nextPowerOfTwo(int n){
        int res = 1;
        while (res < n){
            res = res << 1;
        }
        return res;
    }

    /**
     * Function to perform Fast Fourier Transformation in a 2-dimensional array.
     * Both dimensions have to be a power of two.
     * First fft is applied to each row, then fft is applied to each column of the previous result.
     *
     * @param in 2-dimensional array of ComplexDoubles
     * @return 2-dimensional array of ComplexDoubles
     */
    public static ComplexDouble[][] fft2d(ComplexDouble[][] in) {

        int rows = in.length;
        int cols = in[0].length;

        ComplexDouble[][] out = new ComplexDouble[rows][cols];

        for(int i = 0; i < rows; i++){
            // use fft on row
            out[i] = fft(in[i]);
        }

        for(int j = 0; j < cols; j++){
            // get col as array
            ComplexDouble[] inCol = new ComplexDouble[rows];
            for(int i = 0; i < rows; i++){
                inCol[i] = out[i][j];
            }
            // use fft on col
            ComplexDouble[] resCol = fft(inCol);
            for (int i = 0; i < rows; i++) {
                out[i][j] = resCol[i];
            }
        }

        return out;
    }

    /**
     * Function to perform Fast Fourier Transformation in a 2-dimensional array.
     * Both dimensions have to be a power of two.
     * First fft is applied to each row, then fft is applied to each column of the previous result.
     *
     * @param in 2-dimensional array of doubles
     * @return 2-dimensional array of ComplexDoubles
     */
    public static ComplexDouble[][] fft2d(double[][] in){
        int rows = in.length;
        int cols = in[0].length;

        ComplexDouble[][] complex = new ComplexDouble[rows][cols];
        for(int i=0; i<rows; i++){
            for(int j=0; j<cols; j++){
                complex[i][j] = new ComplexDouble(in[i][j],0);
            }
        }
        return fft2d(complex);
    }


}
