package plagdetect;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlagiarismDetector implements IPlagiarismDetector {
	private int n;
	private Map<String, Set<String>> ngramSets = new HashMap<>();
	private Map<String, Map<String, Integer>> resultMap = new HashMap<>();

	public PlagiarismDetector(int n) {
		this.n = n;
	}
	
	@Override
	public int getN() {
		return n;
	}

	@Override
	public Collection<String> getFilenames() {
		return ngramSets.keySet();
	}

	@Override
	public Collection<String> getNgramsInFile(String filename) {
		return ngramSets.get(filename);
	}

	@Override
	public int getNumNgramsInFile(String filename) {
		return ngramSets.get(filename).size();
	}

	@Override
	public Map<String, Map<String, Integer>> getResults() {
		return resultMap;
	}

	@Override
	public void readFile(File file) throws IOException {
		// most of your work can happen in this method
		String filename = file.getName();
		HashSet<String> ngramSet = new HashSet<>();
		Scanner reader = new Scanner(file);

		while(reader.hasNextLine()) {
			String[] data = reader.nextLine().split(" ");
			if(data.length < n)
				continue;
			for(int i=0; i < data.length-n+1; i++) {
				ngramSet.add(String.join(" ",  Arrays.copyOfRange(data, i, i+n)));
			}
		}

		Map<String, Integer> tempMap = new HashMap<>();

		for (Map.Entry<String, Set<String>> entry : ngramSets.entrySet()) {
			String tempFilename = entry.getKey();
			HashSet<String> tempNgramSet = (HashSet<String>) entry.getValue();

			HashSet<String> tempSet = new HashSet<>(ngramSet);
			tempSet.removeAll(tempNgramSet);
			int count = ngramSet.size() - tempSet.size();

			tempMap.put(tempFilename, count);
			resultMap.get(tempFilename).put(filename, count);
		}

		ngramSets.put(filename, ngramSet);
		resultMap.put(filename, tempMap);
	}

	@Override
	public int getNumNGramsInCommon(String file1, String file2) {
		return resultMap.get(file1).get(file2);
	}

	@Override
	public Collection<String> getSuspiciousPairs(int minNgrams) {
		Set<String> susPairs = new HashSet<>();
		for (Map.Entry<String, Map<String, Integer>> entry : resultMap.entrySet()) {
			for (Map.Entry<String, Integer> entry2 : resultMap.get(entry.getKey()).entrySet()) {
				if(entry2.getValue() >= minNgrams) {
					if(entry.getKey().compareTo(entry2.getKey()) <= 0) {
						susPairs.add(String.format("%s %s %d", entry.getKey(), entry2.getKey(), entry2.getValue()));
					} else {
						susPairs.add(String.format("%s %s %d", entry2.getKey(), entry.getKey(), entry2.getValue()));
					}
				}
			}
		}
		return susPairs;
	}

	@Override
	public void readFilesInDirectory(File dir) throws IOException {
		// delegation!
		// just go through each file in the directory, and delegate
		// to the method for reading a file
		for (File f : dir.listFiles()) {
			readFile(f);
		}
	}
}
