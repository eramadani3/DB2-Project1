public class Frame{
    private byte[] content;
    private boolean dirty;
    private boolean pinned;
    private int blockId;

    public Frame() {
        content = new byte[4096];
        dirty = false;
        pinned = false;
        blockId = -1;
    }
    
    public byte[] getContent() {
        return content;
    }
    public void setContent(byte[] content) {
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

    
    public String getRecord(int recordNumber) {
        int start = recordNumber * 40;
        int end = start + 40;
        return new String(content, start, end - start);
    }

    public void updateRecord(int recordNumber, String newContent) {
        int start = recordNumber * 40;
        int end = start + 40;
        byte[] bytes = newContent.getBytes();
        System.arraycopy(bytes, 0, content, start, Math.min(bytes.length, end - start));
        setDirty(true);
    }

    public void initialize() {
        content = new byte[4096];
        dirty = false;
        pinned = false;
        blockId = -1;
    }

}