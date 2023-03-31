import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Frame{
    private String[] content;
    private boolean dirty;
    private boolean pinned;
    private int blockId;

    //generate comments for each method
    public Frame() {
        content = new String[100];
        dirty = false; //initialize to false
        pinned = false; //initialize to false
        blockId = -1; //initialize to -1
    }
    public void pinFrame(){
        pinned = true;
    }
    public void unpinFrame(){
        pinned = false;
    }
    public String[] getContent() {
        return content;
    }
    public void setContent(int lineNum, String str) {
        this.content = content;
    }
    public boolean isDirty() {
        return dirty;
    }
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
    public boolean isPinned() {
        return pinned;
    }
    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }
    public int getBlockId() {
        return blockId;
    }
    public void setBlockId(int blockId) {
        this.blockId = blockId;
    }

    public String getRecord(int blockID){
        return content[blockID-1];
    }

    
    public void readFile(int recordNumber) throws IOException {
    
      String fileName = "F" + recordNumber + ".txt";

      // reading file
      try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
         StringBuilder sb = new StringBuilder();
         String line = br.readLine();
   
         while (line != null) {
           sb.append(line);
           sb.append(System.lineSeparator());
           line = br.readLine();
         }
   
         // setting content to the file contents (what is read in SB)
         String[] sentences = sb.toString().split("(?<=\\.)");
         this.content = sentences;

       } catch (IOException e) {
         System.err.format("IOException: %s%n", e);
       }
    }

    public void writeToFile(){
        String fileName = "F" + blockId + ".txt";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            for(int i = 0; i < content.length; i++){
                bw.write(content[i]);
            }
        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
        }
    }
    public void updateRecord(int recordNumber, String newContent) {
        content[recordNumber] = newContent;
        dirty=true;
    }

    public void initialize() {
       for(int i = 0; i < 100; i++) {
          content[i] = "";
       }
    }


}