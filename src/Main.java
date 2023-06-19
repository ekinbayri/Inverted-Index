import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) throws FileNotFoundException {
		Scanner scan = new Scanner(System.in);
		int mode = 0;
		//Lets the user select the option they want. (Menu)
		while(true) {
			System.out.print("Select load factor (1 = %50, 5 = %80): ");
			try {
				int test = scan.nextInt();
				if(test == 1 || test == 5)
				mode += test;
				else {					
					throw new Exception();
				}
				System.out.print("\nSelect hashing method (0 = SSF, 2 = PAF): ");
				test = scan.nextInt();
				if(test == 0 || test == 2)
					mode += test;
					else {					
						throw new Exception();
					}
				System.out.print("\nSelect collusion handling method (0 = LP, 1 = DH): ");
				test = scan.nextInt();
				if(test == 0 || test == 1)
					mode += test;
					else {					
						throw new Exception();
					}
				break;
				
			}
			catch(Exception e){
				System.out.println("Please enter one of the specified numbers.");
				mode = 0;
			}
		}
		//HashedDictionary initialization.
		HashedDictionary<String, HashedDictionary<String,Integer>> dataBase = new HashedDictionary<String,HashedDictionary<String,Integer>>(mode);
	      File folder2 = new File("stop_words_en.txt");
		  Scanner sc2 = new Scanner(folder2);	
		  List<String> list = new ArrayList<String>();
		  while(sc2.hasNextLine()) {		 
		  list.add(sc2.nextLine());		 	
		  }
		String[] stopWords = list.toArray(new String[list.size()]); //Stop words are stored
		File folder = new File("bbc");
		File[] subFolders = folder.listFiles();
		 long time = System.currentTimeMillis(); //Records how much time has passed.
		 for(int i = 0; i < subFolders.length; i++) {			//Looping through the files.
			 File[] currentFolder = subFolders[i].listFiles();
			 for(int j = 0; j <  currentFolder.length; j++) {
				 Scanner sc = new Scanner(currentFolder[j]);
				 List<String> list2 = new ArrayList<String>();
			      while (sc.hasNextLine()) {
			    	  String line = sc.nextLine();
			    	  line = line.toLowerCase();
				        String DELIMITERS = "[-+=" +
			        " " +        //space
			        "\r\n " +    //carriage return line fit
					"1234567890" + //numbers
					"’'\"" +       // apostrophe
					"(){}<>\\[\\]" + // brackets
					":" +        // colon
					"," +        // comma
					"‒–—―" +     // dashes
					"…" +        // ellipsis
					"!" +        // exclamation mark
					"." +        // full stop/period
					"«»" +       // guillemets
					"-‐" +       // hyphen
					"?" +        // question mark
					"‘’“”" +     // quotation marks
					";" +        // semicolon
					"/" +        // slash/stroke
					"⁄" +        // solidus
					"␠" +        // space?   
					"·" +        // interpunct
					"&" +        // ampersand
					"@" +        // at sign
					"*" +        // asterisk
					"\\" +       // backslash
					"•" +        // bullet
					"^" +        // caret
					"¤¢$€£¥₩₪" + // currency
					"†‡" +       // dagger
					"°" +        // degree
					"¡" +        // inverted exclamation point
					"¿" +        // inverted question mark
					"¬" +        // negation
					"#" +        // number sign (hashtag)
					"№" +        // numero sign ()
					"%‰‱" +      // percent and related signs
					"¶" +        // pilcrow
					"′" +        // prime
					"§" +        // section sign
					"~" +        // tilde/swung dash
					"¨" +        // umlaut/diaeresis
					"_" +        // underscore/understrike
					"|¦" +       // vertical/pipe/broken bar
					"⁂" +        // asterism
					"☞" +        // index/fist
					"∴" +        // therefore sign
					"‽" +        // interrobang
					"※" +          // reference mark
			        "]";	
				        
				        //Gets rid of stop words.
				        String[] lineWords = line.split(DELIMITERS);
				        for(int z = 0; z < stopWords.length; z++) {
				        	for(int a = 0; a < lineWords.length; a++) {
				        		if((stopWords[z].toLowerCase().equals(lineWords[a]))) {
				        			lineWords[a] = null;
				        		}
				        	}				        	
				        }
				        for(int z = 0; z < lineWords.length; z++) {
				        	if(lineWords[z] != null) {
				        		list2.add(lineWords[z]);
				        	}
				        }
				        
			      }
	      		  String[] words = list2.toArray(new String[list2.size()]);	//Words without the stop words are created for this file.					
				  for(int k = 0; k < words.length; k++) {						  
						if(dataBase.getValue(words[k]) == null) {  //If the entry does not exists creates and adds it to hash table
		        			HashedDictionary<String,Integer> dc = new HashedDictionary<String,Integer>(mode);
		        			dc.add(subFolders[i].getName() + "\\" + currentFolder[j].getName(), 1);
			        		dataBase.add(words[k], dc);
			        	}
			        	else {
			        		//If the entry does exist, updates the value or if it is on another file adds it with its value as 1.
			        		//This HashedDictionary is the one that holds the files. It is the Key holder data type in the original HashedDictionary.
			        		HashedDictionary<String,Integer> dc = dataBase.getValue(words[k]);
			        		if(dc.contains(subFolders[i].getName() + "\\" + currentFolder[j].getName())) {
			        			dc.add(subFolders[i].getName() + "\\" + currentFolder[j].getName(), dc.getValue(subFolders[i].getName() + "\\" + currentFolder[j].getName()) + 1);
			        		    
			        		}
			        		else {
			        			dc.add(subFolders[i].getName() + "\\" + currentFolder[j].getName(), 1);
			        		}
			        			dataBase.add(words[k], dc);				        
		        		}			        			        							        	
					}		        
			      }
			 }
		 	 //Printing of necessary calculations and calling the search function.
		 	 System.out.println("Time elapsed:" + (System.currentTimeMillis() - time) + " milliseconds.");
		 	 System.out.println("Collusion count:" + dataBase.cCount);
		 	 File search = new File("search.txt");
		 	 Scanner sc = new Scanner(search);
		 	 long avgTime = 0, minTime = 100000000, maxTime = 0;
		 	 int loopCount = 0;
		 	 while(sc.hasNextLine()) {
		 		 long checkTime = System.nanoTime();
		 		 dataBase.search(sc.nextLine());
		 		 long elapsedTime = System.nanoTime() - checkTime;
		 		 avgTime += elapsedTime;
		 		 if(elapsedTime > maxTime) maxTime = elapsedTime;
		 		 if(elapsedTime < minTime) minTime = elapsedTime;
		 		 loopCount++;
		 	 }
		 	 System.out.println("Average search time: " + avgTime/loopCount + " nanoseconds");
		 	 System.out.println("Minimum search time: " + minTime + " nanoseconds");
		 	 System.out.println("Maximum search time: " + maxTime + " nanoseconds");
		 	 
		 	 //Lets the user search whatever word they want.
			 Scanner word = new Scanner(System.in);
		      while (true) {
					System.out.print("Search:");
					String wordSearch = word.nextLine();
					HashedDictionary<String,Integer> dc = dataBase.search(wordSearch);
					if(dc != null) {
						System.out.println(dc.getSize() + " document(s) found.");
						 Iterator<String> keyIterator = dc.getKeyIterator();
						 Iterator<Integer> valueIterator = dc.getValueIterator();
						 while (keyIterator.hasNext()) {
							 System.out.println(keyIterator.next() + " " + valueIterator.next());
						 }
					}
					
					else {
						System.out.println("0 documents found.");
					}
					
					
				}
		
		 }
	}


