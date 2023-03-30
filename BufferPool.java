import java.io.IOException;
import java.util.Arrays;

public class BufferPool {
    private Frame[] buffers;
    private int blockSize = 4096; // Define blockSize
    private int numHits = 0; // Number of buffer hits
    private int numMisses = 0; // Number of buffer misses
    private int numReads = 0; // Number of blocks read from disk
    private int numWrites = 0; // Number of blocks written to disk
 
    public BufferPool(int poolSize) {
        initialize(poolSize);
    }

    public void initialize(int poolSize) {
        buffers = new Frame[poolSize];
        for (int i = 0; i < poolSize; i++) {
            buffers[i] = new Frame();
            buffers[i].initialize();
        }
        System.out.println("The program is ready for the next command");
    }

    public int findFrame(int blockId) {
        for (int i = 0; i < buffers.length; i++) {
            if (buffers[i].getBlockId() == blockId) {
                numHits++;
                return i; // found the block in buffer pool
            }
        }
        numMisses++;
        return -1; // block not found in buffer pool
    }

    public byte[] readBlock(int blockId) throws IOException {
        int bufferNum = findFrame(blockId);
        if (bufferNum != -1) {
            Frame frame = buffers[bufferNum];
            frame.setPinned(true); // Set frame as pinned since it is being accessed
            return frame.getContent();
        }
        numReads++;
        return loadBlockFromDisk(blockId);
    }

    public byte[] loadBlockFromDisk(int blockId) throws IOException {
        int emptyFrameNum = findEmptyFrame();
        if (emptyFrameNum != -1) {
            Frame frame = buffers[emptyFrameNum];
            byte[] content = new byte[blockSize];
            // code to read block with blockId from disk and store in content
            // ...
            frame.setContent(content);
            frame.setBlockId(blockId);
            frame.setPinned(true); // Set frame as pinned since it is being accessed
            return content;
        } else {
            int evictedFrameNum = selectFrameToEvict();
            if (evictedFrameNum != -1) {
                Frame frame = buffers[evictedFrameNum];
                if (frame.isDirty()) {
                    writeBlockToDisk(frame.getBlockId(), frame.getContent());
                    numWrites++;
                }
                byte[] content = new byte[blockSize];
                // code to read block with blockId from disk and store in content
                // ...
                frame.setContent(content);
                frame.setBlockId(blockId);
                frame.setPinned(true); // Set frame as pinned since it is being accessed
                numReads++;
                return content;
            } else {
                // There are no empty frames and all frames are pinned, so the block cannot be brought to the pool
                throw new RuntimeException("Cannot bring block to buffer pool. All frames are in use and pinned.");
            }
        }
    }

    public int selectFrameToEvict() {
        for (int i = 0; i < buffers.length; i++) {
            if (!buffers[i].isPinned()) {
                return i; // found an unpinned frame to evict
            }
        }
        // No unpinned frame found, use replacement policy to select frame to evict
        // ...
        return -1; // All frames are pinned, cannot evict any
    }

    public int findEmptyFrame() {
        for (int i = 0; i < buffers.length; i++) {
            if (buffers[i].getBlockId() == -1) {
                return i; // found an empty frame
            }
        }
        return -1; // no empty frame found
    }
    
    public void bringBlockToPool(int blockId) throws IOException {
        int emptyFrameNum = findEmptyFrame();
        if (emptyFrameNum != -1) {
            Frame frame = buffers[emptyFrameNum];
            byte[] blockContent = loadBlockFromDisk(blockId);
            frame.setBlockId(blockId);
            frame.setContent(blockContent);
            frame.setPinned(true);
        } else {
            int evictedFrameNum = selectFrameToEvict();
            if (evictedFrameNum != -1) {
                Frame frame = buffers[evictedFrameNum];
                if (frame.isDirty()) {
                    writeBlockToDisk(frame.getBlockId(), frame.getContent());
                }
                byte[] blockContent = loadBlockFromDisk(blockId);
                frame.setBlockId(blockId);
                frame.setContent(blockContent);
                frame.setPinned(true);
            } else {
                // There are no empty frames and all frames are pinned, so the block cannot be brought to the pool
                throw new RuntimeException("Cannot bring block to buffer pool. All frames are in use and pinned.");
            }
        }
    }

    
    public void writeBlockToDisk(int blockId, byte[] content) throws IOException {
        // Implement the method to write block data to disk
    }

    public void GET(int recordIndex) throws Exception {
        int blockSizeInRecords = blockSize / 100; // Assuming each record is 100 bytes
        int blockIndex = recordIndex / blockSizeInRecords;
        int recordPosition = recordIndex % blockSizeInRecords;

        int frameIndex = findFrame(blockIndex);
        byte[] blockData;

        if (frameIndex != -1) {
            blockData = buffers[frameIndex].getContent();
        } else {
            bringBlockToPool(blockIndex);
            frameIndex = findFrame(blockIndex);
            if (frameIndex == -1) {
                throw new RuntimeException("Failed to bring block to buffer pool.");
            }
            blockData = buffers[frameIndex].getContent();
        }

        // Assuming each record is 100 bytes
        byte[] recordData = Arrays.copyOfRange(blockData, recordPosition * 100, (recordPosition + 1) * 100);
        // Process or return the record data as needed
    }
}
