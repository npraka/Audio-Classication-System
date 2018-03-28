import java.awt.GridLayout;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import src.com.musicg.math.rank.ArrayRankDouble;
import src.com.musicg.representation.timedomain.AmplitudeTimeDomainRepresentation;
import src.com.musicg.representation.timedomain.FrequencyTimeDomainRepresentation;
import src.com.musicg.representation.timedomain.TimeDomainRepresentation;
import src.com.musicg.math.statistics.*;
import src.com.musicg.pitch.PitchHandler;
import src.com.musicg.dsp.FastFourierTransform;
import src.com.musicg.wave.WaveHeader;
import src.com.musicg.wave.WaveInputStream;
import src.com.musicg.wave.Wave;

/*
 * Assignment 4
 * CSS 490
 * Jason Kozodoy, Conor Van Achte, Neil Prakasam
 */
public class Audio extends JFrame {
	Runtime rt = Runtime.getRuntime();
	Process pr;

	private JScrollPane scroller;
	File[] totalRecordings = new File[41];
	private JTextArea output = new JTextArea(12, 60);

	private AudioInputStream[] AudioStream;
	private AudioFormat[] AudioFormat;

	private JLabel photographLabel = new JLabel();
	private JButton[] button;
	private int[] buttonOrder = new int[101];
	private GridLayout gridLayout1;
	private GridLayout gridLayout2;
	private GridLayout gridLayout3;
	private GridLayout gridLayout4;
	private JPanel panelBottom1;
	private JPanel panelBottom2;
	private JPanel panelTop;
	private JPanel buttonPanel;
	int picNo = 0;
	int imageCount = 1;
	int pageNo = 1;

	PrintWriter writer;

