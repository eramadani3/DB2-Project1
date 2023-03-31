import java.io.IOException;


public class BufferPool {
    private Frame[] frames;
    private static BufferPool instance;
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
        //Get the block number
        int blockID = (record % 100 == 0) ? record / 100 : record / 100 + 1;
        //Get the line number
        int line = (record % 100 == 0) ? 100 : record % 100;


        int frameNum = search(blockID);
        if (frameNum != -1) {
            frameNum =findEmptyFrame();

            if(frameNum == -1){
                frameNum = selectFrameToEvict();
            }else{
                frames[frameNum].getRecord(blockID);
                frames[frameNum].setBlockId(blockID);
            }
        }else{
            frames[frameNum].getRecord(blockID);
            frames[frameNum].setBlockId(blockID);
        }
        lastRemovedFrame = frameNum;
        return new int[] {frameNum, line};
    }
    

    public int selectFrameToEvict() {
        for (int i = 0; i < frames.length; i++) {
            if (!frames[i].isPinned()) {
                return i; // found an unpinned frame to evict
            }
        }
        lastRemovedFrame = (lastRemovedFrame == numberFrames - 1) ? 0 : lastRemovedFrame + 1;
        for (int i = lastRemovedFrame; i < numberFrames + (numberFrames - lastRemovedFrame); i++) {
            int k = i % numberFrames;
            if (!frames[k].isPinned()) {
                return k; // found an unpinned frame to evict
            }
        }
        return -1; // All frames are pinned, cannot evict any
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
        if (recordIndex < 1 || recordIndex > 1000) {
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
        if (k < 1 || k > 1000) {
            throw new Exception("Record index out of range.");
        }
        int[] values = loadBlockFromDisk(k);
        int frameNum = values[0];
        int lineNum = values[1];

        if(values[1] == -1 && values[0] == -1){
            return "Record " + k + " is not in the database";
        }
        else{
            frames[frameNum].setContent(lineNum, str);
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
            frames[values[0]].setPinned(true);
            pinnedFrames++;
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
