import java.util.Arrays;

public class BufferPool {
    private Frame[] buffers;

    public void initialize(int poolSize) {
        buffers = new Frame[poolSize];
        for (int i = 0; i < poolSize; i++) {
            buffers[i] = new Frame();
            buffers[i].initialize();
        }
    }

    public int findFrame(int blockId) {
        for (int i = 0; i < buffers.length; i++) {
            if (buffers[i].getBlockId() == blockId) {
                return i; // found the block in buffer pool
            }
        }
        return -1; // block not found in buffer pool
    }

    public byte[] readBlock(int blockId) {
        int bufferNum = findFrame(blockId);
        if (bufferNum != -1) {
            Frame frame = buffers[bufferNum];
            frame.setPinned(true); // Set frame as pinned since it is being accessed
            return frame.getContent();
        }
        return null; // Block not found in buffer pool
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
            byte[] blockContent = loadBlockFromDisk(blockId, frame);
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
                byte[] blockContent = loadBlockFromDisk(blockId, frame);
                frame.setBlockId(blockId);
                frame.setContent(blockContent);
                frame.setPinned(true);
            } else {
                // There are no empty frames and all frames are pinned, so the block cannot be brought to the pool
                throw new RuntimeException("Cannot bring block to buffer pool. All frames are in use and pinned.");
            }
        }
    }
    
    
}
