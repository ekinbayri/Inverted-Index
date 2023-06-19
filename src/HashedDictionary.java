import java.util.Iterator;
import java.util.NoSuchElementException;



public class HashedDictionary<K, V > implements DictionaryInterface<K,V>
 {
 // The dictionary:
 private int numberOfEntries;
 private static final int DEFAULT_CAPACITY = 31; // Must be prime
 // The hash table:
 private TableEntry<K, V>[] hashTable;
 private int tableSize; // Must be prime
 private static final double MAX_LOAD_FACTOR1 = 0.5; // Fraction of hash table
 private static final double MAX_LOAD_FACTOR2 = 0.8;
 // that can be filled
 
 private int mode; //SSF or PAF and DH or LP and %50 or %80 (1234) %50 (5678) %80  1-5 SSF LP   2-6 SSF DH    3-7 PAF LP     4-8 PAF DH  
 private int z = 31 , k = 31, q = 7, d = q - k % q;
 public long cCount = 0;
 public HashedDictionary(int mode)
 {
 this(mode, DEFAULT_CAPACITY); // Call next constructor
 } // end default constructor
 private int getNextPrime(int num) {
	 if(num > 2) {
		 if (num == 3) return num;
		 if (num == 4 || num == 5) return 5;
		 boolean prime = false;
		 while(!prime) {
			int a = num/2;
			for(int i = 2; i <= a; i++) {
				if(num % i == 0) {
					num++;
					break;
				}
				if(i == a) {
					prime = true;
				}
			}
		 }
		 return num;
	 }
	 else {
		 return 2;
	 }
 }
 public HashedDictionary(int mode, int initialCapacity)
 {
	 numberOfEntries = 0; 
	 this.mode = mode;
	 tableSize = getNextPrime(initialCapacity);
	 @SuppressWarnings("unchecked")
	 TableEntry<K, V>[] temp = (TableEntry<K, V>[])new TableEntry[tableSize];
	 hashTable = temp;
 }
 public V getValue(K key) {
	 V result = null;
	 int index = getHashIndex(key);
	 index = locate(index, key);
	 if (index != -1)
	 result = hashTable[index].getValue(); // Key found; get value
	 // Else key not found; return null
	 return result;
 }
 
 public V remove(K key) {
	 V removedValue = null;
	 int index = getHashIndex(key);
	 index = locate(index, key);
	 if (index != -1)
	 { // Key found; flag entry as removed and return its value
	 removedValue = hashTable[index].getValue();
	 hashTable[index].setToRemoved();
	 numberOfEntries--;
	 } // end if
	 // Else key not found; return null
	 return removedValue;
 } // end remove
 
 private int locate(int index, K key) {
	
	 boolean found = false;
	 while ( !found && (hashTable[index] != null) )
	 {
	 if (hashTable[index].isIn() && key.equals(hashTable[index].getKey()) )
	 found = true; // Key found
	 else // Follow probe sequence
	 index = (index + 1) % tableSize; // Linear probing
	 } // end while
	 // Assertion: Either key or null is found at hashTable[index]
	 int result = -1;
	 if (found)
	 result = index;
	 return result;
	 
	 		 		 	
 } // end locate

 public V search(K key) {
	 V result = getValue(key);
	 return result;
 }
 
 public V add(K key, V value) {
	 if ((key == null) || (value == null))
		 throw new IllegalArgumentException();
	 else
	 {
		 V oldValue; // Value to return
		 int index = getHashIndex(key);
		 if(mode == 1 || mode == 3 || mode == 5 || mode == 7)
			 index = probe(index, key); // Check for and resolve collision
		 else
			index = dh(index,key);
		 // Assertion: index is within legal range for hashTable
		 assert (index >= 0) && (index < tableSize);
		 if ( (hashTable[index] == null) || hashTable[index].isRemoved())
		 { // Key not found, so insert new entry
			 hashTable[index] = new TableEntry<>(key, value);
			 numberOfEntries++;
			 oldValue = null;
		 }
		 else
		 { // Key found; get old value for return and then replace it
			 oldValue = hashTable[index].getValue();
			 hashTable[index].setValue(value);
		 } // end if
		 // Ensure that hash table is large enough for another add
		 if (isHashTableTooFull()) enlargeHashTable();
		 return oldValue;
	 } // end if
 } // end add
 
 private boolean isHashTableTooFull() {
	 if(mode < 5) {
		 if(numberOfEntries/tableSize >= MAX_LOAD_FACTOR1) return true;  
		 else return false;
	 }
	 else {
		 if(numberOfEntries/tableSize >= MAX_LOAD_FACTOR2) return true;  
		 else return false;
	 }
	
 }
 private void enlargeHashTable()
 {
	 TableEntry<K, V>[] oldTable = hashTable;
	 int oldSize = tableSize;
	 int newSize = getNextPrime(oldSize + oldSize);
	 tableSize = newSize;
	 // The cast is safe because the new array contains null entries
	 @SuppressWarnings("unchecked")
	 TableEntry<K, V>[] temp = (TableEntry<K, V>[])new TableEntry[newSize];
	 hashTable = temp;
	 numberOfEntries = 0; // Reset number of dictionary entries, since
	 // it will be incremented by add during rehash
	 // Rehash dictionary entries from old array to the new and bigger
	 // array; skip both null locations and removed entries
	 for (int index = 0; index < oldSize; index++)
	 {
		 if ( (oldTable[index] != null) && oldTable[index].isIn() )
		 add(oldTable[index].getKey(), oldTable[index].getValue());
	 } // end for
	 } // end enlargeHashTable
	 
	 private int probe(int index, K key)
	 {
	 boolean found = false;
	 int removedStateIndex = -1; // Index of first location in removed state
	 while ( !found && (hashTable[index] != null) )
	 {
		 if (hashTable[index].isIn())
		 {
			 if (key.equals(hashTable[index].getKey()))
			 found = true; // Key found
			 else {
				 // Follow probe sequence
				 index = (index + 1) % tableSize; // Linear probing
				 cCount++;
			 }
				 			
		 }
		 else // Skip entries that were removed
		 {
			 // Save index of first location in removed state
			 if (removedStateIndex == -1)
			 removedStateIndex = index;
			 index = (index + 1) % tableSize; // Linear probing
			
		 } // end if
	 } // end while
	 // Assertion: Either key or null is found at hashTable[index]
	 if (found || (removedStateIndex == -1) )
	 return index; // Index of either key or null
	 else
	 return removedStateIndex; // Index of an available location
 } // end probe
	 private int dh(int index, K key) {
		 boolean found = false;
		 int removedStateIndex = -1;
		 int multiplier = 0;
		 int h = k % tableSize;
		 while(!found && (hashTable[index] != null)) {
			 if (hashTable[index].isIn())
			 {
				 if (key.equals(hashTable[index].getKey()))
				 found = true; // Key found
				 else {
					 index = (h + (multiplier * d)) % tableSize;
					 cCount++;
				 }
					 
			 }
			 else // Skip entries that were removed
			 {
				 // Save index of first location in removed state
				 if (removedStateIndex == -1)
				 removedStateIndex = index;
				 index = (h + (multiplier * d)) % tableSize;
			 }
			 multiplier++;
		 } 
		 if (found || (removedStateIndex == -1) )
		 return index; // Index of either key or null
		 else
		 return removedStateIndex; // Index of an available location
			
			
		 }
	 
 
 
 public Iterator<K> getKeyIterator() {
		return new KeyIterator();
	}

	/**
	 * Creates an iterator that traverses all values in this dictionary.
	 * 
	 * @return an iterator that provides sequential access to the values in this
	 *         dictionary
	 */
	public Iterator<V> getValueIterator() {
		return new ValueIterator();
	}
	
 private class KeyIterator implements Iterator<K>
 {
	  private int currentIndex; // Current position in hash table
	  private int numberLeft; // Number of entries left in iteration
	  private KeyIterator()
	  {
		  currentIndex = 0;
		  numberLeft = numberOfEntries;
	  } // end default constructor
	  public boolean hasNext()
	  {
		  return numberLeft > 0;
	  } // end hasNext
	  public K next()
	  {
	  K result = null;
	  if (hasNext())
	  {
		  // Skip table locations that do not contain a current entry
		  while ( (hashTable[currentIndex] == null) || hashTable[currentIndex].isRemoved() )
		  {
			  currentIndex++;
		  } // end while
		  result = hashTable[currentIndex].getKey();
		  numberLeft--;
		  currentIndex++;
	  }
	  else
	  throw new NoSuchElementException();
	  return result;
	  } // end next
	  public void remove()
	  {
	  throw new UnsupportedOperationException();
	  } // end remove
 } // end KeyIterator
 
 private class ValueIterator implements Iterator<V>
 {
	  private int currentIndex; // Current position in hash table
	  private int numberLeft; // Number of entries left in iteration
	  private ValueIterator()
	  {
	  currentIndex = 0;
	  numberLeft = numberOfEntries;
	  } // end default constructor
	  public boolean hasNext()
	  {
	  return numberLeft > 0;
	  } // end hasNext
	  public V next()
	  {
	  V result = null;
	  if (hasNext())
	  {
	  // Skip table locations that do not contain a current entry
	  while ( (hashTable[currentIndex] == null) ||
	  hashTable[currentIndex].isRemoved() )
	  {
	  currentIndex++;
	  } // end while
	  result = hashTable[currentIndex].getValue();
	  numberLeft--;
	  currentIndex++;
	  }
	  else
	  throw new NoSuchElementException();
	  return result;
	  } // end next
	  public void remove()
	  {
	  throw new UnsupportedOperationException();
	  } // end remove
 } // end KeyIterator
 
 
 
 
 
public boolean contains(K key) {
	if(getValue(key) != null) {
		return true;
	}
	return false;
}



public boolean isEmpty() {
	return numberOfEntries == 0;
	
}


public int getSize() {
	return numberOfEntries;	
}


public void clear() {
	for(int i = 0; i < numberOfEntries; i++) {
		hashTable[i] = null;
	}
	numberOfEntries = 0;
	
}

private int getHashIndex(K key)
{		
	char ch[];
	ch = ((String) key).toLowerCase().toCharArray();
	int hashCode=0;
	if(mode == 1 || mode == 5 || mode == 2 || mode == 6) {
		 for (int i = 0; i < ch.length; i++) {
	    	 hashCode += ch[i] - 96;
	    }	 
	}
	else {
		for (int i = 0; i < ch.length; i++) {
	    	 hashCode += (ch[i] - 96) * Math.pow(z, ch.length - (1+i) );
	    }	 
	}
      
	 int hashIndex =  hashCode % tableSize;  
	 //int hashIndex = key.hashCode() % hashTable.length;
	 if (hashIndex < 0)
	 hashIndex = hashIndex + tableSize;
	 return hashIndex;
} // end getHashIndex


private static class TableEntry<S, T> {
	private S key;
	private T value;
	private States state; // Flags whether this entry is in the hash table
	private enum States {CURRENT, REMOVED} // Possible values of state
	private TableEntry(S searchKey, T dataValue)
	{
	key = searchKey;
	value = dataValue;
	state = States.CURRENT;
	 // end constructor
	}
	public S getKey() {
		return key;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}
	public void setToRemoved() {
		state = States.REMOVED;
	}
	public boolean isRemoved() {
		if(state == States.REMOVED)
			return true;
		else 
			return false;
	}
	public boolean isIn() {
		if(state == States.CURRENT) return true;
		else return false;
		
	}
}

}
