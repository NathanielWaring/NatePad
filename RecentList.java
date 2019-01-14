import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;

public class RecentList implements Iterable<String>,Serializable {
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
private final ArrayList<String> recentList = new ArrayList<>();
private final ArrayList<File> fileList = new ArrayList<>();
  private final int maxLength;
  
  public RecentList() {
	  this.maxLength = 10;
  }
  public RecentList(int maxLength) {
    this.maxLength = maxLength;
  }

  public void add(String name, File file) {
    recentList.remove(name);
    recentList.add(0, name);
    fileList.remove(file);
    fileList.add(0,file);
    reduce();
    try {
		saveRecent();
	} catch (IOException e) {
		System.out.println("Couldn't save recentList");
		
	}
  }
  public void remove(String name, File file) {
	  recentList.remove(name);
	  fileList.remove(file);
  }

  private void reduce() {
	    while (recentList.size() > maxLength) {
	        recentList.remove(recentList.size() - 1);
	      }
	    while (fileList.size() > maxLength) {
	    	fileList.remove(fileList.size() - 1);
	      }
  }

  public void clear() {
    recentList.clear();
  }
  public int size() {
	  	  return recentList.size();
  }
  public Iterator<String> iterator() {
    return Collections.unmodifiableCollection(recentList).iterator();
  }
  
  public Object[] mostRecent () {
	  Object[] returnObject = {(String) recentList.get(0),(File) fileList.get(0)};
	return returnObject;
	  
  }
  public Object[][] getRecentList() {
	  Object[][] returnList = new Object[2][recentList.size()];
	  for (int i = 0;i < recentList.size();i++) {
		  returnList[0][i] = recentList.get(i);
		  returnList[1][i] = fileList.get(i);  
	  }
	  return returnList;
  }
  
	public void saveRecent() throws IOException {

		FileOutputStream fileOut = null;
		ObjectOutputStream objectOut = null;

		try {

			fileOut = new FileOutputStream("data\\recent.ser");
			objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(this);

		} catch (Exception ex) {
			System.out.println("Save Recent Failed");
			ex.printStackTrace();

		} finally {

			if (fileOut != null) {
				try {
					fileOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (objectOut != null) {
				try {
					objectOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	

	public RecentList loadRecent() throws FileNotFoundException {

		FileInputStream fileIn = null;
		ObjectInputStream objectIn = null;
		RecentList loadList = null;
		try {
			fileIn = new FileInputStream("data\\recent.ser");
			objectIn = new ObjectInputStream(fileIn);
			loadList = (RecentList) objectIn.readObject();

		} catch (Exception e) {
			loadList = new RecentList();
			
			System.out.println("Load Recent failed");
			e.printStackTrace();
		} finally {

			if (fileIn != null) {
				try {
					fileIn.close();
				} catch (IOException ie) {
					ie.printStackTrace();
				}
			}
			if (objectIn != null) {
				try {
					objectIn.close();
				} catch (IOException ie) {
					ie.printStackTrace();
				}
			}
		}

		return loadList;
	}
}    

