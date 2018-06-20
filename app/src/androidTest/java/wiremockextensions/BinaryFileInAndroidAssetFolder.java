package wiremockextensions;

import com.github.tomakehurst.wiremock.common.BinaryFile;
import com.github.tomakehurst.wiremock.common.TextFile;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static com.google.common.base.Charsets.UTF_8;

public class BinaryFileInAndroidAssetFolder extends TextFile {

    private final String name;
    private final InputStream ins;

    public BinaryFileInAndroidAssetFolder(String name, InputStream ins) {
        super(null);
        this.name = name;
        this.ins = ins;
    }


    public byte[] readContents() {
        byte[] bytes;
        try {
            bytes = IOUtils.toByteArray(ins);

        } catch (IOException e) {
            e.printStackTrace();
            bytes = new byte[]{};
        }

        return bytes;

    }

    public URI getUri() {
        try {
            return new URI("raw://"+name);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
     return null;
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name();
    }

    public String readContentsAsString() {
        return new String(readContents(), UTF_8);
    }

    @Override
    public InputStream getStream() {
        return ins;
    }
}
