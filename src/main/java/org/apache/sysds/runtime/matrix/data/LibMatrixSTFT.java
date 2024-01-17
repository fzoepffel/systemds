package org.apache.sysds.runtime.matrix.data;

import java.util.Arrays;

import static org.apache.sysds.runtime.matrix.data.LibMatrixFourier.fft;

public class LibMatrixSTFT {

    public static MatrixBlock[] stft(MatrixBlock re, MatrixBlock im, int windowSize, int overlap) {
        /*
        int dim = re.getNumRows();

        double[][] in = new double[2][dim];
        in[0] = convertToArray(re);
        //in[1] = convertToArray(im);

        double[][] res = one_dim_stft(in[0], windowSize, overlap);

        return convertToMatrixBlocks(res);
        */
        int rows = re.getNumRows();
        int cols = re.getNumColumns();

        double[][] in = new double[2][cols];
        in[0] = convertToArray(re);
        in[1] = convertToArray(im);

        double[][] res = one_dim_stft(in[0], windowSize, overlap);

        return convertToMatrixBlocks(res);
    }

    public static double[][] one_dim_stft(double[] signal, int windowSize, int overlap) {
        if (windowSize < 1 || (windowSize & (windowSize - 1)) != 0) {
            throw new IllegalArgumentException("frameSize is not a power of two");
        }
        int stepSize = windowSize - overlap;
        int totalFrames = (signal.length - overlap) / stepSize;

        // Initialize the STFT output array: [time frame][frequency bin][real & imaginary parts]
        double[][] stftOutput = new double[2][totalFrames * windowSize];

        for (int frame = 0; frame < totalFrames; frame++) {
            double[] windowedSignal = new double[windowSize];


            // Apply window function (rectangular in this case)
            for (int i = 0; i < windowSize; i++) {
                int signalIndex = frame * stepSize + i;
                windowedSignal[i] = (signalIndex < signal.length) ? signal[signalIndex] : 0;
            }

            // Perform FFT on windowed signal
            int l = windowedSignal.length;

            double[] tmp_re = new double[l];
            double[] tmp_im = new double[l];

            for (int i = 0; i < l; i++) {
                tmp_re[i] = windowedSignal[i];
                tmp_im[i] = 0;
            }

            fft(tmp_re, tmp_im, 1, l);

            // Store the FFT result in the output array
            int startIndex = windowSize * frame;
            for (int i = 0; i < l; i++) {
                stftOutput[0][startIndex + i] = tmp_re[i];
                stftOutput[1][startIndex + i] = tmp_im[i];
            }
            //stftOutput[frame] = fftResult;
        }

        return stftOutput;
    }


    private static MatrixBlock[] convertToMatrixBlocks(double[][] in){


        int cols = in[0].length;

        MatrixBlock re = new MatrixBlock(1, cols, in[0]);
        MatrixBlock im = new MatrixBlock(1, cols, in[1]);

        return new MatrixBlock[]{re, im};
    }


    private static double[] convertToArray(MatrixBlock in){
        return in.getDenseBlockValues();
    }

    public static MatrixBlock[] stft(double[] in, int windowSize, int overlap){
        double[][] arr = new double[2][in.length];
        arr[0] = in;
        return stft(convertToMatrixBlocks(arr), windowSize, overlap);
    }

    public static MatrixBlock[] stft(double[][] in, int windowSize, int overlap){
        return stft(convertToMatrixBlocks(in), windowSize, overlap);
    }

    public static MatrixBlock[] stft(MatrixBlock[] in, int windowSize, int overlap){
        return stft(in[0], in[1], windowSize, overlap);
    }

}
