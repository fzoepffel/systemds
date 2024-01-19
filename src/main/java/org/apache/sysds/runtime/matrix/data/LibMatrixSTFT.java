/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sysds.runtime.matrix.data;

import static org.apache.sysds.runtime.matrix.data.LibMatrixFourier.fft;

public class LibMatrixSTFT {

    public static MatrixBlock[] stft(MatrixBlock re, MatrixBlock im, int windowSize, int overlap) {

        int cols = re.getNumColumns();

        double[]in_re = new double[cols];
        in_re = convertToArray(re);
        //in[1] = convertToArray(im);

       MatrixBlock[] out = one_dim_stft(in_re, windowSize, overlap);

        return out;
    }

    public static MatrixBlock[] one_dim_stft(double[] signal, int windowSize, int overlap) {

        if (windowSize < 1 || (windowSize & (windowSize - 1)) != 0) {
            throw new IllegalArgumentException("frameSize is not a power of two");
        }
        int stepSize = windowSize - overlap;
        int totalFrames = (signal.length - overlap) / stepSize;

        // Initialize the STFT output array: [time frame][frequency bin][real & imaginary parts]
        double[] stftOutput_re = new double[totalFrames * windowSize];
        double[] stftOutput_im = new double[totalFrames * windowSize];

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
                stftOutput_re[startIndex + i] = tmp_re[i];
                stftOutput_im[startIndex + i] = tmp_im[i];
            }
            //stftOutput[frame] = fftResult;
        }

        return convertToMatrixBlocks(stftOutput_re, stftOutput_im);
    }

    private static MatrixBlock[] convertToMatrixBlocks(double[] in_re, double[] in_im){

        int cols = in_re.length;

        MatrixBlock re = new MatrixBlock(1, cols, in_re);
        MatrixBlock im = new MatrixBlock(1, cols, in_im);

        return new MatrixBlock[]{re, im};
    }

    private static double[] convertToArray(MatrixBlock in){
        return in.getDenseBlockValues();
    }

    public static MatrixBlock[] stft(double[] in, int windowSize, int overlap){
        double[] in_im = new double[in.length];
        return stft(convertToMatrixBlocks(in, in_im), windowSize, overlap);
    }

    public static MatrixBlock[] stft(MatrixBlock[] in, int windowSize, int overlap){
        return stft(in[0], in[1], windowSize, overlap);
    }

}