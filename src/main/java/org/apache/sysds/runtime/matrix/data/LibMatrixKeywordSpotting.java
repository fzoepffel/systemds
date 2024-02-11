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

import org.apache.sysds.runtime.io.ReaderWavFile;

import java.io.*;
import java.net.URL;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class LibMatrixKeywordSpotting {

	List<double[]> samples = new ArrayList<>();
	//List<String> labels = new ArrayList<>();
	List<double[]> spectrograms = new ArrayList<>(); // Store spectrograms for writing

	public LibMatrixKeywordSpotting() {
		loadAllData();
		convertToSpectrogramsAndSave();
	}

	private void convertToSpectrogramsAndSave() {
		for (int i = 0; i < samples.size(); i++) {
			double[] wave = samples.get(i);
			//double[] magnitudes = convertWaveToMagnitudesSpectrogram(wave);
			spectrograms.add(wave); // Add spectrogram data for saving
		}
		// Now, write the spectrogram data to a text file
		saveSpectrogramsToCSVFile("yes.csv");
	}

	private void saveSpectrogramsToCSVFile(String fileName) {
		try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName)))) {

			for (int i = 0; i < spectrograms.size(); i++) {

				// String label = labels.get(i).substring(0, labels.get(i).lastIndexOf('.'));
				double[] spectrogram = spectrograms.get(i);
				if (spectrogram.length > 0) {
					out.print(spectrogram[0]);
					for (int j = 1; j < spectrogram.length; j++) {
						out.print("," + spectrogram[j]);
					}
				}
				out.println();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadAllData() {
		String folderPath = "./yes";
		File dir = new File(folderPath);


		if (dir.exists() && dir.isDirectory()) {
			File[] files = dir.listFiles((d, name) -> name.endsWith(".wav"));
			if (files != null) {

				for (File file : files) {

					try {

						double[] data = ReaderWavFile.readMonoAudioFromWavFile(file.getAbsolutePath());

						if (data != null) {
							samples.add(data);
							//labels.add(file.getName());
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			System.out.println("Directory does not exist: " + folderPath);
		}
	}

	public static void main(String[] args) {
		new LibMatrixKeywordSpotting();
		System.out.println("Spectrogram processing complete.");
	}
}