	/*
	 * Main method that runs the application.
	 */
	public static void main(String args[]) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Audio app = new Audio();
				app.setVisible(true);

			}
		});
	}

	/*
	 * Constructor that creates the User Interface and creates instances of J48,
	 * SMO (Sequential Minimal Optimization), Zero R, and Naive Bayes that are
	 * used as extraction techniques.
	 */
	public Audio() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Audio Analyzer");
		setSize(1100, 750);
		setResizable(true);
		panelBottom1 = new JPanel();
		panelBottom2 = new JPanel();
		panelTop = new JPanel();
		buttonPanel = new JPanel();
		gridLayout1 = new GridLayout(4, 5, 5, 5);
		gridLayout2 = new GridLayout(2, 1, 5, 5);
		gridLayout3 = new GridLayout(1, 2, 5, 5);
		gridLayout4 = new GridLayout(2, 1, 1, 1);
		setLayout(gridLayout2);
		panelBottom1.setLayout(gridLayout1);
		panelBottom2.setLayout(gridLayout1);
		panelTop.setLayout(gridLayout3);
		add(panelTop);
		add(panelBottom1);
		photographLabel.setVerticalTextPosition(JLabel.BOTTOM);
		photographLabel.setHorizontalTextPosition(JLabel.CENTER);
		photographLabel.setHorizontalAlignment(JLabel.CENTER);
		photographLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		buttonPanel.setLayout(gridLayout4);

		AudioStream = new AudioInputStream[41];
		AudioFormat = new AudioFormat[41];

		JButton clear = new JButton("Clear Output");
		JButton previousPage = new JButton("Previous Page");
		JButton nextPage = new JButton("Next Page");
		JButton j48 = new JButton("J48");
		JButton smo = new JButton("SMO");
		JButton zeroR = new JButton("Zero R");
		JButton naiveBayes = new JButton("Naive Bayes");
		clear.addActionListener(new clearListener());
		j48.addActionListener(new j48Listener());
		smo.addActionListener(new smoListener());
		zeroR.addActionListener(new zeroRListener());
		naiveBayes.addActionListener(new naiveBayesListener());

		setLocationRelativeTo(null);
		button = new JButton[41];

		for (int i = 1; i < 21; i++) {
			totalRecordings[i] = new File("src/Audio/music/mu" + i + ".wav");
		}

		for (int i = 21; i < 41; i++) {
			totalRecordings[i] = new File("src/Audio/music/sp" + (i - 20)
					+ ".wav");
		}

		for (int i = 1; i < 41; i++) {
			button[i] = new JButton();
			button[i].addActionListener(new IconButtonHandler(i));
			buttonOrder[i] = i;
		}

		try {
			writer = new PrintWriter("AudioFeatures.arff");
			configureFile();
		} catch (Exception e) {
		}

		getMusicData();
		getSpeechData();

		writer.close();

		scroller = new JScrollPane(output,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		panelTop.add(scroller);
		panelTop.add(buttonPanel);

		buttonPanel.add(j48);
		buttonPanel.add(smo);
		buttonPanel.add(zeroR);
		buttonPanel.add(naiveBayes);
		buttonPanel.add(previousPage);
		buttonPanel.add(nextPage);
		buttonPanel.add(clear);
		nextPage.addActionListener(new nextPageHandler());
		previousPage.addActionListener(new previousPageHandler());

		displayFirstPage();
	}// end constructor

	/*
	 * This method displays the first 20 music files that the user can view and
	 * play.
	 */
	private void displayFirstPage() {
		int imageButNo = 0;

		panelBottom1.removeAll();
		for (int i = 1; i < 21; i++) {
			String fileName = totalRecordings[i].getName();

			imageButNo = buttonOrder[i];
			button[i].setText(fileName);
			panelBottom1.add(button[imageCount]);
			imageCount++;
		}// end for
		panelBottom1.revalidate();
		panelBottom1.repaint();

	}// end displayFirstPage

	/*
	 * This class implements an ActionListener for the nextPageButton. The next
	 * 20 speech files are displayed when the user clicks the next page button.
	 */
	private class nextPageHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			int imageButNo = 0;
			int endImage = imageCount + 20;
			if (endImage <= 41) { // 41
				panelBottom1.removeAll();
				for (int i = imageCount; i < endImage; i++) {
					String fileName = totalRecordings[i].getName();

					imageButNo = buttonOrder[i];
					button[i].setText(fileName);
					panelBottom1.add(button[imageButNo]);
					imageCount++;
				}// end for
				panelBottom1.revalidate();
				panelBottom1.repaint();
			}// end if
		}// end actionPerformed
	}// end nextPageHandler

	/*
	 * This class implements an ActionListener for the previousPageButton. The
	 * previous 20 music files are displayed for the user to view and play.
	 */
	private class previousPageHandler implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			int imageButNo = 0;
			int startImage = imageCount - 40;
			int endImage = imageCount - 20;
			if (startImage >= 1) {
				panelBottom1.removeAll();
				/*
				 * The for loop goes through the buttonOrder array starting with
				 * the startImage value and retrieves the image at that place
				 * and then adds the button to the panelBottom1.
				 */
				for (int i = startImage; i < endImage; i++) {
					imageButNo = buttonOrder[i];
					panelBottom1.add(button[imageButNo]);
					imageCount--;
				}// end for
				panelBottom1.revalidate();
				panelBottom1.repaint();
			}// end if
		}// end actionPerformed
	}// end previousPageHandler

	/*
	 * This class implements an ActionListener for each iconButton. When an icon
	 * button is clicked, the music or speech file plays.
	 */
	private class IconButtonHandler implements ActionListener {
		int pNo = 0;
		//ImageIcon iconUsed;

		IconButtonHandler(int i) { // This is what populates
			pNo = i;
		}// end IconButtonHandler constructor

		public void actionPerformed(ActionEvent e) { // This is what sets the
														// selected icon
			AudioInputStream AudioIn;

			try {

				if (pNo > 20) {
					pNo -= 20;
					AudioIn = AudioSystem.getAudioInputStream(Audio.class
							.getResource("Audio/speech/sp" + pNo + ".wav"));
					Clip clip = AudioSystem.getClip();
					clip.open(AudioIn);
					clip.start();
					pNo += 20;
				} else {
					AudioIn = AudioSystem.getAudioInputStream(Audio.class
							.getResource("Audio/music/mu" + pNo + ".wav"));
					Clip clip = AudioSystem.getClip();
					clip.open(AudioIn);
					clip.start();
				}

			} catch (Exception eb) {
				System.out.println(eb.getMessage());
			}

		}// end actionPerformed
	}// end iconButtonHandler

	/*
	 * Writes the attribute list in .arff format so that weka could interpret
	 * the values properly.
	 */
	public void configureFile() {
		writer.println("@relation jAudio");
		writer.print("@ATTRIBUTE ");
		writer.println("\"Audio Format\" NUMERIC");
		writer.print("@ATTRIBUTE ");
		writer.println("\"Sample Rate\" NUMERIC");
		writer.print("@ATTRIBUTE ");
		writer.println("\"Bits Per Sample\" NUMERIC");
		writer.print("@ATTRIBUTE ");
		writer.println("\"Block Align\" NUMERIC");
		writer.print("@ATTRIBUTE ");
		writer.println("\"Byte Rate\" NUMERIC");
		writer.print("@ATTRIBUTE ");
		writer.println("\"Channels\" NUMERIC");
		writer.print("@ATTRIBUTE ");
		writer.println("\"Chunk Size\" NUMERIC");
		writer.print("@ATTRIBUTE ");
		writer.println("\"FFT Sample Size\" NUMERIC");
		writer.print("@ATTRIBUTE ");
		writer.println("\"Frames Per Second\" NUMERIC");
		writer.print("@ATTRIBUTE ");
		writer.println("\"Number of Frames\" NUMERIC");
		writer.print("@ATTRIBUTE ");
		writer.println("\"Number of Frequency Units\" NUMERIC");
		writer.print("@ATTRIBUTE ");
		writer.println("\"Unit Frequency\" NUMERIC");
		writer.print("@ATTRIBUTE ");
		writer.println("\"Time Step\" NUMERIC");
		writer.print("@ATTRIBUTE ");
		writer.println("\"Maximum Amplitude\" NUMERIC");
		writer.print("@ATTRIBUTE ");
		writer.println("\"Minimum Amplitutde\" NUMERIC");
		writer.print("@ATTRIBUTE ");
		writer.println("\"Mean\" NUMERIC");
		writer.print("@ATTRIBUTE ");
		writer.println("\"Zero Crossing Rate\" NUMERIC");
		writer.print("@ATTRIBUTE ");
		writer.println("\"Standard Deviation\" NUMERIC");
		writer.print("@ATTRIBUTE");
		writer.println("\"Spectral Centroid\" NUMERIC");
		writer.print("@ATTRIBUTE ");
		writer.println("\"Audio Type\" {MUSIC,SPEECH}");
		writer.println("@DATA");
	}// end configureFile

	/*
	 * Displays the findings to the screen.
	 */
	public void displayFindings() {
		// output
		BufferedReader input = new BufferedReader(new InputStreamReader(
				pr.getInputStream()));

		String line = null;
		try {
			while ((line = input.readLine()) != null) {
				output.append(line + "\n");
			}// end while
		} catch (IOException ioe) {
		}
	}

	/*
	 * Clears away the output in the output display.
	 */
	private class clearListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			output.setText(" ");
		}// end ap
	}// end clearListener

	/*  
     *J48 classifier 
     */
	private class j48Listener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			try {
				pr = rt.exec("java weka.classifiers.trees.J48 -t AudioFeatures.arff -i");
				output.setText("");
				displayFindings();
			} catch (IOException ioe) {

			}
		}// end ap
	}// end j48Listener

	/*
     * SMO (Sequential Minimal Optimization) classifier
     */
	public class smoListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			try {
				pr = rt.exec("java weka.classifiers.functions.SMO -C 1.0 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -t AudioFeatures.arff");
				output.setText("");
				displayFindings();
			} catch (IOException ioe) {
			}
		}// end ap
	}// end smoListener

	/*
     * ZeroR classifier
     */
	public class zeroRListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			try {
				pr = rt.exec("java weka.classifiers.rules.ZeroR  -t AudioFeatures.arff");
				output.setText("");
				displayFindings();
			} catch (IOException ioe) {
			}
		}// end ap
	}// end zeroRListener

	/*
     * NaiveBayes classifier 
     */
	public class naiveBayesListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				pr = rt.exec("java weka.classifiers.bayes.NaiveBayes -t AudioFeatures.arff -i");
				output.setText("");
				displayFindings();
			} catch (IOException ioe) {
			}
		}// end ap
	}// end naiveBayesListener

	/*
	 * Gets the music data from the music files and prints this data.
	 */
	public void getMusicData() {
		for (int i = 1; i < 21; i++) {

			totalRecordings[i] = new File("src/Audio/music/mu" + i + ".wav");

			try {
				String AudioType = "MUSIC";
				AudioStream[i] = AudioSystem
						.getAudioInputStream(totalRecordings[i]);
				WaveHeader wavFile = new WaveHeader(new FileInputStream(
						totalRecordings[i]));
				Wave wave = new Wave(new WaveInputStream(new FileInputStream(
						totalRecordings[i])));
				TimeDomainRepresentation tdr = new TimeDomainRepresentation(
						wave);
				FrequencyTimeDomainRepresentation ftdr = new FrequencyTimeDomainRepresentation(
						tdr.getWave());
				AmplitudeTimeDomainRepresentation atdr = new AmplitudeTimeDomainRepresentation(
						tdr.getWave());
				ftdr.buildSpectrogram();
				Mean mean = new Mean();
				ZeroCrossingRate zcr = new ZeroCrossingRate(
						atdr.getNormalizedAmplitudes(), 1);
				StandardDeviation stdv = new StandardDeviation();
				stdv.setValues(atdr.getNormalizedAmplitudes());
				ArrayRankDouble ard = new ArrayRankDouble();
				mean.setValues(atdr.getNormalizedAmplitudes());
				SpectralCentroid sp = new SpectralCentroid(
						atdr.getNormalizedAmplitudes());

				writer.print(wavFile.getAudioFormat());
				writer.print("," + wavFile.getSampleRate());
				writer.print("," + wavFile.getBitsPerSample());
				writer.print("," + wavFile.getBlockAlign());
				writer.print("," + wavFile.getByteRate());
				writer.print("," + wavFile.getChannels());
				writer.print("," + wavFile.getChunkSize());
				writer.print("," + ftdr.getFftSampleSize());
				writer.print("," + ftdr.getFramesPerSecond());
				writer.print("," + ftdr.getNumFrames());
				writer.print("," + ftdr.getNumFrequencyUnit());
				writer.print("," + ftdr.getUnitFrequency());
				writer.print("," + atdr.getTimeStep());
				writer.print(","
						+ ard.getMaxValueIndex(atdr.getNormalizedAmplitudes()));
				writer.print(","
						+ ard.getMinValueIndex(atdr.getNormalizedAmplitudes()));
				writer.print("," + mean.evaluate());
				writer.print("," + zcr.evaluate());
				writer.print("," + stdv.evaluate());
				writer.print("," + sp.evaluate());
				writer.print("," + AudioType);
				writer.println();
			} catch (Exception e) {
				e.printStackTrace();
			}
			AudioFormat[i] = AudioStream[i].getFormat();
		}
	}

	/*
	 * Gets the speech data from the speech files and prints this data.
	 */
	public void getSpeechData() {
		for (int i = 21; i < 41; i++) {
			totalRecordings[i] = new File("src/Audio/speech/sp" + (i - 20)
					+ ".wav");

			try {
				String AudioType = "SPEECH";
				AudioStream[i] = AudioSystem
						.getAudioInputStream(totalRecordings[i]);
				WaveHeader wavFile = new WaveHeader(new FileInputStream(
						totalRecordings[i]));
				Wave wave = new Wave(new WaveInputStream(new FileInputStream(
						totalRecordings[i])));
				TimeDomainRepresentation tdr = new TimeDomainRepresentation(
						wave);
				FrequencyTimeDomainRepresentation ftdr = new FrequencyTimeDomainRepresentation(
						tdr.getWave());
				AmplitudeTimeDomainRepresentation atdr = new AmplitudeTimeDomainRepresentation(
						tdr.getWave());
				ftdr.buildSpectrogram();
				Mean mean = new Mean();
				ZeroCrossingRate zcr = new ZeroCrossingRate(
						atdr.getNormalizedAmplitudes(), 1);
				StandardDeviation stdv = new StandardDeviation();
				stdv.setValues(atdr.getNormalizedAmplitudes());
				ArrayRankDouble ard = new ArrayRankDouble();
				mean.setValues(atdr.getNormalizedAmplitudes());
				SpectralCentroid sp = new SpectralCentroid(
						atdr.getNormalizedAmplitudes());

				writer.print(wavFile.getAudioFormat());
				writer.print("," + wavFile.getSampleRate());
				writer.print("," + wavFile.getBitsPerSample());
				writer.print("," + wavFile.getBlockAlign());
				writer.print("," + wavFile.getByteRate());
				writer.print("," + wavFile.getChannels());
				writer.print("," + wavFile.getChunkSize());
				writer.print("," + ftdr.getFftSampleSize());
				writer.print("," + ftdr.getFramesPerSecond());
				writer.print("," + ftdr.getNumFrames());
				writer.print("," + ftdr.getNumFrequencyUnit());
				writer.print("," + ftdr.getUnitFrequency());
				writer.print("," + atdr.getTimeStep());
				writer.print(","
						+ ard.getMaxValueIndex(atdr.getNormalizedAmplitudes()));
				writer.print(","
						+ ard.getMinValueIndex(atdr.getNormalizedAmplitudes()));
				writer.print("," + mean.evaluate());
				writer.print("," + zcr.evaluate());
				writer.print("," + stdv.evaluate());
				writer.print("," + sp.evaluate());
				writer.print("," + AudioType);
				writer.println();
			} catch (Exception e) {
				e.printStackTrace();
			}
			AudioFormat[i] = AudioStream[i].getFormat();
		}
	}
}
