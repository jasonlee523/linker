//package homework_1;
import java.util.*;
import java.io.*;
public class Linker {
	public static void main(String[] args) throws Exception {
		Scanner sc = new Scanner(new FileReader(args[0]));
		int modules = sc.nextInt();
		int[] addresses = new int[modules+1];
		Map<String, Integer> symbols = new HashMap<String, Integer>();
		Map<String, Integer> error1 = new HashMap<String, Integer>(); // For error 1
		Map<String, String> symErrors = new HashMap<String, String>(); // For errors 2, 4
		Map<Integer, String> useErrors = new HashMap<Integer, String>(); // For errors 3, 5, 6
				
		for(int i=0; i<modules; i++) {
			Map<String, Integer> curSymbols = new HashMap<String, Integer>();
			int defs = sc.nextInt();
			while(defs-- > 0) {
				String symbol = sc.next();
				int address = sc.nextInt();
				// Error 2
				if(symbols.containsKey(symbol))
					symErrors.put(symbol, "Error: This symbol is multiply defined; first value used.");
				else
					curSymbols.put(symbol, address);
			}
			int uses = sc.nextInt();
			while(uses-- > 0) {
				sc.next();
				sc.nextInt();
			}			
			int length = sc.nextInt();
			addresses[i+1] = addresses[i] + length;
			for(Map.Entry<String, Integer> pair: curSymbols.entrySet()) {
				// Error 4
				if(pair.getValue() > length-1) {
					symbols.put(pair.getKey(), addresses[i]);
					symErrors.put(pair.getKey(), "Error: This symbol is defined outside module "+i+"; 0 (relative) used.");
				}
				else
					symbols.put(pair.getKey(), addresses[i]+pair.getValue());
			}
			while(length-- > 0)
				sc.nextInt();
		}
		sc.close();
		
		Scanner sr = new Scanner(new FileReader(args[0]));
		int modules_ = sr.nextInt();
		int[] output = new int[addresses[modules_]];
		for(int i = 0; i < modules_; i++) {
			int defs = sr.nextInt();
			while(defs-- > 0) {
				sr.next(); 
				sr.nextInt();
			}			
			int uses = sr.nextInt();
			Map<String, Integer> useList = new HashMap<String, Integer>();
			for(int j=0; j<uses; j++) {
				String sym = sr.next(); int address = sr.nextInt();
				// Error 1 --> if a symbol is not in this hashmap, there is an error
				if(error1.containsKey(sym) == false)
					error1.put(sym, 1);
				// Error 3 pt.1
				if(symbols.containsKey(sym) == false)
					useErrors.put(addresses[i]+address, "Error: "+sym+" is not defined; 0 used");
				useList.put(sym, address);
			}			
			int length = sr.nextInt();
			String[] words = new String[length];
			for(int j=0; j<length; j++) {
				words[j] = sr.next();
			}
			for(Map.Entry<String, Integer> pair: useList.entrySet()) {
				String word = words[pair.getValue()];
				int instr = Integer.parseInt(word.substring(1, 4)); int type = Integer.parseInt(word.substring(4));
				// Error 5 pt.1
				if(type != 4) {
					useErrors.put(addresses[i]+pair.getValue(), "Error: Immediate address in use list; treated as external.");
				}
				if(symbols.containsKey(pair.getKey())) {
					int result = Integer.parseInt(word.substring(0, 1))*1000 + symbols.get(pair.getKey()).intValue();
					output[addresses[i]+pair.getValue()] = result;
				}
				else
					output[addresses[i]+pair.getValue()] = Integer.parseInt(word.substring(0, 1))*1000;

				while(instr != 777) {
					int prevInstr = instr;
					word = words[instr];
					instr = Integer.parseInt(word.substring(1, 4)); type = Integer.parseInt( word.substring(4));
					// Error 5 pt.2
					if(type != 4) {
						useErrors.put(addresses[i]+prevInstr, "Error: Immediate address in use list; treated as external.");
					}
					if(symbols.containsKey(pair.getKey())) {
						int result = Integer.parseInt(word.substring(0, 1))*1000 + symbols.get(pair.getKey()).intValue();
						output[addresses[i]+prevInstr] = result;
					}
					// Error 3 pt.2
					else {
						output[addresses[i]+prevInstr] = Integer.parseInt(word.substring(0, 1))*1000;
						useErrors.put(addresses[i]+prevInstr, "Error: "+pair.getKey()+" is not defined; 0 used");
					}
				}
			}
			for(int j=0; j<words.length; j++) {
				if(output[addresses[i]+j] == 0) {
					String word = words[j];
					int type = Integer.parseInt(word.substring(4));
					if(type == 3)
						output[addresses[i]+j] = Integer.parseInt(word.substring(0, 4)) + addresses[i];
					else if(type == 4) {
						output[addresses[i]+j] = Integer.parseInt(word.substring(0, 4));
						useErrors.put(addresses[i]+j, "Error: E type address not on use chain; treated as I type.");
					}
					else
						output[addresses[i]+j] = Integer.parseInt(word.substring(0, 4));
				}
			}
		}
		System.out.println("Symbol Table");
		for(Map.Entry<String, Integer> pair: symbols.entrySet()) {
			System.out.println(pair.getKey()+"="+pair.getValue());
			if(symErrors.containsKey(pair.getKey())) 
				System.out.print("    "+symErrors.get(pair.getKey()).substring(0)+"\n");
			if(error1.containsKey(pair.getKey()) == false)
				System.out.print("    Warning: This symbol is defined but not used\n");
		}
		System.out.println("\nMemory Map");
		for(int i=0; i<output.length; i++) {
			System.out.println(i+": "+output[i]);
			if(useErrors.containsKey(i))
				System.out.print("    "+useErrors.get(i).substring(0)+"\n");
		}
	}
}
