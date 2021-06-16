package Long;

import java.io.*;

public interface StreamCoder {
    void code(InputStream in, OutputStream out) throws IOException;
}