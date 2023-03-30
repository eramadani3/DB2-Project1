public class Main {
    public static void main(String[] args) {
        BufferPool bufferPool = new BufferPool(10); // Initialize BufferPool with blockSize of 10

        try {
            bufferPool.GET(5); // Retrieve and print the content of record #5
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
