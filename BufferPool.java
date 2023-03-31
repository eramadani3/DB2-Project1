import java.io.IOException;


public class BufferPool {
    private Frame[] frames;
    private int lastRemovedFrame =0;
    private int pinnedFrames = 0;
    private int numberFrames = 0;
 
    public BufferPool(int poolSize) {
        initialize(poolSize);
    }

    public void initialize(int poolSize) {
        frames = new Frame[poolSize];
        for (int i = 0; i < poolSize; i++) {
            frames[i] = new Frame();
            frames[i].initialize();
        }
        System.out.println("The program is ready for the next command");
    }

    public int search(int blockId) {
        for (int i = 0; i < frames.length; i++) {
            if (frames[i].getBlockId() == blockId) {
                return i; // found the block in buffer pool
            }
        }
        return -1; // block not found in buffer pool
    }

    public void readBlock(int blockId) throws IOException {
        int bufferNum = search(blockId);
        if (bufferNum != -1) {
            Frame frame = frames[bufferNum];
            frame.setPinned(true); // Set frame as pinned since it is being accessed
            System.out.println(frame.getContent());
        }
    }

    public int[] loadBlockFromDisk(int record) throws IOException {
        int blockID = (record % 100 == 0) ? (record / 100) : (record / 100) + 1;
        int lineNumber = (record % 100 == 0) ? 100 : (record % 100);


        // check if blockID is in the buffer
        int frameNumber = search(blockID);

        if (frameNumber == -1) {

            frameNumber = findEmptyFrame();
            if (frameNumber == -1) {

                frameNumber = selectFrameToEvict();

                if (frameNumber == -1) {
                    return new int[] {-1, -1};
                } else {
                    frames[frameNumber].readFile(blockID);
                    frames[frameNumber].setBlockId(blockID);
                }
            } else {

                frames[frameNumber].readFile(blockID);
                frames[frameNumber].setBlockId(blockID);
            }
            lastRemovedFrame = frameNumber;
            System.out.println("Block " + blockID + " loaded into frame " + frameNumber);
        } else {
            System.out.println("Block " + blockID + " already in buffer in frame " + frameNumber);
        }
        return new int[] {frameNumber, lineNumber};
    }


    public int selectFrameToEvict() {
        lastRemovedFrame = (lastRemovedFrame == numberFrames - 1) ? 0 : lastRemovedFrame + 1;
        for (int i = lastRemovedFrame; i < numberFrames + (numberFrames - lastRemovedFrame); i++) {
            int k = i % numberFrames;
            if (!frames[k].isPinned()) {
                return k; // found an unpinned frame to evict
            }else{
                frames[k].writeToFile();
                frames[k].setDirty(false);
                return i;
            }
        }
        return -1; // no unpinned frame found
    }

    public int findEmptyFrame() {
        for (int i = 0; i < frames.length; i++) {
            if (frames[i].getBlockId() == -1) {
                return i; // found an empty frame
            }
        }
        return -1; // no empty frame found
    }
    
    

    public String GET(int recordIndex) throws Exception {
        if (recordIndex < 1 || recordIndex > 700) {
            throw new Exception("Record index out of range.");
        }
        int[] values = loadBlockFromDisk(recordIndex);

        if (values[1] == -1 && values[0] == -1){
            return "Record " + recordIndex + " is not in the database";
        }
        else{
            return "Record " + recordIndex + " is in block " + frames[values[0]].getBlockId() + " and line " + values[1];
        }
    }
    public String SET(int k, String str) throws Exception {
        if (k < 1 || k > 700) {
            throw new Exception("Record index out of range.");
        }
        int[] values = loadBlockFromDisk(k);
        int frameNum = values[0];
        int lineNum = values[1];

        if(values[1] == -1 && values[0] == -1){
            return "Record " + k + " is not in the database";
        }
        else{
            frames[frameNum].setRecord(lineNum, str);
            frames[frameNum].setDirty(true);
            return "Record " + k + " is in block " + frames[frameNum].getBlockId() + " and line " + lineNum;
        }
    }
    
    public String PIN(int blockId) throws Exception {
        if(blockId > 7 || blockId < 1){
            throw new Exception("Block ID out of range.");
        }

        int[] values = loadBlockFromDisk(blockId*100);

        if(values[1] == -1 && values[0] == -1){
            return "Record " + blockId + " is not in the database";
        }
        else{
            frames[values[0]].pinFrame();
            return "Block " + blockId + " is pinned";
        }
    }

    public String UNPIN(int blockId) throws Exception{
        if(blockId > 7 || blockId < 1){
            throw new Exception("Block ID out of range.");
        }

        for(int i = 0; i < frames.length; i++){
            if(frames[i].getBlockId() == blockId){
                frames[i].setPinned(false);
                pinnedFrames--;
                return "Block " + blockId + " is unpinned";
            }
        }
        return "Block " + blockId + " is not in the database";
    }

    public void clearAll(){
        for (int i = 0; i < numberFrames; i++) {
            if(frames[i].isDirty()){
                frames[i].writeToFile();
                frames[i].setDirty(false);
            }
        }
        System.out.println("All frames are cleared");
    }
    
}
